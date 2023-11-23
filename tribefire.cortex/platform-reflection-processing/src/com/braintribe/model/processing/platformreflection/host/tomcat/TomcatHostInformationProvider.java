// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.platformreflection.host.tomcat;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.net.ssl.SSLServerSocketFactory;

import com.braintribe.logging.Logger;
import com.braintribe.model.platformreflection.HostInfo;
import com.braintribe.model.platformreflection.host.Webapp;
import com.braintribe.model.platformreflection.host.tomcat.Connector;
import com.braintribe.model.platformreflection.host.tomcat.Engine;
import com.braintribe.model.platformreflection.host.tomcat.Ssl;
import com.braintribe.model.platformreflection.host.tomcat.ThreadPool;
import com.braintribe.model.platformreflection.host.tomcat.ThreadPools;
import com.braintribe.model.platformreflection.host.tomcat.TomcatHostInfo;
import com.braintribe.model.processing.platformreflection.host.HostInformationProvider;


public class TomcatHostInformationProvider implements HostInformationProvider {

	private static Logger logger = Logger.getLogger(TomcatHostInformationProvider.class);

	@Override
	public HostInfo provide() throws RuntimeException {

		logger.debug(() -> "Compiling host information.");

		MBeanServer platformMBeanServer = null;
		try {
			platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
		} catch (Error e) {
			logger.debug(() -> "Could not access platform MBean server.", e);
			platformMBeanServer = null;
		}

		TomcatHostInfo host = TomcatHostInfo.T.create();

		host.setHostHomePath(System.getProperty("catalina.home"));
		host.setHostBasePath(System.getProperty("catalina.base"));
		getHostTypeAndVersion(platformMBeanServer, host);

		if (platformMBeanServer != null) {
			List<Connector> connectors = getConnectors(platformMBeanServer);
			host.setConnectors(connectors);
			host.setEngine(getEngine(platformMBeanServer));
			host.setThreadPools(getThreadPools(connectors, platformMBeanServer));
			host.setSsl(getSsl(connectors, platformMBeanServer));
			host.setWebapps(collectAllDeployedApps(platformMBeanServer));
		}

		/*
		 * ObjectName name = new
		 * ObjectName("Catalina:type=Manager,path=/manager,host=localhost");
		 * String attrValue = mb.getAttribute(name,
		 * "activeSessions").toString();
		 **/

		logger.debug(() -> "Done with compiling host information.");

		return host;
	}

	protected List<Ssl> getSsl(List<Connector> connectors, MBeanServer platformMBeanServer) {

		logger.debug(() -> "Getting information about SSL.");

		try {

			List<Ssl> sslList = new ArrayList<Ssl>();

			for (Connector c : connectors) {

				Boolean secure = c.getSecure();
				if (secure == null || !secure) {
					continue;
				}
				Integer port = c.getPort();
				if (port == null) {
					continue;
				}

				AttributeList al = platformMBeanServer.getAttributes(new ObjectName("Catalina:type=ProtocolHandler,port="+port), new String[] {
						"port", "keystoreFile", "keystoreType", "keystoreProvider", "truststoreFile", "truststoreType", "truststoreProvider",
						"keyAlias", "ciphers", "sslProtocol", "sslCertificateFile", "sslCertificateKeyFile", "sslCipherSuite"});
				if (al != null && al.size() > 0) {

					Ssl ssl = Ssl.T.create();

					for (Iterator<Object> it = al.iterator(); it.hasNext(); ) {
						Object o = it.next();
						if (o instanceof Attribute) {
							Attribute a = (Attribute) o;

							switch(a.getName()) {
								case "port":
									ssl.setPort(getIntValue(a));
									break;
								case "keystoreFile":
								case "sslCertificateKeyFile":
									ssl.setKeyStoreFile(getStringValue(a));
									break;
								case "keystoreType":
									ssl.setKeyStoreType(getStringValue(a));
									break;
								case "keystoreProvider":
									ssl.setKeyStoreProvider(getStringValue(a));
									break;
								case "truststoreFile":
								case "sslCertificateFile":
									ssl.setTrustStoreFile(getStringValue(a));
									break;
								case "truststoreType":
									ssl.setTrustStoreType(getStringValue(a));
									break;
								case "truststoreProvider":
									ssl.setTrustStoreProvider(getStringValue(a));
									break;
								case "keyAlias":
									ssl.setKeyAlias(getStringValue(a));
									break;
								case "ciphers":
								case "sslCipherSuite":
									processCiphers(ssl, a);
									break;
								case "sslProtocol":
									ssl.setProtocol(getStringValue(a));
									break;
								default:
									break;
							}
						}
					}

					sslList.add(ssl);
				}
			}

			return sslList;

		} catch(Exception e) {
			logger.debug("Error while trying to get SSL information.", e);
		} finally {
			logger.debug(() -> "Done with getting information about SSL.");
		}
		return null;
	}

