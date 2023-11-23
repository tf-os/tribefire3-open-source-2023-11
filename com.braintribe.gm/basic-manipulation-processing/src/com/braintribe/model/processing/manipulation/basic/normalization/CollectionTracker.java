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
package com.braintribe.model.processing.manipulation.basic.normalization;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;

/**
 * 
 */
abstract class CollectionTracker {

	protected final Owner owner;
	protected final String propertySignature;

	public CollectionTracker(Owner owner, String propertySignature) {
		this.owner = owner;
		this.propertySignature = propertySignature;
	}

	/**
	 * Note that if there is a {@link ClearCollectionManipulation}, it is always the first manipulation for given property, that is assured by the
	 * {@link SimpleManipulationNormalizer}.
	 */
	public abstract void onClearCollection();

	/**
	 * Note that if there is a {@link ChangeValueManipulation}, it is always the first manipulation for given property, that is assured by the
	 * {@link SimpleManipulationNormalizer}.
	 */
	public abstract void onChangeValue(ChangeValueManipulation m);

	public abstract void onBulkInsert(AddManipulation m);

	public abstract void onBulkRemove(RemoveManipulation m);

	public abstract void appendAggregateManipulations(List<AtomicManipulation> manipulations, Set<EntityReference> entitiesToDelete);

	protected static <E> E cast(Object o) {
		return (E) o;
	}

	protected <E> E getTypeSafeCvmValue(Class<E> expectedType, ChangeValueManipulation m) {
		Object value = m.getNewValue();
		if (value == null)
			return null;

		if (!expectedType.isInstance(value))
			throw new IllegalArgumentException("ChangeValueManipulation with an incompatible value found! Property " + owner.getPropertyName()
					+ " of " + owner.ownerEntity() + "  is of type '" + propertySignature + "', but the value was of type '"
					+ value.getClass().getSimpleName() + "'. Actual value: " + value);

		return (E) value;
	}

	protected <T> Set<T> ensureComparable(Collection<T> set) {
		Set<T> s = CodingSet.create(ElementHashingComparator.INSTANCE);
		s.addAll(set);

		return s;
	}

}
