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
package com.braintribe.model.processing.cmd.context.experts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.attribute.common.UserInfo;
import com.braintribe.common.attribute.common.UserInfoAttribute;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.meta.selector.AccessTypeSelector;
import com.braintribe.model.meta.selector.AccessTypeSignatureSelector;
import com.braintribe.model.meta.selector.AclSelector;
import com.braintribe.model.meta.selector.DeclaredPropertySelector;
import com.braintribe.model.meta.selector.EntitySignatureRegexSelector;
import com.braintribe.model.meta.selector.EntityTypeSelector;
import com.braintribe.model.meta.selector.GmEntityTypeSelector;
import com.braintribe.model.meta.selector.IntegerPropertyDiscriminator;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.NullPropertyDiscriminator;
import com.braintribe.model.meta.selector.PropertyOfSelector;
import com.braintribe.model.meta.selector.PropertyRegexSelector;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.meta.selector.StringRegexPropertyDiscriminator;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.cmd.test.model.AclCmdEntity;
import com.braintribe.model.processing.cmd.test.model.CmdTestModelProvider;
import com.braintribe.model.processing.cmd.test.model.Color;
import com.braintribe.model.processing.cmd.test.model.HardwiredAccess;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.model.ServiceProvider;
import com.braintribe.model.processing.cmd.test.model.Teacher;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextImpl;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmEntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmPropertyAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.cmd.context.experts.AccessSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.AccessTypeSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.AccessTypeSignatureSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.AclSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.DeclaredPropertySelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.EntitySignatureRegexSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.EntityTypeSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.GmEntityTypeSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.IntegerPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.NullPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.PropertyOfSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.PropertyRegexSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.RoleSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.StringRegexPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.UseCaseSelectorExpert;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * 
 */
public class ExpertsTests {

	private SelectorContextImpl sci;
	private ModelOracle modelOracle;
	private static GmMetaModel metaModel = CmdTestModelProvider.raw();
	private EntityTypeOracle personTypeOracle;

	@Before
	public void newContext() {
		modelOracle = new BasicModelOracle(metaModel);
		personTypeOracle = modelOracle.getEntityTypeOracle(Person.T);
		sci = new SelectorContextImpl(modelOracle, null);
	}

	@Test
	public void nullPropertyDiscriminator() {
		NullPropertyDiscriminatorExpert expert = new NullPropertyDiscriminatorExpert();
		NullPropertyDiscriminator selector = NullPropertyDiscriminator.T.create();

		Person person = newEntity(Person.T);
		person.setName("name");

		selector.setDiscriminatorProperty(personTypeOracle.getProperty("name").asGmProperty());
		run(expert, selector, false);
		selector.setInverse(true);
		run(expert, selector, true);

		selector.setInverse(false);

		selector.setDiscriminatorProperty(personTypeOracle.getProperty("color").asGmProperty());
		run(expert, selector, true);

		selector.setInverse(true);
		run(expert, selector, false);
	}

	@Test
	public void stringRegex() {
		StringRegexPropertyDiscriminatorExpert expert = new StringRegexPropertyDiscriminatorExpert();
		StringRegexPropertyDiscriminator selector = StringRegexPropertyDiscriminator.T.create();

		Person person = newEntity(Person.T);
		person.setName("name");

		selector.setDiscriminatorProperty(personTypeOracle.getProperty("name").asGmProperty());

		selector.setDiscriminatorRegex("[a-z]+");
		run(expert, selector, true); // regex does match

		selector.setDiscriminatorRegex("[0-9]+");
		run(expert, selector, false); // regex doesn't match

		selector.setDiscriminatorRegex(".*");
		selector.setDiscriminatorProperty(personTypeOracle.getProperty("color").asGmProperty());
		run(expert, selector, false); // null is false

		person.setColor(Color.RED);
		run(expert, selector, true); // enum is handled correctly
	}

	@Test
	public void integer() {
		IntegerPropertyDiscriminatorExpert expert = new IntegerPropertyDiscriminatorExpert();
		IntegerPropertyDiscriminator selector = IntegerPropertyDiscriminator.T.create();

		Person person = newEntity(Person.T);
		person.setAge(50);

		selector.setDiscriminatorProperty(personTypeOracle.getProperty("age").asGmProperty());

		selector.setDiscriminatorValue(50);
		run(expert, selector, true);

		selector.setDiscriminatorValue(500);
		run(expert, selector, false);
	}

