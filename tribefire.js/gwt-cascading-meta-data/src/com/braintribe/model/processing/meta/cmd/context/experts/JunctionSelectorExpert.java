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

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Set;

import com.braintribe.model.meta.selector.JunctionSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.context.LogicalSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.MdSelectorResolverImpl;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;

@SuppressWarnings("unusable-by-js")
abstract class JunctionSelectorExpert<T extends JunctionSelector> implements LogicalSelectorExpert<T> {

	protected final MdSelectorResolverImpl mdSelectorResolver;

	public JunctionSelectorExpert(MdSelectorResolverImpl mdSelectorResolver) {
		this.mdSelectorResolver = mdSelectorResolver;
	}

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(T selector) throws Exception {
		Set<Class<? extends SelectorContextAspect<?>>> result = newSet();

		for (MetaDataSelector mds : selector.getOperands())
			result.addAll(mdSelectorResolver.getRelevantAspects(mds));

		return result;
	}

}
