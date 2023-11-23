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
package com.braintribe.model.processing.access.service.impl.standard;

import static com.braintribe.gwt.utils.genericmodel.GMCoreTools.stringify;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.metaModel;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.firstOrNull;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.NullSafe.nonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.annotations.NonNull;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.access.AccessIdentificationLookup;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.access.AccessServiceException;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.AccessDomain;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.accessapi.ModelEnvironmentServices;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyTransferCompetence;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.access.service.api.registry.AccessRegistrationInfo;
import com.braintribe.model.processing.access.service.api.registry.RegistryBasedAccessService;
import com.braintribe.model.processing.access.service.impl.standard.OriginAwareAccessRegistrationInfo.Origin;
import com.braintribe.model.processing.license.LicenseManager;
import com.braintribe.model.processing.license.LicenseManagerRegistry;
import com.braintribe.model.processing.license.exception.InvalidLicenseManagerConfigurationException;
import com.braintribe.model.processing.license.exception.LicenseExpiredException;
import com.braintribe.model.processing.license.exception.LicenseLoadException;
import com.braintribe.model.processing.license.exception.LicenseViolatedException;
import com.braintribe.model.processing.license.exception.NoLicenseConfiguredException;
import com.braintribe.model.processing.license.exception.SessionUnavailableException;
import com.braintribe.model.processing.license.glf.GlfLicenseManager;
import com.braintribe.model.processing.license.glf.LicenseTools;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.genericmodel.GMCoreTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.lcd.StopWatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The implementation of the {@link AccessService} interface that acts as a delegate service for different {@link IncrementalAccess}es.
 * 
 * @author gunther.schenk
 * 
 */
// TODO: reduce boilerplate code for logging
public class AccessServiceImpl implements RegistryBasedAccessService, AccessIdentificationLookup {
	private final String cortexExternalId = "cortex";

	private static Logger log = Logger.getLogger(AccessServiceImpl.class);

	private final Map<String, OriginAwareAccessRegistrationInfo> accessRegistry = new ConcurrentHashMap<>();

	private final Set<String> coreAccessIds = asSet(TribefireConstants.ACCESS_CORTEX); // Need at least this access because it holds the License
																						// resource

	private LicenseManager licenseManager;
	private boolean initialLicenseCheckPerformed;

	private Supplier<Set<String>> userRolesProvider;
	private ModelAccessoryFactory sysMaFactory;
	private ModelAccessoryFactory userMaFactory;

	private Set<String> trustedRoles = Collections.emptySet();

	private Set<String> workbenchRootFolderNames = asSet("root", "actionbar", "headerbar", "homeFolder", "tab-actionbar", "global-actionbar");

	private long queryExecutionWarningThreshold = 30000L;
	private long queryExecutionInfoThreshold = 10000L;

	private final Object registrationMonitor = new Object();
	private Supplier<PersistenceGmSession> internalCortexSessionSupplier;

	@Override
	public void registerAccess(com.braintribe.model.accessdeployment.IncrementalAccess deployable, IncrementalAccess access) {
		synchronized (registrationMonitor) {
			registerAccessSync(deployable, access);
		}
	}

	private void registerAccessSync(com.braintribe.model.accessdeployment.IncrementalAccess deployable, IncrementalAccess access) {
		Origin origin = (deployable instanceof HardwiredDeployable) ? Origin.CONFIGURATION : Origin.REGISTRATION;

		AccessRegistrationInfo accessInfo = new AccessRegistrationInfo();
		accessInfo.setAccess(access);
		accessInfo.setAccessDenotationType(deployable.entityType().getTypeSignature());
		accessInfo.setAccessId(deployable.getExternalId());
		accessInfo.setModelAccessId(cortexExternalId);

		if (deployable.getMetaModel() != null)
			accessInfo.setModelName(deployable.getMetaModel().getName());

		if (deployable.getName() != null)
			accessInfo.setName(deployable.getName());

		accessInfo.setResourceAccessFactoryId(deployable.getExternalId());

		if (deployable.getWorkbenchAccess() != null) {
			accessInfo.setWorkbenchAccessId(deployable.getWorkbenchAccess().getExternalId());
			if (deployable.getWorkbenchAccess().getMetaModel() != null)
				accessInfo.setWorkbenchModelName(deployable.getWorkbenchAccess().getMetaModel().getName());
		}

		GmMetaModel serviceModel = deployable.getServiceModel();
		if (serviceModel != null)
			accessInfo.setServiceModelName(serviceModel.getName());

		OriginAwareAccessRegistrationInfo regInfo = new OriginAwareAccessRegistrationInfo(accessInfo, origin);

		accessRegistry.put(deployable.getExternalId(), regInfo);

		if (origin == Origin.CONFIGURATION)
			coreAccessIds.add(deployable.getExternalId());
	}

