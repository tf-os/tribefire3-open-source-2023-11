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
package com.braintribe.doc.lunr;

public class FieldRef {
	private static final char joiner = '/';
	public String docRef;
	public String fieldName;
	private String _stringValue;
	
	public FieldRef(String docRef, String fieldName, String stringValue) {
	  this.docRef = docRef;
	  this.fieldName = fieldName;
	  this._stringValue = stringValue;
	}
	
	public FieldRef(String docRef, String fieldName) {
		this.docRef = docRef;
		this.fieldName = fieldName;
	}

		

	public static FieldRef fromString(String s) {
	  int n = s.indexOf(joiner);

	  if (n == -1) {
	    throw new IllegalStateException("malformed field ref string");
	  }

	  String fieldName = s.substring(0, n);
	  String docRef = s.substring(n + 1);

	  return new FieldRef (docRef, fieldName, s);
	}

	@Override
	public String toString() {
	  if (this._stringValue == null) {
	    this._stringValue = fieldName + joiner + docRef;
	  }

	  return _stringValue;
	}

}
