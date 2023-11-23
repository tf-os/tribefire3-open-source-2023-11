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
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class ReferencedTypeValidationTaskTest extends AbstractValidationTaskTest {
	
	private GmMetaModel declaringModel;
	private GmType type;
	private ValidationContext context;
	
	@Before
	public void prepare() {
		declaringModel = TestMetaModel.T.create();
		type = TestType.T.create();
		declaringModel.setTypes(asSet(type));
		type.setDeclaringModel(declaringModel);
		
		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}
	
	@Test
	public void testExecuting_DeclaringModelIsCurrentModel() {
		ValidationTask task = new ReferencedTypeValidationTask(declaringModel, type);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(1);
		assertThat(context.pollValidationTask()).isInstanceOf(TypeValidationTask.class);
	}
	
	@Test
	public void testExecuting_DeclaringModelIsCurrentModelsDependency() {
		ValidationTask task = new ReferencedTypeValidationTask(declaringModel, type);
		TestMetaModel depDeclaringModel = TestMetaModel.T.create();
		type.setDeclaringModel(depDeclaringModel);
		declaringModel.setDependencies(asList(depDeclaringModel));
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(1);
		assertThat(context.pollValidationTask()).isInstanceOf(TypeValidationTask.class);
	}
	
	@Test
	public void testExecuting_DeclaringModelIsCurrentModel_TypeMissing() {
		ValidationTask task = new ReferencedTypeValidationTask(declaringModel, type);
		declaringModel.setTypes(asSet());
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_DeclaringModelIsCurrentModelsDependency_MissingDependency() {
		ValidationTask task = new ReferencedTypeValidationTask(declaringModel, type);
		TestMetaModel depDeclaringModel = TestMetaModel.T.create();
		type.setDeclaringModel(depDeclaringModel);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
}
