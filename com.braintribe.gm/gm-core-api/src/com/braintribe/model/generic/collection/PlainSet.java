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
package com.braintribe.model.generic.collection;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.braintribe.model.generic.reflection.SetType;

/**
 * @author peter.gazdik
 */
public class PlainSet<E> extends LinkedHashSet<E> implements SetBase<E> {

	private static final long serialVersionUID = -6029303086432557408L;

	private final SetType setType;

	public PlainSet(SetType setType) {
		this.setType = setType;
	}

	public PlainSet(SetType setType, Collection<? extends E> c) {
		super(c);
		this.setType = setType;
	}

	@Override
	public SetType type() {
		return setType;
	}

}