	protected void processCiphers(Ssl ssl, Attribute a) {

		logger.debug(() -> "Getting information about ciphers.");

		try {
			List<String> cipherList = getStringListValue(a);
			if (cipherList == null || cipherList.isEmpty()) {
				if (cipherList == null) {
					cipherList = new ArrayList<String>();
				}
				// Default cipher list
				SSLServerSocketFactory ssf = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
				String[] defaultCiphers = ssf.getDefaultCipherSuites();
				if (defaultCiphers != null) {
					for (String dc : defaultCiphers) {
						cipherList.add(dc);
					}
				}
			} else if (cipherList.size() == 1 && cipherList.get(0).equalsIgnoreCase("ALL")) {
				// All ciphers
				SSLServerSocketFactory ssf = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
				String[] supportedCiphers = ssf.getSupportedCipherSuites();
				if (supportedCiphers != null) {
					cipherList.clear();
					for (String sc : supportedCiphers) {
						cipherList.add(sc);
					}
				}
			}
			ssl.setCiphers(cipherList);
		} catch(Exception e) {
			logger.debug("Error while retrieving SSL information.", e);
		} finally {
			logger.debug(() -> "Done with getting information about ciphers.");
		}
	}

	protected ThreadPools getThreadPools(List<Connector> connectors, MBeanServer platformMBeanServer) {

		logger.debug(() -> "Getting information about thread pools.");

		ThreadPools tps = ThreadPools.T.create();

		List<ThreadPool> threadPoolList = this.getThreadPoolList(connectors, platformMBeanServer);

		tps.setThreadPools(threadPoolList);

		if (threadPoolList != null) {
			int totalConnections = 0;
			int totalThreads = 0;
			int totalBusyThreads = 0;
			for (ThreadPool tp : threadPoolList) {
				Long connectionCount = tp.getConnectionCount();
				if (connectionCount != null) {
					totalConnections += connectionCount;
				}
				Integer currentThreadCount = tp.getCurrentThreadCount();
				if (currentThreadCount != null) {
					totalThreads += currentThreadCount;
				}
				Integer currentThreadsBusy = tp.getCurrentThreadsBusy();
				if (currentThreadsBusy != null) {
					totalBusyThreads += currentThreadsBusy;
				}
			}
			tps.setTotalConnectionCount(totalConnections);
			tps.setTotalThreadCount(totalThreads);
			tps.setBusyThreadCount(totalBusyThreads);
		}

		logger.debug(() -> "Done with getting information about thread pools.");

		return tps;

	}
	protected List<ThreadPool> getThreadPoolList(List<Connector> connectors, MBeanServer platformMBeanServer) {

		logger.debug(() -> "Getting information about a thread pool list.");

		List<ThreadPool> threadPools = new ArrayList<ThreadPool>();

		try {

			Set<ObjectName> engines = platformMBeanServer.queryNames(new ObjectName("Catalina:type=ThreadPool,*"), null);
			if (engines != null && engines.size() > 0) {

				for (ObjectName threadPoolOn : engines) {

					AttributeList al = platformMBeanServer.getAttributes(threadPoolOn, new String[] {"name", "connectionCount", "currentThreadCount", "currentThreadsBusy"});
					if (al != null && al.size() > 0) {

						ThreadPool tp = ThreadPool.T.create();

						for (Iterator<Object> it = al.iterator(); it.hasNext(); ) {
							Object o = it.next();
							if (o instanceof Attribute) {
								Attribute a = (Attribute) o;

								switch(a.getName()) {
									case "name":
										tp.setName(getStringValue(a));
										break;
									case "connectionCount":
										tp.setConnectionCount(getLongValue(a));
										break;
									case "currentThreadCount":
										tp.setCurrentThreadCount(getIntValue(a));
										break;
									case "currentThreadsBusy":
										tp.setCurrentThreadsBusy(getIntValue(a));
										break;
									default:
										break;
								}
							}
						}

						for (Connector c : connectors) {
							if (c.getPort() != null && tp.getName() != null) {
								if (tp.getName().contains(""+c.getPort())) {
									tp.setMaxThreadCount(c.getMaxThreads());
									break;
								}
							}
						}

						threadPools.add(tp);
					}
				}
			}

		} catch(Exception e) {
			logger.debug("Error while trying to get access to the thread pools.", e);
			return null;
		} finally {
			logger.debug(() -> "Done with getting information about a thread pool list.");
		}

		return threadPools;
	}

