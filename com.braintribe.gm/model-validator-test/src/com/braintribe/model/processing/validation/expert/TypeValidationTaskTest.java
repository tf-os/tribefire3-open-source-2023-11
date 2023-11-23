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

import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class TypeValidationTaskTest extends AbstractValidationTaskTest {
	
	private GmType type;
	private ValidationContext context;
	
	@Before
	public void prepare() {
		type = TestType.T.create();
		type.setGlobalId("test-global-id");
		type.setDeclaringModel(TestMetaModel.T.create());
		type.setTypeSignature(GmType.T.getTypeSignature());
		
		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}
	
	@Test
	public void testExecuting() {
		ValidationTask task = new TypeValidationTask(type);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}
	
	@Test
	public void testExecuting_MissingGlobalId() {
		ValidationTask task = new TypeValidationTask(type);
		type.setGlobalId(null);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_DeclaringModelMissing() {
		ValidationTask task = new TypeValidationTask(type);
		type.setDeclaringModel(null);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_TypeSignatureMissing() {
		ValidationTask task = new TypeValidationTask(type);
		type.setTypeSignature(null);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_TypeSignatureIsWrong() {
		ValidationTask task = new TypeValidationTask(type);
		type.setTypeSignature("wrong.type.Signature");
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
}
