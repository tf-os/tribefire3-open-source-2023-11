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
package tribefire.extension.xml.schemed.test.commons.commons;

import java.io.File;
import java.io.FileInputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;

import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallRequest;
import tribefire.extension.xml.schemed.test.commons.xsd.test.resource.ResourceGenerator;
import tribefire.extension.xml.schemed.test.commons.xsd.test.resource.ResourceProvidingSession;

public class SchemedXmlXsdMarshallerRequestBuilder {
	protected static File res = new File( "res");
	private ResourceProvidingSession session = new ResourceProvidingSession();
	private StaxMarshaller modelMarshaller = new StaxMarshaller();
	
	@Configurable
	public void setSession(ResourceProvidingSession session) {
		this.session = session;
	}
	@Configurable
	public void setModelMarshaller(StaxMarshaller modelMarshaller) {
		this.modelMarshaller = modelMarshaller;
	}

	private ResourceProvidingSession getSession() {
		if (session == null) {
			session = new ResourceProvidingSession();
		}
		return session;
	}
	
	private StaxMarshaller getModelMarshaller() {
		if (modelMarshaller == null) {
			modelMarshaller = new StaxMarshaller();
		}
		return modelMarshaller;
	}
	
	
	public SchemedXmlMarshallerUnmarshallRequest buildRequest(File input, String xsdName, String model) {
		SchemedXmlMarshallerUnmarshallRequest request = SchemedXmlMarshallerUnmarshallRequest.T.create();
		
		request.setXml(  ResourceGenerator.filesystemResourceFromFile( getSession(), new File( input, xsdName)));
		
		try (FileInputStream stream = new FileInputStream( new File( input, model))) {
			request.setMappingModel((GmMetaModel) getModelMarshaller().unmarshall(stream));
		}
		catch (Exception e) {
			throw new IllegalStateException("cannot read mapping model", e);
		}		
		return request;		
	}
		
	public SchemedXmlMarshallerMarshallRequest buildRequest(File input, GenericEntity assembly, String model) {
		SchemedXmlMarshallerMarshallRequest request = SchemedXmlMarshallerMarshallRequest.T.create();
		request.setAssembly(assembly);
		try (FileInputStream stream = new FileInputStream( new File( input, model))) {
			request.setMappingModel((GmMetaModel) getModelMarshaller().unmarshall(stream));
		}
		catch (Exception e) {
			throw new IllegalStateException("cannot read mapping model", e);
		}
		
		return request;
	}
		
}
