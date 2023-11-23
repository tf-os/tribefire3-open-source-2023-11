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
package com.braintribe.model.meta.restriction;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.weaving.restriction.ProtoGmTypeRestriction;

/**
 * @author peter.gazdik
 */
public interface GmTypeRestriction extends ProtoGmTypeRestriction, GmModelElement {

	EntityType<GmTypeRestriction> T = EntityTypes.T(GmTypeRestriction.class);

	@Override
	List<GmType> getKeyTypes();
	void setKeyTypes(List<GmType> keyTypes);

	@Override
	List<GmType> getTypes();
	void setTypes(List<GmType> types);

	@Override
	boolean getAllowVd();
	@Override
	void setAllowVd(boolean allowVd);

	@Override
	boolean getAllowKeyVd();
	@Override
	void setAllowKeyVd(boolean allowKeyVd);
}
