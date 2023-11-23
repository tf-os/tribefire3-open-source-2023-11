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
package tribefire.extension.spreadsheet.model.exchange.metadata;


import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface SpreadsheetColumnNumberFormatMapping extends SpreadsheetColumnFormatMapping {

	final EntityType<SpreadsheetColumnNumberFormatMapping> T = EntityTypes.T(SpreadsheetColumnNumberFormatMapping.class);
	
	String digitGroupingSymbol = "digitGroupingSymbol";
	String decimalSeparator = "decimalSeparator";
	
	@Mandatory
	String getDigitGroupingSymbol();
	void setDigitGroupingSymbol(String digitGroupingSymbol);
	
	@Mandatory
	String getDecimalSeparator();
	void setDecimalSeparator(String decimalSeparator);
	
	static SpreadsheetColumnNumberFormatMapping create(String decimalSeparator) {
		SpreadsheetColumnNumberFormatMapping mapping = SpreadsheetColumnNumberFormatMapping.T.create();
		mapping.setDecimalSeparator(decimalSeparator);
		return mapping;
	}
	
	static SpreadsheetColumnNumberFormatMapping create(String decimalSeparator, String digitGroupingSymbol) {
		SpreadsheetColumnNumberFormatMapping mapping = create(decimalSeparator);
		mapping.setDigitGroupingSymbol(digitGroupingSymbol);
		return mapping;
	}
}
