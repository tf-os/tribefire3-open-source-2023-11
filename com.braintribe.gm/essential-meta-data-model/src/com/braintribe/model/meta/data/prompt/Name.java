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
package com.braintribe.model.meta.data.prompt;

import com.braintribe.model.descriptive.HasLocalizedName;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.meta.data.ModelSkeletonCompatible;
import com.braintribe.model.meta.data.UniversalMetaData;

public interface Name extends UniversalMetaData, HasLocalizedName, ModelSkeletonCompatible {

	EntityType<Name> T = EntityTypes.T(Name.class);
	
	default Name name(String name) {
		GmSession session = this.session();
		
		if (session != null)
			setName(session.create(LocalizedString.T).putDefault(name));
		else
			setName(LocalizedString.create(name));
		
		return this;
	}
	
	static Name create(LocalizedString name) {
		Name md = T.create();
		md.setName(name);
		return md;
	}
	
	static Name create(String name) {
		return create(LocalizedString.create(name));
	}

}
