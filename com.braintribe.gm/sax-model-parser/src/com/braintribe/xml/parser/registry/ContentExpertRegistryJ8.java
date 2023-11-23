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
package com.braintribe.xml.parser.registry;

import java.util.function.Supplier;

import com.braintribe.xml.parser.experts.ContentExpert;

/**
 * a {@link ContentExpertRegistryJ8} is used to link tags in the xml to experts that can handle the data contained. 
 * @author Pit
 *
 */
public interface ContentExpertRegistryJ8{
	/**
	 * add a tag factory that is able to deliver a new instance of an expert for the tag
	 * @param tag - the key 
	 * @param factory - the factory
	 */
	void addExpertFactory( String tag, Supplier<ContentExpert> supplier);
	ContentExpert getExpertForTag( String tag);
}