	@Test
	public void role() {
		RoleSelectorExpert expert = new RoleSelectorExpert();
		RoleSelector selector = RoleSelector.T.create();

		Person person = newEntity(Person.T);
		person.setAge(50);

		sci.put(RoleAspect.class, asSet("user", "admin"));

		selector.setRoles(asSet("admin", "developer"));
		run(expert, selector, true);

		selector.setRoles(asSet("gatekeeper"));
		run(expert, selector, false);
	}

	/** Tests selector evaluates to false on non entity */
	@Test
	public void acl() {
		AclSelectorExpert expert = new AclSelectorExpert();
		AclSelector selector = AclSelector.T.create();
		selector.setOperation("write");

		AclCmdEntity person = newEntity(AclCmdEntity.T);
		person.setOwner("AclOwner");

		// false as no UserInfo is given in the context, but some is required
		run(expert, selector, false);

		UserInfo ui = UserInfo.of("AclOwner", null);
		AttributeContexts.push(AttributeContexts.derivePeek().set(UserInfoAttribute.class, ui).build());

		// true as UserInfo if an authorized user
		run(expert, selector, true);
	}

	@Test
	public void acl_NonAclEntity() {
		AclSelectorExpert expert = new AclSelectorExpert();
		AclSelector selector = AclSelector.T.create();
		selector.setOperation("write");

		// false as no contextual entity was found
		run(expert, selector, false);

		Person person = newEntity(Person.T);
		person.setAge(50);

		// true as contextual entity is not a HasAcl
		run(expert, selector, true);
	}

	@Test
	public void useCase() {
		UseCaseSelectorExpert expert = new UseCaseSelectorExpert();
		UseCaseSelector selector = UseCaseSelector.T.create();

		Person person = newEntity(Person.T);
		person.setAge(50);

		sci.put(UseCaseAspect.class, asSet("read", "gui"));

		selector.setUseCase("gui");
		run(expert, selector, true);

		selector.setUseCase("modify");
		run(expert, selector, false);
	}

	@Test
	public void access() {
		AccessSelectorExpert expert = new AccessSelectorExpert();
		AccessSelector selector = AccessSelector.T.create();

		Person person = newEntity(Person.T);
		person.setAge(50);

		// test running with empty context
		run(expert, selector, false);

		sci.put(AccessAspect.class, "accessId");

		selector.setExternalId("accessId");
		run(expert, selector, true);

		selector.setExternalId("accessId2");
		run(expert, selector, false);
	}

	@Test
	public void accessType() {
		AccessTypeSelectorExpert expert = new AccessTypeSelectorExpert();
		AccessTypeSelector selector = AccessTypeSelector.T.create();
		selector.setAccessType(MetaModelBuilder.entityType(HardwiredAccess.T.getTypeSignature()));

		// test running with empty context
		run(expert, selector, false);

		sci.put(AccessTypeAspect.class, HardwiredAccess.T.getTypeSignature());
		run(expert, selector, true);

		sci.put(AccessTypeAspect.class, "HibernateAccess");
		run(expert, selector, false);
	}

	@Test
	public void accessTypeSignature() {
		AccessTypeSignatureSelectorExpert expert = new AccessTypeSignatureSelectorExpert();
		AccessTypeSignatureSelector selector = AccessTypeSignatureSelector.T.create();
		selector.setDenotationTypeSignature(HardwiredAccess.T.getTypeSignature());

		// test running with empty context
		run(expert, selector, false);

		sci.put(AccessTypeAspect.class, HardwiredAccess.T.getTypeSignature());
		run(expert, selector, true);

		sci.put(AccessTypeAspect.class, "HibernateAccess");
		run(expert, selector, false);
	}

	@Test
	public void gmEntityType() {
		GmEntityTypeSelectorExpert expert = new GmEntityTypeSelectorExpert();
		GmEntityTypeSelector selector = GmEntityTypeSelector.T.create();

		Person person = newEntity(Person.T);
		person.setAge(50);

		selector.setGmEntityType(personTypeOracle.asGmEntityType());
		run(expert, selector, true);

		selector.setGmEntityType(modelOracle.getEntityTypeOracle(ServiceProvider.T).asGmEntityType());
		run(expert, selector, false);
	}

	@Test
	public void entitySignatureRegex() {
		EntitySignatureRegexSelectorExpert expert = new EntitySignatureRegexSelectorExpert();
		EntitySignatureRegexSelector selector = EntitySignatureRegexSelector.T.create();

		Person person = newEntity(Person.T);
		person.setAge(50);

		selector.setRegex(".*er.*");
		run(expert, selector, true);

		selector.setRegex(".*er");
		run(expert, selector, false);
	}

