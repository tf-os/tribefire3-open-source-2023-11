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
package com.braintribe.transport.messaging.dbm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.braintribe.logging.Logger;
import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.dbm.mbean.MBeanProxy;
import com.braintribe.transport.messaging.dbm.mbean.MessagingMBean;
import com.braintribe.transport.messaging.dbm.mbean.MessagingMBeanImpl;

/**
 * <p>
 * {@link MessagingConnection} implementation representing a connection to a {@link MessagingMBean} through a
 * {@link MBeanServerConnection}.
 * 
 */
public class GmDmbMqConnection implements MessagingConnection {

	private GmDmbMqConnectionProvider connectionProvider;
	private MBeanServerConnection mBeanServerConnection;
	private ObjectInstance messagingMBeanObjectInstance;
	private MessagingMBean messagingMBeanProxy;
	private Long connectionId;
	
	private ReentrantLock connectionLock = new ReentrantLock();
	private long connectionLockTimeout = 10L;
	private TimeUnit connectionLockTimeoutUnit = TimeUnit.SECONDS;
	
	private MessagingComponentStatus status = MessagingComponentStatus.NEW;
	private List<GmDmbMqSession> sessions = new ArrayList<>();
	
	private static final Logger log = Logger.getLogger(GmDmbMqConnection.class);
	
	
	public GmDmbMqConnection() {
	}

	public GmDmbMqConnectionProvider getConnectionProvider() {
		return connectionProvider;
	}

	public void setConnectionProvider(GmDmbMqConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
	}
	
	public MBeanServerConnection getMBeanServerConnection() {
		return mBeanServerConnection;
	}

	public void setMBeanServerConnection(MBeanServerConnection mBeanServerConnection) {
		this.mBeanServerConnection = mBeanServerConnection;
	}
	
	public ObjectInstance getMessagingMBeanObjectInstance() {
		return messagingMBeanObjectInstance;
	}
	
	public MessagingMBean getMessagingMBean() {
		if (messagingMBeanProxy == null) {
			if (status == MessagingComponentStatus.NEW) {
				throw new IllegalStateException("MessagingMBean is not yet initialized on state: "+status);
			} else {
				throw new MessagingException("Connection is "+status);
			}
		}
		return messagingMBeanProxy;
	}
	
	public Long getConnectionId() {
		return connectionId;
	}

