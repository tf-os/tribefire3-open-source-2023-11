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
package com.braintribe.wire.api.context;

import java.util.Collections;
import java.util.Iterator;

import com.braintribe.wire.api.scope.InstanceHolder;

public interface InstancePath extends Iterable<InstanceHolder> {
	int length();
	InstanceHolder lastElement();
	InstanceHolder firstElement();
	boolean isEmpty();
	Iterator<InstanceHolder> descendingIterator();
	
	static InstancePath empty() {
		return EmptyInstancePath.INSTANCE;
	}
}

class EmptyInstancePath implements InstancePath {
	
	public static final EmptyInstancePath INSTANCE = new EmptyInstancePath();

	@Override
	public Iterator<InstanceHolder> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public int length() {
		return 0;
	}

	@Override
	public InstanceHolder lastElement() {
		return null;
	}

	@Override
	public InstanceHolder firstElement() {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public Iterator<InstanceHolder> descendingIterator() {
		return Collections.emptyIterator();
	}

}
