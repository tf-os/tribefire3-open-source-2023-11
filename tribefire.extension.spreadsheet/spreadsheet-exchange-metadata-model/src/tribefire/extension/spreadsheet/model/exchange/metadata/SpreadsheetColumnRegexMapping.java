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

public interface SpreadsheetColumnRegexMapping extends SpreadsheetColumnFormatMapping {

	final EntityType<SpreadsheetColumnRegexMapping> T = EntityTypes.T(SpreadsheetColumnRegexMapping.class);
	
	String pattern = "pattern";
	String template = "template";
	
	@Mandatory
	String getPattern();
	void setPattern(String pattern);
	
	@Mandatory
	String getTemplate();
	void setTemplate(String template);
	
	static SpreadsheetColumnRegexMapping create(String pattern, String template) {
		SpreadsheetColumnRegexMapping mapping = SpreadsheetColumnRegexMapping.T.create();
		mapping.setPattern(pattern);
		mapping.setTemplate(template);
		return mapping;
	}
}
