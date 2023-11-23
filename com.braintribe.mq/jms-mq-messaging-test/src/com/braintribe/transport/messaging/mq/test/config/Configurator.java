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
package com.braintribe.transport.messaging.mq.test.config;

import java.io.File;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.braintribe.logging.Logger;

public class Configurator {

	private static final Logger logger = Logger.getLogger(Configurator.class);

	protected GenericApplicationContext applicationContext;

	public Configurator() throws Exception {
		this.initialize();
	}
	
	public void initialize() throws Exception {
		try {
			
			logger.info("Initializing context");

			applicationContext = new GenericApplicationContext();
			applicationContext.setClassLoader(this.getClass().getClassLoader());


			XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);

			File cfgFile = new File("res/main.xml");
			Resource resource = new FileSystemResource(cfgFile.getAbsolutePath());
			xmlReader.loadBeanDefinitions(resource);
			applicationContext.refresh();

			logger.info("Done initializing context");
			
			

		} catch(Throwable t) {
			logger.info("Error while initializing context", t);
			throw new RuntimeException("Error while initializing context", t);
		}
	}
	
	public void close() {
		this.applicationContext.close();
	}
	
	public GenericApplicationContext getContext() {
		return this.applicationContext;
	}
}
