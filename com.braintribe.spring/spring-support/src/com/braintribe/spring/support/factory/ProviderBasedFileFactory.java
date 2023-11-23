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
import java.util.function.Supplier;

import org.springframework.beans.factory.FactoryBean;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public class ProviderBasedFileFactory implements FactoryBean<File> {

	private Supplier<File> provider;
	
	
	@Configurable @Required
	public void setProvider(Supplier<File> provider) {
		this.provider = provider;
	}
	
	@Override
	public File getObject() throws Exception {
		return provider.get();
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
