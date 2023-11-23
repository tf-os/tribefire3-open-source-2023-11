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
package com.braintribe.logging.ndc.mbean;

import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * This class is the entry point to the Nested Diagnostic Context (NDC) holder. It utilizes
 * Java's MBeans to have a central location for the {@see NdcMBeanImpl} class across multiple
 * Classloaders. Using a simple ThreadLocal would not work as the log formatters in use
 * are loaded by a different classloader than those classes that actually set the NDC.
 *  
 * @author roman.kurmanowytsch
 */
public class NestedDiagnosticContext {

	private NdcMBean mbean = null;

	private static class LazyHolder {
		public static final NestedDiagnosticContext _instance = new NestedDiagnosticContext();
	}
	public static NestedDiagnosticContext getInstance() {
		return LazyHolder._instance;
	}

	private NestedDiagnosticContext() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName("com.braintribe.tribefire:type=NdcMBean");
			
			if (!mbs.isRegistered(name)) {
				this.mbean = new NdcMBeanImpl();
				mbs.registerMBean(this.mbean, name);
			} else {
				this.mbean = JMX.newMBeanProxy(mbs, name, NdcMBean.class);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static void pushContext(String context) {
		getInstance().mbean.pushContext(context);
	}
	public static void popContext() {
		getInstance().mbean.popContext();
	}
	public static void removeContext() {
		getInstance().mbean.removeContext();
	}
	public static Deque<String> getNdc() {
		NestedDiagnosticContext instance = getInstance();
//		if (instance == null)
//			return new ArrayDeque<String>();
		return instance.mbean.getNdc();
	}
	
	public static void clearMdc() {
		getInstance().mbean.clearMdc();
	}
	public static Object get(String key) {
		return getInstance().mbean.get(key);
	}
	public static void put(String key, String value) {
		getInstance().mbean.put(key, value);
	}
	public static void remove(String key) {
		getInstance().mbean.remove(key);
	}
	public static Map<String,String> getMdc() {
		return getInstance().mbean.getMdc();
	}

}
