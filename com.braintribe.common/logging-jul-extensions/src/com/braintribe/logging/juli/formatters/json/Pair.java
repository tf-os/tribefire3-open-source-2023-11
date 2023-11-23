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
package com.braintribe.logging.juli.formatters.json;

public class Pair<F, S> {
	private F firstOfTwo;
	private S secondOfTwo;

	public Pair(final F first, final S second) {
		this.firstOfTwo = first;
		this.secondOfTwo = second;
	}

	public Pair(final Pair<F, S> otherPair) {
		this(otherPair.first(), otherPair.second());
	}

	public F first() {
		return this.firstOfTwo;
	}

	public S second() {
		return this.secondOfTwo;
	}

	@Override
	public String toString() {
		return "(" + first() + "," + second() + ")";
	}

}
