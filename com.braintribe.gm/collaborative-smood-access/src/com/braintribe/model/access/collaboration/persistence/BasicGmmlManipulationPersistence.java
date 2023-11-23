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
package com.braintribe.model.access.collaboration.persistence;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.EntityManager;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.resource.Resource;

public class BasicGmmlManipulationPersistence extends AbstractGmmlManipulationPersistence {

	private File dataFile;

	private Map<Object, String> dataVariables = newMap();

	@Override
	public void configureStage(File parentFolder, String stageName) {
		this.dataFile = createStorageFile(parentFolder, "data");
		this.stage.setName(stageName);

		configureStageFolder(parentFolder);
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		dataVariables = initialize(context, dataFile);
	}

	@Override
	public AppendedSnippet[] append(Manipulation manipulation, ManipulationMode mode) {
		storeManMarkers();

		logAppendedManipulation(dataFile, manipulation);
		AppendedSnippet[] result = new AppendedSnippet[] { null, append(dataFile, manipulation, dataVariables, mode) };

		deleteManMarkers();

		return result;
	}

	@Override
	public void append(Resource[] gmmlResources, EntityManager entityManager) {
		storeManMarkers();

		append(dataFile, gmmlResources[1], dataVariables, entityManager);

		deleteManMarkers();
	}

	@Override
	protected Stream<Map<Object, String>> getVariablesMapStream() {
		return Stream.of(dataVariables);
	}

	@Override
	public Stream<File> getGmmlStageFiles() {
		return Stream.of(dataFile);
	}

}
