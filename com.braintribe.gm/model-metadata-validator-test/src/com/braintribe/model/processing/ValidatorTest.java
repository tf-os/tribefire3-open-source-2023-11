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
package com.braintribe.model.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.basic.IsType;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.impl.ValidatorImpl;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.test.Base;
import com.braintribe.model.processing.test.SelfContaining;
import com.braintribe.model.processing.validation.expert.property.DeprecatedAnnotationExpert;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CommonTools;

public class ValidatorTest extends AbstractTest {
	private static final String ROOT_ELEMENT = "(Root Element)";
	private static final GmMetaModel META_MODEL = GMF.getTypeReflection().getModel("com.braintribe.gm:model-metadata-validation-test-model")
			.getMetaModel();

	@Test
	public void testDeprecated() throws Exception {
		ValidationExpertRegistry validationExperts = ValidationExpertRegistry.createDefault();
		CmdResolverImpl cmdResolver = new CmdResolverImpl(new BasicModelOracle(META_MODEL));
		Validator validator = new ValidatorImpl(cmdResolver, validationExperts);
		
		validationExperts.addPropertyExpert(new DeprecatedAnnotationExpert());
		
		Base create = Base.T.create();
		SelfContaining selfContaining = SelfContaining.T.create();
		selfContaining.setName("child7");
		create.setChild(selfContaining);
		
		testValidation(validator, Arrays.asList(), create);
		
		selfContaining.setPoints(0f);
		testValidation(validator, Arrays.asList(), create);

		selfContaining.setPoints(1f);
		testValidation(validator, Arrays.asList(Violation.of("child.points", "Value of deprecated property was set.", 1f)), create);
		
	}
	
	@Test
	public void testMandatory() throws Exception {
		testValidation("mandatory-metadata.yml", Arrays.asList( //
				Violation.mandatory("child.int"),
				Violation.mandatory("child.child.int"),
				Violation.mandatory("child.listOfChildren[0].int"),
				Violation.mandatory(
						"child.listOfChildren[2].setOfChildren->|SelfContaining: child1.2.b|.child.int"),
				Violation.mandatory(
						"child.listOfChildren[3].mapOfChildren->|SelfContainingDerived: child1.3_key1|.int"),
				Violation.mandatory(
						"child.listOfChildren[3].mapOfChildren->|SelfContainingDerived: child1.3_key1|.child.listOfChildren[0].name"),
				Violation.mandatory(
						"child.listOfChildren[3].mapOfChildren->|SelfContainingDerived: child1.3_key1|.child.listOfChildren[0].child.int"),
				Violation.mandatory(
						"child.listOfChildren[3].mapOfChildren[|SelfContainingDerived: child1.3_key1|].int"),
				Violation.mandatory(
						"child.listOfChildren[3].mapOfChildren[|SelfContainingDerived: child1.3_key1|].child.listOfChildren[0].name"),
				Violation.mandatory(
						"child.listOfChildren[3].mapOfChildren[|SelfContainingDerived: child1.3_key1|].child.listOfChildren[0].child.int"),
				Violation.mandatory(
						"child.listOfChildren[3].mapOfChildren[|SelfContaining: child1.3_key2|].int")));

	}

