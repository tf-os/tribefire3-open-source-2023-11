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

import com.braintribe.model.meta.selector.ConjunctionSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.context.MdSelectorResolverImpl;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;

public class ConjunctionSelectorExpert extends JunctionSelectorExpert<ConjunctionSelector> {

	public ConjunctionSelectorExpert(MdSelectorResolverImpl mdSelectorResolver) {
		super(mdSelectorResolver);
	}

	@Override
	public boolean matches(ConjunctionSelector selector, SelectorContext context) throws Exception {
		for (MetaDataSelector mds : selector.getOperands())
			if (!mdSelectorResolver.matches(mds, context))
				return false;

		return true;
	}

	@Override
	public Boolean maybeMatches(ConjunctionSelector selector, SelectorContext context) throws Exception {
		boolean nullEncountered = false;

		for (MetaDataSelector mds : selector.getOperands()) {
			Boolean maybeMatches = mdSelectorResolver.maybeMatches(mds, context);

			if (maybeMatches == null)
				nullEncountered = true;
			else if (!maybeMatches)
				return false;
		}

		return nullEncountered ? null : true;
	}
}
