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
package com.braintribe.model.access.hibernate.base;

import static com.braintribe.model.access.hibernate.base.tools.HibernateAccessSetupHelper.dataSource_H2;
import static com.braintribe.model.access.hibernate.base.tools.HibernateAccessSetupHelper.hibernateSessionFactoryBean;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.Transaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;

import com.braintribe.model.access.hibernate.HibernateAccess;
import com.braintribe.model.access.hibernate.base.mock.HbmTestSessionFactory;
import com.braintribe.model.access.hibernate.base.mock.NonCommittingSessionFactory;
import com.braintribe.model.access.hibernate.base.model.HibernateAccessEntity;
import com.braintribe.model.access.hibernate.base.tools.HibernateAccessSetupHelper;
import com.braintribe.model.access.hibernate.base.wire.contract.HibernateModelsContract;
import com.braintribe.model.access.hibernate.tests.CompositeId_HbmTest;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.persistence.ExecuteNativeQuery;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.test.tools.QueryResultAssert;
import com.braintribe.model.processing.query.tools.AccessDriver;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.service.commons.StandardServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;
import com.braintribe.persistence.hibernate.HibernateSessionFactoryBean;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.Wire;

/**
 * Base for most hibernate tests.
 * <p>
 * This base class takes care of preparing a {@link HibernateAccess} instance. For performance reasons we minimize the number of these instances by
 * re-using a single instance for all tests which use the same {@link GmMetaModel}, provided via {@link #model()} method.
 * <p>
 * In order to start each test with an empty DB, we ensure that every test runs entirely as one transaction, and any data it has written will be
 * rolled back before the next test begins (see {@link #prepareAccess()}). To achieve this we mock the {@link SessionFactory} / {@link Session} /
 * {@link Transaction} used inside {@link HibernateAccess} to prevent the transaction commit of having an effect (see
 * {@link NonCommittingSessionFactory}). This still works, because queries done within a transaction reflect the yet-to-be-written data of such
 * transaction, we just have to make sure that any changes done by applied manipulations are flushed. Therefore, our session's mock implements the
 * commit by simply doing a flush.
 * <p>
 * LIMITATIONS: Turns out we cannot test everything with this approach. One example is in the {@link CompositeId_HbmTest#propertyQuery_LazyLoading()
 * composite id} use case.
 * 
 * @author peter.gazdik
 */
public abstract class HibernateAccessRecyclingTestBase {

	/**
	 * Static access point to various configured models. See {@link HibernateModelsContract}.
	 */
	public static final HibernateModelsContract hibernateModels = Wire.context(HibernateModelsContract.class)
			.bindContracts(HibernateModelsContract.class).build().contract();

	/**
	 * This is the cache of deployed hibernate access. For each model instance we have exactly one deployed access. The models com from
	 * {@link #hibernateModels}
	 */
	private final Map<GmMetaModel, HbmDeployedUnit> hbmUnits = newMap();

	private HbmDeployedUnit hbmUnit;

	protected HibernateAccess access;
	protected PersistenceGmSession session;
	protected AccessDriver accessDriver;

	/** If true, HQL queries performed by hibernate are printed in the console */
	public static final boolean SHOW_SQL = false;
	public static final boolean FORMAT_SQL = false;

	/**
	 * If true, manipulations applied on hibernate access are actually committed to the underlying database. For regular testing this should be
	 * {@code false}, but when debugging a single test we might want to make sure that the changes are persisted even after the test has finished.
	 * This would only make sense if we didn't use an in-memory-database though, so once this is needed a different deployment needs to be used (see
	 * {@link #dataSource(String)}).
	 */
	public static final boolean COMMIT_HIB_SESSION = false;

	/** Before each test we prepare the {@link HibernateAccess}, {@link PersistenceGmSession} session and an {@link AccessDriver}. */
	@Before
	public void prepareAccess() {
		GmMetaModel model = model();

		hbmUnit = hbmUnits.computeIfAbsent(model, this::deployHbm);

		loadForCurrentTest(hbmUnit);
	}

