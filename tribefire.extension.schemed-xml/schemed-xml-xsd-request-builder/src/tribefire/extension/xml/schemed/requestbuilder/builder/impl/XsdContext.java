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
package tribefire.extension.xml.schemed.requestbuilder.builder.impl;

import java.io.File;
import java.util.function.Supplier;

import com.braintribe.model.resource.Resource;

import tribefire.extension.xml.schemed.requestbuilder.resource.ResourceGenerator;
import tribefire.extension.xml.schemed.requestbuilder.resource.ResourceProvidingSession;

/**
 * a context to handle the input XSDs 
 * @author pit
 *
 * @param <T>
 */
public class XsdContext<T extends XsdResourceConsumer> {		
	private T consumer;
	private Supplier<ResourceProvidingSession> sessionSupplier;
	private Resource resource;
	private String terminalName;
	
	
	private ResourceProvidingSession getSession() {		
		return sessionSupplier.get();		
	}
	public XsdContext(T consumer, Supplier<ResourceProvidingSession> sessionSuplier) {
		this.consumer = consumer;
		this.sessionSupplier = sessionSuplier;
	}
	
	

	/**
	 * the simplest case : the single XSD (or the main XSD if references are used)
	 * @param file - the {@link File} that contains the XSD
	 * @return - this context
	 */
	public XsdContext<T> file(File file) {
		resource = ResourceGenerator.filesystemResourceFromFile( getSession(), file);
		return this;
	}
	



	/**
	 * if you have multiple XSDs in a zip file, use this: specifiy the zip-file and the name of
	 * the top XSD (just the last part of the ZipEntry's name
	 * @param file - the zipfile
	 * @param main - the name of top XSD file
	 * @return
	 */
	public XsdContext<T> archive( File file, String main) {
		resource = ResourceGenerator.filesystemResourceFromFile( getSession(), file);
		terminalName = main;
		return this;
	}
	


	/**
	 * finish and return to parent context
	 * @return - the parent context
	 */
	public T close() {
		if (terminalName == null) {
			consumer.accept(resource);
		}
		else {
			consumer.accept(resource, terminalName);
		}
		return consumer;
	}
}