	protected Engine getEngine(MBeanServer platformMBeanServer) {

		logger.debug(() -> "Getting information about the engine.");

		try {
			Engine engine = null;

			Set<ObjectName> engines = platformMBeanServer.queryNames(new ObjectName("Catalina:type=Engine"), null);
			if (engines != null && engines.size() > 0) {

				ObjectName engineOn = engines.iterator().next();

				engine = Engine.T.create();

				AttributeList al = platformMBeanServer.getAttributes(engineOn, new String[] {"baseDir", "defaultHost", "name", "startStopThreads", "stateName"});
				if (al != null && al.size() > 0) {
					for (Iterator<Object> it = al.iterator(); it.hasNext(); ) {
						Object o = it.next();
						if (o instanceof Attribute) {
							Attribute a = (Attribute) o;

							switch(a.getName()) {
								case "baseDir":
									engine.setBaseDir(getStringValue(a));
									break;
								case "defaultHost":
									engine.setDefaultHost(getStringValue(a));
									break;
								case "name":
									engine.setName(getStringValue(a));
									break;
								case "startStopThreads":
									engine.setStartStopThreads(getIntValue(a));
									break;
								case "stateName":
									engine.setStateName(capitalize(getStringValue(a)));
									break;
								default:
									break;
							}
						}
					}
				}
			}

			return engine;
		} catch(Exception e) {
			logger.debug("Error while trying to get access to the engine.", e);
			return null;
		} finally {
			logger.debug(() -> "Done with getting information about the engine.");
		}
	}

	protected void getHostTypeAndVersion(MBeanServer mbeanServer, TomcatHostInfo host) {

		logger.debug(() -> "Getting host type and version.");

		try {
			ObjectName on = new ObjectName("Catalina:type=Server");
			Object attribute = mbeanServer.getAttribute(on, "serverInfo");
			if (attribute instanceof String) {
				String serverInfo = (String) attribute;
				int idx = serverInfo.indexOf('/');
				if (idx > 0) {
					String hostType = serverInfo.substring(0, idx);
					String version = serverInfo.substring(idx+1);
					host.setHostType(hostType);
					host.setHostVersion(version);
				} else {
					host.setHostType(serverInfo);
				}
			}
		} catch (Exception e) {
			logger.debug("Error while trying to get the server information.", e);
		} finally {
			logger.debug(() -> "Done with getting host type and version.");
		}
	}