	@Override
	public void unregisterAccess(com.braintribe.model.accessdeployment.IncrementalAccess deployable) {
		synchronized (registrationMonitor) {
			unregisterAccessSync(deployable);
		}
	}

	private void unregisterAccessSync(com.braintribe.model.accessdeployment.IncrementalAccess deployable) {
		accessRegistry.remove(deployable.getExternalId());
	}

	/** Returns an unmodifiable view of the internal access registry. */
	protected Map<String, OriginAwareAccessRegistrationInfo> getAccessRegistry() {
		return Collections.unmodifiableMap(accessRegistry);
	}

	/** Configures the accesses hard-wired to the service. */
	@Required
	public void setHardwiredAccessRegistrations(Set<AccessRegistrationInfo> hardwiredAccessRegistrations) {
		Map<String, OriginAwareAccessRegistrationInfo> originAwareDelegateMapping = buildOriginAwareMapping(hardwiredAccessRegistrations);
		this.accessRegistry.putAll(originAwareDelegateMapping);
	}

	// @formatter:off
	@Required public void setUserRolesProvider(Supplier<Set<String>> userRolesProvider) { this.userRolesProvider = userRolesProvider; }
	@Required public void setInternalCortexSessionSupplier(Supplier<PersistenceGmSession> internalCortexSessionSupplier) { this.internalCortexSessionSupplier = internalCortexSessionSupplier; }
	@Required public void setLicenseManager(LicenseManager licenseManager) { this.licenseManager = licenseManager; }
	@Required public void setSystemModelAccessoryFactory(ModelAccessoryFactory maFactory) { this.sysMaFactory = maFactory; }
	@Required public void setUserModelAccessoryFactory(ModelAccessoryFactory maFactory) { this.userMaFactory = maFactory; }

	@Configurable public void setWorkbenchRootFolderNames(Set<String> workbenchRootFolderNames) { this.workbenchRootFolderNames = workbenchRootFolderNames; }
	@Configurable public void setQueryExecutionInfoThreshold(long queryExecutionInfoThreshold) { this.queryExecutionInfoThreshold = queryExecutionInfoThreshold; }
	@Configurable public void setQueryExecutionWarningThreshold(long queryExecutionWarningThreshold) { this.queryExecutionWarningThreshold = queryExecutionWarningThreshold; }
	@Configurable public void setTrustedRoles(Set<String> trustedRoles) { this.trustedRoles = trustedRoles; }
	// @formatter:on

	private static Map<String, OriginAwareAccessRegistrationInfo> buildOriginAwareMapping(Set<AccessRegistrationInfo> hardwiredAccesses) {
		return buildIdMap(wrapHardwiredAccessRegistrations(hardwiredAccesses));
	}

	private static Collection<OriginAwareAccessRegistrationInfo> wrapHardwiredAccessRegistrations(Set<AccessRegistrationInfo> hardwiredAccesses) {
		return Collections2.transform(hardwiredAccesses, new AccessRegistrationWrappingFunction(Origin.CONFIGURATION));
	}

	private static ImmutableMap<String, OriginAwareAccessRegistrationInfo> buildIdMap(Collection<OriginAwareAccessRegistrationInfo> wrappers) {
		return Maps.uniqueIndex(wrappers, new RegistrationWrapperIndexingFunction());
	}

