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
package com.braintribe.model.processing.test.itw;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.test.itw.entity.PrimitivePropsEntity;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class NotNullablePropertiesItwTests extends ImportantItwTestSuperType {

	@Test
	public void testSimpleEntity() {
		GmMetaModel metaModel = prepareModel();
		metaModel.deploy();

		GenericEntity originalEntity = instantiate(PrimitivePropsEntity.class.getName());
		GenericEntity modifiedEntity = instantiate(modifySignature(PrimitivePropsEntity.class.getName()));

		checkEntityHasPrimitiveProperties(originalEntity);
		checkEntityHasPrimitiveProperties(modifiedEntity);
	}

	private static void checkEntityHasPrimitiveProperties(GenericEntity entity) {
		Method[] methods = entity.getClass().getMethods();

		// checking getter returns primitive values (we assume setters use the same type as getters)
		for (Method m : methods) {
			if (m.getName().startsWith("get") && m.getName().endsWith("Value")) {
				Class<?> returnType = m.getReturnType();
				BtAssertions.assertThat(returnType).isPrimitive();
			}
		}

		// check getters/setters work
		EntityType<?> et = entity.entityType();

		for (Property p : et.getProperties()) {
			if (p.isIdentifier())
				continue;

			Object propertyValue = getValueForNonNullableType(p);
			p.set(entity, propertyValue);
			BtAssertions.assertThat(p.<Object> get(entity)).isEqualTo(propertyValue);
		}
	}

	private static Object getValueForNonNullableType(Property p) {
		TypeCode typeCode = p.getType().getTypeCode();

		switch (typeCode) {
			case booleanType:
				return Boolean.TRUE;
			case doubleType:
				return 99d;
			case floatType:
				return 88f;
			case integerType:
				return 11;
			case longType:
				return 11111111111111111L;
			/* Yes, I know it says non-nullable type, but now that GenericEntity has property called partition of type
			 * string, we cannot have only primitive properties, so this makes the test going... */
			case stringType:
				return "";
			default:
				throw new RuntimeException("Unexpected property '" + p.getName() + "' of type: " + typeCode);
		}
	}

	private static GmMetaModel prepareModel() {
		GmMetaModel metaModel = new NewMetaModelGeneration().buildMetaModel("test:PrimitiveEntityModel", asList(PrimitivePropsEntity.T));

		// we need new ArrayList to avoid ConcurrentModificationException!
		for (GmType gmType : new ArrayList<>(metaModel.getTypes())) {
			String ts = gmType.getTypeSignature();
			if (ts.equals(PrimitivePropsEntity.class.getName())) {
				GmEntityType newType = copy((GmEntityType) gmType);
				newType.setTypeSignature(modifySignature(ts));

				metaModel.getTypes().add(newType);
			}
		}

		return metaModel;
	}

	private static String modifySignature(String ts) {
		return ts.replace("entity.", "entity.Modified");
	}

	private static GmEntityType copy(final GmEntityType gmEntityType) {
		Matcher matcher = new Matcher() {
			/** Note that returning false means we will create a copy, returning true means we take original object */
			@Override
			public boolean matches(TraversingContext traversingContext) {
				Object object = traversingContext.getObjectStack().peek();
				if (object == gmEntityType) {
					// we want to create a copy of our top level instance (GmEntityType)
					return false;

				} else if (object instanceof List) {
					// we ant to copy GmEntityType.properties of our top-level instance
					Stack<BasicCriterion> ts = traversingContext.getTraversingStack();
					BasicCriterion bc = ts.peek();
					if (ts.size() == 3 && bc.criterionType() == CriterionType.PROPERTY
							&& ((PropertyCriterion) bc).getPropertyName().equals("properties")) {
						return false;
					}

				} else if (object instanceof GmProperty) {
					GmProperty gmProperty = (GmProperty) object;
					if (gmProperty.getDeclaringType() == gmEntityType) {
						// we ant to copy properties of our top-level instance
						return false;
					}
				}

				// everything else can be copied
				return true;
			}
		};

		return (GmEntityType) gmEntityType.entityType().clone(gmEntityType, matcher, StrategyOnCriterionMatch.reference);
	}

	protected GenericEntity instantiate(String signature) {
		EntityType<GenericEntity> entityType = typeReflection().getType(signature);
		return entityType.create();
	}

	private static GenericModelTypeReflection typeReflection() {
		return GMF.getTypeReflection();
	}

}
