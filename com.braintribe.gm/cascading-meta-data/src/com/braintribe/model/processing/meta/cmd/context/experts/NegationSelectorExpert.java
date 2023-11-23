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

import com.braintribe.model.meta.selector.NegationSelector;
import com.braintribe.model.processing.meta.cmd.context.LogicalSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.MdSelectorResolverImpl;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;

public class NegationSelectorExpert implements LogicalSelectorExpert<NegationSelector> {

	private final MdSelectorResolverImpl mdSelectorResolver;

	public NegationSelectorExpert(MdSelectorResolverImpl mdSelectorResolver) {
		this.mdSelectorResolver = mdSelectorResolver;
	}

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(NegationSelector selector) throws Exception {
		return mdSelectorResolver.getRelevantAspects(selector.getOperand());
	}

	@Override
	public boolean matches(NegationSelector selector, SelectorContext context) throws Exception {
		return !mdSelectorResolver.matches(selector.getOperand(), context);
	}

	@Override
	public Boolean maybeMatches(NegationSelector selector, SelectorContext context) throws Exception {
		Boolean maybeResult = mdSelectorResolver.maybeMatches(selector.getOperand(), context);
		return maybeResult == null ? null : !maybeResult;
	}

}
