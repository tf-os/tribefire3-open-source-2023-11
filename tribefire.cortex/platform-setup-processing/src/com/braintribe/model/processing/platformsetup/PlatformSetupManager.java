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
package com.braintribe.model.processing.platformsetup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.AssetAggregator;
import com.braintribe.model.asset.natures.ContainerProjection;
import com.braintribe.model.asset.natures.LicensePriming;
import com.braintribe.model.asset.natures.ManipulationPriming;
import com.braintribe.model.asset.natures.MarkdownDocumentation;
import com.braintribe.model.asset.natures.MarkdownDocumentationConfig;
import com.braintribe.model.asset.natures.ModelPriming;
import com.braintribe.model.asset.natures.PlatformLibrary;
import com.braintribe.model.asset.natures.PrimingModule;
import com.braintribe.model.asset.natures.ResourcePriming;
import com.braintribe.model.asset.natures.RuntimeProperties;
import com.braintribe.model.asset.natures.ScriptPriming;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.model.asset.natures.TribefireWebPlatform;
import com.braintribe.model.asset.selector.ConjunctionDependencySelector;
import com.braintribe.model.asset.selector.DependencySelector;
import com.braintribe.model.asset.selector.DisjunctionDependencySelector;
import com.braintribe.model.asset.selector.IsDesigntime;
import com.braintribe.model.asset.selector.IsRuntime;
import com.braintribe.model.asset.selector.IsStage;
import com.braintribe.model.asset.selector.JunctionDependencySelector;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeInitializers;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStageToPredecessor;
import com.braintribe.model.cortexapi.access.collaboration.PushCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.RenameCollaborativeStage;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.notification.Level;
import com.braintribe.model.platformsetup.PlatformSetup;
import com.braintribe.model.platformsetup.api.data.AssetNature;
import com.braintribe.model.platformsetup.api.request.AddAssetDependencies;
import com.braintribe.model.platformsetup.api.request.CloseTrunkAsset;
import com.braintribe.model.platformsetup.api.request.CloseTrunkAssetForAccess;
import com.braintribe.model.platformsetup.api.request.CreatePlatformSetup;
import com.braintribe.model.platformsetup.api.request.GetAssets;
import com.braintribe.model.platformsetup.api.request.MergeTrunkAsset;
import com.braintribe.model.platformsetup.api.request.MergeTrunkAssetForAccess;
import com.braintribe.model.platformsetup.api.request.RenameAsset;
import com.braintribe.model.platformsetup.api.request.TransferAsset;
import com.braintribe.model.platformsetup.api.response.AssetCollection;
import com.braintribe.model.platformsetup.api.response.PlatformAssetResponse;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.lcd.LazyInitialized;

public class PlatformSetupManager implements AccessRequestProcessor<AccessRequest, Object> {

	private static final Logger logger = Logger.getLogger(PlatformSetupManager.class);
	private final static String TRUNK_PREFIX = "trunk-";
	private Supplier<PersistenceGmSession> platformSetupSessionProvider;
	private ResourceBuilder resourceBuilder;
	protected Evaluator<ServiceRequest> requestEvaluator;
	private static Map<AssetNature, String> natureToSignature = new HashMap<>();
	
	// cache
	private final Map<String, Boolean> accessIdsToTrunkAssets = new ConcurrentHashMap<>();
	
	@Required
	public void setPlatformSetupSessionProvider(Supplier<PersistenceGmSession> platformSetupSessionProvider) {
		this.platformSetupSessionProvider = platformSetupSessionProvider;
	}

	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
	
	@Required
	public void setResourceBuilder(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}

	private final AccessRequestProcessor<AccessRequest, Object> dispatcher = AccessRequestProcessors.dispatcher(config -> {
		config.register(MergeTrunkAsset.T, this::mergeTrunkAsset);
		config.register(CloseTrunkAsset.T, this::closeTrunkAsset);
		config.register(CreatePlatformSetup.T, this::createPlatformSetup);
		config.register(TransferAsset.T, this::transferAsset);
		config.register(MergeTrunkAssetForAccess.T, this::mergeTrunkAssetForAccess);
		config.register(CloseTrunkAssetForAccess.T, this::closeTrunkAssetForAccess);
		
		config.register(AddAssetDependencies.T, this::addAssetDependencies);
		config.register(RenameAsset.T, this::renameAsset);
		
		config.register(GetAssets.T, this::getAssets);
	});

	@Override
	public Object process(AccessRequestContext<AccessRequest> context) {
		return dispatcher.process(context);
	}

	// ###############################################
	// ## . . . . . . Merge trunk asset . . . . . . ##
	// ###############################################

