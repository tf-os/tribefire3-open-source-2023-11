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
package com.braintribe.xml.stax.parser.registry;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.xml.stax.parser.experts.ContentExpert;

public class TagMapEntry implements ContentExpertFactory {
	private String tag;
	private Map<String, TagMapEntry> children = new HashMap<String, TagMapEntry>();
	private ContentExpertFactory expertFactory;
	
	
	public TagMapEntry(String tag, ContentExpertFactory expertFactory) {
		this.tag = tag;
		this.expertFactory = expertFactory;
	}
	
	public String getTag() {
		return tag;
	}
	
	public TagMapEntry getEntry( String child) {
		return children.get(child);
	}
	
	public void addChild( TagMapEntry entry) {
		children.put( entry.getTag(), entry);
	}
	
	@Override
	public ContentExpert newInstance() {
		return expertFactory.newInstance();
	}
	
	
}
