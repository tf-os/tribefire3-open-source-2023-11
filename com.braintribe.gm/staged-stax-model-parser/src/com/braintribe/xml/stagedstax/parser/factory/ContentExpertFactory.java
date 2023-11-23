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
package com.braintribe.xml.stagedstax.parser.factory;

import com.braintribe.xml.stagedstax.parser.experts.ContentExpert;

/**
 * a {@link ContentExpertFactory} is a factory that can return instances of an {@link ContentExpert}
 * @author Pit
 *
 */
public interface ContentExpertFactory {
	/**
	 * return an instance of the expert
	 * @return - a {@link ContentExpert}
	 */
	ContentExpert newInstance();
	
	void chainFactory( String tag, ContentExpertFactory factory);
	
	ContentExpertFactory getChainedFactory( String tag);
}
