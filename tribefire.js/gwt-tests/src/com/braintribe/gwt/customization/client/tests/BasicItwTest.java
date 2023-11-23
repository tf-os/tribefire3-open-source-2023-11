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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.braintribe.gwt.customization.client.tests.model.basic.non_dynamic.CI1;
import com.braintribe.gwt.customization.client.tests.model.basic.non_dynamic.CI1_COPY;
import com.braintribe.gwt.customization.client.tests.model.basic.non_dynamic.CI2;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.mi.RI_CI_CI;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.mi.RI_CI_CI_COPY;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.mi.RI_CI_RI;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.mi.RI_RI_RI;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.misc.IdEntity;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.misc.ItwTestColor;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.si.I_ToStr;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.si.RI_CI;
import com.braintribe.gwt.customization.client.tests.model.basic.proto.si.RI_RI;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationCollector;
import com.braintribe.model.generic.tracking.StandardManipulationCollector;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;
import com.braintribe.model.processing.session.api.notifying.interceptors.CollectionEnhancer;
import com.braintribe.model.processing.session.api.notifying.interceptors.InterceptorIdentification;
import com.braintribe.model.processing.session.impl.notifying.BasicNotifyingGmSession;
import com.braintribe.model.processing.session.impl.session.collection.CollectionEnhancingPropertyAccessInterceptor;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public class BasicItwTest extends AbstractGwtTest {

	NotifyingGmSession session = new BasicNotifyingGmSession();

	{
		session.interceptors() //
				.with(InterceptorIdentification.class) //
				.before(CollectionEnhancer.class) //
				.add(new ManipulationTrackingPropertyAccessInterceptor());
		session.interceptors() //
				.with(CollectionEnhancer.class) //
				.add(new CollectionEnhancingPropertyAccessInterceptor());
	}

	@Override
	protected void tryRun() {
		log("generating basic-itw model");
		NewMetaModelGeneration mmg = new NewMetaModelGeneration();
		// @formatter:off
			List<EntityType<?>> types = Arrays.asList(
					RI_CI_CI.T,
					RI_CI_CI_COPY.T,
					RI_CI_RI.T,
					RI_RI_RI.T,
					RI_CI.T,
					I_ToStr.T,
					RI_RI.T,
					
					IdEntity.T
			);
			// @formatter:on

		GmMetaModel metaModel = mmg.buildMetaModel("gm:BasicItwTestModel", types);
		makeSignaturesDynamic(metaModel);
		addSubTypeForI_ToStr(metaModel);

		ensureModelTypes(metaModel);

		test(types);
		testMulti();

		testEnum();
	}

	private void addSubTypeForI_ToStr(GmMetaModel metaModel) {
		for (GmType type : newList(metaModel.getTypes()))
			if (type.isGmEntity() && type.getTypeSignature().endsWith("I_ToStr"))
				addSubTypeForEntity(metaModel, (GmEntityType) type);

	}

	/**
	 * Creates a sub type of given {@link GmEntityType}
	 */
	private GmEntityType addSubTypeForEntity(GmMetaModel metaModel, GmEntityType gmEntityType) {
		GmEntityType subType = copy(gmEntityType);
		subType.setSuperTypes(Arrays.asList(gmEntityType));
		metaModel.getTypes().add(subType);

		return subType;
	}

	void testMulti() {
		log("Running test for entity with property introduced in multiple supertypes.");

		GenericModelTypeReflection ref = GMF.getTypeReflection();

		EntityType<GenericEntity> et = ref.getType(adaptTypeSignature(RI_CI_CI_COPY.class.getName()));

		CI1 ci1 = (CI1) et.create();
		ci1.setLong(45L);
		if (ci1.getLong().equals(45L)) {
			log("ci1.getLong has right value");
		} else {
			logError("ci1.getLong should be 45L, but was: " + ci1.getLong());
		}

		CI1_COPY ci1_copy = (CI1_COPY) et.create();
		ci1_copy.setLong(45L);
		if (ci1_copy.getLong().equals(45L)) {
			log("ci1_copy.getLong has right value");
		} else {
			logError("ci1_copy.getLong should be 45L, but was: " + ci1_copy.getLong());
		}

		Object valuePerReflection = ci1.entityType().getProperty("long").get(ci1);
		if (valuePerReflection.equals(45L)) {
			log("ci1.long retrieved per reflection has right value");
		} else {
			logError("ci1.long retrieved per reflection should be 45L, but was: " + valuePerReflection);
		}
	}

	@SuppressWarnings("rawtypes")
	private void testEnum() {
		String enumSignature = adaptTypeSignature(ItwTestColor.class.getName());

		EnumType enumType = GMF.getTypeReflection().getType(enumSignature);
		if (enumType == null) {
			logError("EnumType not found: " + enumSignature);
			return;
		}

		Enum<?> enumInstance = enumType.getInstance("red");
		if (enumInstance == null) {
			logError("EnumInstance is null for constant: red");
			return;
		}

		Class<? extends Enum> clazz = enumInstance.getClass();
		if (clazz != enumType.getJavaType()) {
			logError("enumInstance.getClass() returned wrong value: " + clazz + ", but should have been: " + enumType.getJavaType());
			return;
		}

		GenericModelType actualType = BaseType.INSTANCE.getActualType(enumInstance);
		if (actualType != enumType) {
			logError("ActualEnumType is wrong: " + actualType + ", but should have been: " + enumType);
			return;
		}

		log("Enum test completed successfully.");
	}

	protected String adaptTypeSignature(String typeSignature) {
		return makeSignatureDynamic(typeSignature);
	}

	private static enum Availability {
		both,
		classpath,
		woven
	}

	protected void test(List<EntityType<?>> entityTypes) {
		GenericModelTypeReflection ref = GMF.getTypeReflection();
		for (EntityType<?> entityType : entityTypes) {
			String osi = entityType.getTypeSignature();
			String si = adaptTypeSignature(osi);

			// if (osi.)

			// checking type lookup
			EntityType<?> oType = null;
			EntityType<?> type = null;
			try {
				oType = ref.getType(osi);
				log("found compile time entity type: " + osi);
			} catch (Exception e) {
				logError("did not find compile time entity type: " + osi, e);
				continue;
			}

			try {
				type = ref.getType(si);
				log("found runtime time entity type: " + si);
			} catch (Exception e) {
				logError("did not find runtime time entity type: " + si, e);
				continue;
			}

			// checking Class access via GM type reflection
			Class<?> oTypeReflectionJavaClass = null;
			Class<?> typeReflectionJavaClass = null;

			try {
				oTypeReflectionJavaClass = oType.getJavaType();
				typeReflectionJavaClass = type.getJavaType();
				String className = typeReflectionJavaClass.getName();
				if (className.equals(si)) {
					log("name of runtime time class is matching: " + className);
				} else {
					logError("name of runtime time class is not matching: " + className + ", expected: " + si);
				}
			} catch (Exception e) {
				logError("error while checking class", e);
			}

			try {
				if (!typeReflectionJavaClass.isInterface()) {
					log("normal class");
					Class<?> superClass = typeReflectionJavaClass.getSuperclass();

					String superTypeSignature = adaptTypeSignature(oTypeReflectionJavaClass.getSuperclass().getName());
					Class<?> expectedSuperClass = ref.getType(superTypeSignature).getJavaType();

					if (expectedSuperClass == superClass) {
						log("super class is as expected: " + expectedSuperClass);
					} else {
						logError("super class mismatch: found " + superClass.getName() + " but expected " + expectedSuperClass.getName());
					}
				} else {
					log("interface class");
				}
			} catch (Exception e) {
				logError("error while checking super class", e);
			}

			// checking instantiability
			boolean oInstantiable = !oType.isAbstract();
			boolean instantiable = oInstantiable;
			GenericEntity oInstance = null;
			GenericEntity instance = null;
			GenericEntity oPlainInstance = null;
			GenericEntity plainInstance = null;

			if (oInstantiable == instantiable) {
				log("type is " + (instantiable ? "instantiable" : "not instantiable") + " as expected");
			} else
				logError("type is " + (instantiable ? "instantiable" : "not instantiable") + " which is not expected");

			if (instantiable) {

				// instantiating
				try {
					oInstance = createInstance(oType, osi, "enhanced compile", false);
					instance = createInstance(type, si, "enhanced runtime", false);

					oPlainInstance = createInstance(oType, osi, "plain compile", true);
					plainInstance = createInstance(type, si, "plain runtime", true);

				} catch (Exception e) {
					logError("Error creating entities", e);
				}

				// instanceof validation
				Iterable<EntityType<?>> superTypes = type.getTransitiveSuperTypes(false, true);

				for (EntityType<?> superType : superTypes) {
					Class<?> javaType = superType.getJavaType();
					Predicate<Object> filter = ExpressiveChecks.instanceOfCheckers.get(javaType);
					if (filter != null) {
						try {
							boolean instanceOf = filter.test(instance);
							log("    " + (instanceOf ? "positively" : "negatively") + " checked enhanced instance of " + javaType.getName());

							boolean pInstanceOf = filter.test(plainInstance);
							log("    " + (pInstanceOf ? "positively" : "negatively") + " checked plain instance of " + javaType.getName());
						} catch (Exception e) {
							logError("    " + "error while checking instance of " + javaType.getName());
						}
					}
				}
			}

			// checking properties
			Set<String> oPropertyNames = new HashSet<>();
			Set<String> propertyNames = new HashSet<>();
			Set<String> intersectedPropertyNames = new TreeSet<>();
			Set<String> unifiedPropertyNames = new TreeSet<>();

			try {
				for (Property property : oType.getProperties()) {
					oPropertyNames.add(property.getName());
				}

				for (Property property : type.getProperties()) {
					propertyNames.add(property.getName());
				}

				intersectedPropertyNames.addAll(oPropertyNames);
				intersectedPropertyNames.retainAll(propertyNames);
				unifiedPropertyNames.addAll(oPropertyNames);
				unifiedPropertyNames.addAll(propertyNames);

				log("property analysis (existence, setting and getting each expressive and generic ways and with manipulation tracking)");

				for (String propertyName : unifiedPropertyNames) {
					Availability availability = Availability.both;

					if (!intersectedPropertyNames.contains(propertyName)) {
						if (oPropertyNames.contains(propertyName)) {
							availability = Availability.classpath;
						} else {
							availability = Availability.woven;
						}
					}

					log("    " + propertyName);

					// check availability
					if (availability != Availability.both) {
						logError("        " + "only known in " + (availability == Availability.classpath ? "compile time" : "runtime") + " type");
					}

					Property oProperty = null;
					Property property = null;

					try {
						oProperty = oType.getProperty(propertyName);
						property = type.getProperty(propertyName);
					} catch (Exception e) {
						logError("        " + "error while accessing reflection Property");
						continue;
					}

					if (instantiable) {
						checkPropertyAccess(oType, oInstance, oProperty, "generic enhanced original", true);
						checkPropertyAccess(type, instance, property, "generic enhanced", true);

						checkPropertyAccess(oType, oPlainInstance, oProperty, "generic plain original", false);
						checkPropertyAccess(type, plainInstance, property, "generic plain", false);

						for (EntityType<?> superType : type.getTransitiveSuperTypes(false, true)) {
							Property superProperty = superType.findProperty(propertyName);

							if (superProperty != null) {
								checkPropertyAccess(superType, instance, property, "super property", true);
							}
						}
					}
				}
			} catch (Exception e) {
				logError("failed scanning properties", e);
			}

			if (entityType == IdEntity.T) {
				checkHasId(oInstance, "generic enhanced original");
				checkHasId(instance, "generic enhanced");

				checkHasId(oPlainInstance, "generic plain original");
				checkHasId(plainInstance, "generic plain");
			}

			logSeparator();
		}

	}

	private GenericEntity createInstance(EntityType<?> et, String signature, String desc, boolean plain) {
		try {
			GenericEntity result = plain ? et.createPlain() : session.create(et);
			log("created " + desc + " type: " + signature + ". instance: " + result);

			return result;
		} catch (Exception e) {
			throw new RuntimeException("failed creating " + desc + " type instance: " + signature, e);
		}
	}

	protected void checkPropertyAccess(EntityType<?> et, GenericEntity instance, Property property, String mode, boolean isEnhanced) {
		String signature = et.getTypeSignature();

		GenericModelType propertyType = property.getType();
		Class<?> propertyClass = propertyType.getJavaType();
		Object propertyValue = ExpressiveChecks.defaultValueProviders.get(propertyClass);

		ManipulationCollector manipulationCollector = null;
		if (isEnhanced) {
			manipulationCollector = new StandardManipulationCollector();
			session.listeners().add(manipulationCollector);
		}

		// checking expressive access
		try {
			property.set(instance, propertyValue);
			Object retrievedPropertyValue = property.get(instance);
			if (retrievedPropertyValue == null || !retrievedPropertyValue.equals(propertyValue)) {
				logError("        " + "could not get previously set value of property " + signature + "." + property.getName() + "; mode=" + mode
						+ " set: " + propertyValue + ", retrieved: " + retrievedPropertyValue);
			}

			if (isEnhanced) {
				List<Manipulation> manipulations = manipulationCollector.getManipulations();

				if (manipulations != null && manipulations.size() == 1) {
					Manipulation manipulation = manipulations.get(0);

					if (manipulation instanceof ChangeValueManipulation) {
						ChangeValueManipulation changeValueManipulation = (ChangeValueManipulation) manipulation;
						LocalEntityProperty entityProperty = (LocalEntityProperty) changeValueManipulation.getOwner();

						if (entityProperty != null) {
							if (!property.getName().equals(entityProperty.getPropertyName())) {
								logError("        " + "manipulation tracked for a property change is having wrong property name; mode=" + mode);
							}
						} else
							logError("        " + "manipulation tracked for a property change is missing owner; mode=" + mode);
					} else {
						logError("        " + "invalid type of manipulation tracked for a property change; mode=" + mode);
					}
				} else {
					logError("        " + "invalid amount manipulations tracked for a property change; mode=" + mode);
				}
			}

			// reset property value for potential expressive access
			property.set(instance, property.getType().getDefaultValue());
		} catch (Exception e) {
			logError("        " + "error while accessing set/get; mode=" + mode, e);

		} finally {
			session.listeners().remove(manipulationCollector);
		}

		// test DIRECT property access
		if (isEnhanced) {
			manipulationCollector = new StandardManipulationCollector();
			session.listeners().add(manipulationCollector);
		}

		try {
			property.setDirect(instance, propertyValue);
			Object retrievedPropertyValue = property.getDirect(instance);
			if (retrievedPropertyValue == null || !retrievedPropertyValue.equals(propertyValue)) {
				logError("        " + "could not get previously DIRECTLY set value of property " + signature + "." + property.getName() + "; mode="
						+ mode + " set: " + propertyValue + ", retrieved: " + retrievedPropertyValue);
			}

			if (isEnhanced) {
				List<Manipulation> manipulations = manipulationCollector.getManipulations();

				if (manipulations != null && !manipulations.isEmpty()) {
					logError("        " + "manipulation should not have been tracked when setting DIRECTLY; mode=" + mode);
				}
			}

			// reset property value for potential expressive access
			property.setDirect(instance, property.getType().getDefaultValue());
		} catch (Exception e) {
			logError("        " + "error while DIRECTLY accessing set/get; mode=" + mode, e);

		} finally {
			session.listeners().remove(manipulationCollector);
		}
	}

	private void checkHasId(GenericEntity entity, String mode) {
		Long id = 99L;

		try {
			entity.setId(id);
		} catch (Exception e) {
			logError("        " + "error while DIRECTLY writing persistence id; mode=" + mode, e);
		}

		Object retrievedId = entity.getId();
		if (retrievedId != id) {
			logError("        " + "error while reading persistence id; mode=" + mode + ". Expected: " + id + ", Actual: " + retrievedId);
		}
	}

	static class ExpressiveChecks {
		public static Map<Class<? extends GenericEntity>, Predicate<Object>> instanceOfCheckers;
		public static Map<Class<?>, Object> defaultValueProviders;

		static {
			instanceOfCheckers = new HashMap<>();
			defaultValueProviders = new HashMap<>();

			instanceOfCheckers.put(GenericEntity.class, o -> o instanceof GenericEntity);
			instanceOfCheckers.put(CI1.class, o -> o instanceof CI1);
			instanceOfCheckers.put(CI2.class, o -> o instanceof CI2);

			defaultValueProviders.put(Object.class, "IdValue");
			defaultValueProviders.put(String.class, "Hallo Welt!");
			defaultValueProviders.put(Integer.class, 23);
			defaultValueProviders.put(Long.class, 42L);
			defaultValueProviders.put(Float.class, (float) Math.E);
			defaultValueProviders.put(Double.class, Math.PI);
			defaultValueProviders.put(BigDecimal.class, new BigDecimal("4711"));
			defaultValueProviders.put(Date.class, new Date());
			defaultValueProviders.put(Boolean.class, true);
			defaultValueProviders.put(ItwTestColor.class, ItwTestColor.green);
		}

	}

}