	@Test
	public void propertyRegex() {
		PropertyRegexSelectorExpert expert = new PropertyRegexSelectorExpert();
		PropertyRegexSelector selector = PropertyRegexSelector.T.create();

		sci.put(GmPropertyAspect.class, personTypeOracle.getProperty("name").asGmProperty());

		selector.setRegex(".*am.*");
		run(expert, selector, true);

		selector.setRegex(".*Person#na.*");
		run(expert, selector, false);

		selector.setUseFullyQualifiedName(true);
		run(expert, selector, true);
	}

	@Test
	public void propertyOf() {
		PropertyOfSelectorExpert expert = new PropertyOfSelectorExpert();
		PropertyOfSelector selector = PropertyOfSelector.T.create();

		sci.put(GmPropertyAspect.class, modelOracle.getEntityTypeOracle(Teacher.T).getProperty("schoolName").asGmProperty());

		selector.setEntityType(modelOracle.getEntityTypeOracle(Person.T).asGmEntityType());
		run(expert, selector, false); // schoolName is not a property of Person

		selector.setEntityType(modelOracle.getEntityTypeOracle(Teacher.T).asGmEntityType());
		run(expert, selector, true); // schoolName is a property of Teacher

	}

	@Test
	public void propertyOf_OnlyDeclared() {
		PropertyOfSelectorExpert expert = new PropertyOfSelectorExpert();
		PropertyOfSelector selector = PropertyOfSelector.T.create();

		sci.put(GmPropertyAspect.class, personTypeOracle.getProperty("name").asGmProperty());

		selector.setEntityType(modelOracle.getEntityTypeOracle(Teacher.T).asGmEntityType());
		run(expert, selector, true); // name is a property of Teacher

		selector.setOnlyDeclared(true);
		run(expert, selector, false); // name is not a declared property of Teacher

		selector.setEntityType(modelOracle.getEntityTypeOracle(Person.T).asGmEntityType());
		run(expert, selector, true); // name is a declared property of Person
	}

	@Test
	public void declaredProperty() {
		DeclaredPropertySelectorExpert expert = new DeclaredPropertySelectorExpert();
		DeclaredPropertySelector selector = DeclaredPropertySelector.T.create();

		sci.put(GmEntityTypeAspect.class, personTypeOracle.asGmEntityType());

		sci.put(GmPropertyAspect.class, personTypeOracle.getProperty("name").asGmProperty());
		run(expert, selector, true); // name is declared for Person

		sci.put(GmPropertyAspect.class, personTypeOracle.getProperty("id").asGmProperty());
		run(expert, selector, false); // id is NOT declared for Person, but inherited
	}

	@Test
	public void typeCondition() {
		EntityTypeSelectorExpert expert = new EntityTypeSelectorExpert();
		EntityTypeSelector selector = EntityTypeSelector.T.create();

		Person person = newEntity(Person.T);
		person.setAge(50);

		selector.setTypeCondition(TypeConditions.isType(Person.T));
		run(expert, selector, true);

		selector.setTypeCondition(TypeConditions.isType(ServiceProvider.T));
		run(expert, selector, false);
	}

	private Set<String> asSet(String... strings) {
		return new HashSet<String>(Arrays.asList(strings));
	}

	private <T extends GenericEntity> T newEntity(EntityType<T> mdEt) {
		T person = mdEt.create();
		entity(person);

		return person;
	}

	private <T extends MetaDataSelector> void run(SelectorExpert<T> expert, T selector, boolean expected) {
		Assert.assertEquals("Selector evaluated incorrectly!", expected, matches(expert, selector));
	}

	private <T extends MetaDataSelector> boolean matches(SelectorExpert<T> expert, T selector) {
		try {
			return expert.matches(selector, sci);

		} catch (Exception e) {
			throw new RuntimeException("Expert for selector failed: " + selector, e);
		}
	}

	private void entity(GenericEntity ge) {
		sci.put(EntityAspect.class, ge);

		EntityType<?> et = ge.entityType();
		GmEntityType gmEntityType = modelOracle.getEntityTypeOracle(et).asGmEntityType();

		sci.put(EntityTypeAspect.class, et);
		sci.put(GmEntityTypeAspect.class, gmEntityType);
	}

}
