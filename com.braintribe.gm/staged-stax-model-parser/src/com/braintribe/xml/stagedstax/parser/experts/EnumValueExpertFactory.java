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
package com.braintribe.xml.stagedstax.parser.experts;

import com.braintribe.xml.stagedstax.parser.factory.AbstractContentExpertFactory;
import com.braintribe.xml.stagedstax.parser.factory.ContentExpertFactory;

public class EnumValueExpertFactory extends AbstractContentExpertFactory implements ContentExpertFactory {
	
	private String enumClass;
	private String property;
	
	public EnumValueExpertFactory(String enumClass) {
		this.enumClass = enumClass;
	}
	public EnumValueExpertFactory(String enumClass, String property) {
		this.enumClass = enumClass;
		this.property = property;
	}
	
	@Override
	public ContentExpert newInstance() {
		return new EnumValueExpert(enumClass, property);
	}
	
	
	
	

}
