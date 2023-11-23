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

import static com.braintribe.model.generic.builder.i18n.I18nBuilder.localizedString;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.entityType;
import static com.braintribe.model.processing.test.itw.tools.MetaModelItwTools.addProperty;
import static com.braintribe.model.processing.test.itw.tools.MetaModelItwTools.enumType;
import static com.braintribe.model.processing.test.itw.tools.MetaModelItwTools.newGmEntityType;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Aliases;
import com.braintribe.model.generic.annotation.meta.Bidirectional;
import com.braintribe.model.generic.annotation.meta.Color;
import com.braintribe.model.generic.annotation.meta.CompoundUnique;
import com.braintribe.model.generic.annotation.meta.CompoundUniques;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Descriptions;
import com.braintribe.model.generic.annotation.meta.Emphasized;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Names;
import com.braintribe.model.generic.annotation.meta.NonDeletable;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesis;
import com.braintribe.model.processing.test.itw.tools.MetaModelItwTools;
import com.braintribe.utils.i18n.I18nTools;

/**
 * Tests that annotations are woven properly, i.e. we weave interfaces and using Java reflection check the correct annotations are in place.
 */
public class AnnotationsItwTests extends ImportantItwTestSuperType {

	GenericModelTypeSynthesis gmts = GenericModelTypeSynthesis.standardInstance();

