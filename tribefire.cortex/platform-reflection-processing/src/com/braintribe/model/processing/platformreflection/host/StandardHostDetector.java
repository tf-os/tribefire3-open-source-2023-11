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
package com.braintribe.model.processing.platformreflection.host;

import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.braintribe.logging.Logger;

public class StandardHostDetector implements HostDetector {

	private static Logger logger = Logger.getLogger(StandardHostDetector.class);
	
	@Override
	public String hostIdentification() {
		
		logger.debug(() -> "Starting host identification.");
		
		String tomcat = this.testForTomcat();
		if (tomcat != null) {
			return tomcat;
		}
		
		logger.debug(() -> "Could not find any supported host container identification.");
		
		String env = null;
		InitialContext initialContext = null;
		try {
			initialContext = new InitialContext();
			Object lookup = new InitialContext().lookup("java:comp/env");
			if (lookup != null) {
				env = lookup.toString();
				logger.debug("Application environment entries are available at: "+env);
			}
		} catch (NamingException ex) {
			logger.debug(() -> "Could not lookup java:comp/env. This should actually be present in a Java EE environment.");
		} finally {
			if (initialContext != null) {
				try {
					initialContext.close();
				} catch(Exception e) {
					//ignore
				}
			}
		}
		
		logger.debug(() -> "Done with host identification.");
		
		return env;
	}
	
	protected String testForTomcat() {

		logger.debug(() -> "Checking for the presence of a Tomcat server.");
		
		MBeanServer mbeanServer = null;
		try {
			mbeanServer = ManagementFactory.getPlatformMBeanServer();
		} catch (Error e) {
			logger.debug(() -> "Could not access platform MBean server.", e);
			return null;
		}
		try {
			ObjectName on = new ObjectName("Catalina:type=Server");
			Object attribute = mbeanServer.getAttribute(on, "serverInfo");
			if (attribute instanceof String) {
				String serverInfo = (String) attribute;
				int idx = serverInfo.indexOf('/');
				if (idx > 0) {
					String hostType = serverInfo.substring(0, idx);
					String version = serverInfo.substring(idx+1);
					if (hostType.toLowerCase().contains("tomcat")) {
						if (version.startsWith("7")) {
							return "tomcat7";
						} else if (version.startsWith("8")) {
							return "tomcat8";
						}
					}
				}
				return "tomcat";
			}
		} catch (InstanceNotFoundException | MBeanException | AttributeNotFoundException anfe) {
			logger.debug(() -> "Could not get the server information. This is probably not a Tomcat server.", anfe);
		} catch (Exception e) {
			logger.debug(() -> "Unexpected error while trying to identify the host type.", e);
		} finally {
			logger.debug(() -> "Done with checking for Tomcat.");
		}
		return null;
	}

}