	public PlatformAssetResponse mergeTrunkAsset(AccessRequestContext<MergeTrunkAsset> context) {
		MergeTrunkAsset request = context.getRequest();
		PlatformAsset asset = request.getAsset();
		PersistenceGmSession session = context.getSession();
		
		StringBuilder builder = new StringBuilder();
		
		String accessId = getAccessIdForTrunkAsset(asset);
		try {
			if (request.getMergeIntoPredecessor()) {
				
				if (!isValidCandidateForMerge(asset, accessId))
					throw new IllegalArgumentException(asset.qualifiedAssetName() + " is not a valid merge candidate.");
				
				MergeCollaborativeStageToPredecessor merge = MergeCollaborativeStageToPredecessor.T.create();
				merge.setName("trunk");
				merge.setServiceId(accessId);
				merge.eval(context).get();
				
				SmoodInitializer stage = getLatestStageForAccess(accessId);
				PlatformAsset latest = getAssetByStageName(session, stage.getName());
				latest.setHasUnsavedChanges(true);
				
				builder.append("Merged trunk asset into predecessor " + latest.qualifiedAssetName());
				
			} else {
				PlatformAsset target = request.getTargetAsset();
				
				MergeCollaborativeStage merge = MergeCollaborativeStage.T.create();
				merge.setSource("trunk");
				merge.setTarget(getCollaborativeStageName(target));
				merge.setServiceId(accessId);
				merge.eval(context).get();
				
				target.setHasUnsavedChanges(true);
				
				builder.append("Merged trunk asset into " + target.qualifiedAssetName());
				
			}
			
			session.deleteEntity(asset);
			session.commit();
			
			// purge cache
			accessIdsToTrunkAssets.remove(accessId);
			logger.debug(() -> "Removed trunk asset for access " + accessId + " from cache.");
			
		} catch (RuntimeException e) {
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message(e.getMessage())
						.level(Level.WARNING)
					.close()
				.close()
			.enrichException(e);
			// @formatter:on
		}

		
		
		// check for transfer operations
		String transferOperation = request.getTransferOperation();
		if(!CommonTools.isEmpty(transferOperation)) {
			
			PlatformAsset transferAsset = null;
			if (request.getMergeIntoPredecessor()) {
				// TODO additional Access Ids?
				GetCollaborativeInitializers getInitializers = GetCollaborativeInitializers.T.create();
				getInitializers.setServiceId(accessId);
				List<SmoodInitializer> initializers = getInitializers.eval(requestEvaluator).get();
				
				SmoodInitializer stage = CollectionTools.getElementUnlessIndexOutOfBounds(initializers, initializers.size()-2);
				if (stage != null && stage instanceof ManInitializer) {
					PersistenceGmSession platformSetupSession = platformSetupSessionProvider.get();	
					transferAsset = getAssetByStageName(platformSetupSession, stage.getName());
				}
				
			} else {
				transferAsset = request.getTargetAsset();
			}
			
			if (transferAsset != null) {
				TransferAsset transfer = TransferAsset.T.create();
				transfer.setDomainId(context.getSession().getAccessId());
				transfer.setTransferOperation(transferOperation);
				transfer.setAsset(transferAsset);
				transfer.eval(context.getSession()).get();
				
				builder.append("\n\nAdditionally " + transferOperation + "ed asset '" + transferAsset.qualifiedAssetName() + "'"); // TODO into repo
			}
		}
		
		PlatformAssetResponse response = buildResponse(builder.toString(), Level.INFO);
		return response;
	}

	private boolean isValidCandidateForMerge(PlatformAsset asset, String accessId) {
		List<PlatformAsset> results = getManPrimingAssetsForAccess(asset, accessId);
		
		int size = results.size();
		if (size != 1)
			throw new IllegalStateException("Expected 1 but found " + size + " ManipulationPriming candidates to be merged in dependency chain.");
		
		return true;
	}

	/**
	 * Parses the dependencies of the given <code>asset</code> and searches for {@link ManipulationPriming
	 * ManipulationPriming} assets which relate to the given <code>accessId</code>.<br>
	 * If an asset was found it is added to a list. <i>Its dependencies are not further traversed.</i>.<br>
	 * Afterwards the list of assets found is returned.
	 * 
	 * @param asset
	 *            The entry point for parsing dependencies
	 * @param accessId
	 *            The id of the access the assets relate to
	 * @return a list of {@link PlatformAsset assets}
	 */
	private List<PlatformAsset> getManPrimingAssetsForAccess(PlatformAsset asset, String accessId) {
		List<PlatformAsset> results = new ArrayList<>();
		Set<PlatformAsset> visited = new HashSet<>();
		
		List<PlatformAsset> dependencies = asset.getQualifiedDependencies().stream().map(d -> d.getAsset()).collect(Collectors.toList());
		parseAssetDependencies(dependencies, accessId, results, visited);
		return results;
	}

