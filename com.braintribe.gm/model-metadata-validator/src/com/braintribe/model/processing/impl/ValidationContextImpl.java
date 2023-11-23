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
package com.braintribe.model.processing.impl;

import java.util.function.Consumer;

import com.braintribe.model.processing.ConstraintViolation;
import com.braintribe.model.processing.ValidationContext;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;

/**
 * Contains various information about what is to be validated. Constraint violations can be reported by
 * {@link #notifyConstraintViolation(String)}.
 * 
 * @author Neidhart.Orlich
 *
 */
public class ValidationContextImpl implements ValidationContext {

	private final TraversingModelPathElement pathElement;
	private final Consumer<ConstraintViolation> constraintViolationConsumer;
	private final Object value;

	public ValidationContextImpl(TraversingModelPathElement pathElement, Consumer<ConstraintViolation> constraintViolationConsumer) {
		this.pathElement = pathElement;
		this.constraintViolationConsumer = constraintViolationConsumer;
		this.value = pathElement.getValue();
	}

	@Override
	public void notifyConstraintViolation(String message) {
		constraintViolationConsumer.accept(ConstraintViolation.create(message, pathElement));
	}

	@Override
	public TraversingModelPathElement getPathElement() {
		return pathElement;
	}

	@Override
	public Object getValue() {
		return value;
	}

}