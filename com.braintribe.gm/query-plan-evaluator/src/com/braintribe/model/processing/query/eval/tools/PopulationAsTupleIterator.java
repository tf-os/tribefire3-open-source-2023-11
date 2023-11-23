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

import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.tuple.OneDimensionalTuple;

/**
 * 
 */
public class PopulationAsTupleIterator implements Iterator<Tuple> {

	protected final Iterator<?> populationIterator;
	protected final OneDimensionalTuple singletonTuple;
	protected final int index;

	public PopulationAsTupleIterator(Iterable<?> population, int index) {
		this.populationIterator = population.iterator();
		this.singletonTuple = new OneDimensionalTuple(index);
		this.index = index;
	}

	@Override
	public boolean hasNext() {
		return populationIterator.hasNext();
	}

	@Override
	public Tuple next() {
		singletonTuple.setValueDirectly(index, populationIterator.next());

		return singletonTuple;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove a tuple from a tuple set!");
	}

}