	/**
	 * Returns the {@link IncrementalAccess} identified by accessId.
	 * 
	 * @throws AccessServiceException
	 *             In case there is no access registered for ID <code>accessId</code>
	 */
	public IncrementalAccess getAccessDelegate(String accessId) throws AccessServiceException {
		if (!this.initialLicenseCheckPerformed) {
			this.initialLicenseCheckPerformed = true;
			try {
				this.licenseManager.checkLicense();
				LicenseTools.logLicense(this.licenseManager);

			} catch (SessionUnavailableException sue) {
				log.warn("Could not load the license because of problems with the session.", sue);

			} catch (NoLicenseConfiguredException nlce) {
				log.warn("No license has been configured.");
				log.debug("No license has been configured.", nlce);

			} catch (LicenseLoadException lle) {
				log.warn("Could not load or access the license.");
				log.debug("Could not load or access the license.", lle);

			} catch (InvalidLicenseManagerConfigurationException ilmce) {
				log.warn("The license manager could not be verified.");
				log.debug("The license manager could not be verified.", ilmce);

			} catch (LicenseExpiredException lee) {
				log.warn("The license has expired.");
				log.debug("The license has expired.", lee);

			} catch (LicenseViolatedException lve) {
				log.warn("Could not check the license: " + lve.getMessage());
				log.debug("Could not check the license.", lve);

			} catch (Throwable t) {
				log.error("Unexpected error while checking the license.", t);
			}

		}

		boolean isCoreAccess = this.coreAccessIds.contains(accessId);
		if (!isCoreAccess) {
			try {
				this.licenseManager.checkLicense();
				LicenseManager registeredLicenseManager = LicenseManagerRegistry.getRegisteredLicenseManager();
				if (!(registeredLicenseManager instanceof GlfLicenseManager)) {
					throw new InvalidLicenseManagerConfigurationException("Wrong license manager.");
				}
			} catch (LicenseViolatedException e) {
				throw new AccessServiceException("Could not validate license for access: " + accessId, e);
			}
		}

		OriginAwareAccessRegistrationInfo registrationInfo = getRegistrationInfo(accessId);
		return registrationInfo.getAccess();
	}

	@NonNull
	private OriginAwareAccessRegistrationInfo getRegistrationInfo(String accessId) throws AccessServiceException {
		nonNull(accessId, "accessId");

		OriginAwareAccessRegistrationInfo registrationInfo = this.accessRegistry.get(accessId);
		if (registrationInfo == null)
			throw new AccessServiceException("Access [ " + accessId + " ] is not deployed");

		return registrationInfo;
	}

	@Override
	public GmMetaModel getMetaModel(String accessId) throws AccessServiceException {
		try {
			return this.getAccessDelegate(accessId).getMetaModel();
		} catch (Exception e) {
			throw new AccessServiceException(e);
		}
	}

	private PersistenceGmSession getSession() {
		return this.internalCortexSessionSupplier.get();
	}

	@Override
	public GmMetaModel getMetaModelForTypes(Set<String> typeSignatures) throws AccessServiceException {
		List<GmMetaModel> dependencies = ModelForTypesFinder.find(getSession(), typeSignatures);

		GmMetaModel res = metaModel("virtual:VirtualAccessServiceModel");
		res.setVersion("0.0");
		res.setDependencies(dependencies);

		return res;
	}

	@Override
	public ManipulationResponse applyManipulation(String accessId, ManipulationRequest manipulationRequest) {
		StopWatch stopWatch = null;

		if (log.isTraceEnabled()) {
			log.trace("Received manipulation for access " + accessId + ":" + Constants.LINE_SEPARATOR
					+ GMCoreTools.getDescription(manipulationRequest));
			stopWatch = new StopWatch();
		}
		try {
			ManipulationResponse result = this.getAccessDelegate(accessId).applyManipulation(manipulationRequest);
			if (stopWatch != null)
				log.trace("Successfully applied manipulation via access " + accessId + " (" + stopWatch.getElapsedTime() + "ms).");

			return result;

		} catch (Exception e) {
			log.debug(() -> manDetails(accessId, manipulationRequest));
			throw e;
		}
	}

