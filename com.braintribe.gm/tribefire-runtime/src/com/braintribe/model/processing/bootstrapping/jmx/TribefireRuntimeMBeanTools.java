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
package com.braintribe.model.processing.bootstrapping.jmx;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.braintribe.logging.Logger;

public class TribefireRuntimeMBeanTools {

	private static Logger logger = Logger.getLogger(TribefireRuntimeMBeanTools.class);
			
	private static Map<String,TribefireRuntimeMBean> tribefireCartrigdeRuntimes = new ConcurrentHashMap<>();

	public static final String tribefireRuntimeMBeanPrefix = "com.braintribe.tribefire:type=TribefireRuntime,name=";

	public static TribefireRuntimeMBean getTribefireCartridgeRuntime(String cartridgeName) {
		return tribefireCartrigdeRuntimes.computeIfAbsent(cartridgeName, c -> {
			try {
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				String cartridgeRuntimeName = String.format("%s%s", tribefireRuntimeMBeanPrefix, c);

				TribefireRuntimeMBean bean = null;

				ObjectName as = new ObjectName(cartridgeRuntimeName);
				if (mbs.isRegistered(as)) {
					// Get TribefireRuntime of Cartridge-MBean
					bean = JMX.newMBeanProxy(mbs, as, TribefireRuntimeMBean.class);
				}
				return bean; 
			} catch (MalformedObjectNameException e) {
				logger.error(String.format("Invalid TribefireCartrigdeRuntime name: %s", c), e);
				return null;
			}
		});
	}
}
