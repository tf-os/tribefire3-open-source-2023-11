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
package com.braintribe.model.processing.query.smart.test.setup.base;

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.modelAName;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.modelBName;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.modelSName;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.SmartAccess;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.ConstantPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.ConvertibleQualifiedProperty;
import com.braintribe.model.accessdeployment.smart.meta.DefaultDelegate;
import com.braintribe.model.accessdeployment.smart.meta.EntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.IdentityEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PolymorphicBaseEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PolymorphicDerivateEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAsIs;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.SmartUnmapped;
import com.braintribe.model.accessdeployment.smart.meta.conversion.DateToString;
import com.braintribe.model.accessdeployment.smart.meta.conversion.EnumToSimpleValue;
import com.braintribe.model.accessdeployment.smart.meta.conversion.LongToString;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.accessdeployment.smart.meta.discriminator.CompositeDiscriminator;
import com.braintribe.model.accessdeployment.smart.meta.discriminator.Discriminator;
import com.braintribe.model.accessdeployment.smart.meta.discriminator.SimpleDiscriminator;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.meta.selector.DisjunctionSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.PropertyRegexSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.EnumTypeMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.smart.test.model.EntityList;
import com.braintribe.model.processing.query.smart.test.model.EnumConstantMappingProvider;
import com.braintribe.model.processing.query.smart.test.model.accessA.EntityA;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdentifiableB;
import com.braintribe.model.processing.query.smart.test.model.deployment.MoodAccess;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartGenericEntity;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public abstract class AbstractSmartSetupProvider implements SmartSetupProvider {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected SmartMappingSetup setup;

	protected ModelOracle oracleA;
	protected ModelOracle oracleB;
	protected ModelOracle oracleS;

	protected ModelMetaDataEditor editorA;
	protected ModelMetaDataEditor editorB;
	protected ModelMetaDataEditor editorS;

	protected ModelMetaDataEditor editor;

	@Override
	public SmartMappingSetup setup() {
		if (setup == null) {
			newSetup();
		}

		return setup;
	}

	protected void newSetup() {
		setup = new SmartMappingSetup();

		setup.modelA = rawModelA();
		setup.modelB = rawModelB();
		setup.modelS = rawModelS();

		setup.accessA = newAccess(SmartMappingSetup.accessIdA);
		setup.accessB = newAccess(SmartMappingSetup.accessIdB);
		setup.accessS = newAccess(SmartMappingSetup.accessIdS, SmartAccess.T); // this is just dummy

		setup.accessA.setMetaModel(setup.modelA);
		setup.accessB.setMetaModel(setup.modelB);

		editorA = new BasicModelMetaDataEditor(setup.modelA);
		editorB = new BasicModelMetaDataEditor(setup.modelB);
		editorS = new BasicModelMetaDataEditor(setup.modelS);

		oracleA = new BasicModelOracle(setup.modelA);
		oracleB = new BasicModelOracle(setup.modelB);
		oracleS = new BasicModelOracle(setup.modelS);

		editor = new BasicModelMetaDataEditor(setup.modelS);

		mapPartitionAndGlobalId();

		configureMappings();
	}

	protected abstract void configureMappings();

	private void mapPartitionAndGlobalId() {
		editor.onEntityType(GenericEntity.T) //
				.addPropertyMetaData(GenericEntity.globalId, asIsProperty()) //
				.addPropertyMetaData(GenericEntity.partition, asIsProperty());
	}

	protected GmMetaModel rawModelA() {
		return provideWrappedModel(modelAName, EntityList.accessA);
	}

	protected GmMetaModel rawModelB() {
		return provideWrappedModel(modelBName, EntityList.accessB);
	}

	protected GmMetaModel rawModelS() {
		return provideWrappedModel(modelSName, EntityList.accessS);
	}

	/* we wrap every model in another model as it's dependency, to make sure a correct mechanism for resolving model
	 * information is used */
	private GmMetaModel provideWrappedModel(String name, Collection<EntityType<?>> types) {
		NewMetaModelGeneration mmg = new NewMetaModelGeneration();
		GmMetaModel modelCore = mmg.buildMetaModel(name + "Core", types);

		return mmg.buildMetaModel(name, null, asList(modelCore));
	}

	// #####################################################
	// ## . . . . . . Building Smart Mappings . . . . . . ##
	// #####################################################

	protected KeyPropertyAssignment kpa(ConvertibleQualifiedProperty cqp, QualifiedProperty keyProperty) {
		KeyPropertyAssignment result = newEntity(KeyPropertyAssignment.T);
		result.setProperty(cqp);
		result.setKeyProperty(keyProperty);

		return result;
	}

	protected InverseKeyPropertyAssignment ikpa(ConvertibleQualifiedProperty property, EntityType<?> entityType,
			String keyPropertyName) {

		InverseKeyPropertyAssignment result = newEntity(InverseKeyPropertyAssignment.T);
		result.setProperty(property);
		result.setKeyProperty(qp(entityType, keyPropertyName));

		return result;
	}

	protected InverseKeyPropertyAssignment ikpa(ConvertibleQualifiedProperty property, QualifiedProperty keyProperty) {
		
		InverseKeyPropertyAssignment result = newEntity(InverseKeyPropertyAssignment.T);
		result.setProperty(property);
		result.setKeyProperty(keyProperty);
		
		return result;
	}

	protected <K extends KeyPropertyAssignment> K external(K kpa) {
		kpa.setForceExternalJoin(true);
		return kpa;
	}
	
	protected CompositeKeyPropertyAssignment external(CompositeKeyPropertyAssignment ckpa) {
		for (KeyPropertyAssignment kpa : ckpa.getKeyPropertyAssignments()) {
			kpa.setForceExternalJoin(true);
		}
		return ckpa;
	}
	
	protected CompositeInverseKeyPropertyAssignment external(CompositeInverseKeyPropertyAssignment cikpa) {
		for (InverseKeyPropertyAssignment ikpa : cikpa.getInverseKeyPropertyAssignments()) {
			ikpa.setForceExternalJoin(true);
		}
		return cikpa;
	}
	
	protected LinkPropertyAssignment linkPropertyAssignment(EntityType<?> entityType, String keyPropertyName, EntityType<?> otherEntityType,
			String otherKeyPropertyName, EntityType<?> linkEntityType, String linkKey, String linkOtherKey) {

		return linkPropertyAssignment(newEntity(LinkPropertyAssignment.T), entityType, keyPropertyName, otherEntityType, otherKeyPropertyName,
				linkEntityType, linkKey, linkOtherKey);
	}

	protected OrderedLinkPropertyAssignment orderedLinkPropertyAssignment(EntityType<?> entityType, String keyPropertyName,
			EntityType<?> otherEntityType, String otherKeyPropertyName, EntityType<?> linkEntityType, String linkKey, String linkOtherKey,
			String linkIndex) {

		OrderedLinkPropertyAssignment result = linkPropertyAssignment(newEntity(OrderedLinkPropertyAssignment.T), entityType, keyPropertyName,
				otherEntityType, otherKeyPropertyName, linkEntityType, linkKey, linkOtherKey);
		result.setLinkIndex(findProperty(linkEntityType, linkIndex));

		return result;
	}

	protected <T extends LinkPropertyAssignment> T linkPropertyAssignment(T result, EntityType<?> entityType, String keyPropertyName,
			EntityType<?> otherEntityType, String otherKeyPropertyName, EntityType<?> linkEntityType, String linkKey, String linkOtherKey) {

		result.setKey(qp(entityType, keyPropertyName));
		result.setOtherKey(qp(otherEntityType, otherKeyPropertyName));
		result.setLinkKey(findProperty(linkEntityType, linkKey));
		result.setLinkOtherKey(findProperty(linkEntityType, linkOtherKey));
		result.setLinkAccess(findAccess(linkEntityType));

		return result;
	}

	protected CompositeKeyPropertyAssignment compositeKpa(KeyPropertyAssignment... kpas) {
		CompositeKeyPropertyAssignment result = newEntity(CompositeKeyPropertyAssignment.T);
		result.setKeyPropertyAssignments(asSet(kpas));

		return result;
	}

	protected CompositeKeyPropertyAssignment compositeKpa(EntityType<?> mappedClass, EntityType<?> compositeKeyClass, String... properties) {
		if (properties.length % 2 == 1) {
			throw new RuntimeException();
		}

		Set<KeyPropertyAssignment> kpas = newSet();

		int i = 0;
		while (i < properties.length) {
			String mappedProperty = properties[i++];
			String compositeKeyProperty = properties[i++];

			KeyPropertyAssignment kpa = kpa(cqp(mappedClass, mappedProperty), qp(compositeKeyClass, compositeKeyProperty));
			kpas.add(kpa);
		}

		CompositeKeyPropertyAssignment result = newEntity(CompositeKeyPropertyAssignment.T);
		result.setKeyPropertyAssignments(kpas);

		return result;
	}

	protected CompositeInverseKeyPropertyAssignment inverseCompositeKpa(EntityType<?> compositeKeyClass, EntityType<?> mappedClass,
			String... properties) {

		if (properties.length % 2 == 1) {
			throw new RuntimeException();
		}

		Set<InverseKeyPropertyAssignment> ikpas = newSet();

		int i = 0;
		while (i < properties.length) {
			String compositeKeyProperty = properties[i++];
			String mappedProperty = properties[i++];

			InverseKeyPropertyAssignment kpa = ikpa(cqp(mappedClass, mappedProperty), compositeKeyClass, compositeKeyProperty);
			ikpas.add(kpa);
		}

		CompositeInverseKeyPropertyAssignment result = newEntity(CompositeInverseKeyPropertyAssignment.T);
		result.setInverseKeyPropertyAssignments(ikpas);

		return result;
	}

	protected QualifiedPropertyAssignment qpa(EntityType<?> entityType, String propertyName) {
		return qpa(entityType, propertyName, null);
	}

	protected QualifiedPropertyAssignment qpa(EntityType<?> entityType, String propertyName, SmartConversion conversion) {
		QualifiedPropertyAssignment result = qp(newEntity(QualifiedPropertyAssignment.T), entityType, propertyName);
		result.setConversion(conversion);

		return result;
	}

	protected ConvertibleQualifiedProperty cqp(EntityType<?> entityType, String property) {
		return cqp(entityType, property, null);
	}

	protected ConvertibleQualifiedProperty cqp(EntityType<?> entityType, String property, boolean omitEntityType) {
		return cqp(entityType, property, null, omitEntityType);
	}
	
	protected ConvertibleQualifiedProperty cqp(EntityType<?> entityType, String property, SmartConversion conversion) {
		return cqp(entityType, property, conversion, false);
	}

	protected ConvertibleQualifiedProperty cqp(EntityType<?> entityType, String property, SmartConversion conversion, boolean omitEntityType) {
		ConvertibleQualifiedProperty result = qp(newEntity(ConvertibleQualifiedProperty.T), entityType, property, omitEntityType);
		result.setConversion(conversion);
		
		return result;
	}
	
	protected QualifiedProperty qp(EntityType<?> entityType, String property) {
		return qp(QualifiedProperty.T.createPlain(), entityType, property);
	}

	protected QualifiedProperty qp(EntityType<?> entityType, String property, boolean omitEntityType) {
		return qp(QualifiedProperty.T.createPlain(), entityType, property, omitEntityType);
	}
	
	private <T extends QualifiedProperty> T qp(T result, EntityType<?> entityType, String property) {
		return qp(result, entityType, property, false);
	}

	private <T extends QualifiedProperty> T qp(T result, EntityType<?> entityType, String property, boolean omitEntityType) {
		EntityTypeOracle entityTypeOracle = findEntityTypeOracle(entityType);

		result.setProperty(entityTypeOracle.findProperty(property).asGmProperty());
		if (!omitEntityType)
			result.setEntityType(entityTypeOracle.asGmType());

		return result;
	}

	protected ConstantPropertyAssignment constantProperty(String value) {
		ConstantPropertyAssignment result = ConstantPropertyAssignment.T.createPlain();
		result.setValue(value);

		return result;
	}

	protected SmartUnmapped unmapped() {
		return SmartUnmapped.T.create();
	}

	private GmProperty findProperty(EntityType<?> entityType, String propertyName) {
		return findEntityTypeOracle(entityType).getProperty(propertyName).asGmProperty();
	}

	protected IncrementalAccess findAccess(EntityType<?> entityType) {
		if (isAccessA(entityType)) {
			return setup.accessA;

		} else if (isAccessB(entityType)) {
			return setup.accessB;

		} else {
			throw new RuntimeException("Illegal class.");
		}
	}

	protected ModelMetaDataEditor findEditor(EntityType<?> entityType) {
		if (isAccessA(entityType)) {
			return editorA;

		} else if (isAccessB(entityType)) {
			return editorB;

		} else if (isSmartAccess(entityType)) {
			return editorS;

		} else if (isShared(entityType)) {
			return editorS;

		} else {
			throw new RuntimeException("Illegal class: " + entityType.getTypeSignature());
		}
	}

	protected EntityTypeOracle findEntityTypeOracle(EntityType<?> entityType) {
		return findModelOracle(entityType).getEntityTypeOracle(entityType);
	}

	protected ModelOracle findModelOracle(EntityType<?> entityType) {
		if (isAccessA(entityType)) {
			return oracleA;

		} else if (isAccessB(entityType)) {
			return oracleB;

		} else if (isSmartAccess(entityType)) {
			return oracleS;

		} else if (isShared(entityType)) {
			return oracleS;

		} else {
			throw new RuntimeException("Illegal class: " + entityType.getTypeSignature());
		}
	}

	/**
	 * Creates {@link PropertyMetaData} which maps properties in "asIs" way to a given access, as long as the property
	 * names end with given suffix. We want to map all "-A" properties to accessA and "-B" properties to access B.
	 */
	protected PropertyMetaData asIsForPropertyNameSuffix(String suffix) {
		PropertyRegexSelector selector = PropertyRegexSelector.T.create();
		selector.setRegex(".*" + suffix);

		PropertyAsIs result = asIsProperty();
		result.setSelector(selector);

		return result;
	}

	protected PropertyAsIs asIsProperty() {
		return newEntity(PropertyAsIs.T);
	}

	protected PropertyAsIs asIsProperty(SmartConversion conversion) {
		PropertyAsIs result = asIsProperty();
		result.setConversion(conversion);

		return result;
	}

	protected SmartConversion dateToString(String pattern, boolean inverse) {
		DateToString result = (DateToString) typeReflection.getEntityType(DateToString.class).createPlain();
		result.setPattern(pattern);
		result.setInverse(inverse);

		return result;
	}

	protected SmartConversion longToString(boolean inverse) {
		LongToString result = (LongToString) typeReflection.getEntityType(LongToString.class).createPlain();
		result.setInverse(inverse);

		return result;
	}

	protected SmartConversion enumToOrdinalConversion(Class<? extends Enum<?>> enumClass, ModelOracle oracle) {
		EnumTypeOracle enumTypeOracle = oracle.getEnumTypeOracle(enumClass);

		Map<GmEnumConstant, Object> valueMappings = newMap();

		for (Enum<?> constant : enumClass.getEnumConstants()) {
			valueMappings.put(enumTypeOracle.getConstant(constant).asGmEnumConstant(), constant.ordinal());
		}

		EnumToSimpleValue result = (EnumToSimpleValue) typeReflection.getEntityType(EnumToSimpleValue.class).createPlain();
		result.setValueMappings(valueMappings);
		result.setInverse(true);

		return result;
	}

	protected IdentityEntityAssignment asIsEntity(IncrementalAccess access) {
		return addUseCase(newEntity(IdentityEntityAssignment.T), access);
	}

	protected QualifiedEntityAssignment qualifiedEntityAssignment(EntityType<?> entityType) {
		return qualifiedEntityAssignment(findAccess(entityType), findEntityTypeOracle(entityType).asGmType());
	}

	protected QualifiedEntityAssignment qualifiedEntityAssignment(IncrementalAccess access, GmEntityType entityType) {
		QualifiedEntityAssignment result = newEntity(QualifiedEntityAssignment.T);
		result.setEntityType(entityType);

		return addUseCase(result, access);
	}

	protected <T extends EntityAssignment> T addUseCase(T assignment, IncrementalAccess access) {
		UseCaseSelector newSelector = newEntity(UseCaseSelector.T);
		newSelector.setUseCase(access.getExternalId());

		MetaDataSelector existingSelector = assignment.getSelector();

		if (existingSelector == null) {
			assignment.setSelector(newSelector);

		} else if (existingSelector instanceof UseCaseSelector) {
			DisjunctionSelector ds = newEntity(DisjunctionSelector.T);
			ds.getOperands().add(existingSelector);
			ds.getOperands().add(newSelector);

		} else if (existingSelector instanceof DisjunctionSelector) {
			((DisjunctionSelector) existingSelector).getOperands().add(newSelector);

		} else {
			throw new RuntimeException("Unexpected selector: " + existingSelector);
		}

		return assignment;
	}

	protected PolymorphicBaseEntityAssignment polymorphicBase(EntityType<?> entityType, String... discriminatorProperties) {

		IncrementalAccess access = findAccess(entityType);
		EntityTypeOracle entityTypeOracle = findEntityTypeOracle(entityType);

		Discriminator discriminator = discriminator(entityTypeOracle, discriminatorProperties);

		PolymorphicBaseEntityAssignment result = newEntity(PolymorphicBaseEntityAssignment.T);
		result.setEntityType(entityTypeOracle.asGmType());
		result.setDiscriminator(discriminator);
		result.setDiscriminatorValue(null);

		return addUseCase(result, access);
	}

	protected PolymorphicDerivateEntityAssignment polymorphicDerivation(PolymorphicBaseEntityAssignment base, IncrementalAccess access,
			Object discriminatorValue) {
		PolymorphicDerivateEntityAssignment result = newEntity(PolymorphicDerivateEntityAssignment.T);
		result.setBase(base);
		result.setDiscriminatorValue(discriminatorValue);

		return addUseCase(result, access);
	}

	protected Discriminator discriminator(EntityTypeOracle entityTypeOracle, String[] discriminatorProperties) {
		if (discriminatorProperties.length == 1) {
			SimpleDiscriminator result = newEntity(SimpleDiscriminator.T);
			result.setProperty(entityTypeOracle.getProperty(discriminatorProperties[0]).asGmProperty());
			return result;

		} else {
			List<GmProperty> properties = newList();
			for (String discriminatorProperty : discriminatorProperties) {
				GmProperty gmProperty = entityTypeOracle.getProperty(discriminatorProperty).asGmProperty();
				properties.add(gmProperty);
			}

			CompositeDiscriminator result = newEntity(CompositeDiscriminator.T);
			result.setProperties(properties);
			return result;

		}
	}

	protected DefaultDelegate defaultDelegate(IncrementalAccess access) {
		DefaultDelegate result = newEntity(DefaultDelegate.T);
		result.setAccess(access);

		return result;
	}

	// ###################################
	// ## . . . . . . Enums . . . . . . ##
	// ###################################

	protected void addEnumConstantMapping(Class<? extends Enum<?>> enumClass, EnumConstantMappingProvider constantMappingProvider) {
		EnumTypeMetaDataEditor enumTypeEditor = editor.onEnumType(enumClass);
		EnumTypeOracle enumTypeOracle = oracleS.getEnumTypeOracle(enumClass);

		for (GmEnumConstant enumConstant : enumTypeOracle.asGmEnumType().getConstants()) {
			enumTypeEditor.addConstantMetaData(enumConstant.getName(), constantMappingProvider.provideAssignmentFor(enumConstant));
		}
	}

	protected <T extends MetaData> T priority(T md, int priority) {
		md.setConflictPriority((double) priority);
		return md;
	}

	protected <T extends MetaData> T useCase(T md, String useCase) {
		UseCaseSelector selector = newEntity(UseCaseSelector.T);
		selector.setUseCase(useCase);

		md.setSelector(selector);
		return md;
	}

	protected <T extends MetaData> T important(T md) {
		if (md instanceof PropertyMetaData) {
			((PropertyMetaData) md).setImportant(true);

		} else if (md instanceof EntityTypeMetaData) {
			((EntityTypeMetaData) md).setImportant(true);
		}

		return md;
	}

	protected MoodAccess newAccess(String name) {
		return newAccess(name, MoodAccess.T);
	}

	protected <T extends IncrementalAccess> T newAccess(String name, EntityType<T> entityType) {
		T result = entityType.createPlain();
		result.setExternalId(name);

		return result;
	}

	protected final boolean isAccessA(EntityType<?> entityType) {
		return EntityA.T.isAssignableFrom(entityType);
	}

	protected final boolean isAccessB(EntityType<?> entityType) {
		return StandardIdentifiableB.T.isAssignableFrom(entityType);
	}

	protected final boolean isSmartAccess(EntityType<?> entityType) {
		return SmartGenericEntity.T.isAssignableFrom(entityType);
	}

	protected boolean isShared(EntityType<?> entityType) {
		return SharedEntity.T.isAssignableFrom(entityType);
	}

	/** This exists cause then it's easier to copy new code to QueryLabs, where it is being created on a session ;) */
	protected <T extends GenericEntity> T newEntity(EntityType<T> entityType) {
		return entityType.create();
	}

}
