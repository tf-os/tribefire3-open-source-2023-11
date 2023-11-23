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
package com.braintribe.model.meta.data.components;

import java.util.Set;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;

/**
 * {@link MetaData}, which can be configured for all {@link AccessModelExtension accesses} or {@link ServiceModelExtension service domains} of certain
 * type to specify how the model for the model accessory should be extended - either with additional dependencies bringing extra types, or with extra
 * {@link MetaData} (or both).
 * 
 * <h2>Examples:</h2>
 * 
 * <h3>Automatically add requests to make queries to every Incremental Access</h3>
 * 
 * Since every IncrementalAccess is also a Service Domain, it also has a service-model which describes it's API.
 * <p>
 * Context: Incremental Access is also a component with it's own interface with methods like applyManipulations() and queryEntities(). But this
 * functionality is also modeled in access-api-model with requests like ManipulationRequest or QueryEntities.
 * <p>
 * Now, in order to access this functionality, the access' service-model must include the <tt>access-api-model</tt>. And this should be true for every
 * single Incremental Access, and it should be there automatically.
 * <p>
 * The <b>solution</b> is to create a {@link ServiceModelExtension} instance, add the <tt>access-api-model</tt> to it's models property, and configure
 * it as meta-data on IncrementalAccess type. Like this:
 * 
 * <pre>
 * GmMetaModel accessApiModel = session.getEntityByGlobalId(QueryRequest.T.getModel().globalId());
 *
 * ServiceModelExtension md = session.create(ServiceModelExtension.T, "md://service-model-extension/IncrementalAccess/access-api");
 * md.getModels().add(accessApiModel);
 * 
 * mdEditor.onEntityType(IncrementalAccess.T).addMetaData(md);
 * </pre>
 * 
 * This is an example of additional dependencies.
 * 
 * <h3>Configure lazy-loading of files from distributed storage for every DCSA which stores resources</h3>
 * 
 * When reading a file, i.e. Resource with a {@link FileSystemSource} {@link Resource#getResourceSource() source}, we want to first check if such a
 * file exists on the disk, and if not, we want to load it from the shared storage.
 * <p>
 * There exists a special CsaBinaryRetrieval for that, so all that is left is to make sure this is used in every single DCSA which stores resources
 * (but we don't want to bring it to a DCSA that doesn't store resources, we don't want to extend the data model).
 * <p>
 * The <b>solution</b> has two parts:
 * <ul>
 * <li>Create a new model which depends on "basic-resource-model" (because that contains the FileSystemSource) and in this new model configure
 * StreamWith meta-data on FileSystemSource, using the known csaBinRetrieval.
 * <li>Create an {@link AccessModelExtension} md and, add this new model to it, and configure this md on CollaborativeAccess.
 * <ul>
 * 
 * Like this:
 * 
 * <pre>
 * //
 *		if (systemAccessCommons.isDistributedSetup())
 *			cortexModelMdEditor.onEntityType(CollaborativeAccess.T).addMetaData(extendCsaModel_Set_StreamWith_CsaBinRetrieval(session));
 * //
 *
 * 	private AccessModelExtension extendCsaModel_Set_StreamWith_CsaBinRetrieval(ManagedGmSession session) {
 *		BinaryRetrieval csaBinRetrieval = session.getEntityByGlobalId(systemAccessCommons.csaBinaryRetrievalDeployable().getGlobalId());
 *
 *		StreamWith streamWithMd = session.create(StreamWith.T, "synthetic:stream-with:csa-bin-retrieval");
 *		streamWithMd.setRetrieval(csaBinRetrieval);
 *
 *		GmMetaModel resourceModel = session.getEntityByGlobalId(FileSystemSource.T.getModel().globalId());
 *
 *		String csaBinRetrievalConfigurationModel = "synthetic:csa-bin-retrieval-configuring-model";
 *		GmMetaModel mdModel = session.create(GmMetaModel.T, modelGlobalId(csaBinRetrievalConfigurationModel));
 *		mdModel.setName(csaBinRetrievalConfigurationModel);
 *		mdModel.getDependencies().add(resourceModel);
 *
 *		ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(mdModel).withSession(session).done();
 *		mdEditor.onEntityType(FileSystemSource.T).addMetaData(streamWithMd);
 *
 *		AccessModelExtension result = session.create(AccessModelExtension.T, "md:access-model-extension:" + CollaborativeAccess.T.getShortName());
 *		result.getModels().add(mdModel);
 *
 *		return result;
 *	}
 * </pre>
 * 
 * NOTE: As of 15.1.2023 both of these examples and more can be found in CortextAccessInitializersSpace of the web platform.
 * 
 * @see AccessModelExtension
 * @see ServiceModelExtension
 * 
 * @author peter.gazdik
 */
@Abstract
public interface ModelExtension extends EntityTypeMetaData {

	EntityType<ModelExtension> T = EntityTypes.T(ModelExtension.class);

	/** Models to add as dependencies. */
	Set<GmMetaModel> getModels();
	void setModels(Set<GmMetaModel> models);

	boolean allowTypeExtension();

}
