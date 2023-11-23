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
package tribefire.extension.xml.schemed.xml.marshaller.test;

import java.io.File;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

import tribefire.extension.xml.schemed.marshaller.xml.SchemedXmlMarshallingRequestProcessor;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallResponse;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallResponse;
import tribefire.extension.xml.schemed.test.commons.commons.ArchiveValidator;
import tribefire.extension.xml.schemed.test.commons.commons.SchemedXmlXsdMarshallerRequestBuilder;
import tribefire.extension.xml.schemed.test.commons.xsd.test.resource.ResourceProvidingSession;

@Category(KnownIssue.class)
public abstract class AbstractXmlMarshallerLab {
	protected static File res = new File( "res");
	private ResourceProvidingSession session = new ResourceProvidingSession();
	private StaxMarshaller modelMarshaller = new StaxMarshaller();
	private SchemedXmlMarshallingRequestProcessor marshaller = new SchemedXmlMarshallingRequestProcessor();
	private SchemedXmlXsdMarshallerRequestBuilder requestBuilder;
	
	public SchemedXmlXsdMarshallerRequestBuilder getRequestBuilder() {
		if (requestBuilder == null) {
			requestBuilder = new SchemedXmlXsdMarshallerRequestBuilder();
			requestBuilder.setSession(session);
			requestBuilder.setModelMarshaller( modelMarshaller);
		}
		return requestBuilder;
	}

	protected SchemedXmlMarshallerUnmarshallRequest buildRequest(File input, String xsdName, String model) {		
		return getRequestBuilder().buildRequest(input, xsdName, model);				
	}
	
	protected SchemedXmlMarshallerUnmarshallResponse process( SchemedXmlMarshallerUnmarshallRequest request) {
		return marshaller.process(request);
	}
	
	protected SchemedXmlMarshallerMarshallRequest buildRequest(File input, GenericEntity assembly, String model) {
		return getRequestBuilder().buildRequest(input, assembly, model);		
	}
	protected SchemedXmlMarshallerMarshallResponse process( SchemedXmlMarshallerMarshallRequest request) {
		return marshaller.process(request);
	}
	
	
	protected boolean validate( File xsd, File xml) {
		if (!xsd.getName().endsWith( ".xsd")) 
			return ArchiveValidator.validate(xsd, xml);
		else {
			try {
				return DomParser.validate().from(xml).schema( xsd).makeItSo();
			} catch (DomParserException e) {
				return false;
			}
		}
			
	}
	
	protected void assertValid( File xsdFile, File xmlFile) {
		try {
			if (!DomParser.validate().from(xmlFile).schema(xsdFile).makeItSo()) {
				Assert.fail("validation if [" + xmlFile.getName() + "] vs [" + xsdFile.getName() + "] failed");
			}				
		} catch (DomParserException e) {
			Assert.fail("validation failed as [" + e.getLocalizedMessage() + "]");
		}
		
	}
}
