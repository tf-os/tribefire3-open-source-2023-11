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

import java.util.Map;

import com.braintribe.gwt.customization.client.tests.model.tostring.NoStringEntity;
import com.braintribe.gwt.customization.client.tests.model.tostring.ToStringEntity;
import com.braintribe.gwt.customization.client.tests.model.tostring.ToStringSubEntity;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;

/**
 * @author peter.gazdik
 */
public class ToStringTest extends AbstractGwtTest {

	@Override
	protected void tryRun() throws GmfException {
		GmMetaModel metaModel = generateModel();

		ensureModelTypes(metaModel);

		testToStringMethods(ToStringEntity.class.getName());
		testToStringMethods(makeSignatureDynamic(ToStringEntity.class.getName()));

		testToStringMethodsSub(ToStringSubEntity.class.getName());
		testToStringMethodsSub(makeSignatureDynamic(ToStringSubEntity.class.getName()));
	}

	private void testToStringMethods(String typeSignature) {
		EntityType<StandardStringIdentifiable> et = typeReflection.getEntityType(typeSignature);
		testToStringMethods("plain", et.createPlain());
		testToStringMethods("enhanced", et.create());
	}

	private void testToStringMethods(String type, StandardStringIdentifiable ge) {
		initAndLogTestingEntity(ge, type);
		assertEqual(ge, "toString", type, ge.toString(), "ToString");
		assertEqual(ge, "toSelectiveInformation", type, ge.toSelectiveInformation(), "Selective");

		ge.setId(99L);
		assertEqual(ge, "toString (persistent)", type, ge.toString(), "ToString");
		assertEqual(ge, "toSelectiveInformation (persistent)", type, ge.toSelectiveInformation(), "Selective");
	}

	private void testToStringMethodsSub(String typeSignature) {
		EntityType<GenericEntity> et = typeReflection.getEntityType(typeSignature);
		testToStringMethodsSub("plain", et.createPlain());
		testToStringMethodsSub("enhanced", et.create());
	}

	private void testToStringMethodsSub(String type, GenericEntity ge) {	
		initAndLogTestingEntity(ge, type);
		assertEqual(ge, "toString", type, ge.toString(), "ToStringSub");
		assertEqual(ge, "toSelectiveInformation", type, ge.toSelectiveInformation(), "SelectiveSub");
	}

	private void assertEqual(GenericEntity ge, String method, String type, String actual, String expected) {
		expected = prefixFor(ge) + " " + expected;
		
		if (actual.equals(expected)) {
			log("    method '" + method + "' [OK]");

		} else {
			EntityType<GenericEntity> et = ge.entityType();

			logError(method + " problem [" + type + "]. Wrong Value for type " + et.getTypeSignature() + ". Expected: " + expected + ", actual: "
					+ actual);
		}
	}

	private String prefixFor(GenericEntity entity) {
		EntityType<GenericEntity> et = entity.entityType();
		return et.getTypeSignature() + " " + et.getShortName() + " " + idOrRid(entity) + " " + entity.runtimeId() + " ${N/A}";
	}

	private Object idOrRid(GenericEntity entity) {
		Object id = entity.getId();
		return id != null ? id : entity.runtimeId();
	}

	private void initAndLogTestingEntity(GenericEntity entity, String type) {
		entity.setId("id-" + type);
		log("Testing '" + entity.entityType().getTypeSignature() + "'[" + type + "]");
	}

	private GmMetaModel generateModel() {
		log("generating meta model");
		
		GmMetaModel metaModel = modelForTypes(ToStringEntity.T, ToStringSubEntity.T, NoStringEntity.T);

		Map<String, GmEntityType> gmTypes = indexEntityTypes(metaModel);

		GmEntityType tse = gmTypes.get(ToStringEntity.class.getName());
		GmEntityType tsse = gmTypes.get(ToStringSubEntity.class.getName());
		
		GmEntityType nse = gmTypes.get(NoStringEntity.class.getName());

		GmEntityType dynamicTse = createDynamicSubType(tse);
		GmEntityType dynamicTsse = createDynamicSubType(tsse);

		// just so that we also test the method is taken from supertype other than the first one
		dynamicTse.getSuperTypes().add(0, nse);
		dynamicTsse.getSuperTypes().add(0, nse);
		
		metaModel.getTypes().add(dynamicTse);
		metaModel.getTypes().add(dynamicTsse);

		return metaModel;
	}


}
