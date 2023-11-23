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
package tribefire.extension.scheduling.templates.util;

import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;

import tribefire.extension.scheduling.model.Scheduled;
import tribefire.extension.scheduling.model.context.StringMapContext;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingDbMappingsContract;

public class DbMapper {
	private SchedulingDbMappingsContract dbMappings;
	private ModelMetaDataEditor editor;

	public DbMapper(SchedulingDbMappingsContract dbMappings, ModelMetaDataEditor editor) {
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
		editor.onEntityType(Scheduled.T).addPropertyMetaData(Scheduled.errorMessage, dbMappings.maxLen4k());
		editor.onEntityType(StringMapContext.T).addPropertyMetaData(StringMapContext.data, dbMappings.maxLen4k());
	}
}