	@Override
	public void open() throws MessagingException {
		
		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {
					
					if (status == MessagingComponentStatus.CLOSING || status == MessagingComponentStatus.CLOSED) {
						throw new MessagingException("Connection [ "+connectionId+" ] in unexpected state: "+status.toString().toLowerCase());
					}
					
					if (status == MessagingComponentStatus.OPEN) {
						//opening an already opened connection shall be a no-op
						if (log.isTraceEnabled()) {
							log.trace("open() called in an already opened connection. Connection [ "+connectionId+" ] already established.");
						}
						return;
					}

					if (messagingMBeanObjectInstance == null) {
						messagingMBeanObjectInstance = ensureMessagingMBeanRegistration();
					}

					if (messagingMBeanProxy == null) {
						synchronized(this) {
							if (messagingMBeanProxy == null) {
								messagingMBeanProxy = MBeanProxy.create(MessagingMBean.class, messagingMBeanObjectInstance.getObjectName(), mBeanServerConnection);
							}
						}
					}
					
					this.connectionId = messagingMBeanProxy.connect();
					
					if (log.isDebugEnabled()) {
						log.debug("Connection [ #"+connectionId+" ] established.");
					}
					
					this.status = MessagingComponentStatus.OPEN;
					
				} finally {
					connectionLock.unlock();
				}
			}
		} catch (InterruptedException e) {
			throw new MessagingException("Failed to open the messaging connection. Unable to acquire lock after "+connectionLockTimeout+" "+connectionLockTimeoutUnit.toString().toLowerCase()+" : "+e.getMessage(), e);
		}
		
	}
	
	@Override
	public void close() throws MessagingException {
		
		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {
					
					if (status == MessagingComponentStatus.CLOSING || status == MessagingComponentStatus.CLOSED) {
						//closing an already closed connection shall be a no-op
						if (log.isDebugEnabled()) {
							log.debug("No-op close() call. Connection closing already requested. current state: "+status.toString().toLowerCase());
						}
						return;
					}
					
					if (status == MessagingComponentStatus.NEW && log.isTraceEnabled()) {
						log.trace("Closing a connection which was not opened. current state: "+status.toString().toLowerCase());
					}
					
					this.status = MessagingComponentStatus.CLOSING;
					
					for (GmDmbMqSession session : sessions) {
						try {
							session.close();
						} catch (Throwable t) {
							log.error("Failed to close session created by this messaging connection: "+session+": "+t.getMessage(), t);
						}
					}

					sessions = null;
					
					if (messagingMBeanProxy != null) {
						messagingMBeanProxy.disconnect(connectionId);
					}
					
					messagingMBeanObjectInstance = null;

					messagingMBeanProxy = null;

					if (log.isDebugEnabled()) {
						if (status == MessagingComponentStatus.NEW) {
							log.debug("Unopened connection closed.");
						} else {
							log.debug("Connection [ #"+connectionId+" ] closed.");
						}
					}
					
					this.status = MessagingComponentStatus.CLOSED;
					
				} catch (Throwable t) {
					log.error(t);
				} finally {
					connectionLock.unlock();
				}
			}
		} catch (InterruptedException e) {
			throw new MessagingException("Failed to close the messaging connection. Unable to acquire lock after "+connectionLockTimeout+" "+connectionLockTimeoutUnit.toString().toLowerCase()+" : "+e.getMessage(), e);
		}
		
	}
	
	@Override
	public GmDmbMqSession createMessagingSession() throws MessagingException {
		
		open();
		
		GmDmbMqSession session = new GmDmbMqSession();
		session.setConnection(this);
		session.open();

		sessions.add(session);
		
		return session;
	}
	
	/**
	 * <p>
	 * Ensures that a {@link MessagingMBean} implementation was successfully instantiated and registered to MBean Server
	 * accessible through this connection's {@link MBeanServerConnection} ({@link #mBeanServerConnection}).
	 * 
	 * @return The {@link ObjectInstance} for the {@link MessagingMBean} registered with the MBean server.
	 * @throws MessagingException
	 *             <p>In case of failures during calls to the {@link MBeanServerConnection}; 
	 *             <p>If this connection  {@link MBeanServerConnection} ({@link #mBeanServerConnection}) is not local.
	 */
	protected ObjectInstance ensureMessagingMBeanRegistration() throws MessagingException {
		
		synchronized (mBeanServerConnection) {
			try {
				ObjectName name = new ObjectName(MessagingMBean.name);
				if (!mBeanServerConnection.isRegistered(name)) {
					
					if (mBeanServerConnection instanceof MBeanServer) {
						MBeanServer mbeanServer = (MBeanServer)mBeanServerConnection;
						MessagingMBeanImpl messagingMbean = new MessagingMBeanImpl();
						mbeanServer.registerMBean(messagingMbean, name);
						log.info(messagingMbean.getClass().getSimpleName() + " was successfully registered under " + MessagingMBean.name + " to the "
								+ MBeanServer.class.getSimpleName() + " by " + this);
						log.debug(messagingMbean + " was successfully registered under " + MessagingMBean.name + " to the " + mbeanServer + " by "
								+ this);
					}
					else {
						throw new MessagingException("Only local MBeanServerConnection are allowed. The configured instance does not comply: " + mBeanServerConnection);
					}
					
				}
				
				return mBeanServerConnection.getObjectInstance(name);
			} catch (Exception e) {
				throw new MessagingException("Failed to create/obtain a reference to MBean [ "+MessagingMBean.name+"] : "+e.getMessage(), e);
			}
		}
		
	}
	
	/**
	 * <p>Asserts that this connection is in a valid state to be used: Already open. not "closing" nor "closed";
	 * 
	 * <p>This method does not try to open a new connection.
	 * 
	 * @throws MessagingException
	 *             If this connection is NOT in a valid state to be used
	 */
	protected void assertOpen() throws MessagingException {

		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {
					if (status != MessagingComponentStatus.OPEN) {
						throw new MessagingException("Connection is not opened. Current state: "+status.toString().toLowerCase());
					}
				} finally {
					connectionLock.unlock();
				}
			}
		} catch (InterruptedException e) {
			throw new MessagingException("Failed to assert the state of the messaging connection. Unable to acquire lock after "+connectionLockTimeout+" "+connectionLockTimeoutUnit.toString().toLowerCase()+" : "+e.getMessage(), e);
		}
		
	}

}
