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
package com.braintribe.model.processing.query.selection.experts;

import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;

public class PathAliasResolver extends AbstractAliasResolver {
	
	@Override
	public String getAliasForSource(Source source) {
		StringBuilder aliasBuilder = new StringBuilder();
		buildAliasForSource(aliasBuilder, source);
		return aliasBuilder.toString();
	}

	protected void buildAliasForSource(StringBuilder builder, Source source) {
		if (source instanceof From) {
			String typeSignature = ((From) source).getEntityTypeSignature();
			builder.append(resolveTypeSignature(typeSignature));
		} else if (source instanceof Join) {
			Join join = (Join) source;
			String property = join.getProperty();
			buildAliasForSource(builder, join.getSource());
			builder.append(".");
			builder.append(property);
		}

	}
		

}
