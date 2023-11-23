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
package tribefire.extension.okta.templates.util;

import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;

import tribefire.extension.okta.templates.wire.contract.OktaDbMappingsContract;

public class DbMapper {
	private OktaDbMappingsContract dbMappings;
	private ModelMetaDataEditor editor;

	public DbMapper(OktaDbMappingsContract dbMappings, ModelMetaDataEditor editor) {
		this.dbMappings = dbMappings;
		this.editor = editor;
	}

	public void applyDbMappings() {
		applyDbMappingsInternal();
	}

	private void applyDbMappingsInternal() {
		applyLengthMappings();
		applyIndices();
	}

	private void applyIndices() {
		// dbMappings.applyIndex(editor, AuthenticationToken.T, AuthenticationToken.token);
	}

	private void applyLengthMappings() {
		// editor.onEntityType(Entry.T).addPropertyMetaData(Entry.name, dbMappings.maxLen1k());

	}
}