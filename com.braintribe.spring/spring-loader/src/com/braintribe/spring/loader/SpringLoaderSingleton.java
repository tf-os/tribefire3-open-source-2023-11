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
package com.braintribe.spring.loader;

import org.springframework.core.io.Resource;

/**
 * 
 * @author pit
 *
 */
public class SpringLoaderSingleton {
	
	private static GenericSpringLoader loaderInstance;
	
	private static GenericSpringLoader getLoaderInstance() throws GenericSpringLoaderException {
		if (loaderInstance == null) {
			loaderInstance = new GenericSpringLoader( "spring.config.xml");
		}
		return loaderInstance;
	}
	public static Resource getResource( String name) throws GenericSpringLoaderException {
		return getLoaderInstance().getResource(name);
	}	
	
	public static <T> T getBean( String bean) throws GenericSpringLoaderException {
		return getLoaderInstance().getBean(bean);
	}
	
	
}
