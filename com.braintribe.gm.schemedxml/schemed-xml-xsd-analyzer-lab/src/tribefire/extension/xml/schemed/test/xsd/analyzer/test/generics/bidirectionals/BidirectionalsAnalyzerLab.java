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
package tribefire.extension.xml.schemed.test.xsd.analyzer.test.generics.bidirectionals;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.BidirectionalLink;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.xsd.test.util.TestUtil;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.AbstractXsdAnalyzerLab;

public class BidirectionalsAnalyzerLab extends AbstractXsdAnalyzerLab {
	private static File simple = new File( contents, "bidirectionals");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");

	@BeforeClass
	public static void beforeClass() {
		TestUtil.ensure(output);
	}
	
	@Test
	public void flat_bidirectional1() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.bidi.unique", "link.1.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:BidiUniqueFlatModel#1.0");

		BidirectionalLink link = BidirectionalLink.T.create();
		
		SchemaAddress schemaAddress1 = SchemaAddress.T.create();
		schemaAddress1.setElement("singleChild");
		schemaAddress1.setParent("Root");
		link.setSchemaAddress(schemaAddress1);		
		link.setBacklinkProperty( "root");
		
		request.getBidirectionalLinks().add(link);
		process( request, output);
	}
	
	@Test
	public void flat_bidirectional2() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.bidi.duplicate", "link.2.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:BidiDuplicateFlatModel#1.0");

		BidirectionalLink link = BidirectionalLink.T.create();
		
		SchemaAddress schemaAddress1 = SchemaAddress.T.create();
		schemaAddress1.setElement("singleChild");
		schemaAddress1.setParent("Root");
		link.setSchemaAddress(schemaAddress1);		
		link.setBacklinkProperty( "root");
		
		request.getBidirectionalLinks().add(link);
		
		link = BidirectionalLink.T.create();
		SchemaAddress schemaAddress2 = SchemaAddress.T.create();
		schemaAddress2.setElement("multipleChild");
		schemaAddress2.setParent("Root");
		link.setSchemaAddress(schemaAddress2);		
		link.setBacklinkProperty( "root");
		
		request.getBidirectionalLinks().add(link);
		process( request, output);
	}

	
}
