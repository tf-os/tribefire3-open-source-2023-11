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
package com.braintribe.model.processing.meta.cmd.extended;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;

/**
 * @author peter.gazdik
 */
@Abstract
public interface EntityRelatedMdDescriptor extends MdDescriptor {

	EntityType<EntityRelatedMdDescriptor> T = EntityTypes.T(EntityRelatedMdDescriptor.class);

	/** The type we have given to the CMD resolver as the base for the MD resolution. */
	GmEntityType getResolvedForType();
	void setResolvedForType(GmEntityType resolvedForType);

	/** The {@link GmEntityTypeInfo} which holds our MD. Only null if the MD is configured as default. */
	GmEntityTypeInfo getOwnerTypeInfo();
	void setOwnerTypeInfo(GmEntityTypeInfo ownerTypeInfo);

	/**
	 * @return <tt>true</tt> iff this {@link MetaData} was inherited from a (proper) super-type of the type we were
	 *         resolving for (see {@link #getResolvedForType()}).
	 */
	default boolean isInherited() {
		GmEntityTypeInfo oti = getOwnerTypeInfo();
		return oti != null && getResolvedForType() != getOwnerTypeInfo().addressedType();
	}

	@Override
	default String origin() {
		GmEntityTypeInfo oti = getOwnerTypeInfo();
		return oti == null ? MdDescriptor.super.origin() : oti.nameWithOrigin();
	}

}
