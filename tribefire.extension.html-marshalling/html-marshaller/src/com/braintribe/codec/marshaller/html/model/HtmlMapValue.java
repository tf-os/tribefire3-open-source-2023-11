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
package com.braintribe.codec.marshaller.html.model;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author Christina Wilpernig
 * @author Neidhart.Orlich
 */

public interface HtmlMapValue extends HtmlValue {

	final EntityType<HtmlMapValue> T = EntityTypes.T(HtmlMapValue.class);
	
	Map<HtmlValue, HtmlValue> getEntries();
	void setEntries(Map<HtmlValue, HtmlValue> entries);
	
	String getKeyTypeSignature();
	void setKeyTypeSignature(String keyTypeSignature);
	
	String getValueTypeSignature();
	void setValueTypeSignature(String valueTypeSignature);
	
	@Override
	default RenderType renderType() {
		return RenderType.map;
	}

	@Override
	default boolean linkable() {
		return !getEntries().isEmpty();
	}	
}
