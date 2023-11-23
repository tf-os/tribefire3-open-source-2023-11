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
package com.braintribe.model.processing.dataio;

public interface GmSerializationCodes {
	static final byte CODE_NULL = 0;
	
	static final byte CODE_FALSE = 1;
	static final byte CODE_TRUE = 2;
	
	static final byte CODE_INTEGER = 3; 
	static final byte CODE_LONG = 4; 
	
	static final byte CODE_FLOAT = 5;
	static final byte CODE_DOUBLE = 6;
	static final byte CODE_DECIMAL = 7;
	
	static final byte CODE_STRING = 8;
	static final byte CODE_DATE = 9;
	
	static final byte CODE_REF = 10;
	static final byte CODE_ENUM = 11;
	
	static final byte CODE_LIST = 12;
	static final byte CODE_SET = 13;
	static final byte CODE_MAP = 14;
	
	static final byte CODE_ESCAPE = 15;
	
	// don't add more codes here
	// 0xff -> prop term
}

/*
 * bit shift + if
 * vs.
 * in.readShort()
 */
