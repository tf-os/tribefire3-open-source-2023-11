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
package com.braintribe.model.access;

import java.util.Iterator;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

/**
 * 
 */
public class TypeFilteringIterable implements Iterable<GenericEntity> {

	private final EntityType<?> entityType;
	private final Iterable<GenericEntity> delegateIterable;

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	public TypeFilteringIterable(String typeSignature, Iterable<GenericEntity> delegateIterable) {
		this.entityType = typeReflection.getEntityType(typeSignature);
		this.delegateIterable = delegateIterable;
	}

	@Override
	public Iterator<GenericEntity> iterator() {
		final Iterator<GenericEntity> delegateIterator = delegateIterable.iterator();

		class LocalIterator implements Iterator<GenericEntity> {
			GenericEntity next;

			public LocalIterator() {
				loadNext();
			}

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public GenericEntity next() {
				GenericEntity result = next;
				loadNext();

				return result;
			}

			private void loadNext() {
				while (delegateIterator.hasNext()) {
					GenericEntity entity = delegateIterator.next();

					if (entityType.isInstance(entity)) {
						next = entity;
						return;
					}
				}

				next = null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		}

		return new LocalIterator();
	}

}
