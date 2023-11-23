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
package tribefire.extension.simple.modelexpert.data;

import java.util.HashSet;
import java.util.Set;

import tribefire.extension.simple.model.data.Person;

/**
 * Provides methods for working with {@link Person} entities.<br>
 * The main purpose of this class is to provide an example of how to implement a so-called entity type expert, i.e. a class which provides utility
 * methods to work with entities of particular type.
 *
 * @author michael.lafite
 */
public interface PersonExpert {

	/**
	 * Returns the parents of the specified <code>person</code>, i.e. {@link Person#getMother() mother} and {@link Person#getFather()} (unless
	 * <code>null</code>).
	 *
	 * @throws IllegalArgumentException
	 *             if <code>person</code> is <code>null</code>.
	 */
	static Set<Person> getParents(Person person) {
		if (person == null) {
			throw new IllegalArgumentException("The passed person must not be null!");
		}
		Set<Person> parents = new HashSet<>();
		if (person.getMother() != null) {
			parents.add(person.getMother());
		}
		if (person.getFather() != null) {
			parents.add(person.getFather());
		}
		return parents;
	}

}
