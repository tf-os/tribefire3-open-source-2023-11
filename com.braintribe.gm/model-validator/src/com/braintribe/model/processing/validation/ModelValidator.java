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
package com.braintribe.model.processing.validation;

import java.util.List;

import com.braintribe.model.processing.validation.expert.ValidationTask;

/**
 * <p>
 * The main driver of the model validation.
 * </p>
 * 
 *
 */
public class ModelValidator {

	/**
	 * <p>
	 * Executes validation tasks that are enqueued in the {@link ValidationContext
	 * validation context} starting from the provided root task.
	 * </p>
	 * 
	 * @param context
	 * @param rootValidationTask
	 *            - validation task that corresponds to the model element being
	 *            validated
	 * @return the list of resulting validation messages
	 */
	public List<ValidationMessage> validate(ValidationContext context, ValidationTask rootValidationTask) {
		context.addValidationTask(rootValidationTask);

		while (!context.getValidationTasks().isEmpty()) {
			context.pollValidationTask().execute(context);
		}
		return context.getValidationMessages();
	}
}
