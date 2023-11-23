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
package com.braintribe.model.processing.query.eval.tools;

import java.util.Iterator;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.tuple.OneDimensionalTuple;

/**
 * 
 */
public class TupleIterable implements Iterable<Tuple> {

	protected final int componentIndex;
	protected final Iterable<? extends GenericEntity> entities;
	protected final OneDimensionalTuple singletonTuple;

	public TupleIterable(int componentIndex, Iterable<? extends GenericEntity> entities) {
		this.componentIndex = componentIndex;
		this.entities = entities;
		this.singletonTuple = new OneDimensionalTuple(componentIndex);
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new TupleIterator();
	}

	protected class TupleIterator implements Iterator<Tuple> {
		protected final Iterator<? extends GenericEntity> entitiesIterator;

		protected TupleIterator() {
			entitiesIterator = entities.iterator();
		}

		@Override
		public boolean hasNext() {
			return entitiesIterator.hasNext();
		}

		@Override
		public Tuple next() {
			singletonTuple.setValueDirectly(componentIndex, entitiesIterator.next());

			return singletonTuple;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove a tuple from a tuple set!");
		}

	}

}
