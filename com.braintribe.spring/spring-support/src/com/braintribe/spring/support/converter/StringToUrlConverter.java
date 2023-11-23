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
package com.braintribe.spring.support.converter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;



/**
 * a converter from string to URL
 * 
 * if a fully validated URL is passed, will use that.
 * otherwise it will ask the application context to deliver one.
 * 
 * @author pit
 *
 */
public class StringToUrlConverter implements Converter<String, URL>, ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	private Map<String, Supplier<File>> rootDirProviderMap;
	private Map<String, Supplier<URL>> rootUrlProviderMap;
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setRootDirProviderMap(Map<String, Supplier<File>> rootDirProviderMap) {
		this.rootDirProviderMap = rootDirProviderMap;
	}

	public void setRootUrlProviderMap(Map<String, Supplier<URL>> rootUrlProviderMap) {
		this.rootUrlProviderMap = rootUrlProviderMap;
	}

	@Override
	public URL convert(String source) {
		if (source == null || source.trim().length() == 0) {
			return null;
		} else {
			
			URL assembledURL = assembleByRootDirProvider(source);
			if (assembledURL != null)
				return assembledURL;
			
			assembledURL = assembleByRootUrlProvider(source);
			if (assembledURL != null)
				return assembledURL;

			try {				
				return new URL( source);
			} catch (MalformedURLException e) {
				Resource resource = applicationContext.getResource(source);
				try {
					return resource.getURL();
				} catch (IOException e1) {
					throw new IllegalArgumentException("resource with name " + source + " could not be located", e1);
				}			
			}
		}
		
	}
	
	/**
	 * Tries to create an {@link URL} object from the {@code source} parameter using a
	 * root directory provider configured through {@link #setRootDirProviderMap(Map)}
	 * @param source
	 * @return {@link URL} assembled by combining the {@code source} parameter with the root directory configured for its prefix.
	 */
	private URL assembleByRootDirProvider(String source) {
		return assembleByRootProviderMap(source, rootDirProviderMap);
	}
	
	/**
	 * Tries to create an {@link URL} object from the {@code source} parameter using a
	 * root url provider configured through {@link #setRootUrlProviderMap(Map)}
	 * @param source
	 * @return {@link URL} assembled by combining the {@code source} parameter with the root url configured for its prefix.
	 */
	private URL assembleByRootUrlProvider(String source) {
		return assembleByRootProviderMap(source, rootUrlProviderMap);
	}
	
	/**
	 * Tries to create an {@link URL} object from the {@code source} parameter using a root path 
	 * representation provider configured in the given {@code rootPathProviderMap} parameter
	 * matching {@code source}'s prefix.
	 * <p>
	 * Will return {@code null} if:
	 * <ul>
	 *   <li>No root path provider was configured to {@link #rootPathProviderMap}</li>
	 *   <li>No prefix is found in the {@code source} parameter</li>
	 *   <li>The prefix found in the {@code source} parameter does not map to a configured root path representation provider</li>
	 * </ul>
	 * 
	 * @param source
	 * @return {@link URL} assembled by combining the {@code source} parameter with the root path representation configured for its prefix.
	 */
	private <T> URL assembleByRootProviderMap(String source, Map<String, Supplier<T>> rootPathProviderMap) {
		
		if (isEmpty(rootPathProviderMap)) 
			return null;

		String prefix = prefix(source);
		if (prefix == null)
			return null;
		
		Supplier<T> rootUrlProvider = rootPathProviderMap.get(prefix);
		if (rootUrlProvider == null)
			return null;
		
		return assemble(rootUrlProvider, source);
	}
	
	private String prefix(String source) {
		int prefixIndex = source.indexOf(":");
		if (prefixIndex == -1)
			return null;
		return source.substring(0, prefixIndex).toLowerCase();
	}
	
	private String specific(String source) {
		return source.substring(source.indexOf(":")+1);
	}
	
	private <T> URL assemble(Supplier<T> rootPathProvider, String source) {
		
		T root;
		try {
			root = rootPathProvider.get();
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("provider [ "+rootPathProvider+" ] failed to provide a root path for [ "+source+" ] "+e.getMessage(), e);
		}
		
		if (root == null)
			throw new IllegalArgumentException("provider [ "+rootPathProvider+" ] provided a null root path representation for source [ "+source+" ] ");

		String specific = specific(source);

		try {
			if (root instanceof URL)
				return assemble((URL)root, specific);
			if (root instanceof File)
				return assemble((File)root, specific);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("root path [ "+root+" ] assembled with source [ " +specific+ " ] resulted in an invalid URL", e);
		}

		throw new IllegalArgumentException("root path representation [ "+root+" ] configured with unexpected type");
	}
	
	private URL assemble(URL urlRoot, String specific) throws MalformedURLException {
		return new URL(urlRoot, specific);
	}

	private URL assemble(File fileRoot, String specific) throws MalformedURLException {
		return (new File(fileRoot, specific)).toURI().toURL();
	}
	
	private <K, V> boolean isEmpty(Map<K, V> map) {
		return (map == null || map.isEmpty());
	}

}