	private void parseAssetDependencies(List<PlatformAsset> dependencies, String accessId, List<PlatformAsset> results,
			Set<PlatformAsset> visited) {

		for (PlatformAsset dependency : dependencies) {
			if (!visited.add(dependency))
				continue; // avoid visits of already visited assets

			if (isAccessRelatedManPriming(dependency, accessId)) {
				results.add(dependency);
				//break;
				
			} else {
				List<PlatformAsset> deps = dependency.getQualifiedDependencies().stream().map(d -> d.getAsset()).collect(Collectors.toList());
				if (!CommonTools.isEmpty(deps)) {
					parseAssetDependencies(deps, accessId, results, visited);
				}
			}
		}

	}

	private boolean isAccessRelatedManPriming(PlatformAsset asset, String accessId) {
		if (!(asset.getNature() instanceof ManipulationPriming))
			return false;

		return ((ManipulationPriming) asset.getNature()).getAccessId().equals(accessId);
	}

	private String getAccessIdForTrunkAsset(PlatformAsset asset) {
		String name = asset.getName();
		if (!name.startsWith(TRUNK_PREFIX))
			throw new IllegalArgumentException(asset.qualifiedAssetName() + " is not a trunk asset.");
			
		return name.substring(TRUNK_PREFIX.length());
	}

	// ###############################################
	// ## . . . . . . Close trunk asset . . . . . . ##
	// ###############################################

	public PlatformAssetResponse closeTrunkAsset(AccessRequestContext<CloseTrunkAsset> context) {

		CloseTrunkAsset request = context.getRequest();
		PersistenceGmSession session = context.getSession();

		PlatformAsset asset = request.getAsset();
		String accessId = getAccessIdForTrunkAsset(asset);

		ensureAssetIdentification(request, session);

		enrichAssetInstance(asset, request);

		session.commit();

		// create the correct name for stage
		String stageName = getCollaborativeStageName(asset);
		
		// rename current trunk stage to the name of the asset to be closed
		RenameCollaborativeStage renameStage = RenameCollaborativeStage.T.create();
		renameStage.setNewName(stageName);
		renameStage.setOldName("trunk");
		renameStage.setServiceId(accessId);
		renameStage.eval(context).get();

		// create new trunk stage
		PushCollaborativeStage pushStage = PushCollaborativeStage.T.create();
		pushStage.setName("trunk");
		pushStage.setServiceId(accessId);
		pushStage.eval(context).get();

		// purge the cache to ensure correct trunk information
		accessIdsToTrunkAssets.remove(accessId);
		logger.debug(() -> "Removed trunk asset for access " + accessId + " from cache.");
		
		
		StringBuilder responseBuilder = new StringBuilder();
		responseBuilder.append("Closed asset with values " + asset.qualifiedAssetName());
		
		// check for transfer operations
		if(!CommonTools.isEmpty(request.getTransferOperation())) {
			
			String transferOperation = request.getTransferOperation();
			TransferAsset transfer = TransferAsset.T.create();
			transfer.setDomainId(context.getSession().getAccessId());
			transfer.setTransferOperation(transferOperation);
			transfer.setAsset(asset);
			transfer.eval(context.getSession()).get();
			
			responseBuilder.append("\n\nAdditionally " + transferOperation + "ed asset '" + asset.qualifiedAssetName() + "'");
		}
		
		
		PlatformAssetResponse response = buildResponse(responseBuilder.toString(), Level.INFO);
		return response;
	}

	private String getCollaborativeStageName(PlatformAsset asset) {
		return getCollaborativeStageName(asset.getGroupId(), asset.getName(), asset.getVersion());
	}
	
	private String getCollaborativeStageName(String groupId, String name, String version) {
		return groupId + ':' + name + '#' + version;
	}

	private void ensureAssetIdentification(CloseTrunkAsset request, PersistenceGmSession session) {
		String r_name = request.getName();
		String r_groupId = request.getGroupId();
		String r_version = request.getVersion();

		//@formatter:off
		SelectQuery query = new SelectQueryBuilder().from(PlatformAsset.T, "p")
				.where()
					.conjunction()
						.property("p",PlatformAsset.name).eq(r_name)
						.property("p",PlatformAsset.groupId).eq(r_groupId)
						.property("p",PlatformAsset.version).eq(r_version)
					.close()
				.select().count("p")
				.done();
		//@formatter:on

		Long result = session.query().select(query).unique();
		if (result != 0L) {
			String msg = "Asset '" + r_groupId + ":" + r_name + "#" + r_version + "' already exists! Please provide a unique identifier.";
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message(msg)
					.close()
				.close()
			.enrichException(new IllegalStateException(msg));
			// @formatter:on
		}
	}
	
