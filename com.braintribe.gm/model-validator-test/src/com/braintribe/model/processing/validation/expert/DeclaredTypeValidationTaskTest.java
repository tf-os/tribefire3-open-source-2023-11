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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class DeclaredTypeValidationTaskTest extends AbstractValidationTaskTest {
	
	private GmMetaModel declaringModel;
	private ValidationContext context;
	
	@Before
	public void prepare() {
		declaringModel = TestMetaModel.T.create();
		
		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}
	
	@Test
	public void testExecuting_EntityType() {
		GmType type = TestEntityType.T.create();
		type.setDeclaringModel(declaringModel);
		ValidationTask task = new DeclaredTypeValidationTask(declaringModel, type);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(2);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredEntityTypeValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(TypeValidationTask.class);
	}
	
	@Test
	public void testExecuting_EnumType() {
		GmType type = TestEnumType.T.create();
		type.setDeclaringModel(declaringModel);
		ValidationTask task = new DeclaredTypeValidationTask(declaringModel, type);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(2);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredEnumTypeValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(TypeValidationTask.class);
	}
	
	@Test
	public void testExecuting_WrongDeclaringModel() {
		GmType type = TestEntityType.T.create();
		ValidationTask task = new DeclaredTypeValidationTask(declaringModel, type);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_NotACustomType() {
		GmType type = TestCollectionType.T.create();
		type.setDeclaringModel(declaringModel);
		ValidationTask task = new DeclaredTypeValidationTask(declaringModel, type);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
}