	private String manDetails(String accessId, ManipulationRequest manipulationRequest) {
		String s = manipulationRequest.getManipulation().stringify();
		StringBuilder message = new StringBuilder();
		message.append("An error occurred while applying following Manipulation to access: '" + accessId + "'. ");
		message.append('\n');
		message.append(s.substring(0, Math.min(s.length(), 10_000))); // Cutting manipulation stack to first 10K
		return message.toString();
	}

	// @formatter:off
	@Override public ReferencesResponse getReferences(String accessId, ReferencesRequest request) { return getAccessDelegate(accessId).getReferences(request); }
	@Override public SelectQueryResult query(String accessId, SelectQuery query) { return queryHelper(accessId, query, IncrementalAccess::query); }
	@Override public EntityQueryResult queryEntities(String accessId, EntityQuery query) { return queryHelper(accessId, query, IncrementalAccess::queryEntities); }
	@Override public PropertyQueryResult queryProperty(String accessId, PropertyQuery query) { return queryHelper(accessId, query, IncrementalAccess::queryProperty); }
	// @formatter:on

	private <Q extends Query, R extends QueryResult> R queryHelper(String accessId, Q query, BiFunction<IncrementalAccess, Q, R> queryFunction) {
		log.trace(() -> "Executing query: " + stringify(query) + " on access: " + accessId + ".");

		StopWatch stopWatch = new StopWatch();

		try {
			R result = queryFunction.apply(getAccessDelegate(accessId), query);

			logQueryExecution(accessId, query, result, stopWatch);

			return result;

		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Execution of query: " + stringify(query) + " failed on access: " + accessId + ".");
		}
	}

	private void logQueryExecution(String accessId, Query query, QueryResult queryResult, StopWatch sw) {
		if (!log.isWarnEnabled())
			return;

		long time = sw.getElapsedTime();

		LogLevel level = LogLevel.TRACE;
		if (time > queryExecutionWarningThreshold)
			level = LogLevel.WARN;
		else if (time > queryExecutionInfoThreshold)
			level = LogLevel.INFO;

		log.log(level, () -> "Executed query: " + stringify(query) + " on access: " + accessId + " (in " + time + "ms).");
		log.trace(() -> "Detailed description of query and query result:\n - AccessId: " + accessId + "\n - Time (in ms): " + time + " - Query:"
				+ stringify(query) + " - Result:"
				+ StringTools.truncateIfRequired(GMCoreTools.getDescription(queryResult), Numbers.THOUSAND * 10, true));
	}

	@Override
	public void registerAccess(AccessRegistrationInfo registrationInfo) {
		OriginAwareAccessRegistrationInfo originAwareAccessWrapper = new OriginAwareAccessRegistrationInfo(registrationInfo, Origin.REGISTRATION);
		accessRegistry.put(registrationInfo.getAccessId(), originAwareAccessWrapper);
	}

	/** Unregisters an access from the service. */
	@Override
	public void unregisterAccess(String accessId) {
		accessRegistry.remove(accessId);
	}

	@Override
	public ModelEnvironmentServices getModelEnvironmentServices(String accessId) throws AccessServiceException {
		OriginAwareAccessRegistrationInfo registrationInfo = getRegistrationInfo(accessId);
		return createModelEnvironmentFromRegistrationInfo(registrationInfo);
	}

	@Override
	public ModelEnvironmentServices getModelEnvironmentServicesForDomain(String accessId, AccessDomain accessDomain) throws AccessServiceException {
		return getModelEnvironmentServices(requireAccessIdForDomain(accessId, accessDomain));
	}

