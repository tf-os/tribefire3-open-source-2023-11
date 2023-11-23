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
package tribefire.extension.xml.schemed.test.processing.swift;



import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.test.processing.AbstractTest;


public class SwiftTest extends AbstractTest {
	private static File basic = new File( contents, "swift");
	private static File input = new File( basic, "input");
	private static File output = new File( basic, "output");
	
	@BeforeClass
	public static void before() {
		before( output);
	}
	
	@Test
	public void test_pain() {
		
		try {
			runTest( input, output, "pain.001.001.03.ISO.xsd", "com.braintribe.xml.swift.pain", "com.braintribe.xml.test.swift:pain#1.0");
		} catch (Exception e) {
			Assert.fail("exception [" + e + "] thrown");
		}
	}

	
	@Test
	public void test_pacs() {
		
		try {
			runTest( input, output, "pacs.008.001.01.ISO.xsd", "com.braintribe.xml.swift.pacs", "com.braintribe.xml.test.swift:pacs#1.0");
		} catch (Exception e) {
			Assert.fail("exception [" + e + "] thrown");
		}
	}

}
