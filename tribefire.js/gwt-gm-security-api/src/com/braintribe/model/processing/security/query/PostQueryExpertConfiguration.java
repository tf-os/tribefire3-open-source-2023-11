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
package com.braintribe.model.processing.security.query;

import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.processing.security.query.expert.PostQueryExpert;

/**
 * One configuration entry for {@link PostQueryExpert} used during post-processing of a query (i.e. the configuration
 * consists of a set of these entries). This entry specifies a {@link TypeCondition} and a {@linkplain PostQueryExpert}
 * which together tell which experts should be applied for which entity types.
 * <p>
 * The query post processing is a process that creates a copy of a query result, but may remove some parts of it, that
 * may be resolved as not being visible.
 * 
 */
public class PostQueryExpertConfiguration {

	private TypeCondition typeCondition;
	private PostQueryExpert expert;

	public TypeCondition getTypeCondition() {
		return typeCondition;
	}

	public void setTypeCondition(TypeCondition typeCondition) {
		this.typeCondition = typeCondition;
	}

	public PostQueryExpert getExpert() {
		return expert;
	}

	public void setExpert(PostQueryExpert expert) {
		this.expert = expert;
	}

}
