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
package com.braintribe.model.processing.activemq.service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.store.kahadb.KahaDBStore;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.activemqdeployment.NetworkConnector;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.provider.Holder;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.lcd.StringTools;

public class ActiveMqWorkerImpl implements Worker, InitializationAware {

	private final static Logger logger = Logger.getLogger(ActiveMqWorkerImpl.class);
	
	protected GenericEntity workerIdentification;
	protected String bindAddress = "127.0.0.1";
	protected Integer port = 61616;
	protected File dataDirectory = null;
	protected String brokerName = null;
	protected Boolean useJmx = Boolean.FALSE;
	protected File persistenceDbDir = null;
	protected List<NetworkConnector> clusterNodes = null;
	protected Integer heapUsageInPercent = 70;
	protected Long diskUsageLimit = 100000000L;
	protected Long tempUsageLimit =  10000000L;
	protected Boolean createVmConnector = Boolean.FALSE;
	protected Boolean persistent = Boolean.FALSE;

	protected String discoveryMulticastUri = null;
	protected String discoveryMulticastGroup = null;
	protected String discoveryMulticastNetworkInterface = null;
	protected String discoveryMulticastAddress = null;
	protected String discoveryMulticastInterface = null;

	protected BrokerService broker = null;

	protected Consumer<BrokerService> brokerServiceReceiver;
	
	private static Holder<BrokerService> brokerServiceHolder = new Holder<>();

	public static Consumer<BrokerService> staticBrokerServiceConsumer() {
		return brokerServiceHolder;
	}
	public static BrokerService staticBrokerService() {
		return brokerServiceHolder.get();
	}
	
