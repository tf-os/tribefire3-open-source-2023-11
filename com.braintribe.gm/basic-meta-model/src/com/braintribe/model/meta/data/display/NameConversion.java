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
package com.braintribe.model.meta.data.display;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.UniversalMetaData;

/**
 * Indicates that given model element's name should be converted and how.
 * <p>
 * This is useful to specify a name conversion for a wide range of elements with very few MD instances.
 * 
 * @author peter.gazdik
 */
public interface NameConversion extends UniversalMetaData {

	EntityType<NameConversion> T = EntityTypes.T(NameConversion.class);

	// NOTE these might not exist, so if needed, create it 
	String NAME_CONVERSION_SNAKE_CASE_GLOBAL_ID = "nameConversion:snakeCase";
	String NAME_CONVERSION_SCREAMING_SNAKE_CASE_GLOBAL_ID = "nameConversion:screamingSnakeCase";
	
	NameConversionStyle getStyle();
	void setStyle(NameConversionStyle style);

}
