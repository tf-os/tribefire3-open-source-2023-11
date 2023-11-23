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
package tribefire.extension.xml.schemed.test.xsd.analyzer.test.addidas;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.xsd.test.util.TestUtil;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.AbstractXsdAnalyzerLab;

public class AddiasLab extends AbstractXsdAnalyzerLab {
	private static File simple = new File( contents, "addidas");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");

	@BeforeClass
	public static void beforeClass() {
		TestUtil.ensure(output);
	}


	@Test
	public void inboundASNBridge() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.addias.inbound.asn.bridge", "InboundASNBridge.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:InBoundASNBridgeModel#1.0");
		process( request, output);
	}
	
	@Test
	public void invoiceBridge() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.addias.invoice.bridge", "InvoiceBridge.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:InvoiceBridgeModel#1.0");
		process( request, output);
	}
	@Test
	public void itemMasterBridge() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.addias.item.master.bridge", "ItemMasterBridge.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:ItemMasterBridgeModel#1.0");
		process( request, output);
	}
	@Test
	public void pickTicketBridge() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.addias.pick.ticket.bridge", "PickTicketBridge.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:PickTicketBridgeModel#1.0");
		process( request, output);
	}	
	@Test
	public void pixBridge() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.addias.pix.bridge", "PixBridge.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:PixBridgeModel#1.0");
		process( request, output);
	}

}
