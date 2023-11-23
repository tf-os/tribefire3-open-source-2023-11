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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.util.Map;
import java.util.function.Function;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;

import tribefire.extension.spreadsheet.model.exchange.api.request.ImportSpreadsheetRequest;

public abstract class SheetEntityStreamingContext<T extends ImportSpreadsheetRequest> {
	public abstract EntityType<GenericEntity> getImportTargetType();

	public abstract T getSpreadsheetImport();

	public abstract Function<String, String> getColumnNameAdapter();
	
	public abstract Map<String, Property> getProperties();

	public abstract ModelMdResolver getCmdrContextBuilder();

	public abstract Maybe<Object> convert(Object value, int rowNum, int cell, String columnName, Property property);
	
	public abstract void notifyRowCount(int rowCount);
	
	public abstract void notifyTotalRowCount(int rowCount);
}
