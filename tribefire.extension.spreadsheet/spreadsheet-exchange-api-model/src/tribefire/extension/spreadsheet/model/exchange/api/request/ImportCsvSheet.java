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
package tribefire.extension.spreadsheet.model.exchange.api.request;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ImportCsvSheet extends ImportSpreadsheetRequest {
	EntityType<ImportCsvSheet> T = EntityTypes.T(ImportCsvSheet.class);

	String delimiter = "delimiter";
	String charset = "charset";
	
	String getDelimiter();
	void setDelimiter(String delimiter);
	
	String getCharset();
	void setCharset(String charset);
	
	@Description("Sets the explicit usecase to be used for metadata resolution. The import file type specific use case 'csv-import' will always be applied.")
	@Override
	String getUseCase();
}
