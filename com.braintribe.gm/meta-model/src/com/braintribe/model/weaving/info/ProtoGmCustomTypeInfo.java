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
package com.braintribe.model.weaving.info;

import com.braintribe.model.weaving.ProtoGmCustomModelElement;
import com.braintribe.model.weaving.ProtoGmCustomType;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.data.ProtoHasMetaData;
import com.braintribe.model.weaving.override.ProtoGmCustomTypeOverride;

public interface ProtoGmCustomTypeInfo extends ProtoGmCustomModelElement, ProtoHasMetaData {

	ProtoGmMetaModel getDeclaringModel();

	/**
	 * @return actual ProtoGmCustomType for given info. If we call this on a {@link ProtoGmCustomType} itself, this method returns
	 *         that same instance, if we call it on a {@link ProtoGmCustomTypeOverride}, this method returns the type we are
	 *         overriding.
	 */
	ProtoGmCustomType addressedType();

	@Override
	default ProtoGmMetaModel declaringModel() {
		return getDeclaringModel();
	}

}
