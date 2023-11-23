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
package com.braintribe.model.io.metamodel.testbase;

import static com.braintribe.model.generic.builder.i18n.I18nBuilder.localizedString;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.property;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.typeRestriction;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Collections;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.base.MdaAnalysisTools;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmIntegerType;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModels;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.CompoundUnique;
import com.braintribe.model.meta.data.constraint.Limit;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.display.SelectiveInformation;
import com.braintribe.model.meta.data.mapping.Alias;
import com.braintribe.model.meta.data.mapping.PositionalArguments;
import com.braintribe.model.meta.data.prompt.Deprecated;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.i18n.I18nTools;

public class MetaModelBuilder {

	public static String COMMON_PACKAGE = "com.braintribe.model.io.metamodel.testclass";

	public static String PERSON_TYPE_SIGNATURE = COMMON_PACKAGE + ".Person";
	public static String SPECIAL_PERSON_TYPE_SIGNATURE = COMMON_PACKAGE + ".SpecialPerson";
	public static String INTERFACE_FROM_OTHER_PACKAGE_SIGNATURE = COMMON_PACKAGE + ".subpackage.ExtraSpecialPerson";
	public static String ABSTRACT_INTERFACE = COMMON_PACKAGE + ".AbstractInterface";
	public static String INTERFACE_WITHOUT_PACKAGE = "InterfaceWithoutPackage";
	public static String WITH_BASE_TYPE = COMMON_PACKAGE + ".WithBaseType";
	public static String WITH_INITIALIZER = COMMON_PACKAGE + ".WithInitializer";
	public static String WITH_EVALUATES_TO = COMMON_PACKAGE + ".WithEvaluatesTo";
	public static String WITH_KEYWORD_PROPS = COMMON_PACKAGE + ".WithKeywordProps";
	public static String WITH_TYPE_RESTRICTION = COMMON_PACKAGE + ".WithTypeRestriction";
	public static String WITH_MIN_MAX_PROPERTY = COMMON_PACKAGE + ".WithMinMaxProperty";
	public static String WITH_SELECTIVE_INFORMATION = COMMON_PACKAGE + ".WithSelectiveInformation";
	public static String WITH_COMPOUND_UNIQUE = COMMON_PACKAGE + ".WithCompoundUnique";
	public static String WITH_COMPOUND_UNIQUES = COMMON_PACKAGE + ".WithCompoundUniqueS";

	// Mapping
	public static String WITH_POSITIONAL_ARGUMENTS = COMMON_PACKAGE + ".WithPositionalArguments";
	public static String WITH_ALIAS = COMMON_PACKAGE + ".WithAlias";
	public static String WITH_ALIASES = COMMON_PACKAGE + ".WithAliases";
	public static String WITH_NATURAL_GID_ALIASES = COMMON_PACKAGE + ".WithNaturalGidAliases";

	// Prompt
	public static String DEPRECATED_ENTITY = COMMON_PACKAGE + ".DeprectedEntity";
	public static String WITH_NAME = COMMON_PACKAGE + ".WithName";
	public static String WITH_NAMES = COMMON_PACKAGE + ".WithNames";
	public static String WITH_DESCRIPTIONS = COMMON_PACKAGE + ".WithDescriptions";

	// Enum
	public static String ENUM_TYPE_SIGNATURE = COMMON_PACKAGE + ".TheEnum";
	public static String ENUM_WITHOUT_PACKAGE = "EnumWithoutPackage";

	public static String ENUM_WITH_MD_ANNOTATIONS = COMMON_PACKAGE + ".EnumWithMdAnnotations";

	public static GmSimpleType intType;
	public static GmSimpleType longType;
	public static GmSimpleType stringType;
	public static GmBaseType baseType;

	static {
		initSimpleTypesAndBaseType();
	}

	private static void initSimpleTypesAndBaseType() {
		intType = GmIntegerType.T.create();
		longType = GmLongType.T.create();
		stringType = GmStringType.T.create();
		baseType = GmBaseType.T.create();
	}