	@Override
	public GenericEntity getWorkerIdentification() {
		return workerIdentification;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {

		if (broker != null && workerContext != null) {
			try {
				stop(workerContext);
			} catch(Exception e) {
				logger.error("Could not stop broker service. Restarting...", e);
			} finally {
				broker = null;
			}
		}
		broker = new BrokerService();

		broker.setBrokerName(getBrokerName());
		broker.setUseJmx(useJmx);
		broker.setUseShutdownHook(false);
		broker.setPersistent(persistent);

		File storageDir = null;
		
		if (persistenceDbDir != null) {

			storageDir = persistenceDbDir;
			
			KahaDBStore kaha = new KahaDBStore();
			kaha.setDirectory(persistenceDbDir);
			// Using a bigger journal file
			
			long freeDiskSpace = getFreeDiskSpace(persistenceDbDir);
			long maxJournalFileLengthDefault = 1024*1204*100;
			long maxJournalFileLength = Math.min(freeDiskSpace, maxJournalFileLengthDefault);
			
			kaha.setJournalMaxFileLength((int) maxJournalFileLength);
			// small batch means more frequent and smaller writes
			kaha.setIndexWriteBatchSize(100);
			// do the index write in a separate thread
			kaha.setEnableIndexWriteAsync(true);
			try {
				broker.setPersistenceAdapter(kaha);
			} catch (Exception e) {
				broker = null;
				throw new WorkerException("Could not set the persistence adapter (using "+persistenceDbDir+")", e);
			}
		} else {
			try {
				broker.setPersistenceAdapter(new MemoryPersistenceAdapter());
				File dataDir = dataDirectory;
				if (dataDir == null) {
					dataDir = new File("activemq-data");
				}
				storageDir = dataDir;
				
				broker.setDataDirectory(dataDir.getAbsolutePath());
			} catch (IOException e) {
				broker = null;
				throw new WorkerException("Could not set the memory persistence adapter", e);
			}
		}

		if (createVmConnector) {
			try {
				broker.addConnector("vm://localhost");
			} catch (Exception e) {
				logger.warn("Could not add the VM connector. Continuing the start.", e);
			}
		}
		try {
			broker.addConnector("tcp://"+bindAddress+":"+port);
		} catch (Exception e) {
			broker = null;
			throw new WorkerException("Could not add a connector bound to "+bindAddress+":"+port, e);
		}
		
		setUsageLimits(storageDir);

		if (clusterNodes != null && !clusterNodes.isEmpty()) {
			for (NetworkConnector nc : clusterNodes) {

				String host = nc.getHost();
				if (host == null || host.trim().length() == 0) {
					host = "127.0.0.1";
				}
				Integer ncPort = nc.getPort();
				if (ncPort == null) {
					ncPort = 61616;
				}
				Boolean useExponentialBackOff = nc.getUseExponentialBackOff();
				if (useExponentialBackOff == null) {
					useExponentialBackOff = false;
				}
				Long initialReconnectDelay = nc.getInitialReconnectDelay();
				if (initialReconnectDelay == null) {
					initialReconnectDelay = 5000L;
				}
				Long maxReconnectDelay = nc.getMaxReconnectDelay();
				if (maxReconnectDelay == null) {
					maxReconnectDelay = 5000L;
				}

				String url = "static:(tcp://"+host+":"+ncPort+")?useExponentialBackOff="+useExponentialBackOff+"&initialReconnectDelay="+initialReconnectDelay+"&maxReconnectDelay="+maxReconnectDelay;

				if (logger.isDebugEnabled()) logger.debug("Adding network connector URL "+url);
				
				org.apache.activemq.network.NetworkConnector networkConnector = null;
				try {
					networkConnector = broker.addNetworkConnector(url);
				} catch (Exception e) {
					logger.warn("Could not add cluster node "+url, e);
				}
				String ncName = nc.getName();
				if (ncName == null || ncName.trim().length() == 0) {
					ncName = "unknown";
				}
				Boolean duplex = nc.getDuplex();
				if (duplex == null) {
					duplex = true;
				}
				Boolean conduitSubscriptions = nc.getConduitSubscriptions();
				if (conduitSubscriptions == null) {
					conduitSubscriptions = true;
				}
				networkConnector.setName(ncName);
				networkConnector.setDuplex(duplex);
				networkConnector.setConduitSubscriptions(conduitSubscriptions);
			}
			
		}
		
		if (!StringTools.isBlank(discoveryMulticastUri)) {
			try {
				
				logger.debug(() -> "Adding multicast discovery with group "+discoveryMulticastGroup+" and URI "+discoveryMulticastUri);

				TransportConnector c = broker.addConnector("tcp://0.0.0.0:0");
				c.setDiscoveryUri(new URI(discoveryMulticastUri));
				broker.addNetworkConnector(discoveryMulticastUri);

			} catch(Exception e) {
				throw new WorkerException("Could not configure multicast discovery group with URI "+discoveryMulticastUri, e);
			}
		}

		try {
			broker.start();
			if (brokerServiceReceiver != null) {
				brokerServiceReceiver.accept(broker);
			}
		} catch (Exception e) {
			broker = null;
			throw new WorkerException("Could not start the broker service.", e);
		}

	}

	protected void setUsageLimits(File storageDir) {
		SystemUsage systemUsage = broker.getSystemUsage();
		if (systemUsage == null) {
			systemUsage = new SystemUsage();
		}
		MemoryUsage memoryUsage = systemUsage.getMemoryUsage();
		if (memoryUsage == null) {
			memoryUsage = new MemoryUsage();
			systemUsage.setMemoryUsage(memoryUsage);
		}
		StoreUsage storeUsage = systemUsage.getStoreUsage();
		if (storeUsage == null) {
			storeUsage = new StoreUsage();
			systemUsage.setStoreUsage(storeUsage);
		}
		TempUsage tempDiskUsage = systemUsage.getTempUsage();
		if (tempDiskUsage == null) {
			tempDiskUsage = new TempUsage();
			systemUsage.setTempUsage(tempDiskUsage);
		}
		
		memoryUsage.setPercentOfJvmHeap(heapUsageInPercent);
		
		long freeSpace = this.getFreeDiskSpace(storageDir);
		double storePercentage = ((double) diskUsageLimit) / ((double) freeSpace);
		double tempPercentage = ((double) tempUsageLimit) / ((double) freeSpace);
		if (storePercentage >= 0 && storePercentage < 0.01) {
			storePercentage = 0.01d;
		}
		if (tempPercentage >= 0 && tempPercentage < 0.01) {
			tempPercentage = 0.01d;
		}
		storeUsage.setPercentLimit((int) (100*storePercentage));
		tempDiskUsage.setPercentLimit((int) (100*tempPercentage));
		storeUsage.setLimit(diskUsageLimit);
		tempDiskUsage.setLimit(tempUsageLimit);
		
		broker.setSystemUsage(systemUsage);
	}
	
	protected long getFreeDiskSpace(File path) {
		long freeSpace = 0L;
		File dir = new File(path.getAbsolutePath());
		while (freeSpace == 0L && dir.getParentFile() != null) {
			dir = dir.getParentFile();
			freeSpace = dir.getFreeSpace();
		}
		if (freeSpace == 0L) {
			logger.debug("Could not get the free disk space from path "+path.getAbsolutePath()+". Assuming to get at least 10 MB");
			freeSpace = 10000000L;
		}
		return freeSpace;
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		if (brokerServiceReceiver != null) {
			brokerServiceReceiver.accept(null);
		}
		if (broker != null) {
			try {
				broker.stop();
			} catch (Exception e) {
				throw new WorkerException("Could not stop the ActiveMq service.", e);
			} finally {
				broker = null;
			}
		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	@Required
	@Configurable
	public void setWorkerIdentification(GenericEntity workerIdentification) {
		this.workerIdentification = workerIdentification;
	}

	@Configurable
	public void setBindAddress(String bindAddress) {
		if (bindAddress != null && bindAddress.trim().length() > 0) {
			this.bindAddress = bindAddress;
		}
	}

	@Configurable
	public void setPort(Integer port) {
		if (port != null && port > 0) {
			this.port = port;
		}
	}

	@Configurable
	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	@Configurable
	public void setBrokerName(String brokerName) {
		if (brokerName != null && brokerName.trim().length() > 0) {
			this.brokerName = brokerName;
		}
	}
	public String getBrokerName() {
		if (brokerName == null) {
			String host;
			InetAddress inetAddress = NetworkTools.getNetworkAddress();
			if (inetAddress != null) {
				host = inetAddress.getHostName();
			} else {
				host = RandomTools.newStandardUuid();
			}
			brokerName = "tribefire-activemq-"+host+"-"+port;
		}
		return brokerName;
	}

	@Configurable
	public void setUseJmx(Boolean useJmx) {
		if (useJmx != null) {
			this.useJmx = useJmx;
		}
	}

	@Configurable
	public void setPersistenceDbDir(File persistenceDbDir) {
		this.persistenceDbDir = persistenceDbDir;
	}

	@Configurable
	public void setClusterNodes(List<NetworkConnector> clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	@Configurable
	public void setHeapUsageInPercent(Integer heapUsageInPercent) {
		if (heapUsageInPercent != null && heapUsageInPercent > 0 && heapUsageInPercent <= 100) {
			this.heapUsageInPercent = heapUsageInPercent;
		}
	}

	@Configurable
	public void setDiskUsageLimit(Long diskUsageLimit) {
		if (diskUsageLimit != null && diskUsageLimit > 0) {
			this.diskUsageLimit = diskUsageLimit;
		}
	}

	@Configurable
	public void setTempUsageLimit(Long tempUsageLimit) {
		if (tempUsageLimit != null && tempUsageLimit > 0) {
			this.tempUsageLimit = tempUsageLimit;
		}
	}

	@Configurable
	public void setCreateVmConnector(Boolean createVmConnector) {
		if (createVmConnector != null) {
			this.createVmConnector = createVmConnector;
		}
	}
	
	@Configurable
	public void setPersistent(Boolean persistent) {
		if (persistent != null) {
			this.persistent = persistent;
		}
	}

	@Required
	public void setBrokerServiceReceiver(Consumer<BrokerService> brokerServiceReceiver) {
		this.brokerServiceReceiver = brokerServiceReceiver;
	}

	@Configurable
	public void setDiscoveryMulticastUri(String discoveryMulticastUri) {
		this.discoveryMulticastUri = discoveryMulticastUri;
	}
	@Configurable
	public void setDiscoveryMulticastGroup(String discoveryMulticastGroup) {
		this.discoveryMulticastGroup = discoveryMulticastGroup;
	}
	@Configurable
	public void setDiscoveryMulticastNetworkInterface(String discoveryMulticastNetworkInterface) {
		this.discoveryMulticastNetworkInterface = discoveryMulticastNetworkInterface;
	}
	@Configurable
	public void setDiscoveryMulticastAddress(String discoveryMulticastAddress) {
		this.discoveryMulticastAddress = discoveryMulticastAddress;
	}
	@Configurable
	public void setDiscoveryMulticastInterface(String discoveryMulticastInterface) {
		this.discoveryMulticastInterface = discoveryMulticastInterface;
	}
	
	@Override
	public void postConstruct() {
		if (StringTools.isBlank(discoveryMulticastUri) && !StringTools.isBlank(discoveryMulticastGroup)) {
			String address = "default";
			if (!StringTools.isBlank(discoveryMulticastAddress)) {
				address = discoveryMulticastAddress;
			}
			String uri = "multicast://"+address+"?group="+discoveryMulticastGroup;
			if (!StringTools.isBlank(discoveryMulticastNetworkInterface)) {
				try {
					NetworkInterface byName = NetworkInterface.getByName(discoveryMulticastNetworkInterface);
					if (byName != null) {
						String encodedNi = URLEncoder.encode(discoveryMulticastNetworkInterface, "UTF-8");
						uri += "&networkInterface="+encodedNi+"&joinNetworkInterface="+encodedNi;
					} else {
						Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
						StringBuilder sb = new StringBuilder();
						while (networkInterfaces.hasMoreElements()) {
							NetworkInterface networkInterface = networkInterfaces.nextElement();
							if (sb.length() > 0) {
								sb.append(" ");
							}
							sb.append(networkInterface.getName());
						}
						logger.error("Could not find network interface "+discoveryMulticastNetworkInterface+". Available interfaces: "+sb.toString());
					}
				} catch (Exception e) {
					logger.error("Could not find network interface "+discoveryMulticastNetworkInterface, e);
				}
			}
			if (!StringTools.isBlank(discoveryMulticastInterface)) {
				try {
					InetAddress byName = InetAddress.getByName(discoveryMulticastInterface);
					if (byName != null) {
						String encoded = URLEncoder.encode(discoveryMulticastInterface, "UTF-8");
						uri += "&interface="+encoded;
					}
				} catch(Exception e) {
					logger.error("Could not find interface "+discoveryMulticastInterface, e);
				}
			}
			this.discoveryMulticastUri = uri;
			logger.debug(() -> "Using discover multicast URI "+discoveryMulticastUri);
		}
	}
}
