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
package com.braintribe.model.access.security.testdata;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.NonDeletable;
import com.braintribe.model.meta.data.constraint.NonInstantiable;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.meta.data.constraint.Unmodifiable;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.query.NonQueryable;
import com.braintribe.model.meta.data.security.Administrable;
import com.braintribe.model.meta.selector.ConjunctionSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;

/**
 * @author peter.gazdik
 */
public class MdFactory {

	public static Mandatory mandatory() {
		return Mandatory.T.create();
	}

	public static Unique unique() {
		return Unique.T.create();
	}

	public static NonInstantiable instantiationDisabled() {
		return NonInstantiable.T.create();
	}

	public static NonDeletable entityDeletionDisabled() {
		return NonDeletable.T.create();
	}

	public static Unmodifiable nonModifiable() {
		return Unmodifiable.T.create();
	}

	public static NonQueryable nonQueryable() {
		return NonQueryable.T.create();
	}

	public static Hidden invisible() {
		return Hidden.T.create();
	}

	public static Confidential confidential() {
		return Confidential.T.create();
	}

	public static MetaData administrable(String role, UseCaseSelector useCaseSelector) {
		Administrable result = Administrable.T.create();
		result.setSelector(conjunctionSelector(useCaseSelector, roleSelector(role)));

		return result;
	}

	// #######################################################
	// ## . . . . . . . . . . Selectors . . . . . . . . . . ##
	// #######################################################

	public static ConjunctionSelector conjunctionSelector(MetaDataSelector... selectors) {
		ConjunctionSelector result = ConjunctionSelector.T.create();
		result.setOperands(asList(selectors));

		return result;
	}

	public static RoleSelector roleSelector(String role) {
		RoleSelector result = RoleSelector.T.create();
		result.setRoles(asSet(role));

		return result;
	}

	public static UseCaseSelector useCaseSelector(String useCase) {
		UseCaseSelector result = UseCaseSelector.T.create();
		result.setUseCase(useCase);

		return result;
	}

}
