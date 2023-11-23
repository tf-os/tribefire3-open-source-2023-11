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
package com.braintribe.model.processing.smart.query.planner.base;

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.builder.vd.VdBuilder;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.smart.test.check.SmartEntityAssemblyChecker;
import com.braintribe.model.processing.query.smart.test.debug.SmartTupleSetViewer;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.Id2UniqueEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.setup.BasicSmartSetupProvider;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartSetupProvider;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlanner;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.StaticModelExpert;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;

public abstract class AbstractSmartQueryPlannerTests {

	private final SmartQueryPlanner queryPlanner = newSmartQueryPlanner();
	private ModelExpert modelExpert;
	private static Map<SmartSetupProvider, ModelExpert> modelExpertForSetupProvider = newMap();

	protected int i;

	private SmartQueryPlanner newSmartQueryPlanner() {
		SmartQueryPlanner queryPlanner = new SmartQueryPlanner();
		queryPlanner.setFunctionExperts(FunctionExperts.DEFAULT_FUNCTION_EXPERTS);
		queryPlanner.setConversionExperts(ConversionExperts.DEFAULT_CONVERSION_EXPERTS);
		
		return queryPlanner;
	}
	
	@Before
	public void prepareModelExpert() {
		SmartSetupProvider setupProvider = getSmartSetupProvider();

		modelExpert = modelExpertForSetupProvider.computeIfAbsent(setupProvider, sp -> modelExpert(sp.setup()));
	}

	private ModelExpert modelExpert(SmartMappingSetup setup) {
		GmMetaModel modelS = setup.modelS;
		BasicModelOracle modelOracleS = new BasicModelOracle(modelS);
		CmdResolver cmdResolver = new CmdResolverImpl(modelOracleS);
		StaticModelExpert sme = new StaticModelExpert(modelOracleS);

		return new ModelExpert(cmdResolver, sme, setup.accessS, accessMapping(setup));
	}

	private static Map<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping(SmartMappingSetup setup) {
		TestAccess accessA = new TestAccess(accessIdA);
		TestAccess accessB = new TestAccess(accessIdB);
		TestAccess accessS = new TestAccess(accessIdA, accessIdB);

		return asMap(setup.accessA, accessA, setup.accessB, accessB, setup.accessS, accessS);
	}

	protected SmartSetupProvider getSmartSetupProvider() {
		return BasicSmartSetupProvider.INSTANCE;
	}

	protected SmartQueryPlan queryPlan;

	protected SelectQueryBuilder query() {
		return new SelectQueryBuilder();
	}

	protected void runTest(SelectQuery selectQuery) {
		queryPlan = queryPlanner.buildQueryPlan(selectQuery, modelExpert);

		SmartTupleSetViewer.view(getTestName(), selectQuery, queryPlan);
	}

	protected void assertEmptyQueryPlan() {
		assertQueryPlan(0) //
				.hasType(StaticSet.T) //
				.whereProperty("values").isSetWithSize(0);
	}

	protected SmartEntityAssemblyChecker assertQueryPlan(int totalComponentCount) {
		return new SmartEntityAssemblyChecker(queryPlan).whereProperty("totalComponentCount").is_(totalComponentCount).whereProperty("tupleSet");
	}

	protected SmartEntityAssemblyChecker assertQueryPlan() {
		return new SmartEntityAssemblyChecker(queryPlan.getTupleSet());
	}

	@Rule
	public TestName testName = new TestName();

	private String getTestName() {
		return testName.getMethodName();
	}

	protected static PersistentEntityReference reference(Class<? extends StandardIdentifiable> clazz, String partition, long id) {
		return VdBuilder.persistentReference(clazz.getName(), id, partition);
	}

	protected Company company(Long id) {
		Company result = Company.T.createPlain();
		result.setId(id);
		result.setPartition(accessIdA);

		return result;
	}

	protected Id2UniqueEntity id2UniqueEntity(String id) {
		Id2UniqueEntity result = Id2UniqueEntity.T.createPlain();
		result.setId(id);
		result.setPartition(accessIdA);

		return result;
	}

	protected SmartPersonA person(Long id) {
		SmartPersonA result = SmartPersonA.T.createPlain();
		result.setId(id);
		result.setPartition(accessIdA);

		return result;
	}

	protected SmartItem item(Long id) {
		SmartItem result = SmartItem.T.createPlain();
		result.setId(id);
		result.setPartition(accessIdB);

		return result;
	}

}
