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
package com.braintribe.wire.test.context.wirable;

public class Person {
	private String name;
	private boolean important;
	private Person mother;
	private Person partner;
	
	public void setMother(Person mother) {
		this.mother = mother;
	}
	
	public Person getMother() {
		return mother;
	}
	
	public void setPartner(Person partner) {
		this.partner = partner;
	}
	
	public Person getPartner() {
		return partner;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean getImportant() {
		return important;
	}
	
	public void setImportant(boolean important) {
		this.important = important;
	}
	
}
