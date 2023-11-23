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
package com.braintribe.spring.support;

import java.io.File;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class FileResourceLoaderImpl extends DefaultResourceLoader implements ResourceLoader {

	protected File basePath = null;
	
	public FileResourceLoaderImpl(File basePath) {
		this.basePath = basePath;
	}
	
	@Override
	protected Resource getResourceByPath(String path) {
		return new FileSystemResource(new File(this.basePath, path));
	}
	
	public Resource getResource(String location) {
		return super.getResource(location);
	}

}
