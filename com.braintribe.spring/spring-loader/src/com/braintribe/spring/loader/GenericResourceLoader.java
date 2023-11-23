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

import java.net.URL;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

/**
 * extends the {@link DefaultResourceLoader} and can load a {@link Resource}
 * @author pit
 *
 */
public class GenericResourceLoader extends DefaultResourceLoader implements ResourceLoader{

	@Override
	protected Resource getResourceByPath(String name) {
		URL url = this.getClassLoader().getResource( name);
		Resource resource = null;
		if (url != null) {
			resource = new UrlResource( url);									
		} else {
			resource = new FileSystemResource( name);			
		}
		if (resource.exists() == false) {		
			return null;
		}
		return resource;
	}

	
}
