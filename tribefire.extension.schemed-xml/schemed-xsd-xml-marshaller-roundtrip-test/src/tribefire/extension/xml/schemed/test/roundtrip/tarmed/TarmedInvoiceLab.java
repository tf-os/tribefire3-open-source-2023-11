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
package tribefire.extension.xml.schemed.test.roundtrip.tarmed;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class TarmedInvoiceLab extends AbstractTarmedLab{

	private static final String COM_BRAINTRIBE_SCHEMEDXML_TARMED = "com.braintribe.schemedxml.tarmed";
	private static final String MAPPING_MODEL = COM_BRAINTRIBE_SCHEMEDXML_TARMED + ":TarmedFlatModel#1.0";
	private static final String XSD = "MDInvoiceRequest_400.xsd";
	private File workingDirectory = new File( tarmed, "invoice");
	
	
	@Test
	public void test_TarmedInvoice() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_TARMED, XSD, Arrays.asList("4187_10734361.xml", "4187_10734368.xml"), MAPPING_MODEL);
	}
	
}
