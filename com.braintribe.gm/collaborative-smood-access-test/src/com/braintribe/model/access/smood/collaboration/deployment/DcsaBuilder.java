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
package com.braintribe.model.access.smood.collaboration.deployment;

import java.io.File;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.smood.collaboration.deployment.wrappers.DistributedCollaborativeSmoodAccess4Test;
import com.braintribe.model.processing.dataio.FileBasedPersistence;
import com.braintribe.provider.Hub;

/**
 * @author peter.gazdik
 */
public class DcsaBuilder extends AbstractCsaBuilder<DistributedCollaborativeSmoodAccess4Test, DcsaDeployedUnit, DcsaBuilder> {

	private File markerFile;

	private DcsaSharedStorage sharedStorage;
	private Hub<String> markerPersistence;

	public static DcsaBuilder create() {
		return new DcsaBuilder();
	}

	// #################################################
	// ## . . . . . . . Fluid methods . . . . . . . . ##
	// #################################################

	@Override
	public DcsaBuilder baseFolder(File baseFolder) {
		super.baseFolder(baseFolder);

		this.markerFile = new File(baseFolder, "marker.txt");
		this.markerPersistence = markerPersistence();

		return self;
	}

	public DcsaBuilder sharedStorage(DcsaSharedStorage sharedStorage) {
		this.sharedStorage = sharedStorage;
		return self;
	}

	// #################################################
	// ## . . . . . Implementation providers . . . . .##
	// #################################################

	@Override
	protected DistributedCollaborativeSmoodAccess4Test newCsa() {
		DistributedCollaborativeSmoodAccess4Test result = new DistributedCollaborativeSmoodAccess4Test();
		result.setSharedStorage(sharedStorage);
		result.setCsaStatePersistence(statePersistence);
		result.setBinaryPersistenceEventSource(binaryProcessor);

		return result;
	}

	@Override
	protected DcsaDeployedUnit newUnit() {
		DcsaDeployedUnit result = new DcsaDeployedUnit();
		result.sharedStorage = sharedStorage;
		result.markerPersistence = markerPersistence;

		return result;
	}

	// #################################################
	// ## . . . . . . . . . Helpers . . . . . . . . . ##
	// #################################################

	protected Hub<String> markerPersistence() {
		return fileBasedPersistence(markerFile);
	}

	private <T> Hub<T> fileBasedPersistence(File file) {
		FileBasedPersistence<T> bean = new FileBasedPersistence<>();
		bean.setSerializationOptions(GmSerializationOptions.defaultOptions.derive().setOutputPrettiness(OutputPrettiness.high).build());
		bean.setFile(file);
		bean.setMarshaller(new JsonStreamMarshaller());
		
		return bean;
	}
	
	@Override
	protected void validate() {
		super.validate();

		checkConfigured(sharedStorage, "sharedStorage");
		checkConfigured(markerPersistence, "markerPersistence");
		checkConfigured(markerPersistence, "markerPersistence");
	}

}
