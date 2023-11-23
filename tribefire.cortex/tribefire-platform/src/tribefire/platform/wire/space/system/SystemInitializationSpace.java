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
package tribefire.platform.wire.space.system;

import static com.braintribe.wire.api.scope.InstanceConfiguration.currentInstance;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;

import java.util.Map;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.shutdown.JvmShutdownWatcher;
import com.braintribe.utils.FileAutoDeletion;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class SystemInitializationSpace implements WireSpace {

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		setSystemProperties();
		// We need to load this bean explicitly so that it gets closed when the services are stopped
		fileAutoDeletion();

		shutdownWatcher();
	}

	@Managed
	private JvmShutdownWatcher shutdownWatcher() {
		JvmShutdownWatcher watcher = new JvmShutdownWatcher();
		currentInstance().onDestroy(watcher::startShutdownWatch);
		return watcher;
	}

	@Managed
	public FileAutoDeletion fileAutoDeletion() {
		FileAutoDeletion bean = new FileAutoDeletion();
		return bean;
	}

	public void setSystemProperties() {
		// @formatter:off
		setSystemProperties(
				map(
					entry("net.sf.ehcache.skipUpdateCheck", "true")
				)
			);
		// @formatter:on
	}

	protected void setSystemProperties(Map<String, String> systemProperties) {
		for (Map.Entry<String, String> entry : systemProperties.entrySet()) {

			String key = entry.getKey();
			String value = entry.getValue();

			try {
				System.setProperty(key, value);
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Failed to set the system property: " + key + "=" + value);
			}
		}
	}

}
