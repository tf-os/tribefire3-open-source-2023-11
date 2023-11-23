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
package com.braintribe.model.processing.detachrefs;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

/**
 * 
 * @author peter.gazdik
 */
public abstract class AbstractAccessBasedReferenceDetacher<T extends IncrementalAccess> extends AbstractReferenceDetacher {

	protected final T access;
	protected final CmdResolver cmdResolver;


	public AbstractAccessBasedReferenceDetacher(T access, CmdResolver cmdResolver) {
		this.access = access;
		this.cmdResolver = cmdResolver;
	}

	@Override
	protected CmdResolver acquireCmdResolver() {
		return cmdResolver;
	}

}
