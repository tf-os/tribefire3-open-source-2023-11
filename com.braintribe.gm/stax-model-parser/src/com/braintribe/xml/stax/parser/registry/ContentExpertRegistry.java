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

import com.braintribe.xml.stax.parser.experts.ContentExpert;

/**
 * a {@link ContentExpertRegistry} is used to link tags in the xml to experts that can handle the data contained. 
 * @author Pit
 *
 */
public interface ContentExpertRegistry {
	/**
	 * add a tag factory that is able to deliver a new instance of an expert for the tag
	 * @param tag - the tag in the XML
	 * @param factory - a factory that can return an instance of the expert 
	 */
	void addExpertFactory( String tag, ContentExpertFactory factory);
	
	/**
	 * return an instance of the content expert that has been declared for the tag 
	 * @param tag - the tag in the XML
	 * @return - an instance of the expert for the tag 
	 */
	ContentExpert getExpertForTag( String tag);
	
	
}
