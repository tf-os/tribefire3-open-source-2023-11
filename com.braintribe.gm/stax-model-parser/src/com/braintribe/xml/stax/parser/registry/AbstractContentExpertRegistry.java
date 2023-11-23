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

public class AbstractContentExpertRegistry implements ContentExpertRegistry {
	private Map<String, ContentExpertFactory> tagToExpertMap = new HashMap<String, ContentExpertFactory>();
	private Map<String, ContentExpertFactory> globalTagToExpertMap = new HashMap<String, ContentExpertFactory>();
	
	@Override
	public void addExpertFactory(String tag, ContentExpertFactory factory) {
		if (tag.startsWith("*")) {
			String suspect = tag.substring(tag.indexOf('/') + 1);			
			globalTagToExpertMap.put( suspect, factory);
		}
		else {
			tagToExpertMap.put( tag, factory);
		}
	}

	@Override
	public synchronized ContentExpert getExpertForTag(String tag) {		
		ContentExpertFactory factory = null;
		
		int indexOf = tag.lastIndexOf('/');
		// no last, so it must be the main container
		if (indexOf < 0) {
			factory = tagToExpertMap.get(tag);
			if (factory == null)
				return null;
		}		
		else {				
			// might be a global processing instruction
			String pi = tag.substring( indexOf + 1);
			factory = globalTagToExpertMap.get( pi);
			// no global, try standard expert 
			if (factory == null) {
				factory = tagToExpertMap.get(tag);
			}		
			// no standard, might be a repeating expert for children 
			if (factory == null) {
				String parent = tag.substring(0, indexOf);		
				factory = tagToExpertMap.get( parent + "/*");
				if (factory == null)
					return null;
			}
		}
		
		return factory.newInstance();
	}
	
	
	
}
