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
package com.braintribe.spring.support.factory;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public class StringBasedFileFactory implements FactoryBean<File> {

	private String path;
	private boolean errorIsFatal = true;
	private boolean ignoreEmptyPath = true;
	private File parent;
	
	@Configurable @Required
	public void setPath(String path) {
		this.path = path;
	}
	@Configurable
	public void setErrorIsFatal(boolean errorIsFatal) {
		this.errorIsFatal = errorIsFatal;
	}
	@Configurable
	public void setIgnoreEmptyPath(boolean ignoreEmptyPath) {
		this.ignoreEmptyPath = ignoreEmptyPath;
	}
	@Configurable
	public void setParent(File parent) {
		this.parent = parent;
	}
	
	@Override
	public File getObject() throws Exception {
		
		if (ignoreEmptyPath && StringUtils.isEmpty(path)) {
			return null;
		}
		
		try {
			File file = new File(path);
			if (file.isAbsolute()) {
				return file;
			} else {
				return new File(parent,path);
			}
			
				
		} catch (Exception e) {
			if (errorIsFatal)
				throw e;
		}
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return File.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
