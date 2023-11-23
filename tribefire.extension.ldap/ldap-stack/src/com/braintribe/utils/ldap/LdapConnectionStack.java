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
package com.braintribe.utils.ldap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Deque;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.transport.ssl.SslSocketFactoryProvider;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.ldap.factory.DelegatingSslSocketFactory;
import com.braintribe.utils.ldap.invocation.SelfHealingLdapContext;

public class LdapConnectionStack implements LdapConnection, DestructionAware {

	private static Logger logger = Logger.getLogger(LdapConnectionStack.class);

	protected Deque<LdapContext> stack = new ConcurrentLinkedDeque<LdapContext>();

	protected SslSocketFactoryProvider sslSocketFactoryProvider;

	protected String connectionUrl = null;
	protected String username = null;
	protected String password = null;

	protected String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
	protected boolean referralFollow = false;
	protected boolean useTLSExtension = false;
	protected long connectTimeout = 30000L;
	protected long dnsTimeoutInitial = 10000L;
	protected int dnsTimeoutRetries = 3;
	protected Map<String, String> environmentSettings = null;

	protected boolean supportedCapabilitiesChecked = false;
	protected boolean isActiveDirectory = false;

	@Override
	public void push(LdapContext item) {
		if (item != null) {
			try {
				item.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
				item.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
			} catch (Exception e) {
				logger.debug("Error while resetting DirContext.", e);
				try {
					item.close();
				} catch (Exception e2) {
					logger.debug("Error while closing DirContext.", e2);
				} finally {
					item = null;
				}
			}
			if (item != null) {
				this.stack.push(item);
			}
		}
	}

	@Override
	public LdapContext pop() throws Exception {
		LdapContext dirContext = this.stack.poll();
		if (dirContext == null) {
			dirContext = this.createContextProxy();
		}
		return dirContext;
	}

	protected LdapContext createContextProxy() throws Exception {
		LdapContext dc = this.createContext();

		Class<?>[] ctxInterfaces = dc.getClass().getInterfaces();
		InvocationHandler ih = new SelfHealingLdapContext(dc, () -> {

			try {
				return createContext();
			} catch (NamingException e) {
				throw new RuntimeException("Could not provide a DirContext.", e);
			}

		});
		ClassLoader cl = this.getClass().getClassLoader();
		dc = (LdapContext) Proxy.newProxyInstance(cl, ctxInterfaces, ih);

		return dc;
	}

	protected LdapContext createContext() throws NamingException {

		/* create the environment */
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		env.put(Context.PROVIDER_URL, this.connectionUrl);

		// set timeout settings if available
		if (this.connectTimeout > 0) {
			env.put("com.sun.jndi.ldap.connect.timeout", "" + this.connectTimeout);
		}

		if (this.dnsTimeoutInitial > 0) {
			env.put("com.sun.jndi.dns.timeout.initial", "" + this.dnsTimeoutInitial);
		}

		if (this.dnsTimeoutRetries > 0) {
			env.put("com.sun.jndi.dns.timeout.retries", "" + this.dnsTimeoutRetries);
		}

		if (this.referralFollow) {
			env.put(Context.REFERRAL, "follow");
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Creating LDAP context: URL=" + this.connectionUrl + ", follow referrals=" + this.referralFollow + ", use TLS="
					+ this.useTLSExtension + "\nEnvironment: " + env.toString());
		}

		if (this.connectionUrl.toLowerCase().startsWith("ldaps:")) {
			env.put(Context.SECURITY_PROTOCOL, "ssl");

			boolean socketFactorySet = false;
			if (this.sslSocketFactoryProvider != null) {
				try {
					logger.debug(() -> "A custom SSL Socket Factory provider will be used: " + this.sslSocketFactoryProvider);
					SSLSocketFactory delegateSslSocketFactory = this.sslSocketFactoryProvider.provideSSLSocketFactory();
					DelegatingSslSocketFactory.setDelegate(delegateSslSocketFactory);

					env.put(Constants.ENV_SOCKET_FACTORY, "com.braintribe.utils.ldap.factory.DelegatingSslSocketFactory");
					socketFactorySet = true;

					logger.debug(() -> "Set delegating SSL Socket Factory pointing at " + delegateSslSocketFactory);
				} catch (Exception e) {
					logger.error("Error while trying to get the SSL Socket Factory. Using the default implementation.", e);
				}
			}

			if (!socketFactorySet) {
				env.put(Constants.ENV_SOCKET_FACTORY, "com.braintribe.utils.ldap.factory.SslConnectionFactory");
			}
		}

		/* the the credentials in the environment */
		this.setPrincipalCredentialsInEnv(env, this.username, this.password);

		/* create the context and pass it back */
		LdapContext result = null;

