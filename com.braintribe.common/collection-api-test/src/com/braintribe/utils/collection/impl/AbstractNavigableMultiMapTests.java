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
package com.braintribe.utils.collection.impl;

import org.junit.Before;

import com.braintribe.utils.collection.api.NavigableMultiMap;
import com.braintribe.utils.collection.impl.compare.ComparableComparator;

/**
 * 
 */
public abstract class AbstractNavigableMultiMapTests extends AbstractMultiMapTests<NavigableMultiMap<Long, Long>> {

	@Before
	public void setUp() throws Exception {
		ComparableComparator<Long> lc = ComparableComparator.instance();
		multiMap = new ComparatorBasedNavigableMultiMap<>(lc, lc);
	}

}
