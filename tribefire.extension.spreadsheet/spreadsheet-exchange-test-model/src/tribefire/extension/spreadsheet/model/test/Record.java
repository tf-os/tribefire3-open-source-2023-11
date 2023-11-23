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
package tribefire.extension.spreadsheet.model.test;

import java.util.Date;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Record extends TestRecord {
	EntityType<Record> T = EntityTypes.T(Record.class);
	
	String doubleValue = "doubleValue";
	String integerValue = "integerValue";
	String stringValue = "stringValue";
	String booleanValue = "booleanValue";
	String dateValue = "dateValue";
	
	double getDoubleValue();
	void setDoubleValue(double doubleValue);
	
	int getIntegerValue();
	void setIntegerValue(int integerValue);
	
	String getStringValue();
	void setStringValue(String stringValue);
	
	Date getDateValue();
	void setDateValue(Date dateValue);
	
	boolean getBooleanValue();
	void setBooleanValue(boolean booleanValue);
}
