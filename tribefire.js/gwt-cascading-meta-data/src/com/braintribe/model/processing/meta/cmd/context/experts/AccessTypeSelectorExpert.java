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

import com.braintribe.model.meta.selector.AccessTypeSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools;

/**
 */
@SuppressWarnings("unusable-by-js")
public class AccessTypeSelectorExpert implements CmdSelectorExpert<AccessTypeSelector> {

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(AccessTypeSelector selector) throws Exception {
		return MetaDataTools.aspects(AccessTypeAspect.class);
	}

	@Override
	public boolean matches(AccessTypeSelector selector, SelectorContext context) throws Exception {
		return AccessTypeSignatureSelectorExpert.matches(selector.getAccessType().getTypeSignature(), selector.getAssignable(), context);
	}

}