	public GmMetaModel buildMetaModel() {

		GmMetaModel gmMetaModel = new NewMetaModelGeneration().buildMetaModel("test:SourceWriterModel", Collections.emptyList());

		GmEnumType normalEnum = newEnum(ENUM_TYPE_SIGNATURE).setConstants("RED", "GREEN", "BLUE").create();
		GmEnumType enumWithDescription = newEnum(ENUM_WITH_MD_ANNOTATIONS) //
				.setConstants("CONSTANT_1") //
				.addConstantMd("CONSTANT_1", minMd(0), maxMd(100)) //
				.addMd(descriptionMd("gid1", "default", "default description", "de", "Unfug")) //
				.create();
		GmEnumType enumWithoutPackage = newEnum(ENUM_WITHOUT_PACKAGE).setConstants("NO_PACKAGE_1", "NO_PACKAGE_2", "NO_PACKAGE_3").create();

		GmEntityType genericEntity = newEntity(GenericEntity.class.getName()).create();
		GmEntityType person = newEntity(PERSON_TYPE_SIGNATURE).setProperties("name", stringType, "age", intType).create();
		GmEntityType extendingInterface = newEntity(SPECIAL_PERSON_TYPE_SIGNATURE).addAncestor(person)
				.setProperties("name", stringType, "age", intType, "title", stringType, "slave", person, "longAge", longType).create();
		GmEntityType interfaceFromOtherPackage = newEntity(INTERFACE_FROM_OTHER_PACKAGE_SIGNATURE).addAncestor(person).create();
		GmEntityType abstractInterface = newEntity(ABSTRACT_INTERFACE).setIsAbstract().addAncestor(genericEntity).create();
		GmEntityType interfaceWithoutPackage = newEntity(INTERFACE_WITHOUT_PACKAGE).addAncestor(genericEntity).create();
		GmEntityType withBaseType = newEntity(WITH_BASE_TYPE).setProperties("base", baseType).create();
		GmEntityType withInitializer = newEntity(WITH_INITIALIZER) //
				.setProperties("name", stringType, "color", normalEnum, "object", baseType)//
				.setInitializer("name", "joe") //
				.setInitializer("object", EnumReference.create(ENUM_TYPE_SIGNATURE, "GREEN")) //
				.setInitializer("color", EnumReference.create(ENUM_TYPE_SIGNATURE, "GREEN")) //
				.create();
		GmEntityType withEvaluatesTo = newEntity(WITH_EVALUATES_TO).setEvaluatesTo(stringType).create();
		GmEntityType withKeywordProps = newEntity(WITH_KEYWORD_PROPS).setProperties("for", stringType, "for_", stringType, "foobar_", stringType)
				.create();
		GmEntityType withTypeRestriction = newEntity(WITH_TYPE_RESTRICTION)
				.addProperty(property(null, "friend", genericEntity, typeRestriction(asList(person, withInitializer), asList(), true, false)))
				.create();
		GmEntityType withMinMaxProperty = newEntity(WITH_MIN_MAX_PROPERTY) //
				.setProperties("efficiency", intType) //
				.addPropertyMd("efficiency", minMd(0), maxMd(100)) //
				.create();
		GmEntityType withSelectiveInformation = newEntity(WITH_SELECTIVE_INFORMATION).addMd(selectiveInformation("Selective INFO")).create();
		GmEntityType withCompoundUnique = newEntity(WITH_COMPOUND_UNIQUE).addMd(compoundUnique(null, "prop1", "prop2")).create();
		GmEntityType withCompoundUniques = newEntity(WITH_COMPOUND_UNIQUES)
				.addMd(compoundUnique("gid1", "prop1", "prop2"), compoundUnique("gid2", "propA", "propB")).create();

		// Mapping
		GmEntityType withPositionalArguments = newEntity(WITH_POSITIONAL_ARGUMENTS).addMd(positionalArgs("a1", "a2")).create();
		GmEntityType withAlias = newEntity(WITH_ALIAS).addMd(aliasMd(null, "alias")).create();
		GmEntityType withAliases = newEntity(WITH_ALIASES).addMd(aliasMd(null, "a"), aliasMd("alias2", "ALIAS")).create();
		GmEntityType withNaturalGidAliases = newEntity(WITH_NATURAL_GID_ALIASES) //
				.addMd( //
						aliasMd(naturalGid(Alias.T, GmModels.typeGlobalId(WITH_NATURAL_GID_ALIASES), "_0"), "a"), //
						aliasMd(naturalGid(Alias.T, GmModels.typeGlobalId(WITH_NATURAL_GID_ALIASES), "_1"), "ALIAS") //
				).create();

		// Prompt
		GmEntityType deprectedEntity = newEntity(DEPRECATED_ENTITY).addMd(Deprecated.T.create("ignoredGlobalId")).create();
		GmEntityType withName = newEntity(WITH_NAME) //
				.addMd(nameMd(naturalGid(Name.T, GmModels.typeGlobalId(WITH_NAME), ""), "default", "default name")) //
				.create();
		GmEntityType withNames = newEntity(WITH_NAMES).addMd(nameMd("gid1", "default", "default name", "de", "Der Name")).create();
		GmEntityType withDescriptions = newEntity(WITH_DESCRIPTIONS) //
				.addMd(descriptionMd("gid1", "default", "default description", "de", "Unfug")).create();

		// @formatter:off
		gmMetaModel.getTypes().addAll(asSet(
				// entity types
				person,
				extendingInterface,
				interfaceFromOtherPackage,
				abstractInterface,
				interfaceWithoutPackage,
				withBaseType,
				withInitializer,
				withEvaluatesTo,
				withKeywordProps,
				withTypeRestriction,
				withMinMaxProperty,
				withSelectiveInformation,
				withCompoundUnique,
				withCompoundUniques,

				// mapping
				withPositionalArguments,
				withAlias,
				withAliases,
				withNaturalGidAliases,

				// prompt
				deprectedEntity,
				withName,
				withNames,
				withDescriptions,

				// enums
				normalEnum,
				enumWithDescription,
				enumWithoutPackage
		));
		// @formatter:on

		return gmMetaModel;
	}

