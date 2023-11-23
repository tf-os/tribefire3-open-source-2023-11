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
package com.braintribe.model.processing.validation.expert;

import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.baseType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.dateType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.enumType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.integerType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.listType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.mapType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.property;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.setType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.stringType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.NullDescriptor;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class InitializerValidationTaskTest extends AbstractValidationTaskTest {

	private ValidationContext context;

	@Before
	public void prepare() {
		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}

	@Test
	public void testExecuting_InitializerIsNullDescriptor() {
		NullDescriptor initializer = NullDescriptor.T.create();

		GmProperty property = property(null, "test.property", integerType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsNullDescriptor_PropertyIsOfBaseType() {
		NullDescriptor initializer = NullDescriptor.T.create();

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsNullDescriptor_PropertyIsNotNullable() {
		NullDescriptor initializer = NullDescriptor.T.create();

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		property.setNullable(false);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsEnumReference() {
		EnumReference initializer = EnumReference.T.create();
		initializer.setTypeSignature("enum.type");

		GmProperty property = property(null, "test.property", enumType("enum.type"));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsEnumReference_PropertyIsOfBaseType() {
		EnumReference initializer = EnumReference.T.create();
		initializer.setTypeSignature("enum.type");

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsEnumReference_TypeSignatureMismatch() {
		EnumReference initializer = EnumReference.T.create();
		initializer.setTypeSignature("enum.type");

		GmProperty property = property(null, "test.property", enumType("enum.type.other"));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsEnumReference_TypeMismatch() {
		EnumReference initializer = EnumReference.T.create();
		initializer.setTypeSignature("enum.type");

		GmProperty property = property(null, "test.property", integerType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsNow() {
		Now initializer = Now.T.create();

		GmProperty property = property(null, "test.property", dateType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsNow_PropertyIsOfBaseType() {
		Now initializer = Now.T.create();

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsNow_TypeMismatch() {
		Now initializer = Now.T.create();

		GmProperty property = property(null, "test.property", integerType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsObject() {
		Object initializer = new Object();

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsObject_TypeMismatch() {
		Object initializer = new Object();

		GmProperty property = property(null, "test.property", integerType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsList() {
		List<String> initializer = new ArrayList<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", listType(stringType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsList_PropertyIsOfBaseType() {
		List<String> initializer = new ArrayList<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsList_PropertyElementIsOfBaseType() {
		List<String> initializer = new ArrayList<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", listType(baseType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsList_ListIsEmpty() {
		List<String> initializer = new ArrayList<>();

		GmProperty property = property(null, "test.property", listType(stringType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsList_TypeMismatch() {
		List<String> initializer = new ArrayList<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", stringType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsList_CollectionTypeMismatch() {
		List<String> initializer = new ArrayList<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", setType(stringType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsList_ElementTypeMismatch() {
		List<String> initializer = new ArrayList<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", listType(integerType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSet() {
		Set<String> initializer = new HashSet<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", setType(stringType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSet_PropertyIsOfBaseType() {
		Set<String> initializer = new HashSet<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSet_PropertyElementIsOfBaseType() {
		Set<String> initializer = new HashSet<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", setType(baseType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSet_SetIsEmpty() {
		Set<String> initializer = new HashSet<>();

		GmProperty property = property(null, "test.property", setType(stringType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSet_TypeMismatch() {
		Set<String> initializer = new HashSet<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", stringType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSet_CollectionTypeMismatch() {
		Set<String> initializer = new HashSet<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", listType(stringType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSet_ElementTypeMismatch() {
		Set<String> initializer = new HashSet<>();
		initializer.add("test.element.1");
		initializer.add("test.element.2");

		GmProperty property = property(null, "test.property", setType(integerType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap() {
		Map<String, Integer> initializer = new HashMap<>();
		initializer.put("test.element.1", 1);
		initializer.put("test.element.2", 2);

		GmProperty property = property(null, "test.property", mapType(stringType(), integerType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap_PropertyIsOfBaseType() {
		Map<String, Integer> initializer = new HashMap<>();
		initializer.put("test.element.1", 1);
		initializer.put("test.element.2", 2);

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap_PropertyKeyElementIsOfBaseType() {
		Map<String, Integer> initializer = new HashMap<>();
		initializer.put("test.element.1", 1);
		initializer.put("test.element.2", 2);

		GmProperty property = property(null, "test.property", mapType(baseType(), integerType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap_PropertyValueElementIsOfBaseType() {
		Map<String, Integer> initializer = new HashMap<>();
		initializer.put("test.element.1", 1);
		initializer.put("test.element.2", 2);

		GmProperty property = property(null, "test.property", mapType(stringType(), baseType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap_MapIsEmpty() {
		Map<String, Integer> initializer = new HashMap<>();

		GmProperty property = property(null, "test.property", mapType(stringType(), integerType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap_TypeMismatch() {
		Map<String, Integer> initializer = new HashMap<>();
		initializer.put("test.element.1", 1);
		initializer.put("test.element.2", 2);

		GmProperty property = property(null, "test.property", stringType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap_CollectionTypeMismatch() {
		Map<String, Integer> initializer = new HashMap<>();
		initializer.put("test.element.1", 1);
		initializer.put("test.element.2", 2);

		GmProperty property = property(null, "test.property", listType(stringType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap_KeyElementTypeMismatch() {
		Map<String, Integer> initializer = new HashMap<>();
		initializer.put("test.element.1", 1);
		initializer.put("test.element.2", 2);

		GmProperty property = property(null, "test.property", mapType(integerType(), integerType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsMap_ValueElementTypeMismatch() {
		Map<String, Integer> initializer = new HashMap<>();
		initializer.put("test.element.1", 1);
		initializer.put("test.element.2", 2);

		GmProperty property = property(null, "test.property", mapType(stringType(), stringType()));
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSimple() {
		String initializer = "initializer";

		GmProperty property = property(null, "test.property", stringType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSimple_PropertyIsOfBaseType() {
		String initializer = "initializer";

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsSimple_TypeMismatch() {
		String initializer = "initializer";

		GmProperty property = property(null, "test.property", integerType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_InitializerIsUnknownType() {
		Character initializer = 't';

		GmProperty property = property(null, "test.property", baseType());
		property.setInitializer(initializer);
		ValidationTask task = new InitializerValidationTask(property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	public enum TestEnum {
		TEST
	}
}
