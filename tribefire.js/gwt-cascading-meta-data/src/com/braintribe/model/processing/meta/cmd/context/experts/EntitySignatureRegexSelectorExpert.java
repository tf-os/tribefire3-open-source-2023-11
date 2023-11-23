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

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.selector.EntitySignatureRegexSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmEntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools;

/**
 * @see EntitySignatureRegexSelector
 */
@SuppressWarnings("unusable-by-js")
public class EntitySignatureRegexSelectorExpert implements SelectorExpert<EntitySignatureRegexSelector> {

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(EntitySignatureRegexSelector selector) throws Exception {
		return MetaDataTools.aspects(GmEntityTypeAspect.class);
	}

	@Override
	public boolean matches(EntitySignatureRegexSelector selector, SelectorContext context) throws Exception {
		GmEntityType gmEntityType = context.get(GmEntityTypeAspect.class);

		if (gmEntityType == null) {
			return false;
		}

		return gmEntityType.getTypeSignature().matches(selector.getRegex());
	}

}