	private void enrichAssetInstance(PlatformAsset asset, CloseTrunkAsset request) {

		String r_name = request.getName();
		String r_groupId = request.getGroupId();
		String r_version = request.getVersion();

		validateAssetIdentification(r_name, r_groupId);
		
		if (r_version == null)
			r_version = "1.0";

		asset.setName(r_name);
		asset.setGroupId(r_groupId);
		asset.setVersion(r_version);

	}

	// #####################################################################
	// ## . . . . . . MergeTrunkAssetForAccess implementation . . . . . . ##
	// #####################################################################
	
	public com.braintribe.model.notification.Notifications mergeTrunkAssetForAccess(AccessRequestContext<MergeTrunkAssetForAccess> context) {
		
		MergeTrunkAssetForAccess request = context.getRequest();
		String accessId = request.getAccessId();

		PlatformAsset asset = getTrunkAsset(context.getSession(), accessId);
		
		MergeTrunkAsset merge = MergeTrunkAsset.T.create();
		merge.setAsset(asset);
		merge.setTransferOperation(request.getTransferOperation());
		
		PlatformAssetResponse platformResponse = merge.eval(context.getSession()).get();
		com.braintribe.model.notification.Notifications response = com.braintribe.model.notification.Notifications.T.create();
		response.setNotifications(platformResponse.getNotifications());
		return response;
		
	}
	
	private PlatformAsset getTrunkAsset(PersistenceGmSession session, String accessId) {
		
		EntityQuery query = EntityQueryBuilder.from(PlatformAsset.T).where().property(PlatformAsset.name).eq(TRUNK_PREFIX + accessId).done();

		PlatformAsset asset = session.query().entities(query).first();
		
		if (asset == null) {
			String msg = "Could not determine trunk asset (access: " + accessId+ "). Queried asset is null.";
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message(msg)
					.close()
				.close()
			.enrichException(new IllegalStateException(msg));
			// @formatter:on
		}
		
		return asset;
	}
	
	private PlatformAsset getAsset(PersistenceGmSession session, String stageName) {
		
		PlatformAsset asset = findAssetByStageName(session, stageName);
		
		if (asset == null) {
			String msg = "Could not determine asset for " + stageName;
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message(msg)
					.close()
				.close()
			.enrichException(new IllegalStateException(msg));
			// @formatter:on
		}

		return asset;
	}

	// #####################################################################
	// ## . . . . . . CloseTrunkAssetForAccess implementation . . . . . . ##
	// #####################################################################
	
	public com.braintribe.model.notification.Notifications closeTrunkAssetForAccess(AccessRequestContext<CloseTrunkAssetForAccess> context) {
		
		CloseTrunkAssetForAccess request = context.getRequest();
		String accessId = request.getAccessId();
		
		PlatformAsset asset = getTrunkAsset(context.getSession(), accessId);
		
		CloseTrunkAsset close = CloseTrunkAsset.T.create();
		close.setAsset(asset);
		close.setGroupId(request.getGroupId());
		close.setName(request.getName());
		close.setVersion(request.getVersion());
		close.setTransferOperation(request.getTransferOperation());

		PlatformAssetResponse platformResponse = close.eval(context.getSession()).get();
		com.braintribe.model.notification.Notifications response = com.braintribe.model.notification.Notifications.T.create();
		response.setNotifications(platformResponse.getNotifications());
		return response;
		
	}
	
	// ################################################################
	// ## . . . . . . . . TransferAsset implementation . . . . . . . ##
	// ################################################################
	
	public PlatformAssetResponse transferAsset(AccessRequestContext<TransferAsset> context) {
		PlatformAsset asset = context.getRequest().getAsset();
		try {
			return new AssetArtifactBuilder(asset, context, resourceBuilder).build();

		} catch (RuntimeException e) {
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message("Transfer failed: " + e.getMessage() + "\n\nFor more details please check the log. ")
						.level(Level.WARNING)
					.close()
				.close()
			.enrichException(e);
			// @formatter:on
		}
	}
	
	// ################################################################
	// ## . . . . . . CreatePlatformSetup implementation . . . . . . ##
	// ################################################################

	public PlatformAssetResponse createPlatformSetup(AccessRequestContext<CreatePlatformSetup> context) {
		
		PersistenceGmSession platformSession = platformSetupSessionProvider.get();
		
		EntityQuery query = EntityQueryBuilder.from(PlatformSetup.T).done();
		PlatformSetup platformSetup = platformSession.query().entities(query).unique();

		PlatformAsset terminalAsset = context.getRequest().getTerminalAsset();
		PlatformAsset setupAsset = context.getRequest().getSetupAsset();

		StringBuilder responseBuilder = new StringBuilder();
		if (platformSetup != null) {
			platformSetup.setTerminalAsset(terminalAsset);
			platformSetup.setSetupAsset(setupAsset);
			
			responseBuilder.append("Updated PlatformSetup instance:");
			
		} else {
			PlatformSetup setup = PlatformSetup.T.create();
			setup.setTerminalAsset(terminalAsset);
			setup.setTerminalAsset(setupAsset);
			
			responseBuilder.append("Created PlatformSetup:");
		}
		
		platformSession.commit();

		responseBuilder.append("\n\nTerminal Asset:\n");
		responseBuilder.append((terminalAsset==null) ? "<not set>" : terminalAsset.qualifiedAssetName());

		responseBuilder.append("\n\nSetup Asset:\n");
		responseBuilder.append((setupAsset==null) ? "<not set>" : setupAsset.qualifiedAssetName());
		
		
		PlatformAssetResponse response = buildResponse(responseBuilder.toString(), Level.INFO);
		return response;
	}

