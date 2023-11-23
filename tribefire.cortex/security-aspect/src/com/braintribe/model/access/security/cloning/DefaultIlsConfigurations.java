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
package com.braintribe.model.access.security.cloning;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Set;

import com.braintribe.model.access.security.cloning.experts.AclEntityAccessExpert;
import com.braintribe.model.access.security.cloning.experts.EntityVisibilityIlsExpert;
import com.braintribe.model.access.security.cloning.experts.PropertyVisibilityIlsExpert;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAnyType;
import com.braintribe.model.processing.security.query.PostQueryExpertConfiguration;
import com.braintribe.model.processing.security.query.expert.PostQueryExpert;

/**
 * 
 */
public class DefaultIlsConfigurations {

	private static final TypeCondition ANY_TYPE = IsAnyType.T.create();
	private static final Set<PostQueryExpertConfiguration> configs;

	static {
		configs = asSet( //
				config(new AclEntityAccessExpert()), //
				config(new EntityVisibilityIlsExpert()), //
				config(new PropertyVisibilityIlsExpert()) //
		);
	}

	private static PostQueryExpertConfiguration config(PostQueryExpert expert) {
		PostQueryExpertConfiguration result = new PostQueryExpertConfiguration();

		result.setExpert(expert);
		result.setTypeCondition(ANY_TYPE);

		return result;
	}

	public static Set<PostQueryExpertConfiguration> get() {
		return configs;
	}

}
