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
package com.braintribe.wire.test.aggregate.payload;

public class TestNode {
	private TestNode next;
	private TestNode altNext;
	private TestNode extraNext;
	private String name;
	
	public TestNode(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setExtraNext(TestNode extraNext) {
		this.extraNext = extraNext;
	}
	
	public TestNode getExtraNext() {
		return extraNext;
	}
	
	public void setAltNext(TestNode altNext) {
		this.altNext = altNext;
	}
	
	public TestNode getAltNext() {
		return altNext;
	}
	
	public void setNext(TestNode next) {
		this.next = next;
	}
	
	public TestNode getNext() {
		return next;
	}
}