	@Test
	public void testStrings() throws Exception {
		testValidation("string-metadata.yml", Arrays.asList( //
				Violation.of("child.listOfChildren[1].name", //
						"String has 5 chars and is shorter than its allowed minimum length: 6", //
						"child"), //
				Violation.of(
						"child.listOfChildren[3].mapOfChildren[|SelfContainingDerived: child1.3_key1|].name", //
						"String does not match the following RegEx: 'child.*'", //
						"imposter"), //
				Violation.of(
						"child.listOfChildren[3].mapOfChildren->|SelfContaining: imposterchild1.3_value2xxxxxxxxxxxxxxxxxxxxxx|.name", //
						"String has 45 chars and is longer than its allowed maximum length: 23", //
						"imposterchild1.3_value2xxxxxxxxxxxxxxxxxxxxxx"), //
				Violation.of(
						"child.listOfChildren[3].mapOfChildren->|SelfContaining: imposterchild1.3_value2xxxxxxxxxxxxxxxxxxxxxx|.name", //
						"String does not match the following RegEx: 'child.*'", //
						"imposterchild1.3_value2xxxxxxxxxxxxxxxxxxxxxx"), //
				Violation.of(
						"child.listOfChildren[3].mapOfChildren[|SelfContaining: imposterchild1.3_value2xxxxxxxxxxxxxxxxxxxxxx|].name", //
						"String has 37 chars and is longer than its allowed maximum length: 23", //
						"child1.3_value2xxxxxxxxxxxxxxxxxxxxxx")));
	}

	@Test
	public void testLimits() throws Exception {
		testValidation("limits-metadata.yml", Arrays.asList( //
				Violation.of("child.listOfChildren[1].age", //
						"Property exceeds its allowed maximum value. Max: 130", //
						200L), //
				Violation.of(
						"child.listOfChildren[3].mapOfChildren->|SelfContainingDerived: child1.3_key1|.points", //
						"Property exceeds its allowed maximum value. Max: 1.2345", //
						1.2346f), //
				Violation.of(
						"child.listOfChildren[3].mapOfChildren[|SelfContainingDerived: child1.3_key1|].age", //
						"Property exceeds its allowed minimum value. Min: 0", //
						-7L) //
		));
	}

	@Test
	public void testMixed() throws Exception {
		testValidation("mixed-metadata.yml", Arrays.asList( //
				Violation.of("child.listOfChildren[1].age", //
						"Property exceeds its allowed maximum value. Max: 130", //
						200L), //
				Violation.of(
						"child.listOfChildren[3].mapOfChildren->|SelfContainingDerived: child1.3_key1|.points", //
						"Property exceeds its allowed maximum value. Max: 1.2345", //
						1.2346f), //
				Violation.of(
						"child.listOfChildren[3].mapOfChildren[|SelfContainingDerived: child1.3_key1|].age", //
						"Property exceeds its allowed minimum value. Min: 0", //
						-7L), //
				Violation.mandatory(
						"child.listOfChildren[3].mapOfChildren[|SelfContainingDerived: child1.3_key1|].int"), //
				Violation.of("child.listOfChildren[3].mapOfChildren[|SelfContaining: child1.3_key2|].name", //
						"String has 4 chars and is shorter than its allowed minimum length: 6", //
						"carl"), //
				Violation.of("child.listOfChildren[3].mapOfChildren[|SelfContaining: child1.3_key2|].name", //
						"String does not match the following RegEx: 'child.*'", //
						"carl") //
		));
	}

	@Test
	public void testOk() throws Exception {
		testValidation("ok-metadata.yml", null);
	}

	@Test
	public void testSimpleTypes() throws Exception {
		testValidation(GenericModelTypeReflection.TYPE_BOOLEAN, true, null);

		testValidation(GenericModelTypeReflection.TYPE_BOOLEAN, "true", Arrays.asList( //
				Violation.of(ROOT_ELEMENT, //
						"Root type mismatch: Expected: type boolean but got type string", //
						"true") //
		));
	}

