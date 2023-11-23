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
package com.braintribe.model.processing.findrefs.meta;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.manipulator.api.PropertyReferenceAnalyzer;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * This is a a {@link PropertyReferenceAnalyzer} for accesses which support full querying flexibility when it comes to
 * polymorphism. This analyzer only considers properties on the level they are declared, so that all the references of
 * that property for the entire hierarchy are considered at the same time.
 * 
 * 
 * h3. Example Model:
 * 
 * {@code
 * abstract entity AbstractPerson
 * 		Address address; 
 * 
 * entity GreenPerson
 * 		String greenProperty
 * 
 * entity BluePerson
 * 		String blueProperty
 * }
 * 
 * Now we are going to delete an instance of Address, thus we analyze where it might be referenced from. The regular
 * {@link BasicPropertyReferenceAnalyzer} would give us a list of three properties:
 * <ul>
 * <li>AbstractPerson.address</li>
 * <li>GreenPerson.address</li>
 * <li>BluePerson.address</li>
 * </ul>
 * 
 * If however, we are dealing with an access that has the ability for any type to do the query on the abstract level, we
 * only need to consider the <code>AbstractPerson.address</code>, which is what this implementation would return.
 * 
 * @author peter.gazdik
 */
public class OptimizedPolymorphicPropertyReferenceAnalyzer extends BasicPropertyReferenceAnalyzer {

	public OptimizedPolymorphicPropertyReferenceAnalyzer(ModelOracle modelOracle) {
		super(modelOracle);
	}

	@Override
	protected boolean ignoreEntity(GmEntityType entityType) {
		return isEmpty(entityType.getProperties());
	}

	@Override
	protected boolean ignoreProperty(GmEntityType actualOwner, GmProperty property) {
		return property.getDeclaringType() != actualOwner;
	}

}
