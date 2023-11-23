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

import java.math.BigDecimal;
import java.util.Date;

import com.braintribe.gwt.customization.client.tests.model.initializer.Color;
import com.braintribe.gwt.customization.client.tests.model.initializer.InitializedEntity;
import com.braintribe.gwt.customization.client.tests.model.initializer.InitializedSubEntity;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.lcd.CommonTools;

/**
 * @author peter.gazdik
 */
public class InitializerTest extends AbstractGwtTest {

	private static final long SECOND_IN_MILLIS = 1000;

	@Override
	protected void tryRun() throws GmfException {
		GmMetaModel metaModel = generateModel();

		makeSignaturesDynamic(metaModel, false);
		ensureModelTypes(metaModel);

		testInitialized(InitializedEntity.class.getName());
		testInitialized(makeSignatureDynamic(InitializedEntity.class.getName()));

		EntityType<?> et = typeReflection.getEntityType(makeSignatureDynamic(InitializedEntity.class.getName()));
		GenericEntity entity = et.create();
		
		log("OldBoolean: " + et.getProperty("uninitializedBooleanValue").setDirect(entity, true));
		log("OldLong: " + et.getProperty("uninitializedLongValue").setDirect(entity, 0l));
		
		testInitializedSub(InitializedSubEntity.class.getName());
		testInitializedSub(makeSignatureDynamic(InitializedSubEntity.class.getName()));
	}

	private void testInitialized(String typeSignature) {
		EntityType<?> et = typeReflection.getEntityType(typeSignature);

		testInitialized(et, "plain", et.createPlain());
		testInitialized(et, "enhanced", et.create());
	}

	private void testInitialized(EntityType<?> et, String type, GenericEntity entity) {
		logTestingEntity(et, type);

		assertProperty(et, type, entity, "intValue", 99);
		assertProperty(et, type, entity, "longValue", 11L);
		assertProperty(et, type, entity, "floatValue", 123f);
		assertProperty(et, type, entity, "doubleValue", -123D);
		assertProperty(et, type, entity, "bigFloatValue", 1.0e30f);
		assertProperty(et, type, entity, "bigDoubleValue", -1.0e30d);
		assertProperty(et, type, entity, "decimalValue", new BigDecimal("99889988.00"));
		assertProperty(et, type, entity, "booleanValue", true);
		assertProperty(et, type, entity, "enumValue", Color.green);
		assertProperty(et, type, entity, "enumShort", Color.green);
		assertProperty(et, type, entity, "uninitializedDateValue", null);
		assertProperty(et, type, entity, "uninitializedBooleanValue", false);
		assertProperty(et, type, entity, "uninitializedLongValue", 0L);
		assertDateNow(et, type, entity, "dateValue");
	}

	private void testInitializedSub(String typeSignature) {
		EntityType<GenericEntity> et = typeReflection.getEntityType(typeSignature);

		testInitializedSub(et, "plain", et.createPlain());
		testInitializedSub(et, "enhanced", et.create());
	}

	private void testInitializedSub(EntityType<?> et, String type, GenericEntity entity) {
		logTestingEntity(et, type);

		assertProperty(et, type, entity, "intValue", 88); // overridden
		assertProperty(et, type, entity, "longValue", 0L); // overridden with 0 as nothing was stated, but this is
															// default
		assertProperty(et, type, entity, "newLongValue", 0L); // new property which is primitive
		assertProperty(et, type, entity, "floatValue", 123f); // overridden with null as nothing was stated, but this is
																// default
		assertProperty(et, type, entity, "booleanValue", true);
		assertProperty(et, type, entity, "dateValue", null); // inherited
	}

	private void assertProperty(EntityType<?> et, String type, GenericEntity entity, String propertyName, Object expected) {
		Object actual = et.getProperty(propertyName).get(entity);

		if (!CommonTools.equalsOrBothNull(expected, actual)) {
			logError("Property: [" + type + "] " + et.getTypeSignature() + "." + propertyName + " has wrong value. Expected: " + expected
					+ ", actual: " + actual);
		} else {
			log("    property '" + propertyName + "' [OK]");
		}
	}

	private void assertDateNow(EntityType<?> et, String type, GenericEntity entity, String propertyName) {
		Date actual = (Date) et.getProperty(propertyName).get(entity);
		if (actual == null) {
			logError("Property: [" + type + "] " + et.getTypeSignature() + "." + propertyName + " should not be null!");
			return;
		}
		Date now = new Date();
		long diffInMillis = now.getTime() - actual.getTime();
		if (diffInMillis > 10 * SECOND_IN_MILLIS) {
			logError("Property: [" + type + "] " + et.getTypeSignature() + "." + propertyName
					+ " has wrong value. The difference in millis is too big. Now: " + now + ", actual: " + actual + ", Difference: " + diffInMillis);
		} else {
			log("    property '" + propertyName + "' [OK]");
		}
	}

	private void logTestingEntity(EntityType<?> et, String type) {
		log("Testing '" + et.getTypeSignature() + "'[" + type + "]");
	}

	private GmMetaModel generateModel() {
		log("generating meta model");

		return new NewMetaModelGeneration().buildMetaModel("test.gwt27.InitializerModel", asList(InitializedEntity.T, InitializedSubEntity.T));
	}

}
