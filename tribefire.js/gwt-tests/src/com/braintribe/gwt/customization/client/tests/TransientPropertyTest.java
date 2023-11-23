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
package com.braintribe.gwt.customization.client.tests;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.sql.Timestamp;
import java.util.List;

import com.braintribe.gwt.customization.client.tests.model.transientProperties.TransientPropertyEntity;
import com.braintribe.gwt.customization.client.tests.model.transientProperties.TransientPropertySubEntity;
import com.braintribe.gwt.customization.client.tests.model.transientProperties.TransientPropertySuper1;
import com.braintribe.gwt.customization.client.tests.model.transientProperties.TransientPropertySuper2;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.TransientProperty;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public class TransientPropertyTest extends AbstractGwtTest {

	private EntityType<?> DYNAMIC_ET;

	@Override
	protected void tryRun() throws GmfException {
		deployDynamicModel();

		log("COMPILE-TIME");
		test_CompileTime_ExpressiveAccess();
		test_CompileTime_Reflected();
		test_CompileTime_ReflectionAccess();

		log("RUNTIME");
		test_Runtime_ExpressiveAccess();
		test_Runtime_Reflected();
		test_Runtime_ReflectedAccess();
	}

	private void test_CompileTime_ExpressiveAccess() {
		log("testing: expressive access");

		TransientPropertyEntity entity = TransientPropertyEntity.T.create();

		entity.setName("compile-time value");
		checkEqual("compile-time value", entity.getName());

		entity.setSelf(entity);
		checkEqual(entity, entity.getSelf());

		entity.setSuper1("compile-time super 1");
		checkEqual("compile-time super 1", entity.getSuper1());

		entity.setSuper2("compile-time super 2");
		checkEqual("compile-time super 2", entity.getSuper2());
	}

	private void test_CompileTime_Reflected() {
		checkNumberOfPropes(GenericEntity.T, 0); // just to see this doesn't return null 

		EntityType<?> et = TransientPropertyEntity.T;

		log("testing: reflected properties of " + et.getShortName());

		checkNumberOfPropes(et, TransientPropertyEntity.NUMBER_OF_PROPS);

		assertTp(et, "name", String.class);
		assertTp(et, "object", Object.class);
		assertTp(et, "timestamp", Timestamp.class);
		assertTp(et, "self", et.getJavaType());

		EntityType<?> subEt = TransientPropertySubEntity.T;

		log("testing: reflected properties of " + et.getShortName());

		checkNumberOfPropes(subEt, TransientPropertySubEntity.NUMBER_OF_PROPS);

		assertTp(subEt, "name", String.class, TransientPropertyEntity.T);
		assertTp(subEt, "subName", String.class, TransientPropertySubEntity.T);
	}

	private void test_CompileTime_ReflectionAccess() {
		log("testing: reflection access");

		EntityType<TransientPropertyEntity> et = TransientPropertyEntity.T;
		TransientProperty tName = et.getTransientProperty("name");

		TransientPropertyEntity e = et.create();
		tName.set(e, "reflection value");
		checkEqual("reflection value", e.getName());

		e.setName("expressive value");
		checkEqual("expressive value", e.getName());
	}

	// #################################################
	// ## . . . . . . . . . Runtime . . . . . . . . . ##
	// #################################################

	private void test_Runtime_ExpressiveAccess() {
		log("testing: expressive access");

		TransientPropertyEntity e = (TransientPropertyEntity) DYNAMIC_ET.create();

		e.setSuper1("dynamic super 1");
		checkEqual("dynamic super 1", e.getSuper1());

		e.setSuper2("dynamic super 2");
		checkEqual("dynamic super 2", e.getSuper2());
	}

	private void test_Runtime_Reflected() {
		log("testing: reflected properties of " + DYNAMIC_ET.getShortName());

		checkNumberOfPropes(DYNAMIC_ET, TransientPropertyEntity.NUMBER_OF_PROPS);

		assertTp(DYNAMIC_ET, "super1", String.class, TransientPropertySuper1.T);
		assertTp(DYNAMIC_ET, "super2", String.class, TransientPropertySuper2.T);
		assertTp(DYNAMIC_ET, "name", String.class, TransientPropertyEntity.T);
		assertTp(DYNAMIC_ET, "object", Object.class, TransientPropertyEntity.T);
		assertTp(DYNAMIC_ET, "timestamp", Timestamp.class, TransientPropertyEntity.T);
		assertTp(DYNAMIC_ET, "self", TransientPropertyEntity.class, TransientPropertyEntity.T);
	}

	private void test_Runtime_ReflectedAccess() {
		log("testing: reflection access");

		TransientProperty tName = DYNAMIC_ET.getTransientProperty("name");

		TransientPropertyEntity e = (TransientPropertyEntity) DYNAMIC_ET.create();
		tName.set(e, "reflection value");
		checkEqual("reflection value", e.getName());

		e.setName("expressive value");
		checkEqual("expressive value", e.getName());
	}

	// #################################################
	// ## . . . . . . . . . Asserts . . . . . . . . . ##
	// #################################################

	private void checkEqual(Object expected, Object actual) {
		if (!expected.equals(actual))
			logError("Wrong transient property value: Expected: " + expected + ", actual: " + actual);
		else
			log("    transient value: '" + actual + "' [OK]");
	}

	private void checkNumberOfPropes(EntityType<?> et, int expected) {
		List<TransientProperty> tps = et.getTransientProperties();
		if (tps == null) {
			logError("Method 'getTransientProperties' returns null for: " + et.getTypeSignature());
			return;
		}

		int actual = tps.size();
		if (actual != expected)
			logError("Wrong number of transient properties for: " + et.getTypeSignature() + " Expected: " + expected + ", actual: " + actual);
		else
			log("    Number of reflected properties for: '" + et.getTypeSignature() + "' [OK]");
	}

	private void assertTp(EntityType<?> et, String name, Class<?> type) {
		assertTp(et, name, type, et);
	}

	private void assertTp(EntityType<?> et, String name, Class<?> type, EntityType<?> declaringType) {
		TransientProperty tp = et.getTransientProperty(name);
		if (tp == null) {
			logError("Transient property should not be null: " + et.getShortName() + "." + name);
			return;
		}

		isTpAspectOk("name", tp.getName(), name);
		isTpAspectOk("type", tp.getType(), type);
		isTpAspectOk("declaring type", tp.getDeclaringType(), declaringType);
	}

	private void isTpAspectOk(String aspect, Object actual, Object expected) {
		if (!expected.equals(actual))
			logError("    Wrong " + aspect + ". Expected '" + expected + "', actual: '" + actual);
		else
			log("    " + aspect + ": '" + actual + "' [OK]");
	}

	// #################################################
	// ## . . . . . . . Dynamic Model . . . . . . . . ##
	// #################################################

	private void deployDynamicModel() throws GmfException {
		GmMetaModel metaModel = generateModel();

		makeSignaturesDynamic(metaModel);
		ensureModelTypes(metaModel);

		loadDynamicEt();
	}

	private void loadDynamicEt() {
		String dynamicSignature = makeSignatureDynamic(TransientPropertySubEntity.T.getTypeSignature());
		DYNAMIC_ET = GMF.getTypeReflection().getEntityType(dynamicSignature);
	}

	private GmMetaModel generateModel() {
		NewMetaModelGeneration mmg = new NewMetaModelGeneration();

		GmMetaModel superModel = mmg.buildMetaModel("test.gwt.TransientPropertyTestSuper", asList( //
				TransientPropertySuper1.T, //
				TransientPropertySuper2.T, //
				TransientPropertyEntity.T));

		return mmg.buildMetaModel( //
				"test.gwt.TransientPropertyTest", //
				asList(TransientPropertySubEntity.T), //
				asList(superModel));
	}

}