	@Test
	public void entityWithSelectiveInformation() {
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasSelectiveInformation", ge);
		gmType.getMetaData().add(selectiveInformation("md.selectiveInfo", "Selective INFO"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert annotations
		SelectiveInformation declaredAnnotation = et.getJavaType().getDeclaredAnnotation(SelectiveInformation.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.selectiveInfo");
		assertThat(declaredAnnotation.value()).isEqualTo("Selective INFO");
	}

	private com.braintribe.model.meta.data.display.SelectiveInformation selectiveInformation(String globalId, String value) {
		com.braintribe.model.meta.data.display.SelectiveInformation result = com.braintribe.model.meta.data.display.SelectiveInformation.T.create();
		result.setGlobalId(globalId);
		result.setTemplate(I18nTools.createLs(value));
		return result;
	}

	@Test
	public void entityWithBidiProperty() throws Exception {
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasBidiProperty", ge);
		GmProperty bestFriendProperty = addProperty(gmType, "bestFriend", gmType);

		bestFriendProperty.getMetaData().add(bidiProperty("md.bidi", bestFriendProperty));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert annotations
		Bidirectional declaredAnnotation = getAnno(et, "getBestFriend", Bidirectional.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.bidi");
		assertThat(declaredAnnotation.property()).isEqualTo("bestFriend");
		assertThat(declaredAnnotation.type()).isNotNull();
		assertThat(declaredAnnotation.type().getName()).isEqualTo(gmType.getTypeSignature());
	}

	private com.braintribe.model.meta.data.constraint.Bidirectional bidiProperty(String globalId, GmProperty linkedProperty) {
		com.braintribe.model.meta.data.constraint.Bidirectional result = com.braintribe.model.meta.data.constraint.Bidirectional.T.create(globalId);
		result.setLinkedProperty(linkedProperty);

		return result;
	}

	@Test
	public void entityWithCompoundUnique() {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasCompoundUnique", ge);
		gmType.getMetaData().add(compoundUnique("md.compoundUnique", "prop1", "prop2"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert annotations
		CompoundUnique declaredAnnotation = et.getJavaType().getDeclaredAnnotation(CompoundUnique.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.compoundUnique");
		assertThat(declaredAnnotation.value()).containsExactly("prop1", "prop2");
	}

	@Test
	public void entityWithCompoundUniques() {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasCompoundUniques", ge);
		gmType.getMetaData().add(compoundUnique("md.compoundUniques1", "prop1", "prop2"));
		gmType.getMetaData().add(compoundUnique("md.compoundUniques2", "propA", "propB"));

		// Run GMTS
		EntityType<GenericEntity> et = gmts.ensureEntityType(gmType);

		// Assert annotations
		CompoundUniques declaredAnnotation = et.getJavaType().getDeclaredAnnotation(CompoundUniques.class);

		assertThat(declaredAnnotation).isNotNull();

		Map<String, String[]> globalIdToPropertyNames = Stream.of(declaredAnnotation.value()) //
				.collect(Collectors.toMap( //
						com.braintribe.model.generic.annotation.meta.CompoundUnique::globalId, //
						com.braintribe.model.generic.annotation.meta.CompoundUnique::value) //
				);

		assertThat(globalIdToPropertyNames).hasSize(2).containsKeys("md.compoundUniques1", "md.compoundUniques2");
		assertThat(globalIdToPropertyNames.get("md.compoundUniques1")).containsExactly("prop1", "prop2");
		assertThat(globalIdToPropertyNames.get("md.compoundUniques2")).containsExactly("propA", "propB");
	}

	private static com.braintribe.model.meta.data.constraint.CompoundUnique compoundUnique(String globalId, String... propertyNames) {
		com.braintribe.model.meta.data.constraint.CompoundUnique result = com.braintribe.model.meta.data.constraint.CompoundUnique.T.create(globalId);
		result.setUniqueProperties(asSet(propertyNames));
		return result;
	}

	//
	// Mapping
	//

	@Test
	public void entityWithAliases() {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasAliases", ge);
		gmType.getMetaData().add(alias("md.alias1", "a1"));
		gmType.getMetaData().add(alias("md.alias2", "a2"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert annotations
		Aliases declaredAnnotation = et.getJavaType().getDeclaredAnnotation(Aliases.class);

		assertThat(declaredAnnotation).isNotNull();

		Map<String, String> globalIdToName = Stream.of(declaredAnnotation.value()) //
				.collect(Collectors.toMap( //
						Alias::globalId, //
						Alias::value) //
				);

		assertThat(globalIdToName).hasSize(2).containsKeys("md.alias1", "md.alias2");
		assertThat(globalIdToName.get("md.alias1")).isEqualTo("a1");
		assertThat(globalIdToName.get("md.alias2")).isEqualTo("a2");
	}

	private static com.braintribe.model.meta.data.mapping.Alias alias(String globalId, String name) {
		com.braintribe.model.meta.data.mapping.Alias result = com.braintribe.model.meta.data.mapping.Alias.T.create(globalId);
		result.setName(name);
		return result;
	}

	@Test
	public void entityWithPositionalArgs() {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasPositionalArgs", ge);
		gmType.getMetaData().add(positionalArgs("md.posArgs", "p1", "p2"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		PositionalArguments declaredAnnotation = et.getJavaType().getDeclaredAnnotation(PositionalArguments.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.posArgs");
		assertThat(declaredAnnotation.value()).containsExactly("p1", "p2");
	}

	private com.braintribe.model.meta.data.mapping.PositionalArguments positionalArgs(String globalId, String... properties) {
		com.braintribe.model.meta.data.mapping.PositionalArguments result = com.braintribe.model.meta.data.mapping.PositionalArguments.T.create();
		result.setGlobalId(globalId);
		result.setProperties(asList(properties));

		return result;
	}

	//
	// Prompt
	//

	@Test
	public void entityWithName() throws Exception {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasName", ge);

		GmProperty nameProp = addProperty(gmType, "propertyWithName", ge);
		nameProp.getMetaData().add(nameMd("md.name", "default", "default name"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		Name declaredAnnotation = getAnno(et, "getPropertyWithName", Name.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.name");
		assertThat(declaredAnnotation.locale()).isEqualTo(LocalizedString.LOCALE_DEFAULT);
		assertThat(declaredAnnotation.value()).isEqualTo("default name");
	}

	@Test
	public void entityWithNames() throws Exception {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasNames", ge);

		GmProperty namesProp = addProperty(gmType, "propertyWithNames", ge);
		namesProp.getMetaData().add(nameMd("md.names", "default", "default name", "de", "Der Name"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		Names declaredAnnotation = getAnno(et, "getPropertyWithNames", Names.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.value()).hasSize(2);

		Stream.of(declaredAnnotation.value()) //
				.map(com.braintribe.model.generic.annotation.meta.Name::globalId) //
				.forEach(gid -> assertThat(gid).isEqualTo("md.names"));

		Map<String, String> localizedValues = Stream.of(declaredAnnotation.value()) //
				.collect(Collectors.toMap( //
						com.braintribe.model.generic.annotation.meta.Name::locale, //
						com.braintribe.model.generic.annotation.meta.Name::value) //
				);

		assertThat(localizedValues) //
				.containsEntry("default", "default name") //
				.containsEntry("de", "Der Name");
	}

	private com.braintribe.model.meta.data.prompt.Name nameMd(String globalId, String... localizedValues) {
		com.braintribe.model.meta.data.prompt.Name result = com.braintribe.model.meta.data.prompt.Name.T.create(globalId);
		result.setName(localizedString(localizedValues));

		return result;
	}

	@Test
	public void entityWithDescriptions() throws Exception {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasDescriptions", ge);

		GmProperty namesProp = addProperty(gmType, "propertyWithDescriptions", ge);
		namesProp.getMetaData().add(descriptionMd("md.descriptions", "default", "default description", "de", "Unfug"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		Descriptions declaredAnnotation = getAnno(et, "getPropertyWithDescriptions", Descriptions.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.value()).hasSize(2);

		Stream.of(declaredAnnotation.value())//
				.map(Description::globalId)//
				.forEach(gid -> assertThat(gid).isEqualTo("md.descriptions"));

		Map<String, String> localizedValues = Stream.of(declaredAnnotation.value())//
				.collect(Collectors.toMap(Description::locale, Description::value));

		assertThat(localizedValues) //
				.containsEntry("default", "default description") //
				.containsEntry("de", "Unfug");
	}

	private com.braintribe.model.meta.data.prompt.Description descriptionMd(String globalId, String... localizedValues) {
		com.braintribe.model.meta.data.prompt.Description result = com.braintribe.model.meta.data.prompt.Description.T.create(globalId);
		result.setDescription(localizedString(localizedValues));

		return result;
	}

	@Test
	public void nonDeletableEntity() {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.NonDeletable", ge);

		gmType.getMetaData().add(com.braintribe.model.meta.data.constraint.NonDeletable.T.create("md.nonDeletable"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		NonDeletable declaredAnnotation = et.getJavaType().getDeclaredAnnotation(NonDeletable.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.nonDeletable");
	}

	@Test
	public void entityWithEmphasized() throws Exception {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasEmphasized", ge);

		GmProperty nameProp = addProperty(gmType, "emphasizedProperty", ge);
		nameProp.getMetaData().add(com.braintribe.model.meta.data.display.Emphasized.T.create("md.emphasized"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		Emphasized declaredAnnotation = getAnno(et, "getEmphasizedProperty", Emphasized.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.emphasized");
	}

	@Test
	public void entityWithPrioritized() throws Exception {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasPrioritized", ge);

		GmProperty nameProp = addProperty(gmType, "prioritizedProperty", ge);
		nameProp.getMetaData().add(priorityMd("md.priority", 666.666d));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		Priority declaredAnnotation = getAnno(et, "getPrioritizedProperty", Priority.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.priority");
		assertThat(declaredAnnotation.value()).isEqualTo(666.666d);
	}

	private com.braintribe.model.meta.data.prompt.Priority priorityMd(String globalId, double priority) {
		com.braintribe.model.meta.data.prompt.Priority result = com.braintribe.model.meta.data.prompt.Priority.T.create(globalId);
		result.setPriority(priority);
		return result;
	}

	@Test
	public void entityWithColored() throws Exception {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasColored", ge);

		GmProperty nameProp = addProperty(gmType, "coloredProperty", ge);
		nameProp.getMetaData().add(colorMd("md.color", "#0F0"));

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		Color declaredAnnotation = getAnno(et, "getColoredProperty", Color.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.color");
		assertThat(declaredAnnotation.value()).isEqualTo("#0F0");
	}

	private com.braintribe.model.meta.data.display.Color colorMd(String globalId, String code) {
		com.braintribe.model.meta.data.display.Color result = com.braintribe.model.meta.data.display.Color.T.create(globalId);
		result.setCode(code);
		return result;
	}

	@Test
	public void entityWithDeprecated() throws Exception {
		// Build model
		GmEntityType ge = geGmEntityType();

		GmEntityType gmType = newGmEntityType("itw.test.HasDeprecated", ge);

		GmProperty nameProp = addProperty(gmType, "deprecatedProperty", ge);
		nameProp.getMetaData().add(com.braintribe.model.meta.data.prompt.Deprecated.T.create());

		// Run GMTS
		EntityType<?> et = gmts.ensureEntityType(gmType);

		// Assert name annotations
		Deprecated declaredAnnotation = getAnno(et, "getDeprecatedProperty", Deprecated.class);

		assertThat(declaredAnnotation).isNotNull();
	}

	// ###############################################
	// ## . . . . . . . . . Enum . . . . . . . . . .##
	// ###############################################

	@Test
	public void enumWithName() {
		// Build model
		GmEnumType gmType = enumType("itw.test.TestEnum_WithAnnotation");
		MetaModelItwTools.addConstant(gmType, "constant1");

		// MD on enum itself
		gmType.getMetaData().add(nameMd("md.name", "default", "default name"));

		// Run GMTS
		EnumType et = (EnumType) gmts.ensureType(gmType);

		// Assert name annotations
		Name declaredAnnotation = et.getJavaType().getDeclaredAnnotation(Name.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.name");
		assertThat(declaredAnnotation.locale()).isEqualTo(LocalizedString.LOCALE_DEFAULT);
		assertThat(declaredAnnotation.value()).isEqualTo("default name");
	}

	@Test
	public void enumConstantWithName() throws Exception {
		// Build model
		GmEnumType gmType = enumType("itw.test.TestEnum_ConstantWithAnnotation");
		GmEnumConstant const1 = MetaModelItwTools.addConstant(gmType, "constant1");

		// MD on constant
		const1.getMetaData().add(nameMd("md.name", "default", "default name"));

		// Run GMTS
		EnumType et = (EnumType) gmts.ensureType(gmType);

		// Assert name annotations
		Name declaredAnnotation = getAnno(et, "constant1", Name.class);

		assertThat(declaredAnnotation).isNotNull();
		assertThat(declaredAnnotation.globalId()).isEqualTo("md.name");
		assertThat(declaredAnnotation.locale()).isEqualTo(LocalizedString.LOCALE_DEFAULT);
		assertThat(declaredAnnotation.value()).isEqualTo("default name");
	}

	private <A extends Annotation> A getAnno(EntityType<?> et, String getterName, Class<A> annoClass) throws Exception {
		return et.getJavaType().getDeclaredMethod(getterName).getDeclaredAnnotation(annoClass);
	}

	private <A extends Annotation> A getAnno(EnumType et, String constantName, Class<A> annoClass) throws Exception {
		return et.getJavaType().getField(constantName).getDeclaredAnnotation(annoClass);
	}

	private GmEntityType geGmEntityType() {
		GmEntityType ge = entityType(GenericEntity.T.getTypeSignature());
		ge.setIsAbstract(true);
		return ge;
	}

}
