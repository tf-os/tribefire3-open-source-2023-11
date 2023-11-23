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
package com.braintribe.wire.impl;

import java.awt.Button;

import com.braintribe.wire.api.EnrichedWireSpace;
import com.braintribe.wire.api.ImportFieldRecorder;
import com.braintribe.wire.api.annotation.Import;

public class BytecodeLab extends X implements EnrichedWireSpace {
	@Import
	private String field1;
	@Import
	private String field2;
	
	public static void main(String[] args) {
		Runnable r = () -> System.out.println("foobar");
	}
	
	@Override
	public void __listImportFields(ImportFieldRecorder recorder) {
		if (EnrichedWireSpace.class.isAssignableFrom(BytecodeLab.class.getSuperclass()))
			super.__listImportFields(recorder);
		
		recorder.record(BytecodeLab.class, String.class, 0);
		recorder.record(BytecodeLab.class, String.class, 1);
	}

	@Override
	public void __setImportField(Class<?> atClass, int index, Object value) {
		if (BytecodeLab.class != atClass) {
			// super.__setImportField(atClass, index, value);
			return;
		}
		
		switch (index) {
		case 0:
			field1 = (String)value;
			break;
		case 1:
			field2 = (String)value;
			break;
		default:
			throw new IllegalArgumentException("index out of bounds");
		}
	}

}

class X implements EnrichedWireSpace {
	@Import
	private Integer fieldA;
	@Import
	private Integer fieldB;

	
	@Override
	public void __listImportFields(ImportFieldRecorder recorder) {
		if (EnrichedWireSpace.class.isAssignableFrom(X.class.getSuperclass())) {
			// super.__listImportFields(recorder);
		}
		
		recorder.record(X.class, Integer.class, 0);
		recorder.record(X.class, Integer.class, 1);
	}
	
	public void __setImportField_fieldA(Integer value) {
		fieldA = value;
	}
	
	@Override
	public void __setImportField(Class<?> atClass, int index, Object value) {
		if (X.class != atClass) {
			// super.__setImportField(atClass, index, value);
			return;
		}

		switch (index) {
		case 0:
			fieldA = (Integer)value;
			break;
		case 1:
			fieldB = (Integer)value;
			break;
		default:
			throw new IllegalArgumentException("index out of bounds");
		}

	}
}

