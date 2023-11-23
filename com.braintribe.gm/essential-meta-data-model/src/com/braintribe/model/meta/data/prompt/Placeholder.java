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

import com.braintribe.model.descriptive.HasLocalizedDescription;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.meta.data.ModelSkeletonCompatible;
import com.braintribe.model.meta.data.UniversalMetaData;

public interface Placeholder extends UniversalMetaData, HasLocalizedDescription, ModelSkeletonCompatible {

	EntityType<Placeholder> T = EntityTypes.T(Placeholder.class);
	
	default Placeholder placeholder(String placeholder) {
		GmSession session = this.session();
		
		if (session != null)
			setDescription(session.create(LocalizedString.T).putDefault(placeholder));
		else
			setDescription(LocalizedString.create(placeholder));
		
		return this;
	}
	
	static Placeholder create(LocalizedString placeholder) {
		Placeholder md = T.create();
		md.setDescription(placeholder);
		return md;
	}
	
	static Placeholder create(String placeholder) {
		return create(LocalizedString.create(placeholder));
	}

}
