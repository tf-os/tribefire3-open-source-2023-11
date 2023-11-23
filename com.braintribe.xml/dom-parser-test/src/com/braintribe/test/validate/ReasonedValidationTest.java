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
package com.braintribe.test.validate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class ReasonedValidationTest {
	private static File contents = new File("res/maven");
	
	private static File xsd = new File( contents, "maven-4.0.0.xsd");
	
	
	
	private boolean test(File pomFile) {
	
		// validate against schema (for some reason, validating while loading wants a DTD?) 
		try (
				InputStream pomStream = new FileInputStream( pomFile);
				InputStream schemaStream = new FileInputStream( xsd); 
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				) {
			
			boolean isValid = DomParser.validate().from( pomStream).schema(schemaStream).makeItSo( out);			
			String msg = new String( out.toByteArray());
			
			if (!isValid) {
				System.out.println( "validation message : " + msg);
				return false;
			}
			return true;
			
		} catch (FileNotFoundException e) {
			Assert.fail( e.getMessage());
			return false;
		} catch (IOException e) {			
			Assert.fail( e.getMessage());
			return false;		
		} catch (DomParserException e) {
			Assert.fail( e.getMessage());
			return false;
		}	
	}
	
	@Test
	public void testSuccess() {
		boolean retval = test( new File( contents, "valid.pom.xml"));
		Assert.assertTrue( "expected test to succeed, but it failed", retval);
		
	}
	@Test
	public void testFail() {
		boolean retval = test( new File( contents, "invalid.pom.xml"));
		Assert.assertTrue( "expected test to fail, but it succeeded", !retval);
	}
}
