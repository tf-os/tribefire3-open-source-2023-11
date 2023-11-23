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
package tribefire.extension.elastic.elasticsearch;

import java.util.Collection;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginsService;

import com.braintribe.logging.Logger;

public class PluginEnabledNode extends Node {

	private static final Logger logger = Logger.getLogger(PluginEnabledNode.class);

	public PluginEnabledNode(Settings settings, Collection<Class<? extends Plugin>> plugins) {
		// TODO: check migration from 2.2.1
		// super(InternalSettingsPreparer.prepareEnvironment(settings, null), Version.CURRENT, plugins);
		super(InternalSettingsPreparer.prepareEnvironment(settings, null), plugins);
	}

	// TODO: check migration from 2.2.1
	@Override
	public PluginsService getPluginsService() {
		try {
			PluginsService services = super.getPluginsService();
			return services;
		} catch (Exception e) {
			logger.debug("Could not access the plugins service.", e);
		}
		return null;
	}
}