	protected List<Connector> getConnectors(MBeanServer mbeanServer) {

		logger.debug(() -> "Getting connectors.");

		List<Connector> connectors = new ArrayList<Connector>();
		try {
			Set<ObjectName> ports = mbeanServer.queryNames(new ObjectName("Catalina:type=Connector,*"), null);
			for (ObjectName port : ports) {
				AttributeList al = mbeanServer.getAttributes(port, new String[] {
						"URIEncoding", "acceptCount", "connectionLinger", "connectionTimeout", "keepAliveTimeout", "localPort", "maxHeaderCount",
						"maxParameterCount", "maxPostSize", "maxSavePostSize", "maxSwallowSize", "maxThreads", "minSpareThreads", "packetSize",
						"port", "processorCache", "protocol", "proxyName", "proxyPort", "redirectPort", "scheme", "secure", "sslProtocols", "stateName",
						"tcpNoDelay", "threadPriority"});
				if (al != null && al.size() > 0) {

					Connector c = Connector.T.create();

					for (Iterator<Object> it = al.iterator(); it.hasNext(); ) {
						Object o = it.next();
						if (o instanceof Attribute) {
							Attribute a = (Attribute) o;

							switch(a.getName()) {
								case "URIEncoding":
									c.setUriEncoding(getStringValue(a));
									break;
								case "acceptCount":	
									c.setAcceptCount(getIntValue(a));
									break;
								case "connectionLinger":	
									c.setConnectionLinger(getIntValue(a));
									break;
								case "connectionTimeout":	
									c.setConnectionTimeout((long) getIntValue(a));
									break;
								case "keepAliveTimeout":	
									c.setKeepAliveTimeout((long) getIntValue(a));
									break;
								case "localPort":	
									c.setLocalPort(getIntValue(a));
									break;
								case "maxHeaderCount":	
									c.setMaxHeaderCount(getIntValue(a));
									break;
								case "maxParameterCount":
									c.setMaxParameterCount(getIntValue(a));
									break;
								case "maxPostSize":	
									c.setMaxPostSize(getIntValue(a));
									break;
								case "maxSavePostSize":
									c.setMaxSavePostSize(getIntValue(a));
									break;
								case "maxSwallowSize":	
									c.setMaxSwallowSize(getIntValue(a));
									break;
								case "maxThreads":	
									c.setMaxThreads(getIntValue(a));
									break;
								case "minSpareThreads":
									c.setMinSpareThreads(getIntValue(a));
									break;
								case "packetSize":	
									c.setPacketSize(getIntValue(a));
									break;
								case "port":	
									c.setPort(getIntValue(a));
									break;
								case "processorCache":
									c.setProcessorCache(getIntValue(a));
									break;
								case "protocol":	
									c.setProtocol(getStringValue(a));
									break;
								case "proxyName":	
									c.setProxyName(getStringValue(a));
									break;
								case "proxyPort":	
									c.setProxyPort(getIntValue(a));
									break;
								case "redirectPort":	
									c.setRedirectPort(getIntValue(a));
									break;
								case "scheme":	
									c.setScheme(getStringValue(a));
									break;
								case "secure":
									c.setSecure(getBoolValue(a));
									break;
								case "sslProtocols":
									break;
								case "stateName":	
									c.setStateName(capitalize(getStringValue(a)));
									break;
								case "tcpNoDelay":	
									break;
								case "threadPriority":
									c.setThreadPriority(getIntValue(a));
									break;
								default:
									break;
							}


						}
					}

					collectConnectorStats(c, mbeanServer);

					connectors.add(c);

				}
			}
		} catch(Exception e) {
			logger.debug("Error while trying to get access to the connectors.", e);
		} finally {
			logger.debug(() -> "Done with getting connectors.");
		}
		return connectors;
	}

	protected void collectConnectorStats(Connector c, MBeanServer mbeanServer) {

		logger.debug(() -> "Getting connector statistics.");

		try {
			Integer port = c.getPort();
			if (port == null) {
				logger.debug("No port available for connector: "+c);
				return;
			}
			String protocol = c.getProtocol();
			if (protocol == null) {
				logger.debug("No protocol available for connector: "+c);
				return;
			}
			int idx = protocol.indexOf('/');
			if (idx <= 0) {
				logger.debug("Cannot extract protocol name from: "+protocol);
				return;
			}
			String scheme = protocol.substring(0, idx).toLowerCase();

			try {
				String objectNameString = "Catalina:type=GlobalRequestProcessor,name=\""+scheme+"-bio-"+port+"\"";

				try {
					ObjectName phOn = new ObjectName("Catalina:type=ProtocolHandler,port="+port);
					Object handlerNameObject = mbeanServer.getAttribute(phOn, "name");
					if (handlerNameObject instanceof String) {
						objectNameString = "Catalina:type=GlobalRequestProcessor,name="+handlerNameObject;
					}
				} catch(InstanceNotFoundException e) {
					//Well, use the assumed name
				}

				ObjectName on = new ObjectName(objectNameString);
				AttributeList al = mbeanServer.getAttributes(on, new String[] {
						"bytesReceived", "bytesSent", "errorCount", "maxTime", "processingTime", "requestCount"});
				if (al != null && al.size() > 0) {
					for (Iterator<Object> it = al.iterator(); it.hasNext(); ) {
						Object o = it.next();
						if (o instanceof Attribute) {
							Attribute a = (Attribute) o;

							switch(a.getName()) {
								case "bytesReceived":
									c.setBytesReceived(getLongValue(a));
									break;
								case "bytesSent":
									c.setBytesSent(getLongValue(a));
									break;
								case "errorCount":
									c.setErrorCount(getIntValue(a));
									break;
								case "maxTime":
									c.setMaxTime(getLongValue(a));
									break;
								case "processingTime":
									c.setProcessingTime(getLongValue(a));
									break;
								case "requestCount":
									c.setRequestCount(getIntValue(a));
									break;
								default:
									break;
							}
						}
					}
				}
			} catch(InstanceNotFoundException infe) {
				logger.debug(() -> "Could not find stats for "+scheme+"-bio-"+port);
			} catch(Exception e) {
				logger.debug(() ->"Could not get stats for port "+port, e);
			}

		} finally {
			logger.debug(() -> "Done with getting connector statistics.");
		}
	}

