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
package com.braintribe.model.processing.meta.cmd.index;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.MdIndex;

/**
 * Key of a meta-data index (see {@link MdIndex}).
 */
public final class MetaDataIndexKey {

	private final EntityType<? extends MetaData> mdType;
	private final boolean inheritableOnly;

	public static MetaDataIndexKey forInherited(EntityType<? extends MetaData> metaDataType) {
		return new MetaDataIndexKey(metaDataType, true);
	}

	public static MetaDataIndexKey forAll(EntityType<? extends MetaData> metaDataType) {
		return new MetaDataIndexKey(metaDataType, false);
	}

	private MetaDataIndexKey(EntityType<? extends MetaData> mdType, boolean inheritableOnly) {
		this.mdType = mdType;
		this.inheritableOnly = inheritableOnly;
	}

	public EntityType<? extends MetaData> mdType() {
		return mdType;
	}

	public boolean inheritableOnly() {
		return inheritableOnly;
	}

	public MetaDataIndexKey indexKeyForAll() {
		return new MetaDataIndexKey(mdType, false);
	}

	@Override
	public boolean equals(Object obj) {
		/* This may seem weird, but basically the implementation of this artifact should ensure that the exception is
		 * never thrown, i.e. this class should be used in such way that it is only being check for equality with other
		 * instances of this class. Therefore, for performance reasons, we do not make an instanceof check, but
		 * optimistically assume we can do the cast right away. */
		try {
			MetaDataIndexKey otherKey = (MetaDataIndexKey) obj;
			return mdType == otherKey.mdType && inheritableOnly == otherKey.inheritableOnly;

		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 31 * mdType.hashCode() + (inheritableOnly ? 1231 : 1237);
	}

}
