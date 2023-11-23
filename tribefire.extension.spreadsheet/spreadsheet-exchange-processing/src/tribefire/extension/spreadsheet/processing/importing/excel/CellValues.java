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
package tribefire.extension.spreadsheet.processing.importing.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;

public interface CellValues {
	static Object getCellValue(Cell cell, GenericModelType targetType) {
		if (targetType == EssentialTypes.TYPE_STRING && cell.getCellStyle().getDataFormat() == 0) {
			return getGeneralCellValue(cell);
		}
		
		switch (cell.getCellType()) {
		case BLANK: return null;
		case ERROR: return cell.getErrorCellValue();
		case STRING: return cell.getStringCellValue();
		case BOOLEAN: return cell.getBooleanCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell))
				return cell.getDateCellValue();
			else
				return cell.getNumericCellValue();
		default:
			throw new UnsupportedOperationException("unsupported cell type " + cell.getCellType());
		}
	}
	
	static Object getGeneralCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case BLANK: return null;
		case ERROR: return cell.getErrorCellValue();
		case STRING: return cell.getStringCellValue();
		case BOOLEAN: 
			throw new IllegalStateException("'General' formatted BOOLEAN value [" + cell.getBooleanCellValue() + "] cannot be safely converted to string due to Excel locale sensitivity");
		case NUMERIC:
			double number = cell.getNumericCellValue();
			long integerPart = (long)number;
			double fraction = number - integerPart;
			if (fraction != 0)
				throw new IllegalStateException("'General' formatted NUMBER value [" +  number + "] that is not an integer cannot be safely converted to string due to Excel locale sensitivity");
			return Long.toString(integerPart);
		default:
			throw new UnsupportedOperationException("unsupported cell type " + cell.getCellType());
		}
	}
}