	// ##########################################################
	// ## . . . . . . Manipulation Append Listener . . . . . . ##
	// ##########################################################
	
	public void notifyAppendedManipulation(CollaborativeAccess access, @SuppressWarnings("unused") Manipulation manipulation) {

		String accessId = ((IncrementalAccess) access).getAccessId();

		accessIdsToTrunkAssets.computeIfAbsent(accessId, k -> {
			return ensureTrunkAssetFor(accessId);
		});

	}
	
	private boolean ensureTrunkAssetFor(String accessId) {
		PersistenceGmSession session = platformSetupSessionProvider.get();
		
		try {
			if(trunkAssetExists(accessId, session))
				return true;

			PlatformAsset trunkAsset = session.create(PlatformAsset.T);
			trunkAsset.setName(TRUNK_PREFIX + accessId);

			ManipulationPriming manPrimingNature = session.create(ManipulationPriming.T);
			manPrimingNature.setAccessId(accessId);
			trunkAsset.setNature(manPrimingNature);
			trunkAsset.setNatureDefinedAsPart(true);
			
			
			GetCollaborativeInitializers getInitializers = GetCollaborativeInitializers.T.create();
			getInitializers.setServiceId(accessId);
			List<SmoodInitializer> initializers = getInitializers.eval(requestEvaluator).get();
			
			Set<PlatformAsset> assetsForAccess = new LinkedHashSet<>();
			for (SmoodInitializer initializer : initializers) {
				String stageName = initializer.getName();
				PlatformAsset asset = findAssetByStageName(session, stageName);
				if (asset != null)
					assetsForAccess.add(asset);
			}
			
			removeTransitiveDependencies(assetsForAccess);
				
			for (PlatformAsset asset : assetsForAccess) {
				PlatformAssetDependency dependency = craftAssetDependency(session, asset);
				trunkAsset.getQualifiedDependencies().add(dependency);
			}
				
			
			trunkAsset.setHasUnsavedChanges(true);
			session.commit();
			
			logger.debug(() -> "Created trunk asset for access " + accessId);

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while ensuring trunk asset (access: " + accessId + ").");
		}

		return true;
	}

	private void removeTransitiveDependencies(Set<PlatformAsset> assets) {
		
		Set<PlatformAsset> visited = new HashSet<>();
		Set<PlatformAsset> transitiveDependencies = new HashSet<>();
		for (PlatformAsset asset : assets) {
			collectTransitiveDependencies(asset, visited, transitiveDependencies);
		}
		
		assets.removeAll(transitiveDependencies); // top level candidates retain
		
	}

	private void collectTransitiveDependencies(PlatformAsset asset, Set<PlatformAsset> visited, Set<PlatformAsset> transitiveDependencies) {
		if(!visited.add(asset)) {
			transitiveDependencies.add(asset);
			return;
		}
		
		asset.getQualifiedDependencies().stream().map(PlatformAssetDependency::getAsset).forEach(a -> collectTransitiveDependencies(a, visited, transitiveDependencies));
	}

	private PlatformAsset findAssetByStageName(PersistenceGmSession session, String stageName) {
		String[] split = stageName.split(":|#");
		if(split.length != 3)
			return null;
		
		//@formatter:off
		EntityQuery query = EntityQueryBuilder.from(PlatformAsset.T)
				.where()
					.conjunction()
						.property(PlatformAsset.groupId).eq(split[0])
						.property(PlatformAsset.name).eq(split[1])
						.property(PlatformAsset.version).eq(split[2])
					.close()
				.done();
		//@formatter:on

		PlatformAsset asset = session.query().entities(query).first();
		return asset;
	}

	private boolean trunkAssetExists(String accessId, PersistenceGmSession session) {
		//@formatter:off
		EntityQuery query = EntityQueryBuilder.from(PlatformAsset.T)
				.where()
				.property(PlatformAsset.name).eq(TRUNK_PREFIX + accessId)
				.done();
		//@formatter:on
		
		return session.query().entities(query).first() != null;
	}