		if (this.useTLSExtension) {
			// Start TLS
			try {
				InitialLdapContext ilc = new InitialLdapContext(env, null);
				StartTlsResponse tls = (StartTlsResponse) ilc.extendedOperation(new StartTlsRequest());
				SSLSession sess = null;

				if (this.sslSocketFactoryProvider != null) {
					try {
						logger.debug(() -> "A custom SSL Socket Factory provider will be used: " + this.sslSocketFactoryProvider);
						SSLSocketFactory delegateSslSocketFactory = this.sslSocketFactoryProvider.provideSSLSocketFactory();
						sess = tls.negotiate(delegateSslSocketFactory);
					} catch (Exception e) {
						logger.error("Error while trying to get the SSL Socket Factory. Using the default implementation.", e);
					}
				}
				if (sess == null) {
					sess = tls.negotiate();
				}

				logger.debug("Created a valid TLS session: " + sess.isValid());
				result = ilc;
			} catch (Exception e) {
				NamingException ne = new NamingException("Could not create a secured LDAP/AD connection: " + e.getMessage());
				ne.setRootCause(e);
				throw ne;
			}
		} else {
			result = new InitialLdapContext(env, null);
		}

		if (!this.supportedCapabilitiesChecked) {
			this.checkSupportedCapabilities(result);
		}

		return result;
	}

	protected void checkSupportedCapabilities(LdapContext ctx) {
		this.supportedCapabilitiesChecked = true;

		boolean debug = logger.isDebugEnabled();

		try {
			Attributes attrs = ctx.getAttributes("", new String[] { Constants.ATTR_SUPPORTED_CAPABILITIES });
			if (attrs != null) {
				if (debug)
					logger.debug("Found server capabilities.");

				NamingEnumeration<?> attributeEnumeration = attrs.getAll();
				for (; attributeEnumeration.hasMoreElements();) {
					Attribute attr = (Attribute) attributeEnumeration.nextElement();
					String attrId = attr.getID();
					if ((attrId != null) && (attrId.equalsIgnoreCase(Constants.ATTR_SUPPORTED_CAPABILITIES))) {
						NamingEnumeration<?> attributeValueEnumeration = attr.getAll();
						for (; attributeValueEnumeration.hasMoreElements();) {
							Object o = attributeValueEnumeration.nextElement();
							if (o instanceof String) {
								String capability = (String) o;
								if (debug)
									logger.debug("Supported capability: " + capability);

								if (capability.equals(Constants.SUPPORTED_CAPABILITIES_ACTIVEDIRECTORY)) {
									if (debug)
										logger.debug("Identified the server as an Active Directory server.");
									this.isActiveDirectory = true;
								}
							}
						}
					}
				}
			} else {
				if (debug)
					logger.debug("No server capabilities were found.");
			}
		} catch (Exception e) {
			logger.error("Could not determine JNDI server capabilities.", e);
		}
	}

	protected void setPrincipalCredentialsInEnv(Hashtable<String, String> env, String userDN, String pwd) {
		if ((userDN != null) && (userDN.trim().length() > 0) && (pwd != null)) {

			if (logger.isTraceEnabled())
				logger.trace("Setting principal " + userDN + " with password " + StringTools.simpleObfuscatePassword(pwd) + "...");

			env.put(Context.SECURITY_PRINCIPAL, userDN);
			env.put(Context.SECURITY_CREDENTIALS, pwd);
		} else {
			if (logger.isTraceEnabled())
				logger.trace("No principal added to LDAP Context environment (user=" + userDN + ", pwd=" + pwd + ").");
		}
	}

	@Override
	public void preDestroy() {
		while (!this.stack.isEmpty()) {
			LdapContext dirContext = this.stack.remove();
			if (dirContext != null) {
				try {
					dirContext.close();
				} catch (Exception e) {
					logger.debug("Could not close DirContext " + dirContext, e);
				}
			}
		}
	}

	@Configurable
	public void setStack(Deque<LdapContext> stack) {
		this.stack = stack;
	}

	@Required
	@Configurable
	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	@Configurable
	public void setInitialContextFactory(String initialContextFactory) {
		this.initialContextFactory = initialContextFactory;
	}

	@Configurable
	public void setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	@Configurable
	public void setDnsTimeoutInitial(long dnsTimeoutInitial) {
		this.dnsTimeoutInitial = dnsTimeoutInitial;
	}

	@Configurable
	public void setDnsTimeoutRetries(int dnsTimeoutRetries) {
		this.dnsTimeoutRetries = dnsTimeoutRetries;
	}

	@Configurable
	public void setEnvironmentSettings(Map<String, String> environmentSettings) {
		this.environmentSettings = environmentSettings;
	}

	@Configurable
	public void setReferralFollow(boolean referralFollow) {
		this.referralFollow = referralFollow;
	}

	@Configurable
	public void setUseTLSExtension(boolean useTLSExtension) {
		this.useTLSExtension = useTLSExtension;
	}

	@Required
	public void setUsername(String username) {
		this.username = username;
	}

	@Required
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean isActiveDirectory() {
		return isActiveDirectory;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LDAP:");
		sb.append(this.username);
		sb.append("@");
		sb.append(this.connectionUrl);
		return sb.toString();
	}

	@Configurable
	@Required
	public void setSslSocketFactoryProvider(SslSocketFactoryProvider sslSocketFactoryProvider) {
		this.sslSocketFactoryProvider = sslSocketFactoryProvider;
	}

}
