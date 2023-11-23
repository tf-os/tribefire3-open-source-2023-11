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
package com.braintribe.model.processing.query.planner.core.cross;

import java.util.List;
import java.util.Set;

import com.braintribe.model.processing.query.planner.context.OrderedSourceDescriptor;
import com.braintribe.model.query.From;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * 
 */
public class FromGroup {

	public final TupleSet tupleSet;
	public final Set<From> froms;
	public final List<OrderedSourceDescriptor> osds;
	public final int osdIndex;

	/**
	 * This <tt>id</tt> is used to identify a use-case when joining two FromGroups together, or a FromGroup and an index. The point is, every
	 * condition that joins the same pair of such objects must be identified as same use-case, so we can take advantage of taking more conditions at
	 * once.
	 * <p>
	 * Using IDs is better than creating a wrapper with special equals/hashCode methods.
	 */
	public final int id;

	private static int ID_COUNTER = 0;

	public FromGroup(TupleSet tupleSet, Set<From> froms, List<OrderedSourceDescriptor> osds) {
		this.tupleSet = tupleSet;
		this.froms = froms;
		this.osds = osds;
		this.osdIndex = minIndex(osds);
		this.id = ID_COUNTER++;
	}

	private static int minIndex(List<OrderedSourceDescriptor> osds) {
		return osds.stream().mapToInt(osd -> osd.index).min().orElse(Integer.MAX_VALUE);
	}

}
