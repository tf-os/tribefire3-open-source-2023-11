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
package com.braintribe.model.generic.reflection;

import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * @author peter.gazdik
 */
@GmSystemInterface
public abstract class GmtsPlainEntityStub extends GmtsEntityStub {

	@Override
	public final boolean isEnhanced() {
		return false;
	}

	@Override
	public final void write(Property p, Object value) {
		p.setDirectUnsafe(this, value);
	}

	@Override
	public final Object read(Property p) {
		return p.getDirectUnsafe(this);
	}

	@Override
	public void writeVd(Property p, ValueDescriptor value) {
		p.setVdDirect(this, value);
	}

	@Override
	public ValueDescriptor readVd(Property p) {
		return p.getVdDirect(this);
	}
	
	@Override
	public final GmSession session() {
		return null;
	}

	@Override
	public final void attach(GmSession session) {
		throw new UnsupportedOperationException("Cannot attach session to a plain entity.");
	}

	@Override
	public final GmSession detach() {
		throw new UnsupportedOperationException("Cannot detach session from a plain entity.");
	}

}
