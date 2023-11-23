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
package com.braintribe.product.rat.imp.impl.service;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.cortexapi.access.ConfigureWorkbench;
import com.braintribe.model.cortexapi.access.SetupAccessResponse;
import com.braintribe.model.cortexapi.access.SetupAspects;
import com.braintribe.model.cortexapi.access.SetupWorkbench;
import com.braintribe.model.cortexapi.connection.TestConnectionResponse;
import com.braintribe.model.cortexapi.connection.TestDatabaseConnection;
import com.braintribe.model.cortexapi.model.AddDependencies;
import com.braintribe.model.cortexapi.model.CreateModel;
import com.braintribe.model.cortexapi.model.CreateModelResponse;
import com.braintribe.model.cortexapi.model.MergeModelsResponse;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deploymentapi.request.DeployWithDeployables;
import com.braintribe.model.deploymentapi.request.DeploymentOperationWithDeployables;
import com.braintribe.model.deploymentapi.request.RedeployWithDeployables;
import com.braintribe.model.deploymentapi.request.UndeployWithDeployables;
import com.braintribe.model.deploymentapi.response.DeployResponse;
import com.braintribe.model.deploymentapi.response.RedeployResponse;
import com.braintribe.model.deploymentapi.response.UndeployResponse;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.product.rat.imp.impl.model.ModelImpCave;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Additionally to the functionality of {@link ServiceHelperCave} there are lots of utility methods to create typical
 * cortex service requests
 */
public class CortexServiceHelperCave extends ServiceHelperCave {

	public CortexServiceHelperCave(PersistenceGmSession session) {
		super(session);
	}

	public ServiceHelperWithNotificationResponse<CreateModel, CreateModelResponse> createModelRequest(String groupId, String modelName,
			GmMetaModel... baseModels) throws GmSessionException {
		return createModelRequest(groupId, modelName, "1.0", baseModels);
	}

	public ServiceHelperWithNotificationResponse<CreateModel, CreateModelResponse> createModelRequest(String groupId, String modelName,
			String version, GmMetaModel... baseModels) throws GmSessionException {
		logger.info("Creating custom model '" + modelName + "#" + version + "'");

		for (GmMetaModel baseModel : baseModels) {
			logger.info("based on the model [" + baseModel.getName() + "]");
		}

		CreateModel createModelRequest = CreateModel.T.create();
		createModelRequest.setDependencies(CommonTools.getList(baseModels));
		createModelRequest.setGroupId(groupId);
		createModelRequest.setVersion(version);
		createModelRequest.setName(modelName);

		ModelImpCave models = new ModelImpCave(session());
		return new ServiceHelperWithNotificationResponse<CreateModel, CreateModelResponse>(session(), createModelRequest)
				.verifyingResultBy(models.find(modelName)::isPresent);
	}

	/**
	 * @param deployables
	 *            to be deployed. Must be already committed or will fail!
	 */
	public <D extends Deployable> ServiceHelperWithNotificationResponse<DeployWithDeployables, DeployResponse> deployRequest(
			Collection<D> deployables) {
		
		DeployWithDeployables deployRequest = createDeploymentRequest(DeployWithDeployables.T, deployables);
				
		return new ServiceHelperWithNotificationResponse<DeployWithDeployables, DeployResponse>(session(), deployRequest)
				.verifyingResultBy(() -> deployables.stream().allMatch(d -> d.getDeploymentStatus().equals(DeploymentStatus.deployed)))
				.addEntitiesForAutoRefresh(deployables);
	}
	
	/**
	 * @param deployables
	 *            to be deployed. Must be already committed or will fail!
	 */
	public <D extends Deployable> ServiceHelperWithNotificationResponse<DeployWithDeployables, DeployResponse> deployRequest(D... deployables)
			throws GmSessionException {
		Set<Deployable> deployableSet = CommonTools.getSet(deployables);
		return deployRequest(deployableSet);
	}

	public <D extends Deployable> ServiceHelperWithNotificationResponse<UndeployWithDeployables, UndeployResponse> undeployRequest(
			final D... deployables) {
		return undeployRequest(CommonTools.getSet(deployables));
	}

	/**
	 * @param deployables
	 *            to be undeployed. Must be already committed or will fail!
	 */
	public <D extends Deployable> ServiceHelperWithNotificationResponse<UndeployWithDeployables, UndeployResponse> undeployRequest(
			final Collection<D> deployables) {
		
		UndeployWithDeployables undeployRequest = createDeploymentRequest(UndeployWithDeployables.T, deployables);

		return new ServiceHelperWithNotificationResponse<UndeployWithDeployables, UndeployResponse>(session(), undeployRequest)
				.verifyingResultBy(() -> deployables.stream().allMatch(d -> d.getDeploymentStatus().equals(DeploymentStatus.undeployed)))
				.addEntitiesForAutoRefresh(deployables);
	}

	public <D extends Deployable> ServiceHelperWithNotificationResponse<RedeployWithDeployables, RedeployResponse> redeployRequest(
			final D... deployables) {
		return redeployRequest(CommonTools.getSet(deployables));
	}

	public <D extends Deployable> ServiceHelperWithNotificationResponse<RedeployWithDeployables, RedeployResponse> redeployRequest(
			final Collection<D> deployables) {
		
		RedeployWithDeployables redeployRequest = createDeploymentRequest(RedeployWithDeployables.T, deployables);

		return new ServiceHelperWithNotificationResponse<RedeployWithDeployables, RedeployResponse>(session(), redeployRequest)
				.verifyingResultBy(() -> deployables.stream().allMatch(d -> d.getDeploymentStatus().equals(DeploymentStatus.deployed)))
				.addEntitiesForAutoRefresh(deployables);
	}

