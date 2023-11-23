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
package com.braintribe.model.processing.session.impl.managed;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.managed.ManipulationApplicationContext;
import com.braintribe.model.processing.session.api.managed.ManipulationApplicationContextBuilder;
import com.braintribe.model.processing.session.api.managed.ManipulationLenience;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;

/**
 * 
 */
public class BasicManipulationApplicationContext implements ManipulationApplicationContext, ManipulationApplicationContextBuilder {

	private final AbstractManagedGmSession session;
	private ManipulationMode mode;
	private ManipulationLenience lenience = ManipulationLenience.none;

	public BasicManipulationApplicationContext(AbstractManagedGmSession session) {
		this.session = session;
	}

	@Override
	public ManipulationReport apply(Manipulation manipulation) throws GmSessionException {
		return session.apply(manipulation, context());
	}

	@Override
	public ManipulationApplicationContext context() {
		return this;
	}

	@Override
	public ManipulationApplicationContextBuilder mode(ManipulationMode mode) {
		this.mode = mode;
		return this;
	}

	@Override
	public ManipulationMode getMode() {
		return mode;
	}
	
	@Override
	public ManipulationLenience getLenience() {
		return lenience;
	}
	
	@Override
	public ManipulationApplicationContextBuilder lenience(ManipulationLenience lenience) {
		this.lenience = lenience;
		return this;
	}
}
