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
package com.braintribe.model.processing;

import java.util.List;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.impl.ValidatorImpl;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.validation.expert.TypeExpert;

/**
 * Validates an entity and its properties transitively. What exactly is
 * validated depends on the implementations of the experts: {@link ValidationExpert}s and
 * {@link PropertyValidationExpert}s which can be passed via an {@link ValidationExpertRegistry}.
 */
public interface Validator {
	/**
	 * Creates a simple Validator using the {@link ValidationExpertRegistry#createDefault() default
	 * ValidationExpertRegistry}. It also adds another expert that checks if the object to be validated is assignable to
	 * a certain type.
	 * 
	 * @param rootType
	 *            The validated object will be validated against being assignable to this type.
	 * @param model
	 *            will be used to determine the metadata of the validated object
	 */
	static Validator create(GenericModelType rootType, GmMetaModel model) {
		IsAssignableTo isType = IsAssignableTo.T.create();
		isType.setTypeSignature(rootType.getTypeSignature());
		return create(isType, model);
	}

	/**
	 * Creates a simple Validator using the {@link ValidationExpertRegistry#createDefault() default
	 * ValidationExpertRegistry}. It also adds another expert that checks if the object to be validated meets a certain
	 * type condotion.
	 * 
	 * @param typeCondition
	 *            The validated object will be validated against meeting this type condition.
	 * @param model
	 *            will be used to determine the metadata of the validated object
	 */
	static Validator create(TypeCondition typeCondition, GmMetaModel model) {
		CmdResolverImpl cmdResolver = new CmdResolverImpl(new BasicModelOracle(model));
		ValidationExpertRegistry validationExpertRegistry = ValidationExpertRegistry.createDefault();
		Validator validator = new ValidatorImpl(cmdResolver, validationExpertRegistry);

		validationExpertRegistry.addRootExpert(new TypeExpert(typeCondition));
		return validator;
	}

	/**
	 * Traverses passed value and runs validations. As opposed to {@link #validate(Object)} this method does not throw
	 * an exception if any validation failed but it returns a List of modeled {@link ConstraintViolation}s. The
	 * traversing is always finished so you will get all violated constraints as a result.
	 * 
	 * @param rootValue
	 *            Value to be validated
	 */
	List<ConstraintViolation> checkConstraints(Object rootValue);

	/**
	 * Traverses passed value and runs validations. As opposed to {@link #checkConstraints(Object)} this method throws
	 * an {@link IllegalArgumentException} with a detailed human-readable message listing all violated constraints if any was reported by a
	 * ValidationExpert.
	 * 
	 * @param rootValue
	 *            Value to be validated
	 * @throws IllegalArgumentException if any constraint violation was detected           
	 */
	void validate(Object rootValue);

}