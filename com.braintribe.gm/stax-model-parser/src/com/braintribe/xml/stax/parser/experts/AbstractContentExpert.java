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
package com.braintribe.xml.stax.parser.experts;

public abstract class AbstractContentExpert implements ContentExpert {	
	protected StringBuilder buffer;
	protected String tag;
	protected String property;
	protected boolean skip = false;
	
	@Override
	public void characters(char[] ch, int start, int length) {
		if (skip)
			return;
		if (buffer == null) 
			buffer = new StringBuilder();
		buffer.append( ch, start, length);
	}

	@Override
	public void setTag(String tag) {		
		this.tag = tag;
	}

	@Override
	public String getTag() {		
		return tag;
	}

	@Override
	public String getProperty() {
		return property;
	}
	
	public void setSkip(boolean skip) {
		this.skip = skip;
	}
	
	
}
