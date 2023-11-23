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
package com.braintribe.gwt.async.client;

import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;

import com.google.gwt.core.client.GWT;

public class ResourceProvider<T> implements Supplier<Future<T>> {
	private Codec<T, String> codec;
	private String pathToResource;
	private Future<T> future;
	
	@Configurable @Required
	public void setCodec(Codec<T, String> codec) {
		this.codec = codec;
	}
	
	@Configurable
	public void setPathToResource(String pathToResource) {
		this.pathToResource = pathToResource;
	}
	
	@Configurable
	public void setNameOfResource(String nameOfResource) {
		//extract context name for the module
		String baseUrl = GWT.getHostPageBaseURL();
		if (baseUrl.endsWith("/"))
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		
		int index = baseUrl.lastIndexOf('/');
		String moduleContextName = baseUrl.substring(index + 1);
		String individualConfigContext = moduleContextName + "-config";
		
		pathToResource = "../../" + individualConfigContext + "/" + nameOfResource;
	}
	
	@Override
	public Future<T> get() throws RuntimeException {
		try {
			if (future == null) {
				future = LoaderChain
						.begin(AsyncUtils.loadStringResource(pathToResource))
						.decode(codec)
						.load();
			}
			return future;
		} catch (RuntimeException e) {
			throw new RuntimeException(e);
		}
	}
}
