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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;

/**
 * @author peter.gazdik
 */
public interface ConstantMdDescriptor extends EnumRelatedMdDescriptor {

	EntityType<ConstantMdDescriptor> T = EntityTypes.T(ConstantMdDescriptor.class);

	/**
	 * Can be null if the MD is configured as default, on {@link GmMetaModel#getEnumConstantMetaData()} or
	 * {@link GmEnumTypeInfo#getEnumConstantMetaData()}
	 */
	GmEnumConstantInfo getOwnerConstantInfo();
	void setOwnerConstantInfo(GmEnumConstantInfo ownerConstantInfo);

	@Override
	default String origin() {
		GmEnumConstantInfo oci = getOwnerConstantInfo();
		return oci == null ? EnumRelatedMdDescriptor.super.origin() : oci.nameWithOrigin();
	}

}