	private PlatformAssetDependency craftAssetDependency(PersistenceGmSession session, PlatformAsset dependency) {
		PlatformAssetDependency assetDependency = session.create(PlatformAssetDependency.T);
		assetDependency.setAsset(dependency);
		
		return assetDependency;
	}

	private SmoodInitializer getLatestStageForAccess(String accessId) {
		GetCollaborativeInitializers getInitializers = GetCollaborativeInitializers.T.create();
		getInitializers.setServiceId(accessId);
		List<SmoodInitializer> initializers = getInitializers.eval(requestEvaluator).get();
		
		SmoodInitializer stage = CollectionTools.getElementUnlessIndexOutOfBounds(initializers, initializers.size()-2);
		return stage;
	}

	private boolean isDepender(PlatformAsset asset, PlatformAsset candidate, Set<PlatformAsset> visited, Stack<PlatformAsset> path) {
		if (!visited.add(asset))
			return false; // avoid visits of already visited assets
		
		path.push(asset);
		
		if (asset == candidate)
			return true;
			
		boolean isDepender = asset.getQualifiedDependencies().stream().filter(d -> isDepender(d.getAsset(), candidate, visited, path)).findFirst().isPresent();
		if (!isDepender)
			path.pop();
		
		return isDepender;
	}

	private PlatformAsset getAssetByStageName(PersistenceGmSession session, String stageName) {
		
		
		return getAsset(session, stageName);
	}
	
	
	// ####################################################
	// ## . . . . . . Add asset dependencies . . . . . . ##
	// ####################################################
	
	public PlatformAssetResponse addAssetDependencies(AccessRequestContext<AddAssetDependencies> context) {
		
		AddAssetDependencies request = context.getRequest();
		PlatformAsset depender = request.getDepender();
		
		StringBuilder builder = new StringBuilder();
		try {
			if (depender == null)
				throw new IllegalArgumentException("Property depender must not be null.");
			
			builder.append("Added ");
			builder.append(request.getDependencies().size());
			builder.append(" asset(s) as dependency to ");
			builder.append(depender.qualifiedAssetName());
			
			List<PlatformAsset> dependencies = request.getDependencies();

			HashSet<PlatformAsset> visited = new HashSet<>();
			
			for (PlatformAsset dependency : dependencies) {
				Stack<PlatformAsset> path = new Stack<>();
				if (isDepender(dependency, depender, visited, path)) {
					StringBuilder errorBuilder = new StringBuilder();
					
					errorBuilder.append("Operation denied. You would introduce a cyclic dependency via " + dependency.qualifiedAssetName() + ". Critical path:\n");
					for (PlatformAsset a : path) {
						errorBuilder.append("  - ");
						errorBuilder.append(a.qualifiedAssetName());
						errorBuilder.append('\n');
					}
					
					throw new IllegalArgumentException(errorBuilder.toString());
				}
			}
			
			
			if (request.getRemoveRedundantDepsOfDepender()) {
				Iterator<PlatformAssetDependency> iterator = depender.getQualifiedDependencies().iterator();
				while (iterator.hasNext()) {
					PlatformAssetDependency dependency = iterator.next();
					PlatformAsset asset = dependency.getAsset();
					
					if (visited.contains(asset)) {
						iterator.remove();
						context.getSession().deleteEntity(dependency);
						
						builder.append("\nRemoved redundant dependency ");
						builder.append(asset.qualifiedAssetName());
						
					}
				}
			}
			
			DependencySelector selector = buildSelector(context);
			for (PlatformAsset dependency : dependencies) {
				
				PlatformAssetDependency pad = context.getSession().create(PlatformAssetDependency.T);
				pad.setAsset(dependency);
				pad.setIsGlobalSetupCandidate(request.getAsGlobalSetupCandidate());
				
				if (selector != null)
					pad.setSelector(selector);
				
				depender.getQualifiedDependencies().add(pad);
				
				depender.setHasUnsavedChanges(true);
			}
			
		} catch (RuntimeException e) {
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message(e.getMessage())
						.level(Level.WARNING)
					.close()
				.close()
			.enrichException(e);
			// @formatter:on
		}
		
		
		PlatformAssetResponse response = buildResponse(builder.toString(), Level.INFO);
		return response;
	}
	
	// ##########################################
	// ## . . . . . . Rename asset . . . . . . ##
	// ##########################################
	
