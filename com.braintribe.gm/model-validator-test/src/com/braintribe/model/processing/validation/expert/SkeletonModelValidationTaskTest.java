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

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class SkeletonModelValidationTaskTest extends AbstractValidationTaskTest {
	
	private GmMetaModel model;
	private ValidationContext context;
	
	@Before
	public void prepare() {
		model = TestMetaModel.T.create();
		model.setGlobalId("test-global-id");
		model.setName("test:name");
		model.setVersion("1.0.1");
		model.setDependencies(asList(TestMetaModel.T.create(), TestMetaModel.T.create()));
		model.setTypes(asSet(TestType.T.create(), TestType.T.create()));
		
		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}
	
	@Test
	public void testExecuting() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(2);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredTypeValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredTypeValidationTask.class);
	}
	
	@Test
	public void testExecuting_MissingGlobalId() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.setGlobalId(null);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_MissingName() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.setName(null);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_InvalidName() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.setName("invalid-name");
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_MissingVersion() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.setVersion(null);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_InvalidVersion() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.setVersion("invalid-version");
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_TypeOverridesNotEmpty() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.getTypeOverrides().add(TestTypeOverride.T.create());
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_MetaDataNotEmpty() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.getMetaData().add(TestMetaData.T.create());
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_EnumTypeMetaDataNotEmpty() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.getEnumTypeMetaData().add(TestMetaData.T.create());
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_EnumConstantMetaDataNotEmpty() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.getEnumConstantMetaData().add(TestMetaData.T.create());
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_NoDependencies() {
		ValidationTask task = new SkeletonModelValidationTask(model);
		model.getDependencies().clear();
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
}
