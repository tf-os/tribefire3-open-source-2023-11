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
package com.braintribe.model.generic.commons;

import java.util.Comparator;

import com.braintribe.model.generic.value.EntityReference;

/**
 * @deprecated it seems it makes little sense to compare references while ignoring partitions
 */
@Deprecated
public class PartitionIgnoringEntityReferenceComparator implements Comparator<EntityReference> {

	public final static PartitionIgnoringEntityReferenceComparator INSTANCE = new PartitionIgnoringEntityReferenceComparator();

	@Override
	public int compare(EntityReference o1, EntityReference o2) {
		if (o1 == o2)
			return 0;

		if (o1.referenceType() != o2.referenceType()) {
			return o1.referenceType().compareTo(o2.referenceType());
		}

		String t1 = o1.getTypeSignature();
		String t2 = o2.getTypeSignature();

		if (t1 != t2) {
			if (t1 == null)
				return -1;

			if (t2 == null)
				return 1;

			int res = t1.compareTo(t2);
			if (res != 0)
				return res;
		}

		Comparable<Object> id1 = (Comparable<Object>) o1.getRefId();
		Comparable<Object> id2 = (Comparable<Object>) o2.getRefId();

		if (id1 == id2)
			return 0;
		if (id1 == null)
			return -1;
		if (id2 == null)
			return 1;

		return id1.compareTo(id2);
	}

}
