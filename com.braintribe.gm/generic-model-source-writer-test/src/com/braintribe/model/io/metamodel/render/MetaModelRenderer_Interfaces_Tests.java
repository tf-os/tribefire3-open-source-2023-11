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
package com.braintribe.model.io.metamodel.render;

import org.junit.Test;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.GlobalId;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.TypeRestriction;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.CompoundUnique;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.io.metamodel.testbase.MetaModelBuilder;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;

public class MetaModelRenderer_Interfaces_Tests extends MetaModelRendererTestBase {

	@Test
	public void testGeneratesCodeForSimpleInterface() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.PERSON_TYPE_SIGNATURE);
		currentCode = renderEntityType(gmEntityType);
		assertContainsSubstring("package " + MetaModelBuilder.COMMON_PACKAGE);
		assertHasNoAnnotation(GlobalId.class);
		assertHasProperty("Name", "String");
		assertHasProperty("Age", "Integer");
		assertContainsSubstring("EntityType<Person> T = EntityTypes.T(Person.class);");

		assertNotContains("extends");
	}

	@Test
	public void testGeneratesCodeForSimpleInterface_CustomGlobalId() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.PERSON_TYPE_SIGNATURE);

		gmEntityType.setGlobalId("entityGlobalid");
		gmEntityType.getProperties().forEach(p -> p.setGlobalId("property:" + p.getName()));

		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(GlobalId.class);
		assertHasProperty("Name", "String");
		assertHasProperty("Age", "Integer");
		assertContainsSubstring("package " + MetaModelBuilder.COMMON_PACKAGE);
		assertContainsSubstring("EntityType<Person> T = EntityTypes.T(Person.class);");

		assertContainsSubstring("@GlobalId(\"" + gmEntityType.getGlobalId() + "\")");
		for (GmProperty p : gmEntityType.getProperties())
			assertContainsSubstring("@GlobalId(\"" + p.getGlobalId() + "\")");

		assertNotContains("extends");
	}

	@Test
	public void testGeneratesCodeForSimpleEnum() throws Exception {
		GmEnumType gmEnumType = getEnumBySignature(MetaModelBuilder.ENUM_TYPE_SIGNATURE);
		currentCode = renderEnumType(gmEnumType);
		assertHasNoAnnotation(GlobalId.class);
		assertContainsSubstrings("RED,", "GREEN,", "BLUE,");
		assertContainsSubstrings("BLUE,\n\t;");
		assertContainsSubstring("package " + MetaModelBuilder.COMMON_PACKAGE);

		assertContainsSubstring(
				"public static final com.braintribe.model.generic.reflection.EnumType T = com.braintribe.model.generic.reflection.EnumTypes.T(TheEnum.class);");
		assertContainsSubstring("public com.braintribe.model.generic.reflection.EnumType type() {");
		assertContainsSubstring("return T;");
	}

	@Test
	public void testGeneratesCodeForSimpleEnum_CustomGlobalId() throws Exception {
		GmEnumType gmEnumType = getEnumBySignature(MetaModelBuilder.ENUM_TYPE_SIGNATURE);

		gmEnumType.setGlobalId("enumGlobalid");
		gmEnumType.getConstants().forEach(c -> c.setGlobalId("constant:" + c.getName()));

		currentCode = renderEnumType(gmEnumType);
		assertHasAnnotation(GlobalId.class);
		assertContainsSubstrings("RED,", "GREEN,", "BLUE,");
		assertContainsSubstring("package " + MetaModelBuilder.COMMON_PACKAGE);

		assertContainsSubstring("@GlobalId(\"" + gmEnumType.getGlobalId() + "\")");
		for (GmEnumConstant c : gmEnumType.getConstants())
			assertContainsSubstring("@GlobalId(\"" + c.getGlobalId() + "\")");

		assertContainsSubstring(
				"public static final com.braintribe.model.generic.reflection.EnumType T = com.braintribe.model.generic.reflection.EnumTypes.T(TheEnum.class);");
		assertContainsSubstring("public com.braintribe.model.generic.reflection.EnumType type() {");
		assertContainsSubstring("return T;");
	}

	@Test
	public void testGeneratesCodeForInterfaceWithSuperInterface() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.SPECIAL_PERSON_TYPE_SIGNATURE);
		currentCode = renderEntityType(gmEntityType);
		assertHasProperty("Title", "String");
		assertHasProperty("Slave", "Person");
		assertHasProperty("LongAge", "Long");
		// Properties inherited from SuperInterface are not rendered
		assertDoesntHaveProperty("Name", "String");
		assertDoesntHaveProperty("Age", "Integer");
		assertContainsSubstring(" extends Person");

	}

	@Test
	public void testGeneratesCodeForInterfaceWithSuperInterfaceInOtherPackage() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.INTERFACE_FROM_OTHER_PACKAGE_SIGNATURE);
		currentCode = renderEntityType(gmEntityType);
		assertContainsSubstring(".subpackage");
		assertContainsSubstring(" extends Person");
	}

	@Test
	public void testAbstractInterfaceHasAnnotation() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.ABSTRACT_INTERFACE);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Abstract.class);
	}

	@Test
	public void testWorksForInterfaceWithoutPackage() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.INTERFACE_WITHOUT_PACKAGE);
		currentCode = renderEntityType(gmEntityType);
		assertNotContains("package");
	}

	@Test
	public void testWorksForEnumWithoutPackage() throws Exception {
		GmEnumType gmEnumType = getEnumBySignature(MetaModelBuilder.ENUM_WITHOUT_PACKAGE);
		currentCode = renderEnumType(gmEnumType);
		assertNotContains("package");
	}

	@Test
	public void testWritesPropertyOfBaseTypeCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_BASE_TYPE);
		currentCode = renderEntityType(gmEntityType);
		assertHasProperty("Base", Object.class.getSimpleName());
	}

	@Test
	public void testWritesInitializedPropertyCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_INITIALIZER);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Initializer.class, "'joe'");
		assertHasAnnotation(Initializer.class, "GREEN");
		assertHasAnnotation(Initializer.class, "enum(" + MetaModelBuilder.ENUM_TYPE_SIGNATURE + ",GREEN)");
	}

	@Test
	public void testWritesEvaluatesToCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_EVALUATES_TO);
		currentCode = renderEntityType(gmEntityType);
		assertContainsSubstring("EvalContext<String> eval(Evaluator<ServiceRequest> evaluator);");
	}

	@Test
	public void testWritesJavaKeywordPropertiesCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_KEYWORD_PROPS);
		currentCode = renderEntityType(gmEntityType);
		assertHasEscapedProperty("For", "String", "for_", "for_");
		assertHasEscapedProperty("For_", "String", "for__", "for_");
		assertHasEscapedProperty("Foobar_", "String", "foobar__", "foobar_");
	}

	@Test
	public void writesTypeRestrictionCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_TYPE_RESTRICTION);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(TypeRestriction.class);
		assertContainsSubstring("@TypeRestriction(value={" + MetaModelBuilder.PERSON_TYPE_SIGNATURE + ".class," + MetaModelBuilder.WITH_INITIALIZER
				+ ".class},key={},allowVd=true,allowKeyVd=false)");
	}

	@Test
	public void testWritesMinMaxCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_MIN_MAX_PROPERTY);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Min.class);
		assertHasAnnotation(Max.class);
		assertContainsSubstring("@Max(exclusive=false, value=\"100\")");
		assertContainsSubstring("@Min(exclusive=false, value=\"0\")");
	}

	@Test
	public void testWritesSelectiveInformationCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_SELECTIVE_INFORMATION);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(SelectiveInformation.class);
		assertContainsSubstring("@SelectiveInformation(value=\"Selective INFO\")");
	}

	@Test
	public void testWritesCompoundUniqueCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_COMPOUND_UNIQUE);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(CompoundUnique.class);
		assertContainsSubstring("@CompoundUnique(value={\"prop1\", \"prop2\"})");
	}

	@Test
	public void testWritesCompoundUniquesCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_COMPOUND_UNIQUES);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(CompoundUnique.class);
		assertContainsSubstring("@CompoundUnique(globalId=\"gid1\", value={\"prop1\", \"prop2\"})");
		assertContainsSubstring("@CompoundUnique(globalId=\"gid2\", value={\"propA\", \"propB\"})");
	}

	@Test
	public void testWritesAliasCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_ALIAS);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Alias.class);
		assertContainsSubstring("@Alias(value=\"alias\")");
	}

	@Test
	public void testWritesAliasesCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_ALIASES);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Alias.class);
		assertContainsSubstring("@Alias(globalId=\"alias2\", value=\"ALIAS\")");
	}

	@Test
	public void testWritesAliasesCorrectly_NaturalGid() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_NATURAL_GID_ALIASES);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Alias.class);
		assertContainsSubstring("@Alias(value=\"ALIAS\")");
	}

	@Test
	public void testWritesPositionalArgumentsCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_POSITIONAL_ARGUMENTS);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(PositionalArguments.class);
		assertContainsSubstring("@PositionalArguments(value={\"a1\", \"a2\"})");
	}

	@Test
	public void testWritesNameCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_NAME);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Name.class);
		// NOTE globalId is omitted as it is the natural one
		assertContainsSubstring("@Name(locale=\"default\", value=\"default name\")");
	}

	@Test
	public void testWritesNamesCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_NAMES);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Name.class);
		assertContainsSubstring("@Name(globalId=\"gid1\", locale=\"de\", value=\"Der Name\")");
		assertContainsSubstring("@Name(globalId=\"gid1\", locale=\"default\", value=\"default name\")");
	}

	@Test
	public void testWritesDescriptionsCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.WITH_DESCRIPTIONS);
		currentCode = renderEntityType(gmEntityType);
		assertHasAnnotation(Description.class);
		assertContainsSubstring("@Description(globalId=\"gid1\", locale=\"de\", value=\"Unfug\")");
		assertContainsSubstring("@Description(globalId=\"gid1\", locale=\"default\", value=\"default description\")");
	}

	@Test
	public void testWritesEnumDescriptionsCorrectly() throws Exception {
		GmEnumType gmEnumType = getEnumBySignature(MetaModelBuilder.ENUM_WITH_MD_ANNOTATIONS);
		currentCode = renderEnumType(gmEnumType);

		// On Enum itself
		assertHasAnnotation(Description.class);
		assertContainsSubstring("@Description(globalId=\"gid1\", locale=\"de\", value=\"Unfug\")");
		assertContainsSubstring("@Description(globalId=\"gid1\", locale=\"default\", value=\"default description\")");

		// On Constant_!
		assertHasAnnotation(Min.class);
		assertHasAnnotation(Max.class);
		assertContainsSubstring("@Max(exclusive=false, value=\"100\")");
		assertContainsSubstring("@Min(exclusive=false, value=\"0\")");
	}

	@Test
	public void testWritesDeprecatedCorrectly() throws Exception {
		GmEntityType gmEntityType = getTypeBySignature(MetaModelBuilder.DEPRECATED_ENTITY);
		currentCode = renderEntityType(gmEntityType);
		assertContainsSubstring("@Deprecated");
		assertNotContains("@Deprecated("); // no attributes like globalId
		assertNotContains("java.lang.Deprecated"); // no import
	}

}
