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

import static com.braintribe.model.generic.value.EntityReferenceType.global;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.utils.lcd.CommonTools;

/**
 * 
 */
public class EntRefHashingComparator implements HashingComparator<EntityReference> {

	/* Based on DEVCX-1032 we consider a null partition to be equal to any partition. For this reason we cannot have
	 * partition as part of the hash-code. */

	public static final EntRefHashingComparator INSTANCE = new EntRefHashingComparator();

	@Override
	public boolean compare(EntityReference e1, EntityReference e2) {
		if (e1.referenceType() != e2.referenceType())
			return false;

		/* Comparing IDs first, because this comparison is much faster in case the values are different (in case they
		 * are the same, signatures will probably also be the same) */

		// @formatter:off
		return 	e1.getRefId().equals(e2.getRefId()) &&
				e1.getTypeSignature().equals(e2.getTypeSignature()) &&
				// partition is ignored when using global references.
				// the only other option are persistent references, as preliminary would never have the same id  
				(e1.referenceType() == global || arePartitionsEqual(e1.getRefPartition(), e2.getRefPartition()));
		// @formatter:on
	}

	private boolean arePartitionsEqual(String p1, String p2) {
		return CommonTools.equalsOrBothNull(p1, p2) || EntityReference.ANY_PARTITION.equals(p1) || EntityReference.ANY_PARTITION.equals(p2);
	}

	@Override
	public int computeHash(EntityReference ref) {
		return PartitionIgnoringEntRefHashingComparator.INSTANCE.computeHash(ref);
	}
}