	@Override
	public ModelEnvironment getModelAndWorkbenchEnvironment(String accessId, Set<String> workbenchPerspectiveNames) throws AccessServiceException {
		log.debug(
				() -> "Called getModelAndWorkbenchEnvironment for access [" + accessId + "] and workbench perspectives " + workbenchPerspectiveNames);

		StopWatch watch = new StopWatch();
		ModelEnvironment modelEnvironment = _getModelAndWorkbenchEnvironment(accessId, workbenchPerspectiveNames);

		log.debug(() -> "Finished getModelAndWorkbenchEnvironment for access [" + accessId + "] and workbench perspectives "
				+ workbenchPerspectiveNames + " in " + watch.getElapsedTime() + "ms.");

		return modelEnvironment;
	}

	@Override
	public ModelEnvironment getModelAndWorkbenchEnvironment(String accessId) throws AccessServiceException {
		log.debug(() -> "Called getModelAndWorkbenchEnvironment for access: " + accessId);

		StopWatch watch = new StopWatch();
		ModelEnvironment modelEnvironment = _getModelAndWorkbenchEnvironment(accessId, null);

		log.debug(() -> "Finished getModelEnvironment for access: " + accessId + " in " + watch.getElapsedTime() + "ms.");

		return modelEnvironment;
	}

	private ModelEnvironment _getModelAndWorkbenchEnvironment(String accessId, Set<String> workbenchPerspectiveNames) throws AccessServiceException {
		ModelEnvironment modelEnvironment = getModelEnvironment(accessId);
		String workbenchAccessId = modelEnvironment.getWorkbenchModelAccessId();
		if (workbenchAccessId != null) {
			if (workbenchPerspectiveNames != null) {
				List<WorkbenchPerspective> perspectives = queryWorkbenchPerspectives(workbenchAccessId, workbenchPerspectiveNames);
				modelEnvironment.setPerspectives(perspectives);
			} else {
				Set<Folder> workbenchRootFolders = queryWorkbenchRootFolders(workbenchAccessId);
				modelEnvironment.setWorkbenchRootFolders(workbenchRootFolders);
			}

			WorkbenchConfiguration workbenchConfiguration = queryWorkbenchConfiguration(workbenchAccessId);
			modelEnvironment.setWorkbenchConfiguration(workbenchConfiguration);
		}

		return modelEnvironment;
	}

	private Set<Folder> queryWorkbenchRootFolders(String workbenchAccessId) throws AccessServiceException {
		EntityQuery wbRootFolderQuery = EntityQueryBuilder.from(Folder.class).where().property("name").in(workbenchRootFolderNames).done();
		wbRootFolderQuery.setNoAbsenceInformation(true);
		wbRootFolderQuery.setTraversingCriterion(createWorkbenchTraversingCriterion());

		EntityQueryResult wbRootFolderResult = queryEntities(workbenchAccessId, wbRootFolderQuery);
		Set<Folder> wbRootFolders = new HashSet<>();
		if (wbRootFolderResult != null)
			wbRootFolders.addAll((List<Folder>) (List<?>) wbRootFolderResult.getEntities());

		return wbRootFolders;
	}

	private WorkbenchConfiguration queryWorkbenchConfiguration(String workbenchAccessId) throws AccessServiceException {
		EntityQuery query = EntityQueryBuilder //
				.from(WorkbenchConfiguration.class) //
				.tc(createWorkbenchTraversingCriterion()) //
				.done();

		List<GenericEntity> wbConfigs = queryEntities(workbenchAccessId, query).getEntities();

		return wbConfigs.isEmpty() ? null : first(wbConfigs);
	}

	@Override
	public ModelEnvironment getModelEnvironment(String accessId) throws AccessServiceException {
		log.debug(() -> "Called getModelEnvironment for access: " + accessId);

		StopWatch watch = new StopWatch();
		OriginAwareAccessRegistrationInfo registrationInfo = getRegistrationInfo(accessId);

		ModelEnvironment modelEnvironment = createModelEnvironmentFromRegistrationInfo(registrationInfo);
		fillGmModels(modelEnvironment, registrationInfo);

		log.debug(() -> "Finished getModelEnvironment for access: " + accessId + " in " + watch.getElapsedTime() + "ms.");

		return removeDuplicates(modelEnvironment);
	}

