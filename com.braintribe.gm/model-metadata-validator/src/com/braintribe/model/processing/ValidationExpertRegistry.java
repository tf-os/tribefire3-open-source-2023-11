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

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.processing.validation.expert.property.MandatoryMetadataExpert;
import com.braintribe.model.processing.validation.expert.property.MaxLengthMetadataExpert;
import com.braintribe.model.processing.validation.expert.property.MaxMetadataExpert;
import com.braintribe.model.processing.validation.expert.property.MinLengthMetadataExpert;
import com.braintribe.model.processing.validation.expert.property.MinMetadataExpert;
import com.braintribe.model.processing.validation.expert.property.PatternMetadataExpert;

/**
 * Used to configure the experts of a {@link Validator}.
 * 
 * @author Neidhart.Orlich
 *
 */
public class ValidationExpertRegistry {
	private final List<PropertyValidationExpert> propertyValidationExperts = new ArrayList<>();
	private final List<ValidationExpert> entityValidationExperts = new ArrayList<>();
	private final List<ValidationExpert> rootEntityValidationExperts = new ArrayList<>();

	/**
	 * Creates a {@link ValidationExpertRegistry} with generic experts that validate against well known metadata and
	 * always make sense to be used. (At least when validating GenericEntities that are expected to have metadata
	 * attached).
	 */
	public static ValidationExpertRegistry createDefault() {
		ValidationExpertRegistry validationExperts = new ValidationExpertRegistry();

		validationExperts.addPropertyExpert(new MandatoryMetadataExpert());
		validationExperts.addPropertyExpert(new MaxLengthMetadataExpert());
		validationExperts.addPropertyExpert(new MinLengthMetadataExpert());
		validationExperts.addPropertyExpert(new MaxMetadataExpert());
		validationExperts.addPropertyExpert(new MinMetadataExpert());
		validationExperts.addPropertyExpert(new PatternMetadataExpert());

		return validationExperts;
	}

	public void addPropertyExpert(PropertyValidationExpert expert) {
		propertyValidationExperts.add(expert);
	}

	public void addRootExpert(ValidationExpert expert) {
		rootEntityValidationExperts.add(expert);
	}

	public List<ValidationExpert> getEntityExperts() {
		return entityValidationExperts;
	}

	public List<ValidationExpert> getRootEntityExperts() {
		return rootEntityValidationExperts;
	}

	public List<PropertyValidationExpert> getPropertyExperts() {
		return propertyValidationExperts;
	}
}