	public static String naturalGid(EntityType<? extends MetaData> mdType, String ownerGlobalId, String globalIdSuffix) {
		return MdaAnalysisTools.naturalGlobalId(mdType, ownerGlobalId, globalIdSuffix);
	}

	private EntityTypeBuilder newEntity(String typeSignature) {
		return new EntityTypeBuilder(typeSignature);
	}

	private EnumTypeBuilder newEnum(String typeSignature) {
		return new EnumTypeBuilder(typeSignature);
	}

	private SelectiveInformation selectiveInformation(String value) {
		SelectiveInformation result = SelectiveInformation.T.create();
		result.setTemplate(I18nTools.createLs(value));
		return result;
	}

	private static CompoundUnique compoundUnique(String globalId, String... propertyNames) {
		CompoundUnique result = CompoundUnique.T.create(globalId);
		result.setUniqueProperties(asSet(propertyNames));
		return result;
	}

	private PositionalArguments positionalArgs(String... props) {
		PositionalArguments result = PositionalArguments.T.create();
		result.setProperties(asList(props));

		return result;
	}

	private Alias aliasMd(String globalId, String name) {
		Alias result = Alias.T.create(globalId);
		result.setName(name);

		return result;
	}

	private Name nameMd(String globalId, String... localizedValues) {
		Name result = Name.T.create(globalId);
		result.setName(localizedString(localizedValues));

		return result;
	}

	public static Min minMd(int limit) {
		return limitMd(Min.T, limit);
	}

	public static Max maxMd(int limit) {
		return limitMd(Max.T, limit);
	}

	public static <L extends Limit> L limitMd(EntityType<L> et, int limit) {
		L result = et.create();
		result.setLimit(limit);

		return result;
	}

	private Description descriptionMd(String globalId, String... localizedValues) {
		Description result = Description.T.create(globalId);
		result.setDescription(localizedString(localizedValues));

		return result;
	}

}