	private <T> T removeDuplicates(T value) {
		return BaseType.INSTANCE.clone(new DuplicatesRemovingCloningContext(), value, null);
	}

	private static class DuplicatesRemovingCloningContext extends StandardCloningContext implements PropertyTransferCompetence {
		private final Map<Object, GenericEntity> idToEntity = newMap();

		@Override
		public <T> T getAssociated(GenericEntity entity) {
			T result = super.getAssociated(entity);
			if (result == null && entity.getId() != null)
				result = (T) idToEntity.get(entity.getId());
			return result;
		}

		@Override
		public void transferProperty(EntityType<?> sourceEt, GenericEntity entity, GenericEntity clone, Property property, Object value) {
			property.set(clone, value);

			if (value != null && property.isIdentifier())
				idToEntity.put(value, clone);
		}
	}

	private ModelEnvironment createModelEnvironmentFromRegistrationInfo(OriginAwareAccessRegistrationInfo ri) {
		ModelEnvironment modelEnvironment = ModelEnvironment.T.create();

		modelEnvironment.setDataAccessId(ri.getAccessId());
		modelEnvironment.setDataAccessDenotationType(ri.getAccessDenotationType());
		modelEnvironment.setMetaModelAccessId(ri.getMetaModelAccessId());
		modelEnvironment.setMetaModelName(ri.getDataMetaModelName());
		modelEnvironment.setResourceAccessFactoryId(ri.getResourceAccessFactoryId());
		modelEnvironment.setServiceModelName(ri.getServiceModelName());

		String wbAccessId = ri.getWorkbenchAccessId();
		if (wbAccessId != null && isAccessRegistered(wbAccessId))
			modelEnvironment.setWorkbenchModelAccessId(wbAccessId);

		return modelEnvironment;
	}

	private boolean isAccessRegistered(String accessId) {
		return accessRegistry.containsKey(accessId);
	}

	private void fillGmModels(ModelEnvironment modelEnvironment, OriginAwareAccessRegistrationInfo regInfo) {
		String accessId = regInfo.getAccessId();

		modelEnvironment.setDataModel(getDataModel(accessId));
		modelEnvironment.setServiceModel(getServiceModel(accessId));
		modelEnvironment.setWorkbenchModel(wbModel(regInfo));
	}

	private GmMetaModel wbModel(OriginAwareAccessRegistrationInfo regInfo) {
		String wbModelName = regInfo.getWorkbenchMetaModelName();
		if (wbModelName == null)
			return null;

		EntityQuery query = ModelEnvironmentQueryBuilder.buildModelEnvironmentQuery(wbModelName);
		return firstOrNull(getSession().queryDetached().entities(query).list());
	}

	private GmMetaModel getDataModel(String accessId) {
		ModelAccessory accessMa = sysMaFactory.getForAccess(accessId);
		GmMetaModel model = accessMa.getModel();

		if (isCurrentUserTrusted())
			return model;

		try {
			ModelAccessory modelAccessory = userMaFactory.getForAccess(accessId);
			if (!modelAccessory.getMetaData().is(Visible.T)) {
				log.info("Model: " + model.getName() + " is not visible for this request.");
				return truncateModel(model);
			}

		} catch (Exception e) {
			log.error("Could not truncate model.", e);
		}

		return model;
	}

	private GmMetaModel getServiceModel(String accessId) {
		try {
			ModelAccessory serviceMa = sysMaFactory.getForServiceDomain(accessId);
			return serviceMa.getModel();

		} catch (Exception e) {
			log.error("Service model could not be retrieved for access: " + accessId, e);
			return null;
		}
	}

	@Override
	public ModelEnvironment getModelEnvironmentForDomain(String accessId, AccessDomain accessDomain) throws AccessServiceException {
		return getModelEnvironment(requireAccessIdForDomain(accessId, accessDomain));
	}

