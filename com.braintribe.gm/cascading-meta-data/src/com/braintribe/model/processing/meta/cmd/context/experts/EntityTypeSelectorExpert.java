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
package com.braintribe.model.processing.meta.cmd.context.experts;

import java.util.Collection;

import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.selector.EntitySignatureRegexSelector;
import com.braintribe.model.meta.selector.EntityTypeSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmEntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmEnumTypeAspect;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools;

/**
 * @see EntitySignatureRegexSelector
 */
public class EntityTypeSelectorExpert implements SelectorExpert<EntityTypeSelector> {

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(EntityTypeSelector selector) throws Exception {
		return MetaDataTools.aspects(GmEntityTypeAspect.class);
	}

	@Override
	public boolean matches(EntityTypeSelector selector, SelectorContext context) throws Exception {
		GmType gmType = context.get(GmEntityTypeAspect.class);

		if (gmType == null) {
			gmType = context.get(GmEnumTypeAspect.class);
		}

		if (gmType == null) {
			return false;
		}

		TypeCondition typeCondition = selector.getTypeCondition();

		if (typeCondition == null) {
			return false;
		}

		return typeCondition.matches(gmType);
	}
}