	public ServiceHelperWithNotificationResponse<AddDependencies, MergeModelsResponse> mergeModelsRequest(GmMetaModel targetModel,
			GmMetaModel... modelsToMerge) throws GmSessionException {
		return mergeModelsRequest(targetModel, CommonTools.getList(modelsToMerge));
	}

	public ServiceHelperWithNotificationResponse<AddDependencies, MergeModelsResponse> mergeModelsRequest(GmMetaModel targetModel,
			Collection<GmMetaModel> modelsToMerge) throws GmSessionException {
		logger.info("Merging model " + targetModel + " with the models " + modelsToMerge);

		AddDependencies mergeRequest = AddDependencies.T.create();
		mergeRequest.setModel(targetModel);
		mergeRequest.setDependencies(new ArrayList<>(modelsToMerge));

		return new ServiceHelperWithNotificationResponse<AddDependencies, MergeModelsResponse>(session(), mergeRequest)
				.verifyingResultBy(() -> targetModel.getDependencies().containsAll(modelsToMerge));

	}

	public ServiceHelperWithNotificationResponse<TestDatabaseConnection, TestConnectionResponse> testConnectionRequest(
			DatabaseConnectionPool connector) throws GmSessionException {
		logger.info("Testing connection with externalId '" + connector.getExternalId() + "'");

		TestDatabaseConnection testConnection = TestDatabaseConnection.T.create();
		testConnection.setConnectionPool(connector);

		return new ServiceHelperWithNotificationResponse<>(session(), testConnection);
	}

	/**
	 * Calls SetupAspects service, which ensures the default aspects on given access
	 *
	 * @param access
	 *            The access where you want to set up the default aspects
	 * @param resetToDefault
	 *            <u>true</u>: clear all assigned aspects first. <u>false</u>: only add aspects when they are not
	 *            already assigned
	 */
	public ServiceHelperWithNotificationResponse<SetupAspects, SetupAccessResponse> setupAspectsRequest(IncrementalAccess access,
			boolean resetToDefault) {
		SetupAspects request = SetupAspects.T.create();
		request.setAccess(access);
		request.setResetToDefault(resetToDefault);

		return new ServiceHelperWithNotificationResponse<>(session(), request);
	}

	/**
	 * Be aware that the service looks first if there exists already in the cortex a matching workbench model or
	 * workbench access. These are identified via name. i.e. if the access external id is "my.access" and the meta model
	 * "com.braintribe.MyModel" the service looks for "my.access.wb" and "com.braintribe.MyWorkbenchModel". If the
	 * service finds a matching model and/or access, as a safety measure the respective reset-flag must be set or
	 * nothing will be done. If it doesn't find one, it will create a minimal new one
	 *
	 * @param access
	 *            The access where you want to set up the workbench.
	 * @param resetAccess
	 *            Reset the properties (not the stored entities=folders) of the found matching access and assign this
	 *            access as workbench to given access if it isn't already assigned.
	 * @param resetModel
	 *            Reset the matching model and assign it as metamodel to the workbench access
	 */
	public ServiceHelperWithNotificationResponse<SetupWorkbench, SetupAccessResponse> setupWorkbenchRequest(IncrementalAccess access,
			boolean resetAccess, boolean resetModel) {
		SetupWorkbench request = SetupWorkbench.T.create();
		request.setAccess(access);
		request.setResetExistingAccess(resetAccess);
		request.setResetExistingModel(resetModel);

		return new ServiceHelperWithNotificationResponse<>(session(), request);
	}

	/**
	 * @param access
	 *            The access where you want to ensure the workbench standard folders. <b>NOT</b> the workbench access
	 *            but the access itself (that holds the workbench access)
	 */
	public ServiceHelperWithNotificationResponse<ConfigureWorkbench, SetupAccessResponse> ensureStandardWorkbenchFoldersRequest(
			IncrementalAccess access) {
		ConfigureWorkbench request = ConfigureWorkbench.T.create();
		request.setAccess(access);
		request.setEnsureStandardFolders(true);

		return new ServiceHelperWithNotificationResponse<>(session(), request);
	}
	
	private Deployable shallowify(Deployable deployable) {
		// @formatter:off
	    TraversingCriterion shallowifyingCriteria = 
	            TC.create()
	                .conjunction()
	                    .property()
	                    .typeCondition(or(isKind(TypeKind.collectionType), isKind(TypeKind.entityType)))
	                .close()
	            .done();
	    // @formatter:on
	    
		if (deployable == null) {
			throw new ImpException("null-deployable not allowed");
		}

        EntityType<? extends Deployable> entityType = deployable.entityType();

        StandardMatcher matcher = new StandardMatcher();
        matcher.setCriterion(shallowifyingCriteria);

        StandardCloningContext cloningContext = new StandardCloningContext();
        cloningContext.setAbsenceResolvable(true);
        cloningContext.setMatcher(matcher);

        Deployable exportedDeployable = (Deployable) entityType.clone(cloningContext, deployable, StrategyOnCriterionMatch.partialize);

        return exportedDeployable;
    }
	
	private <D extends DeploymentOperationWithDeployables> D createDeploymentRequest(EntityType<D> requestType, Collection<? extends Deployable> deployables){
		D request = requestType.create();
		
		request.setSessionId(session().getSessionAuthorization().getSessionId());

		// @formatter:off
		deployables.stream()
			.map(this::shallowify)
			.forEach(request.getDeployables()::add);
		// @formatter:on
		
		return request;
	}
	
}
