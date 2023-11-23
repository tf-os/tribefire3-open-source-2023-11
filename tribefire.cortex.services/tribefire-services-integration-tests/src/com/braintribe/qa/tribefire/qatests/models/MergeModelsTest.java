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
package com.braintribe.qa.tribefire.qatests.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.cortexapi.model.AddDependencies;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;

public class MergeModelsTest {

	private final static String PREFIX = "tfqa" + MergeModelsTest.class.getSimpleName();
	private final static String GROUP_ID = MergeModelsTest.class.getPackage().getName();
	private final static String FIRST_MODEL_NAME = GROUP_ID + ":" + PREFIX + "FirstModel";
	private final static String SECOND_MODEL_NAME = GROUP_ID + ":" + PREFIX + "SecondModel";

	private final static String DUMMY_MODEL_BASENAME = GROUP_ID + ":" + PREFIX + "Dummy";

	private final static String ROOT_MODEL_NAME = "com.braintribe.gm:root-model";

	private final static int NUM_DUMMY_MODELS = 10;

	private final static ImpApiFactory cave = ImpApiFactory.with();// .url("https://192.168.6.224:8443/tribefire-services");

	@Before
	public void init() {
		ImpApi imp = cave.build();

		GmMetaModel firstModel = imp.model().create(FIRST_MODEL_NAME).get();
		GmMetaModel secondModel = imp.model().create(SECOND_MODEL_NAME).get();

		// @formatter:off
		imp.model().entityType()
					.create(GROUP_ID, PREFIX + "TestType1", firstModel)
						.addProperty(PREFIX + "testProperty1", SimpleType.TYPE_INTEGER)
						.addProperty(PREFIX + "testProperty11", SimpleType.TYPE_BOOLEAN);

		imp.model().enumType()
					.create(GROUP_ID, PREFIX + "TestType2", secondModel)
						.addConstants(PREFIX + "c1", PREFIX + "c2");
		// @formatter:on

		imp.commit();
	}

	@After
	public void tearDown() {
		ImpApi imp = cave.build();

		erase(imp, FIRST_MODEL_NAME);
		erase(imp, SECOND_MODEL_NAME);

		for (int i = 0; i < NUM_DUMMY_MODELS; i++) {
			erase(imp, DUMMY_MODEL_BASENAME + i);

		}

		imp.commit();
	}

	public void erase(ImpApi imp, String modelName) {
		imp.model().deleteRecursivelyIfPresent(modelName);
		imp.commit();
	}

	@Test
	public void testBasicMerge() {
		GmMetaModel mergedModelBeforeCallingService = merge(FIRST_MODEL_NAME, SECOND_MODEL_NAME);

		expecting(FIRST_MODEL_NAME, mergedModelBeforeCallingService, ROOT_MODEL_NAME, SECOND_MODEL_NAME);

		// this is to test the test methods of this class and that mergedModel really represents the old version before
		// calling the service request
		assertThatNumDependenciesIncreased(mergedModelBeforeCallingService);
	}

	@Test
	public void testMergeWhatIsAlreadyPresent() {
		GmMetaModel mergedModelBeforeCallingService = merge(FIRST_MODEL_NAME, ROOT_MODEL_NAME);

		expecting(FIRST_MODEL_NAME, mergedModelBeforeCallingService, ROOT_MODEL_NAME);
	}

	@Test
	public void testCyclicMerge() {
		GmMetaModel mergedModelBeforeCallingService1 = merge(FIRST_MODEL_NAME, SECOND_MODEL_NAME);
		GmMetaModel mergedModelBeforeCallingService2 = merge(SECOND_MODEL_NAME, FIRST_MODEL_NAME);

		expecting(FIRST_MODEL_NAME, mergedModelBeforeCallingService1, ROOT_MODEL_NAME, SECOND_MODEL_NAME);
		expecting(SECOND_MODEL_NAME, mergedModelBeforeCallingService2, ROOT_MODEL_NAME, FIRST_MODEL_NAME);
	}

	@Test
	public void testSelfMerge() {
		GmMetaModel mergedModelBeforeCallingService = merge(FIRST_MODEL_NAME, FIRST_MODEL_NAME);
		expecting(FIRST_MODEL_NAME, mergedModelBeforeCallingService, FIRST_MODEL_NAME, ROOT_MODEL_NAME);

		GmMetaModel mergedModelBeforeCallingService2 = merge(FIRST_MODEL_NAME, FIRST_MODEL_NAME, FIRST_MODEL_NAME, FIRST_MODEL_NAME);
		expecting(FIRST_MODEL_NAME, mergedModelBeforeCallingService2, FIRST_MODEL_NAME, ROOT_MODEL_NAME);
	}

	@Test
	public void testMergeMany() {
		ImpApi imp = cave.build();

		String[] names = new String[NUM_DUMMY_MODELS + 1];
		names[0] = ROOT_MODEL_NAME;

		for (int i = 0; i < NUM_DUMMY_MODELS; i++) {

			// @formatter:off
			GmMetaModel dummyModel = imp.model()
				.create(DUMMY_MODEL_BASENAME + i).get();

			imp.model().entityType()
					.create(GROUP_ID, PREFIX + "DummyEntity" + i, dummyModel)
					.addProperty("dummyProp" + i, SimpleType.TYPE_BOOLEAN);
			// @formatter:on

			names[i + 1] = DUMMY_MODEL_BASENAME + i;
		}

		imp.commit();

		GmMetaModel m = merge(FIRST_MODEL_NAME, names);

		expecting(FIRST_MODEL_NAME, m, names);

	}

	private void expecting(String modelToExamineName, GmMetaModel oldModel, String... actualDependencyModelNames) {
		ImpApi assertionImp = cave.build();

		GmMetaModel modelToExamine = assertionImp.model(modelToExamineName).get();
		Collection<GmMetaModel> foundDependencies = assertionImp.model(modelToExamineName).dependencies().get();
		Collection<GmMetaModel> expectedDependencies = assertionImp.model().with(actualDependencyModelNames).get();

		assertThat(foundDependencies).as("Dependencies are different than expected").containsAll(expectedDependencies)
				.containsOnlyElementsOf(expectedDependencies);

		// TODO use PropertyByProperty comparator as soon as finished
		assertThat(modelToExamine.getTypes()).as("Model has unexpected Types").hasSameSizeAs(oldModel.getTypes());
		assertThat(modelToExamine.getVersion()).as("Model has unexpected Version").isEqualTo(oldModel.getVersion());
	}

	private void assertThatNumDependenciesIncreased(GmMetaModel oldModel) {
		ImpApi assertionImp = cave.build();

		GmMetaModel oldModelInNewSession = assertionImp.model(oldModel.getName()).get();

		assertThat(oldModel.getDependencies()).size().as("There are no new dependencies. The test methods or tools themselves are corrupt")
				.isLessThan(oldModelInNewSession.getDependencies().size());
	}

	private GmMetaModel merge(String targetModelName, String... metaModelNames) {
		ImpApi imp = cave.build();

		GmMetaModel targetModel = imp.model(targetModelName).get();
		targetModel.getDependencies(); // remove potential absence information to be able to compare collection sizes
										// later

		Collection<GmMetaModel> modelsToMerge = imp.model().with(metaModelNames).get();

		AddDependencies request = imp.service().mergeModelsRequest(targetModel, modelsToMerge).get();
		imp.session().eval(request).get();

		return targetModel;
	}
}