	@Test
	public void testTypeConditions() throws Exception {
		IsType isBase = IsType.T.create();
		isBase.setTypeSignature(Base.T.getTypeSignature());

		IsType isSelfContaining = IsType.T.create();
		isSelfContaining.setTypeSignature(SelfContaining.T.getTypeSignature());

		IsAssignableTo assignableToSelfContaining = IsAssignableTo.T.create();
		assignableToSelfContaining.setTypeSignature(SelfContaining.T.getTypeSignature());

		Base ok = (Base) loadEntity("ok-metadata.yml");

		testValidation(isBase, ok, null);

		TypeConditionDisjunction disjunction = TypeConditionDisjunction.T.create();
		disjunction.setOperands(Arrays.asList(isBase, isSelfContaining));

		testValidation(disjunction, ok, null);

		// Test violation output of general type condition
		testValidation(isSelfContaining, ok, Arrays.asList( //
				Violation.of(ROOT_ELEMENT, //
						"Root type (com.braintribe.model.processing.test.Base) did not match type condition: " + isSelfContaining, //
						ok) //
		));

		// Test violation output of well known type condition
		testValidation(assignableToSelfContaining, ok, Arrays.asList( //
				Violation.of(ROOT_ELEMENT, //
						"Root type mismatch: Expected: type com.braintribe.model.processing.test.SelfContaining but got type com.braintribe.model.processing.test.Base", //
						ok) //
		));

		testValidation(assignableToSelfContaining, ok.getChild(), null);
	}

	public void testValidation(TypeCondition typeCondition, Object object, List<Violation> expectedViolations) throws Exception {
		Validator validator = Validator.create(typeCondition, META_MODEL);

		testValidation(validator, expectedViolations, object);
	}

	public void testValidation(Validator validator, List<Violation> expectedViolations, Object object) throws Exception {
		if (expectedViolations != null && !expectedViolations.isEmpty()) {
			Assertions.assertThatThrownBy(() -> validator.validate(object)).isExactlyInstanceOf(IllegalArgumentException.class);

			List<ConstraintViolation> violations = validator.checkConstraints(object);

			Assertions.assertThat(violations).hasSameSizeAs(expectedViolations);

			for (int i = 0; i < violations.size(); i++) {
				ConstraintViolation violation = violations.get(i);
				Violation expectedViolation = expectedViolations.get(i);

				expectedViolation.assertMatches(violation);
			}
		} else {
			validator.validate(object);
			Assertions.assertThat(validator.checkConstraints(object)).as("Expected an empty result after an OK validation.").isEmpty();
		}
	}

	public void testValidation(GenericModelType rootType, Object object, List<Violation> expectedViolations) throws Exception {
		Validator validator = Validator.create(rootType, META_MODEL);

		testValidation(validator, expectedViolations, object);
	}

	public void testValidation(String sourceFileName, List<Violation> expectedViolations) throws Exception {
		EntityType<?> rootType = Base.T;
		GenericEntity parsedEntity = loadEntity(sourceFileName);

		testValidation(rootType, parsedEntity, expectedViolations);

		System.out.println(parsedEntity);

	}

	private GenericEntity loadEntity(String sourceFileName) throws FileNotFoundException {
		File file = existingTestFile(sourceFileName);

		YamlMarshaller yamlMarshaller = new YamlMarshaller();
		GenericEntity parsedEntity = (GenericEntity) yamlMarshaller.unmarshall(new FileInputStream(file));
		return parsedEntity;
	}

	private static class Violation {
		private final String path;
		private final String message;
		private final Object value;

		public Violation(String path, String message, Object value) {
			this.path = path;
			this.message = message;
			this.value = value;
		}

		public static Violation of(String path, String message, Object value) {
			return new Violation(path, message, value);
		}

		public static Violation mandatory(String path) {
			return new Violation(path, "Mandatory property is not set", null);
		}

		public void assertMatches(ConstraintViolation constraintViolation) {
			String complaint = "Found violation: \"" + constraintViolation + "\",\n\tbut expected: " + toString();
			Assertions.assertThat(path).as(complaint).isEqualTo(constraintViolation.getPathString());
			Assertions.assertThat(message).as(complaint).isEqualTo(constraintViolation.getMessage());
			Assertions.assertThat(value).as(complaint).isEqualTo(constraintViolation.getValue());
		}

		@Override
		public String toString() {
			return "path: '" + path + "'; message: '" + message + "'; value: " + CommonTools.getStringRepresentation(value);
		}
	}
}
