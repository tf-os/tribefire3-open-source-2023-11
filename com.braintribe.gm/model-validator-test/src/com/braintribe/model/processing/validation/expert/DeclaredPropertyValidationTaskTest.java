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

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class DeclaredPropertyValidationTaskTest extends AbstractValidationTaskTest {

	private GmMetaModel model;
	private GmEntityType declaringType;
	private GmProperty property;
	private ValidationContext context;

	@Before
	public void prepare() {
		declaringType = TestEntityType.T.create();
		property = TestProperty.T.create();
		property.setDeclaringType(declaringType);
		property.setType(TestEntityType.T.create());
		model = TestMetaModel.T.create();

		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}

	@Test
	public void testExecuting() {
		ValidationTask task = new DeclaredPropertyValidationTask(model, declaringType, property);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(2);
		assertThat(context.pollValidationTask()).isInstanceOf(PropertyValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(ReferencedTypeValidationTask.class);
	}

	@Test
	public void testExecuting_WrongDeclaringType() {
		ValidationTask task = new DeclaredPropertyValidationTask(model, declaringType, property);
		property.setDeclaringType(TestEntityType.T.create());

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
	}
}
