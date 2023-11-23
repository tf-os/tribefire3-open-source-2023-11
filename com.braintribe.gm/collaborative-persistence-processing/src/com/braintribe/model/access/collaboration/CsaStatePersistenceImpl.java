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
package com.braintribe.model.access.collaboration;

import com.braintribe.cfg.Required;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.utils.file.api.PathValueStore;

/**
 * @author peter.gazdik
 */
public class CsaStatePersistenceImpl implements CsaStatePersistence {

	private PathValueStore keyValueStore;

	private static final String configJson = "config.json";
	private static final String configOriginalJson = "config.original.json";
	private static final String markerTxt = "marker.txt";

	@Required
	public void setPathValueStore(PathValueStore keyValueStore) {
		this.keyValueStore = keyValueStore;
	}

	@Override
	public CollaborativeSmoodConfiguration readConfiguration() {
		CollaborativeSmoodConfiguration result = read(configJson);
		if (result == null) {
			result = defaultCsaConfiguration();
			write(configJson, result);
		}

		if (!keyValueStore.hasEntry(configOriginalJson))
			write(configOriginalJson, result);

		return result;
	}

	@Override
	public CollaborativeSmoodConfiguration readOriginalConfiguration() {
		CollaborativeSmoodConfiguration result = read(configOriginalJson);
		return result == null ? readConfiguration() : result;
	}

	@Override
	public void writeConfiguration(CollaborativeSmoodConfiguration value) {
		write(configJson, value);
	}

	@Override
	public void overwriteOriginalConfiguration(CollaborativeSmoodConfiguration value) {
		write(configJson, value);
		write(configOriginalJson, value);
	}

	public static CollaborativeSmoodConfiguration defaultCsaConfiguration() {
		ManInitializer trunk = ManInitializer.T.create();
		trunk.setName("trunk");
		CollaborativeSmoodConfiguration result = CollaborativeSmoodConfiguration.T.create();
		result.getInitializers().add(trunk);

		return result;
	}

	@Override
	public String readMarker() {
		return read(markerTxt);
	}

	@Override
	public void writeMarker(String marker) {
		write(markerTxt, marker);
	}

	private <T> T read(String fileName) {
		return keyValueStore.read(fileName);
	}

	private void write(String fileName, Object value) {
		keyValueStore.write(fileName, value);
	}

}