	public PlatformAssetResponse renameAsset(AccessRequestContext<RenameAsset> context) {
		RenameAsset request = context.getRequest();
		PlatformAsset asset = request.getAsset();
		
		try {
			String r_groupId = request.getGroupId();
			String r_name = request.getName();
			String r_version = request.getVersion();
			
			if (r_groupId == null && r_name == null && r_version == null)
				throw new IllegalArgumentException("At least property group id, name or version must be set!");
			
			String groupId = r_groupId != null ? r_groupId : asset.getGroupId();
			String name = r_name != null ? r_name : asset.getName();
			String version = r_version != null ? r_version : asset.getVersion();
			
			if (asset.getNature() instanceof ManipulationPriming) {
				ManipulationPriming manPrimingNature = (ManipulationPriming)asset.getNature();
				
				Stream.concat(Stream.of(manPrimingNature.getAccessId()), manPrimingNature.getAdditionalAccessIds().stream()) //
						.forEach(accessId -> {
							RenameCollaborativeStage rename = RenameCollaborativeStage.T.create();
							rename.setOldName(getCollaborativeStageName(asset));
							rename.setNewName(getCollaborativeStageName(groupId, name, version));
							rename.setServiceId(accessId);
							rename.eval(context).get();
						});
			}
			
			asset.setGroupId(groupId);
			asset.setName(name);
			asset.setVersion(version);
			
		} catch (RuntimeException e) {
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message(e.getMessage())
						.level(Level.WARNING)
					.close()
				.close()
			.enrichException(e);
			// @formatter:on
		}
		
		PlatformAssetResponse response = buildResponse("Renamed asset to " + asset.qualifiedAssetName(), Level.INFO);
		return response;
	}
	
	public AssetCollection getAssets(AccessRequestContext<GetAssets> context) {
		// session to setup access
		PersistenceGmSession session = platformSetupSessionProvider.get();
		
		GetAssets request = context.getRequest();
		
		SelectQuery query = buildQuery(request);
		List<PlatformAsset> assets = session.queryDetached().select(query).setTraversingCriterion(TC.create().negation().joker().done()).list();
		
		AssetCollection response = AssetCollection.T.create();
		response.setAssets(assets);
		return response;
	}
	
	private SelectQuery buildQuery(GetAssets request) {
		SelectQueryBuilder qb = null;
		String alias = "a";
		
		if (request.getSetupAssets()) {
			qb = new SelectQueryBuilder().from(PlatformSetup.T, "s").join("s", PlatformSetup.setupAsset, alias);
		} else {
			qb = new SelectQueryBuilder().from(PlatformAsset.T, alias);
		}
		
		boolean originContainsWildcard = request.getRepoOrigin().stream().anyMatch(r -> r.contains("*"));
		if(originContainsWildcard)
			qb.join(alias, "repositoryOrigins", "r");
		
		LazyInitialized<JunctionBuilder<SelectQueryBuilder>> conjunctionSupplier = new LazyInitialized<>(qb.where()::conjunction);
		
		// name
		Set<String> names = request.getName();
		if(!names.isEmpty()) {
			for(String n : names) {
				if(n.contains("*"))
					conjunctionSupplier.get().property(alias, PlatformAsset.name).like(n);
				else
					conjunctionSupplier.get().property(alias, PlatformAsset.name).eq(n);
			}
		}
		
		// group id
		Set<String> groupIds = request.getGroupId();
		if(!groupIds.isEmpty()) {
			for (String s : groupIds) {
				if (s.contains("*"))
					conjunctionSupplier.get().property(alias, PlatformAsset.groupId).like(s);
				else
					conjunctionSupplier.get().property(alias, PlatformAsset.groupId).eq(s);
			}
			
		}
		
		// repo origins
		Set<String> origins = request.getRepoOrigin();
		if(!origins.isEmpty()) {
			if(!originContainsWildcard) {
				JunctionBuilder<JunctionBuilder<SelectQueryBuilder>> nestedRepoOriginsDisjunction = conjunctionSupplier.get().disjunction();
				for(String o : origins) {
					nestedRepoOriginsDisjunction.value(o).in().property("repositoryOrigins");
				}
			} else {
				JunctionBuilder<JunctionBuilder<SelectQueryBuilder>> nestedRepoOriginsDisjunction = conjunctionSupplier.get().disjunction();
				for(String o : origins) {
					if (o.contains("*"))
						nestedRepoOriginsDisjunction.property(alias, null).like(o);
					else
						nestedRepoOriginsDisjunction.property(alias, null).eq(o);
				}
			}
		}
		
		
		// nature
		Set<String> natureSignatures = mapToNatureSig(request.getNature());
		if(!natureSignatures.isEmpty())
			conjunctionSupplier.get().entitySignature(alias, PlatformAsset.nature).in().value(natureSignatures);
		
		// effective
		Boolean effective = request.getEffective();
		if(effective != null)
			conjunctionSupplier.get().property(alias, PlatformAsset.effective).eq(effective);
		
		if(conjunctionSupplier.isInitialized())
			conjunctionSupplier.get().close();
		
		if(originContainsWildcard)
			qb.distinct();
		
		qb.select(alias);

		return qb.done();
	}
	
