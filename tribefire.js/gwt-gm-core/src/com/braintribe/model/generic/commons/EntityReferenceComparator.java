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
 * @deprecated Currently there are no usages, except for one also deprecated. I removed them all, as all of them just
 *             checked whether the result of {@link #compare} was equal to zero, and instead you can use
 *             {@link EntRefHashingComparator#compare(EntityReference, EntityReference)}
 */
@Deprecated
public class EntityReferenceComparator implements Comparator<EntityReference> {
	public final static EntityReferenceComparator INSTANCE = new EntityReferenceComparator();

	public static boolean isSameReferenceKind(EntityReference e1, EntityReference e2) {
		return e1.referenceType() == e2.referenceType();
	}

	@Override
	public int compare(EntityReference o1, EntityReference o2) {
		if (o1 == o2)
			return 0;

		if (!isSameReferenceKind(o1, o2)) {
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

		if (id1 != id2) {
			if (id1 == null)
				return -1;
			if (id2 == null)
				return 1;

			int res = id1.compareTo(id2);
			if (res != 0)
				return res;
		}

		String p1 = o1.getRefPartition();
		String p2 = o1.getRefPartition();

		if (p1 != p2) {
			if (p1 == null)
				return -1;
			if (p2 == null)
				return 1;

			return p1.compareTo(p2);
		}

		return 0;
	}

}
