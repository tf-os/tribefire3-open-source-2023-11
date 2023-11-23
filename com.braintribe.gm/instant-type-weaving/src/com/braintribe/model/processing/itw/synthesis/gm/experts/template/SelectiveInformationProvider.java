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
package com.braintribe.model.processing.itw.synthesis.gm.experts.template;

import java.util.List;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;


public class SelectiveInformationProvider implements Function<GenericEntity, String> {

	private final List<TemplatePart> parts;
	private final String pattern;
	private final String replacement;
	private final EntityType<?> entityType;

	public SelectiveInformationProvider(EntityType<?> entityType, List<TemplatePart> parts, String pattern, String replacement) {
		super();
		this.entityType = entityType;
		this.parts = parts;
		this.pattern = pattern;
		this.replacement = replacement;
	}

	@Override
	public String apply(GenericEntity index) throws RuntimeException {
		StringBuilder builder = new StringBuilder();
		for (TemplatePart part: parts) {
			part.append(builder, index, entityType);
		}

		String result = builder.toString();

		if (pattern != null) {
			result = result.replaceAll(pattern, replacement);
		}

		return result;
	}
}
