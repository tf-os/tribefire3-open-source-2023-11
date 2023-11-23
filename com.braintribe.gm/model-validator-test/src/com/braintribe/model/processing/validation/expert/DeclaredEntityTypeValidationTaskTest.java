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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class DeclaredEntityTypeValidationTaskTest extends AbstractValidationTaskTest {
	
	private GmMetaModel model;
	private GmEntityType type;
	private ValidationContext context;
	
	@Before
	public void prepare() {
		type = TestEntityType.T.create();
		type.setGlobalId("test-global-id");
		TestProperty prop1 = TestProperty.T.create();
		prop1.setName("prop1");
		TestProperty prop2 = TestProperty.T.create();
		prop2.setName("prop2");
		type.setProperties(asList(prop1, prop2));
		type.setEvaluatesTo(TestEntityType.T.create());
		type.setMetaData(asSet(TestMetaData.T.create(), TestMetaData.T.create()));
		type.setPropertyMetaData(asSet(TestMetaData.T.create(), TestMetaData.T.create()));
		type.setPropertyOverrides(asList(TestPropertyOverride.T.create(), TestPropertyOverride.T.create()));
		type.setSuperTypes(asList(TestEntityType.T.create(), TestEntityType.T.create()));
		
		model = TestMetaModel.T.create();
		type.setDeclaringModel(model);
		
		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}
	
	@Test
	public void testExecuting() {
		ValidationTask task = new DeclaredEntityTypeValidationTask(model, type);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(11);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredPropertyValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredPropertyValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(ReferencedTypeValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(CoreMetaDataValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(CoreMetaDataValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(CoreMetaDataValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(CoreMetaDataValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredPropertyOverrideValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredPropertyOverrideValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(ReferencedTypeValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(ReferencedTypeValidationTask.class);
	}
	
	@Test
	public void testExecuting_MissingGlobalId() {
		ValidationTask task = new DeclaredEntityTypeValidationTask(model, type);
		type.setGlobalId(null);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_DuplicateProperties() {
		ValidationTask task = new DeclaredEntityTypeValidationTask(model, type);
		TestProperty prop1 = TestProperty.T.create();
		prop1.setName("prop1");
		type.getProperties().add(prop1);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_NoSuperTypes() {
		ValidationTask task = new DeclaredEntityTypeValidationTask(model, type);
		type.getSuperTypes().clear();
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
}
