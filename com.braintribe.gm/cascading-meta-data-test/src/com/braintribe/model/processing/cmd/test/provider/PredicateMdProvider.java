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
package com.braintribe.model.processing.cmd.test.provider;

import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Unmodifiable;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.NonConfidential;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.cmd.test.model.Person;

/**
 * 
 */
public class PredicateMdProvider extends AbstractModelSupplier {

	public static final String PREDICATE = "predicate";
	public static final String PREDICATE_ERASURE = "predicateErasure";
	public static final String EXPLICIT_PREDICATE = "explicitPredicate";
	public static final String EXPLICIT_PREDICATE_ERASURE = "explicitPredicateErasure";

	public static final String UNMODIFIABLE_PROPETY = "longValue";
	public static final String UNMODIFIABLE_MANDTORY_PROPETY = "name";

	@Override
	protected void addMetaData() {
		addPredicateMd();
		addPredicateErasureMd();
		addExplicitPredicateMd();
		addExplicitPredicateErasureMd();
		addUnmodifiableProperty();
		addUnmodifiableMandatoryProperty();
	}

	private void addPredicateMd() {
		fullMdEditor. //
				addModelMetaData(append(Visible.T.create(), useCase(PREDICATE)));
	}

	private void addPredicateErasureMd() {
		fullMdEditor. //
				addModelMetaData(append(Hidden.T.create(), useCase(PREDICATE_ERASURE)));
	}

	private void addExplicitPredicateMd() {
		fullMdEditor. //
				addModelMetaData(append(Confidential.T.create(), useCase(EXPLICIT_PREDICATE)));
	}

	private void addExplicitPredicateErasureMd() {
		fullMdEditor. //
				addModelMetaData(append(NonConfidential.T.create(), useCase(EXPLICIT_PREDICATE_ERASURE)));
	}

	private void addUnmodifiableProperty() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData(UNMODIFIABLE_PROPETY, Unmodifiable.T.create());
	}

	private void addUnmodifiableMandatoryProperty() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData(UNMODIFIABLE_MANDTORY_PROPETY, Unmodifiable.T.create(), Mandatory.T.create());
	}

}
