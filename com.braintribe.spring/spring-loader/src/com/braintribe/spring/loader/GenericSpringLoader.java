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

import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * a helper for internal spring configuration <br/>
 * designed to access a jar-internal spring configuration 
 * 
 * 
 * @author pit
 *
 */
public class GenericSpringLoader {

	protected GenericApplicationContext applicationContext = null;
	
	/**
	 * scans for the configuration file in the classpath and loads it 
	 * @param configName - name of the configuration file 
	 */
	public GenericSpringLoader( String configName) throws GenericSpringLoaderException{
		applicationContext = new GenericApplicationContext();
		applicationContext.setClassLoader(this.getClass().getClassLoader());		
		applicationContext.setResourceLoader(new GenericResourceLoader());
		
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);        
	
		Resource resource = getResource( configName);		
				
		xmlReader.loadBeanDefinitions( resource);	
		applicationContext.refresh();
							
	}
	
	
	/**
	 * loads a resource either from the classpath or from the file system (as fallback) 
	 * @param name - name of the resource 
	 * @return - the {@link Resource}
	 * @throws GenericSpringLoaderException - if resource cannot be found 
	 */
	public Resource getResource( String name) throws GenericSpringLoaderException {
		URL url = applicationContext.getClassLoader().getResource( name);
		Resource resource = null;
		if (url != null) {
			resource = new UrlResource( url);									
		} else {
			resource = new FileSystemResource( name);			
		}
		if (resource.exists() == false) {
			throw new GenericSpringLoaderException( "cannot load configuration file [" + name + "] as it doesn't exist");
		}
		return resource;
	}
	
	/**
	 * returns a bean with the passed id 
	 * @param bean - the id of the bean 
	 * @return - the bean of type T 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean( String bean) {
		return (T) applicationContext.getBean(bean);
	}
	
	/**
	 * returns a bean of the correct type from the configuration, must be the only bean of that type in context 
	 * @param requiredType - the type of the bean to get
	 */
	public <T> T getBean( Class<T> requiredType) {
		return applicationContext.getBean(requiredType);
	}
	
	/**
	 * returns a {@link Scope} from the context.
	 * @param id - the id of the {@link Scope} as {@link String}
	 * @return - the instance of the class extending a {@link Scope}
	 */
	public <T extends Scope> T getScope(String id) {
		T scope = (T)applicationContext.getBeanFactory().getRegisteredScope( id);
		return scope;
	}	
	
}
