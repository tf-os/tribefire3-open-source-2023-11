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

public interface SerializationCodes {
	static final byte CODE_NULL = 0;
	
	static final byte CODE_BOOLEAN = 1;
	static final byte CODE_INTEGER = 2;
	static final byte CODE_LONG = 3;
	static final byte CODE_FLOAT = 4;
	static final byte CODE_DOUBLE = 5;
	static final byte CODE_DECIMAL = 6;
	static final byte CODE_STRING = 7;
	static final byte CODE_DATE = 8;
	
	static final byte CODE_ENTITY = 9;
	static final byte CODE_ENUM = 10;
	
	static final byte CODE_LIST = 11;
	static final byte CODE_SET = 12;
	static final byte CODE_MAP = 13;
	
	static final byte CODE_CLOB = 14;
	
	static final byte CODE_REQUIRED_TYPES = 15;

	static final byte PROPERTY_TERMINATOR = 0; 
	static final byte PROPERTY_ABSENT = -1; 
	static final byte PROPERTY_DEFINED = 1;
	
	static final byte PROPERTY_NAME_PLAIN = 0;
	static final byte PROPERTY_NAME_DEF = 1; 
	static final byte PROPERTY_NAME_REF = 2;
	
	static final byte TYPE_SIG_PLAIN = 0;
	static final byte TYPE_SIG_DEF = 1; 
	static final byte TYPE_SIG_REF = 2;
	
	static final byte ENTITY_DEF = 0;
	static final byte ENTITY_REF = 1;
}
