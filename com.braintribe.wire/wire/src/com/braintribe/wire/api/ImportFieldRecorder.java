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
package com.braintribe.wire.api;

import java.util.ArrayList;
import java.util.List;

public class ImportFieldRecorder {
	private List<ImportField> importFields = new ArrayList<>();
	private EnrichedWireSpace enhancedWireSpace;
	
	public ImportFieldRecorder(EnrichedWireSpace enhancedWireSpace) {
		super();
		this.enhancedWireSpace = enhancedWireSpace;
	}

	public void record(Class<?> origin, Class<?> type, int key) {
		importFields.add(new RecorderImportField(origin, type, key));
	}
	
	public List<ImportField> getImportFields() {
		return importFields;
	}
	
	private class RecorderImportField implements ImportField {
		Class<?> origin;
		Class<?> type;
		int key;

		public RecorderImportField(Class<?> origin, Class<?> type, int key) {
			super();
			this.origin = origin;
			this.type = type;
			this.key = key;
		}

		@Override
		public void set(Object value) {
			enhancedWireSpace.__setImportField(origin, key, value);
		}
		
		@Override
		public Class<?> type() {
			return type;
		}
	}
}
