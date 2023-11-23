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
package tribefire.extension.spreadsheet.exchange_processing.test;

import java.io.FileInputStream;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.model.generic.typecondition.basic.IsTypeKind;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.utils.lcd.CollectionTools;

import tribefire.extension.spreadsheet.model.exchange.api.data.ExportModelColumn;
import tribefire.extension.spreadsheet.model.exchange.api.data.ModelSpreadsheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ExportModelSpreadsheet;

public class SpreadsheetModelExportTests extends SpreadsheetExchangeProcessingTestBase implements TestConstants {
	
	@Test
	public void textStringPropertyExportWithSelect() throws IOException {
		IsTypeKind isStringType = IsTypeKind.T.create();
		isStringType.setKind(TypeKind.stringType);
		
		ExportModelSpreadsheet exportModelSpreadsheet = ExportModelSpreadsheet.T.create();
		exportModelSpreadsheet.setDomainId(ACCESS_IMPORT);
		exportModelSpreadsheet.setDelimiter(";");
		exportModelSpreadsheet.setSelect(CollectionTools.getList(
				ExportModelColumn.model, 
				ExportModelColumn.packageName, 
				ExportModelColumn.simpleTypeName, 
				ExportModelColumn.propertyName));
		exportModelSpreadsheet.setPropertyTypeFilter(isStringType);
		
		ModelSpreadsheet modelSpreadsheet = exportModelSpreadsheet.eval(evaluator).get();

		Assertions.assertThat(modelSpreadsheet.getSheet().openStream()).hasSameContentAs(new FileInputStream("res/export/model-export-access.import-filtered-selected.csv"));
	}
}
