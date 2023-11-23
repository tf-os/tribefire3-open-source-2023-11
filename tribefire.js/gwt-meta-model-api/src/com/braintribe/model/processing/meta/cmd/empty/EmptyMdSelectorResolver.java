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
package com.braintribe.model.processing.meta.cmd.empty;

import static java.util.Collections.emptySet;

import java.util.Collection;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.MdSelectorResolver;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;

public class EmptyMdSelectorResolver implements MdSelectorResolver {

	public static final EmptyMdSelectorResolver INSTANCE = new EmptyMdSelectorResolver();

	private EmptyMdSelectorResolver() {
	}

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(MetaDataSelector selector) throws Exception {
		return emptySet();
	}

	@Override
	public boolean matches(MetaDataSelector selector, SelectorContext context) throws Exception {
		return false;
	}

	@Override
	public SelectorExpert<?> findExpertFor(EntityType<? extends MetaDataSelector> et) {
		return null;
	}

}
