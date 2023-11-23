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

import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.mapping.Alias;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMode;
import com.braintribe.model.processing.validation.ValidationType;

public class CoreMetaDataValidationTaskTest extends AbstractValidationTaskTest {

	private ValidationContext context;

	@Before
	public void prepare() {
		context = new ValidationContext(ValidationType.SKELETON, ValidationMode.DECLARATION);
	}

	@Test
	public void testExecuting() {
		MetaData metaData = Alias.T.create();
		metaData.setGlobalId("test-global-id");
		ValidationTask task = new CoreMetaDataValidationTask(metaData);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(0);
		assertThat(context.getValidationTasks()).hasSize(0);
	}

	@Test
	public void testExecuting_MissingGlobalId() {
		MetaData metaData = Alias.T.create();
		metaData.setGlobalId(null);
		ValidationTask task = new CoreMetaDataValidationTask(metaData);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
	}

	@Test
	public void testExecuting_NotAllowedMetaData() {
		MetaData metaData = TestMetaData.T.create();
		metaData.setGlobalId("test-global-id");
		ValidationTask task = new CoreMetaDataValidationTask(metaData);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
	}

	@Test
	public void testExecuting_PresentMetaDataSelector() {
		MetaData metaData = Alias.T.create();
		metaData.setGlobalId("test-global-id");
		metaData.setSelector(TestMetaDataSelector.T.create());
		ValidationTask task = new CoreMetaDataValidationTask(metaData);

		task.execute(context);

		assertThat(extractErrorMessages(context)).hasSize(1);
	}
}
