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
package com.braintribe.model.processing.deployment.processor;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.impl.aop.AopAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.aspect.StateProcessingAspect;
import com.braintribe.model.processing.sp.commons.ConfigurableStateChangeProcessorRuleSet;
import com.braintribe.model.processing.test.impl.session.TestModelAccessory;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver.SessionRunnable;
import com.braintribe.model.query.EntityQuery;

/**
 * 
 */
public abstract class AbstractScpTests {

	private AopAccess aopAccess;
	private GmMetaModel metaModel;
	protected ManipulationDriver manipulationDriver;
	protected ManagedGmSession localSession;

	@Before
	public void setup() throws Exception {
		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(metaModel());

		SmoodAccess access = new SmoodAccess();
		access.setAccessId("TestSmoodAccess");
		access.setDataDelegate(new TransientAccess(metaModel()));
		access.setDefaultTraversingCriteria(defaultTraversingCriteria());
		access.setReadWriteLock(EmptyReadWriteLock.INSTANCE);

		manipulationDriver = new ManipulationDriver(access);

		localSession = new BasicPersistenceGmSession(access);

		aopAccess = new AopAccess();
		aopAccess.setAspects(Arrays.asList(aspect()));
		aopAccess.setDelegate(manipulationDriver.getAccess());
		aopAccess.setUserSessionFactory(new TestGmSessionFactory());
		aopAccess.setSystemSessionFactory(new ReentrantTestGmSessionFactory());
	}

	private class TestGmSessionFactory implements PersistenceGmSessionFactory {
		@Override
		public PersistenceGmSession newSession(String accessId) throws GmSessionException {
			GmMetaModel metaModel = metaModel();
			BasicModelOracle modelOracle = new BasicModelOracle(metaModel);
			CmdResolver cmdResolver = new CmdResolverImpl(modelOracle);

			BasicPersistenceGmSession session = new BasicPersistenceGmSession();
			session.setIncrementalAccess(getIncrementalAccess());
			session.setModelAccessory(new TestModelAccessory(cmdResolver));
			return session;
		}

		protected IncrementalAccess getIncrementalAccess() {
			return manipulationDriver.getAccess();
		}
	}

	/**
	 * Edited on 22.4.2015 - currently the SystemSession as configured in real-life cases is actually reentrant (meaning
	 * manipulations committed by some aspect go through the whole {@link AopAccess}, including that very same aspect),
	 * which was the cause for BTT-5295, thus we also make sure the tests copy the same configuration.
	 */
	private class ReentrantTestGmSessionFactory extends TestGmSessionFactory {
		@Override
		protected IncrementalAccess getIncrementalAccess() {
			return aopAccess;
		}
	}

	private Map<Class<? extends GenericEntity>, TraversingCriterion> defaultTraversingCriteria() {
		// @formatter:off
		TraversingCriterion defaultTc = TC.create()
					.conjunction()
						.property()
						.typeCondition(
							or(
								isKind(TypeKind.entityType),
								isKind(TypeKind.collectionType)
							)
						)
					.close()
				.done();
		// @formatter:on

		return asMap(GenericEntity.class, defaultTc);
	}

	protected AccessAspect aspect() {
		ConfigurableStateChangeProcessorRuleSet ruleSet = new ConfigurableStateChangeProcessorRuleSet();
		ruleSet.setProcessorRules(Arrays.asList(stateChangeProcessorRule()));

		StateProcessingAspect spa = new StateProcessingAspect();
		spa.setProcessorRuleSet(ruleSet);

		return spa;
	}

	protected abstract StateChangeProcessorRule stateChangeProcessorRule();

	private GmMetaModel metaModel() {
		if (metaModel == null) {
			metaModel = newMetaModel();

			if (metaModel == null) {
				throw new RuntimeException("Error in test! Method 'newMetaModel' returned null.");
			}
		}
		return metaModel;
	}

	protected abstract GmMetaModel newMetaModel();

	// ###################################
	// ## . . manipulation utilities . .##
	// ###################################

	/**
	 * Runs given {@link SessionRunnable} with a new session and commits the changes. The session works directly with
	 * the underlying smood, aspects are not used.
	 */
	protected void prepare(SessionRunnable runnable) {
		manipulationDriver.run(runnable);
	}

	/**
	 * Runs given {@link SessionRunnable} with a new session, but the recorded manipulations are not committed via the
	 * session directly, but wrapped inside a new {@link ManipulationRequest}, which is then sent as a parameter for
	 * {@link AopAccess#applyManipulation(ManipulationRequest)}.
	 */
	protected void apply(SessionRunnable runnable) throws ModelAccessException {
		ManipulationRequest request = manipulationDriver.dryRunAsRequest(runnable);
		apply(request);
	}

	protected ManipulationResponse apply(ManipulationRequest request) {
		return aopAccess.applyManipulation(request);
	}

	// ###################################
	// ## . . . . . queries . . . . . . ##
	// ###################################

	protected <T extends GenericEntity> T queryFirst(Class<T> clazz) {
		return queryAll(clazz).iterator().next();
	}

	protected <T extends GenericEntity> T queryFirst(Class<T> clazz, PersistenceGmSession session) {
		try {
			EntityQuery eq = EntityQueryBuilder.from(clazz).done();
			return clazz.cast(session.query().entities(eq).list().iterator().next());

		} catch (Exception e) {
			throw new RuntimeException("Query failed!", e);
		}
	}

	protected <T extends GenericEntity> T queryByName(Class<T> clazz, String name, PersistenceGmSession session) {
		try {
			EntityQuery eq = EntityQueryBuilder.from(clazz).where().property("name").eq(name).done();
			return clazz.cast(session.query().entities(eq).list().iterator().next());

		} catch (Exception e) {
			throw new RuntimeException("Query for '" + clazz.getSimpleName() + " with name '" + name + "' failed!", e);
		}
	}

	protected <T extends GenericEntity> List<T> queryAll(Class<T> clazz) {
		return entityQuery(EntityQueryBuilder.from(clazz).done());
	}

	protected <T extends GenericEntity> T byName(Class<T> clazz, String name) {
		EntityQuery eq = EntityQueryBuilder.from(clazz).where().property("name").eq(name).done();
		return clazz.cast(entityQuery(eq).iterator().next());
	}

	@SuppressWarnings("unchecked")
	private <T extends GenericEntity> List<T> entityQuery(EntityQuery eq) {
		try {
			List<T> result = (List<T>) manipulationDriver.getAccess().queryEntities(eq).getEntities();
			return localSession.merge().doFor(result);

		} catch (Exception e) {
			throw new RuntimeException("Query failed!", e);
		}
	}

}
