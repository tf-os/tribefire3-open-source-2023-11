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
package com.braintribe.model.processing.deployment.access;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.i18n.I18nTools;

/**
 * @deprecated should not be needed anymore, will be deleted soon if nothing pops out.
 */
@Deprecated
public class HardwiredAccessSynchronization {

	protected static Logger logger = Logger.getLogger(HardwiredAccessSynchronization.class);

	private Supplier<PersistenceGmSession> cortexSessionFactory;
	private DeployRegistry deployRegistry;

	@Required
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}

	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

	public void synchronize() throws Exception {

		logger.info("Start synchronization of hardwired accesses.");

		PersistenceGmSession session = cortexSessionFactory.get();

		Map<String, IncrementalAccess> cortexAccesses = cortexHardwiredAccesses(session);

		Set<String> hardwiredAccessesIds = new HashSet<>();

		// @formatter:off
		deployRegistry
			.getDeployables()
			.stream()
			.filter(d -> d instanceof HardwiredAccess)
			.forEach((d) -> synchronize(session, cortexAccesses, hardwiredAccessesIds, d)
		);
		// @formatter:on

		/* Finally deleting accesses from DB that are not hardwired (anymore). */
		for (IncrementalAccess access : cortexAccesses.values()) {
			if (!hardwiredAccessesIds.contains(access.getExternalId())) {
				logger.info("Removing stale hardwired access: " + access.getExternalId()
						+ " from cortex db. Not found as hardwired in the deploy registry.");
				session.deleteEntity(access);
			}
		}

		commitIfNecessary(session);

	}

	private void synchronize(PersistenceGmSession session, Map<String, IncrementalAccess> cortexAccesses, Set<String> hardwiredAccessesIds,
			Deployable deployable) {

		HardwiredAccess hardwiredAccess = (HardwiredAccess) deployable;
		String accessId = hardwiredAccess.getExternalId();
		hardwiredAccessesIds.add(accessId);
		IncrementalAccess access = cortexAccesses.get(accessId);

		if (access == null) {
			access = create(hardwiredAccess, session);
			if (logger.isDebugEnabled()) {
				logger.debug("Created hardwired access: " + access);
			}
			cortexAccesses.put(accessId, access);
		} else {
			update(hardwiredAccess, access, session);
			if (logger.isDebugEnabled()) {
				logger.debug("Updated hardwired access: " + access);
			}
		}

	}

	private IncrementalAccess create(HardwiredAccess hardwiredAccess, PersistenceGmSession session) {

		GmMetaModel model = findModel(hardwiredAccess, session);

		EntityType<IncrementalAccess> accessEntityType = hardwiredAccess.entityType();
		IncrementalAccess cortexAccess = session.create(accessEntityType);
		cortexAccess.setMetaModel(model);
		update(hardwiredAccess, cortexAccess, session);

		return cortexAccess;

	}

	private void update(HardwiredAccess hardwiredAccess, IncrementalAccess cortexAccess, PersistenceGmSession session) {

		updatePropertyIfNecessary(cortexAccess, "deployed", true);
		updatePropertyIfNecessary(cortexAccess, "hardwired", true);
		updatePropertyIfNecessary(cortexAccess, "externalId", hardwiredAccess.getExternalId());
		updatePropertyIfNecessary(cortexAccess, "name", hardwiredAccess.getName());
		
		GmMetaModel currentModel = cortexAccess.getMetaModel();
		GmMetaModel newModel = findModel(hardwiredAccess, session);

		if (!(currentModel.getName().equals(newModel.getName()))) {
			cortexAccess.setMetaModel(newModel);
		}
	}

	/**
	 * <p>
	 * Returns the hardwired accesses currently persisted to the cortex.
	 */
	protected Map<String, IncrementalAccess> cortexHardwiredAccesses(PersistenceGmSession session) throws GmSessionException {

		// @formatter:off
		EntityQuery query = 
				EntityQueryBuilder
					.from(IncrementalAccess.class)
					.where()
						.property("hardwired").eq(true)
					.done();
		// @formatter:on

		List<IncrementalAccess> cortexHardwiredAccesses = session.query().entities(query).list();
		Map<String, IncrementalAccess> cortexAccesses = new HashMap<>(cortexHardwiredAccesses.size());

		for (IncrementalAccess cortexHardwiredAccess : cortexHardwiredAccesses) {
			cortexAccesses.put(cortexHardwiredAccess.getExternalId(), cortexHardwiredAccess);
		}

		return cortexAccesses;

	}

	private void commitIfNecessary(PersistenceGmSession session) throws GmSessionException {
		if (session.getTransaction().hasManipulations()) {
			session.commit();
			logger.info("Finished synchronization of hardwired accesses.");
		} else {
			logger.info("Finished synchronization of hardwired accesses. No manipulations to sync.");
		}
	}

	private GmMetaModel findModel(HardwiredAccess hardwiredAccess, PersistenceGmSession session) {

		Objects.requireNonNull(hardwiredAccess, "hardwiredAccess");
		Objects.requireNonNull(hardwiredAccess.getMetaModel(), "metaModel");

		String metaModelName = hardwiredAccess.getMetaModel().getName();

		Objects.requireNonNull(metaModelName, "metaModel name");

		// @formatter:off
		EntityQuery modelQuery = 
				EntityQueryBuilder
					.from(GmMetaModel.class)
					.where()
						.property("name").eq(metaModelName)
					.done();
		// @formatter:on

		GmMetaModel model = null;
		try {
			model = session.query().entities(modelQuery).unique();
		} catch (GmSessionException e) {
			throw new GmSessionRuntimeException(e.getMessage(), e);
		}

		if (model == null) {
			throw new GmSessionRuntimeException("Meta model not found: " + metaModelName);
		}

		return model;

	}

	private void updatePropertyIfNecessary(GenericEntity entity, String propertyName, Object newValue) {

		EntityType<GenericEntity> type = entity.entityType();
		Property property = type.getProperty(propertyName);
		Object currentValue = property.get(entity);

		if (currentValue == null) {
			if (newValue != null) {
				property.set(entity, newValue);
			}
		} else if (currentValue instanceof LocalizedString) {
			LocalizedString currentLs = (LocalizedString) currentValue;
			LocalizedString newLs = (LocalizedString) newValue;

			String lsDefault = I18nTools.getDefault(currentLs, "");
			String newLsDefault = I18nTools.getDefault(newLs, "");

			if (!lsDefault.equals(newLsDefault)) {
				currentLs.getLocalizedValues().put("default", newLsDefault);
			}

		} else if (!currentValue.equals(newValue)) {
			property.set(entity, newValue);
		}

	}

//	private LocalizedString acquireLocalizedString(LocalizedString originalLs, LocalizedString newLs, PersistenceGmSession session) {
//
//		Objects.requireNonNull(newLs, "The hardwired LocalizedString must not be null");
//
//		if (originalLs == null) {
//			return mergeLocalizedString(newLs, session);
//		}
//
//		String originalLsDefault = I18nTools.getDefault(originalLs, "");
//		String newLsDefault = I18nTools.getDefault(newLs, "");
//
//		if (!originalLsDefault.equals(newLsDefault)) {
//			originalLs.getLocalizedValues().put("default", newLsDefault);
//		}
//
//		return originalLs;
//
//	}
//
//	private LocalizedString mergeLocalizedString(LocalizedString unmanagedls, PersistenceGmSession session) {
//		LocalizedString ls = session.create(LocalizedString.T);
//		for (Entry<String, String> entry : unmanagedls.getLocalizedValues().entrySet()) {
//			ls.getLocalizedValues().put(entry.getKey(), entry.getValue());
//		}
//		return ls;
//	}

}