	private Set<String> mapToNatureSig(Set<AssetNature> natures) {
		Set<String> signatures = new HashSet<>();
		if (natures.isEmpty())
			return signatures;
		
		for(AssetNature n : natures)
			signatures.add(natureToSignature.get(n));
	
		return signatures;
	}

	static {
		// TODO create enum switch
		natureToSignature.put(AssetNature.AssetAggregator, AssetAggregator.T.getTypeSignature());
		natureToSignature.put(AssetNature.ContainerProjection, ContainerProjection.T.getTypeSignature());
		natureToSignature.put(AssetNature.LicensePriming, LicensePriming.T.getTypeSignature());
		natureToSignature.put(AssetNature.ManipulationPriming, ManipulationPriming.T.getTypeSignature());
		natureToSignature.put(AssetNature.MarkdownDocumentation, MarkdownDocumentation.T.getTypeSignature());
		natureToSignature.put(AssetNature.MarkdownDocumentationConfig, MarkdownDocumentationConfig.T.getTypeSignature());
		natureToSignature.put(AssetNature.ModelPriming, ModelPriming.T.getTypeSignature());
		natureToSignature.put(AssetNature.TribefireModule, TribefireModule.T.getTypeSignature());
		natureToSignature.put(AssetNature.PlatformLibrary, PlatformLibrary.T.getTypeSignature());
		natureToSignature.put(AssetNature.PrimingModule, PrimingModule.T.getTypeSignature());
		natureToSignature.put(AssetNature.ResourcePriming, ResourcePriming.T.getTypeSignature());
		natureToSignature.put(AssetNature.RuntimeProperties, RuntimeProperties.T.getTypeSignature());
		natureToSignature.put(AssetNature.ScriptPriming, ScriptPriming.T.getTypeSignature());
		natureToSignature.put(AssetNature.TribefireWebPlatform, TribefireWebPlatform.T.getTypeSignature());
	}
	
	// #############################################
	// ## . . . . . . Utility Methods . . . . . . ##
	// #############################################
	
	private DependencySelector buildSelector(AccessRequestContext<AddAssetDependencies> context) {
		AddAssetDependencies request = context.getRequest();
		PersistenceGmSession session = context.getSession();
		
		List<DependencySelector> selectors = new ArrayList<>();
		
		boolean asDesigntimeOnly = request.getAsDesigntimeOnly();
		boolean asRuntimeOnly = request.getAsRuntimeOnly();
		
		if (asDesigntimeOnly && asRuntimeOnly)
			throw new IllegalArgumentException("Proprties asDesigntimeOnly and asRuntimeOnly must not be true at the same time.");
		
		if (asDesigntimeOnly)
			selectors.add(session.create(IsDesigntime.T));
		
		if (asRuntimeOnly)
			selectors.add(session.create(IsRuntime.T));
		
		if (!request.getForStages().isEmpty()) {
			List<IsStage> stages = new ArrayList<>();
			
			for (String stage : request.getForStages()) {
				IsStage isStage = session.create(IsStage.T);
				isStage.setStage(stage);
				
				stages.add(isStage);
			}
			
			selectors.add(combineSelectors(stages, () -> session.create(DisjunctionDependencySelector.T)));
		}
		
		return combineSelectors(selectors, () -> session.create(ConjunctionDependencySelector.T));
		
	}
	
	private DependencySelector combineSelectors(Collection<? extends DependencySelector> selectors, Supplier<? extends JunctionDependencySelector> junctionFactory) {
		switch (selectors.size()) {
			case 0:
				return null;
				
			case 1:
				return selectors.iterator().next();
				
			default:
				JunctionDependencySelector junctionSelector = junctionFactory.get();
				junctionSelector.getOperands().addAll(selectors);
				return junctionSelector;
		}
		
	}

	private PlatformAssetResponse buildResponse(String message, Level level) {
		PlatformAssetResponse response = PlatformAssetResponse.T.create();
		response.setNotifications(Notifications
				.build()
					.add()
						.message()
							.message(message)
							.level(level)
						.close()
						.command().refresh("Refresh Access")
					.close()
				.list());
		return response;
	}
	
	private void validateAssetIdentification(String r_name, String r_groupId) {
		boolean success = true;
		String msg = null;
		
		if (r_name == null) {
			success = false;
			msg = "Please provide a name.";
		} else if (r_name.contains(" ")) {
			success = false;
			msg = "Name: '" + r_name + "' must not contain spaces.";
		}
		
		if (r_groupId == null) {
			success = false;
			msg = "Please provide a group id.";
		} else if (r_groupId.contains(" ")) {
			success = false;
			msg = "Group Id: '" +r_groupId + "' must not contain spaces.";
		}
		
		if (!success)
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message(msg)
						.level(Level.WARNING)
					.close()
				.close()
			.enrichException(new IllegalArgumentException(msg));
			// @formatter:on
			
	}

	
}