	private List<WorkbenchPerspective> queryWorkbenchPerspectives(String wbAccessId, Set<String> wbPerspectiveNames) throws AccessServiceException {
		EntityQuery wbPerspectivesQuery = EntityQueryBuilder.from(WorkbenchPerspective.class) //
				.where().property("name").in(wbPerspectiveNames) //
				.done();
		wbPerspectivesQuery.setNoAbsenceInformation(true);
		wbPerspectivesQuery.setTraversingCriterion(createWorkbenchTraversingCriterion());

		EntityQueryResult wbPerspectivesResult = queryEntities(wbAccessId, wbPerspectivesQuery);
		List<WorkbenchPerspective> wbPerspectives = new ArrayList<>();
		if (wbPerspectivesResult != null)
			wbPerspectives.addAll((List<WorkbenchPerspective>) (List<?>) wbPerspectivesResult.getEntities());

		return wbPerspectives;
	}

	private static TraversingCriterion createWorkbenchTraversingCriterion() {
		return TC.create().pattern().entity(Resource.class).property(Resource.resourceSource).close().done();
	}

	public boolean isCurrentUserTrusted() {
		try {
			return CollectionTools.containsAny(trustedRoles, NullSafe.collection(userRolesProvider.get()));
		} catch (Exception e) {
			log.error("Could not get current user roles to evaluate trusted roles.", e);
			return false;
		}
	}

	private static GmMetaModel truncateModel(GmMetaModel metaModel) {
		GmMetaModel truncatedModel = GmMetaModel.T.create();
		truncatedModel.setName(metaModel.getName());
		truncatedModel.setTypes(newSet());
		truncatedModel.setMetaData(newSet());

		for (MetaData mmd : NullSafe.collection(metaModel.getMetaData()))
			if (mmd instanceof Visible)
				truncatedModel.getMetaData().add(mmd);

		return truncatedModel;
	}

	@Override
	public Set<String> getAccessIds() throws AccessServiceException {
		return this.accessRegistry.keySet();
	}

	@Override
	public IncrementalAccess lookupAccess(String id) {
		OriginAwareAccessRegistrationInfo registryInfo = getRegistryInfo(id);
		return registryInfo.getAccess();
	}

	@Override
	public String lookupAccessId(IncrementalAccess access) {
		OriginAwareAccessRegistrationInfo registryInfo = getRegistryInfo(access);
		return registryInfo.getAccessId();
	}

	private OriginAwareAccessRegistrationInfo getRegistryInfo(String id) {
		OriginAwareAccessRegistrationInfo registryInfo = accessRegistry.get(id);
		if (registryInfo == null)
			throw new IllegalArgumentException("No access registered for id: " + id);

		return registryInfo;
	}

	private OriginAwareAccessRegistrationInfo getRegistryInfo(IncrementalAccess access) {
		for (OriginAwareAccessRegistrationInfo registryInfo : accessRegistry.values())
			if (registryInfo.getAccess() == access)
				return registryInfo;

		throw new IllegalArgumentException("No access registered for access: " + access);
	}

	protected String getAccessIdForDomain(String accessId, AccessDomain accessDomain) {
		OriginAwareAccessRegistrationInfo registryInfo = getRegistryInfo(accessId);

		switch (accessDomain) {
			case workbench:
				return registryInfo.getWorkbenchAccessId();
			case meta:
				return registryInfo.getMetaModelAccessId();
			case data:
				return registryInfo.getAccessId();
			default:
				return registryInfo.getAccessId();
		}
	}

	protected String requireAccessIdForDomain(String accessId, AccessDomain accessDomain) throws AccessServiceException {
		String domainAccessId = getAccessIdForDomain(accessId, accessDomain);
		if (domainAccessId != null)
			return domainAccessId;

		throw new AccessServiceException("invalid domain [ " + accessDomain + " ]. there is no " + accessDomain
				+ " access id registered to data access id \"" + accessId + "\".");
	}

	@Override
	public Set<String> getPartitions(String accessId) throws AccessServiceException {
		try {
			return getAccessDelegate(accessId).getPartitions();

		} catch (ModelAccessException e) {
			throw new AccessServiceException(e);
		}
	}

}
