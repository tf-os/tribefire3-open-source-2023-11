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
package tribefire.cortex.services.tribefire_web_platform_test.impl.hardwired;

import java.util.Collection;
import java.util.Collections;

import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.experts.CmdSelectorExpert;

import tribefire.cortex.services.model.test.hardwired.mdselector.CustomMdSelector4Test;

/**
 * @author peter.gazdik
 */
public class CustomMdSelector4TestExpert implements CmdSelectorExpert<CustomMdSelector4Test> {

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(CustomMdSelector4Test selector) throws Exception {
		return Collections.emptyList();
	}

	@Override
	public boolean matches(CustomMdSelector4Test selector, SelectorContext context) throws Exception {
		return selector.getIsActive();
	}

}
