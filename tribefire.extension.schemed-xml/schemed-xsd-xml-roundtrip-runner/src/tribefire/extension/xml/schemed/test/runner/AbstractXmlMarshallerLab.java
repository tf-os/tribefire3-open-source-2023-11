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
package tribefire.extension.xml.schemed.test.runner;

import java.io.File;

import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

import tribefire.extension.xml.schemed.marshaller.xml.SchemedXmlMarshallingRequestProcessor;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallResponse;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallResponse;
import tribefire.extension.xml.schemed.test.commons.commons.SchemedXmlXsdMarshallerRequestBuilder;
import tribefire.extension.xml.schemed.test.commons.xsd.test.resource.ResourceProvidingSession;

public abstract class AbstractXmlMarshallerLab {
	protected static File res = new File( "res");
	private ResourceProvidingSession session = new ResourceProvidingSession();
	private StaxMarshaller modelMarshaller = new StaxMarshaller();
	private SchemedXmlXsdMarshallerRequestBuilder requestBuilder;
	private SchemedXmlMarshallingRequestProcessor requestProcessor = new SchemedXmlMarshallingRequestProcessor();
	
	public SchemedXmlXsdMarshallerRequestBuilder getRequestBuilder() {
		if (requestBuilder == null) {
			requestBuilder = new SchemedXmlXsdMarshallerRequestBuilder();
			requestBuilder.setSession(session);
			requestBuilder.setModelMarshaller( modelMarshaller);
		}
		return requestBuilder;
	}
	

	private void initialize() {
		
	}

	protected SchemedXmlMarshallerUnmarshallRequest buildRequest(File input, String xsdName, String model) {		
		return getRequestBuilder().buildRequest(input, xsdName, model);				
	}
	
	protected SchemedXmlMarshallerUnmarshallResponse process( SchemedXmlMarshallerUnmarshallRequest request) {
		initialize();
		return requestProcessor.process(request);
	}
	
	protected SchemedXmlMarshallerMarshallRequest buildRequest(File input, GenericEntity assembly, String model) {
		return getRequestBuilder().buildRequest(input, assembly, model);		
	}
	protected SchemedXmlMarshallerMarshallResponse process( SchemedXmlMarshallerMarshallRequest request) {
		initialize();
		return requestProcessor.process(request);
	}
	
	
	protected boolean validate( File xsdFile, File xmlFile) {
		try {
			return DomParser.validate().from(xmlFile).schema(xsdFile).makeItSo();
		} catch (DomParserException e) {
			throw new IllegalStateException(e);		
		}		
	}
}
