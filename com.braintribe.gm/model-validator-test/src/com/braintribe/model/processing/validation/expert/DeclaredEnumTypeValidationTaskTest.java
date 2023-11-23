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

import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class DeclaredEnumTypeValidationTaskTest extends AbstractValidationTaskTest {
	
	private GmEnumType type;
	private ValidationContext context;
	
	@Before
	public void prepare() {
		type = TestEnumType.T.create();
		type.setGlobalId("test-global-id");
		TestEnumConstant const1 = TestEnumConstant.T.create();
		const1.setName("const1");
		TestEnumConstant const2 = TestEnumConstant.T.create();
		const2.setName("const2");
		type.setConstants(asList(const1, const2));
		type.setMetaData(asSet(TestMetaData.T.create(), TestMetaData.T.create()));
		type.setEnumConstantMetaData(asSet(TestMetaData.T.create(), TestMetaData.T.create()));
		
		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}
	
	@Test
	public void testExecuting() {
		ValidationTask task = new DeclaredEnumTypeValidationTask(type);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(6);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredConstantValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(DeclaredConstantValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(CoreMetaDataValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(CoreMetaDataValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(CoreMetaDataValidationTask.class);
		assertThat(context.pollValidationTask()).isInstanceOf(CoreMetaDataValidationTask.class);
	}
	
	@Test
	public void testExecuting_MissingGlobalId() {
		ValidationTask task = new DeclaredEnumTypeValidationTask(type);
		type.setGlobalId(null);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
	
	@Test
	public void testExecuting_DuplicateConstants() {
		ValidationTask task = new DeclaredEnumTypeValidationTask(type);
		TestEnumConstant const1 = TestEnumConstant.T.create();
		const1.setName("const1");
		type.getConstants().add(const1);
		
		task.execute(context);
		
		assertThat(extractErrorMessages(context)).hasSize(1);
	}
}