	private void loadForCurrentTest(HbmDeployedUnit _hbmUnit) {
		hbmUnit = _hbmUnit;
		access = hbmUnit.access;
		resetGmSession();
	}

	protected void resetGmSession() {
		session = new BasicPersistenceGmSession(access);
		accessDriver = new AccessDriver(access, session);
	}

	/** After each test we cleanup the data by doing a transaction rollback. */
	@After
	public void rollbackTransaction() {
		// could be null in case there's an error in prepareAccess
		if (hbmUnit != null)
			hbmUnit.reset();
	}

	protected abstract GmMetaModel model();

	private HbmDeployedUnit deployHbm(GmMetaModel model) {
		return deployHbm(model, "default_" + dbName(model));
	}

	// ################################################
	// ## . . . . . . . . Deployment . . . . . . . . ##
	// ################################################

	protected static HbmDeployedUnit deployHbm(GmMetaModel model, String dbName) {
		HbmDeployedUnit hbmUnit = new HbmDeployedUnit();
		hbmUnit.sessionFactory = sessionFactory(model, dbName);
		hbmUnit.access = HibernateAccessSetupHelper.hibernateAccess("test.access.for." + dbName, () -> model, hbmUnit.sessionFactory);

		return hbmUnit;
	}

	private static HbmTestSessionFactory sessionFactory(GmMetaModel model, String dbName) {
		HibernateSessionFactoryBean hsfb = hibernateSessionFactoryBean( //
				() -> model, //
				dataSource(dbName));

		hsfb.setShowSql(SHOW_SQL);
		hsfb.setFormatSql(FORMAT_SQL);

		return HbmTestSessionFactory.newInstance(hsfb.getObject(), false);
	}

	private static DataSource dataSource(String dbName) {
		return dataSource_H2(dbName);
	}

	private static String dbName(GmMetaModel model) {
		return StringTools.findSuffix(model.getName(), ":");
	}

	private static class HbmDeployedUnit {
		public HibernateAccess access;
		public HbmTestSessionFactory sessionFactory;

		void reset() {
			sessionFactory.reset();
		}
	}

	// #################################################
	// ## . . . . . . . . Querying . . . . . . . . . .##
	// #################################################

	protected SelectQueryBuilder from(EntityType<?> entityType, String alias) {
		return new SelectQueryBuilder().from(entityType, alias);
	}

	protected QueryResultAssert qra;

	protected void runSelectQuery(SelectQuery query) {
		SelectQueryResult result = session.query().select(query).result();
		qra = new QueryResultAssert(result);
	}

	// #################################################
	// ## . . . . . . . Native Querying . . . . . . . ##
	// #################################################

	protected ServiceRequestContext serviceRequestContext;

	protected void executeNativeQuery(ExecuteNativeQuery queryRequest) {
		List<?> result = (List<?>) access.processCustomRequest(serviceRequestContext(), queryRequest);
		qra = new QueryResultAssert(result);
	}

	private ServiceRequestContext serviceRequestContext() {
		if (serviceRequestContext == null)
			serviceRequestContext = internalUserServiceRequestContext();
		return serviceRequestContext;
	}

	protected ServiceRequestContext internalUserServiceRequestContext() {
		UserSession session = UserSession.T.create();
		session.setType(UserSessionType.internal);

		StandardServiceRequestContext src = new StandardServiceRequestContext((Evaluator<ServiceRequest>) null);
		src.setAttribute(UserSessionAspect.class, session);

		return src;
	}

	// #################################################
	// ## . . . . . . . . . Misc . . . . . . . . . . .##
	// #################################################

	protected <T extends HibernateAccessEntity> T create(EntityType<T> et, String name) {
		T result = session.create(et);
		result.setName(name);

		return result;
	}

}
