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

import java.io.File;

import com.braintribe.utils.paths.PathCollectors;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.PreLoader;
import tribefire.platform.wire.space.MasterResourcesSpace;

/**
 * @see PreLoader
 * 
 * @author peter.gazdik
 */
@Managed
public class PreLoadingSpace implements WireSpace {

	@Import
	private MasterResourcesSpace resources;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		preLoader().startWiredPreLoading();
	}

	private PreLoader preLoader() {
		PreLoader bean = new PreLoader();
		bean.setCortexStorageBase(cortexStorageBase());

		return bean;
	}

	private File cortexStorageBase() {
		String configFilePath = PathCollectors.filePath.join("cortex", "data");
		return resources.database(configFilePath).asFile();
	}

}
