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

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractContentExpertFactory implements ContentExpertFactory {
	private Map<String, ContentExpertFactory> tagToFactoryMap = new HashMap<>();
	

	@Override
	public void chainFactory(String tag, ContentExpertFactory factory) {
		tagToFactoryMap.put(tag, factory);
	}

	@Override
	public ContentExpertFactory getChainedFactory(String tag) {
		return tagToFactoryMap.get(tag);
	}

}