	protected String getStringValue(Object value) {
		if (value instanceof String) {
			return (String) value;
		}
		return null;
	}
	protected Integer getIntValue(Object value) {
		if (value instanceof Integer) {
			return (Integer) value;
		}
		return null;
	}
	protected Long getLongValue(Object value) {
		if (value instanceof Long) {
			return (Long) value;
		}
		return null;
	}
	protected Boolean getBoolValue(Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return null;
	}
	protected List<String> getStringListValue(Object value) {
		if (value instanceof String) {
			List<String> list = new ArrayList<String>(1);
			String sValue = (String) value;
			String[] split = sValue.split(",");
			if (split != null) {
				for (String s : split) {
					list.add(s);
				}
			}
			return list;
		}
		if (value instanceof String[]) {
			String[] array = (String[]) value;
			List<String> list = new ArrayList<String>(array.length);
			for (String e : array) {
				list.add(e);
			}
			return list;
		}
		return null;
	}

	protected String getStringValue(Attribute attr) {
		return getStringValue(attr.getValue());
	}
	protected Integer getIntValue(Attribute attr) {
		return getIntValue(attr.getValue());
	}
	protected Long getLongValue(Attribute attr) {
		return getLongValue(attr.getValue());
	}
	protected Boolean getBoolValue(Attribute attr) {
		return getBoolValue(attr.getValue());
	}
	protected List<String> getStringListValue(Attribute a) {
		return getStringListValue(a.getValue());
	}

	protected String capitalize(String text) {
		if (text == null) {
			return null;
		}
		return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
	}

	protected List<Webapp> collectAllDeployedApps(MBeanServer mbeanServer) {
		
		logger.debug(() -> "Collecting all deployed webapps.");
		
		try {
			final List<Webapp> webappList = new ArrayList<Webapp>();

			Set<ObjectName> names = mbeanServer.queryNames(new ObjectName("Catalina:type=Host,*"), null);

			for (ObjectName on : names) {

				String host = on.getKeyProperty("host");

				ObjectName[] webapps = (ObjectName[]) mbeanServer.getAttribute(on, "children");
				for (ObjectName ob : webapps) {

					Webapp webapp = Webapp.T.create();
					webapp.setHost(host);

					AttributeList al = mbeanServer.getAttributes(ob, new String[] {"name", "stateName"});
					if (al != null && al.size() > 0) {
						for (Iterator<Object> it = al.iterator(); it.hasNext(); ) {
							Object o = it.next();
							if (o instanceof Attribute) {
								Attribute a = (Attribute) o;

								switch(a.getName()) {
									case "name":
										String stringValue = getStringValue(a);
										if (stringValue == null || stringValue.trim().length() == 0) {
											stringValue = "/";
										}
										webapp.setName(stringValue);
										break;
									case "stateName":
										webapp.setStateName(capitalize(getStringValue(a)));
										break;
									default:
										break;
								}
							}
						}
					}


					webappList.add(webapp);
				}

			}

			Collections.sort(webappList, new Comparator<Webapp>() {
				@Override
				public int compare(Webapp o1, Webapp o2) {
					if (o1 == null && o2 == null) {
						return 0;
					}
					if (o1 == null) {
						return 1;
					}
					if (o2 == null) {
						return -1;
					}
					String name1 = o1.getName();
					String name2 = o2.getName();
					if (name1 == null && name2 == null) {
						return 0;
					}
					if (name1 == null) {
						return 1;
					}
					if (name2 == null) {
						return -1;
					}
					return name1.compareTo(name2);
				}
			});

			return webappList;
		} catch (Exception e) {
			logger.debug("Error while trying to collect webapps.", e);
			return null;
		} finally {
			logger.debug(() -> "Done with collecting all deployed webapps.");
		}
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
