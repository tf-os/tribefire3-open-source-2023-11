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
package com.braintribe.xml.parser.sax.test.framework;

import java.io.File;
import java.util.Date;

import org.junit.Assert;

import com.braintribe.utils.xml.parser.sax.SaxParser;
import com.braintribe.utils.xml.parser.sax.SaxParserException;
import com.braintribe.utils.xml.parser.sax.builder.SaxContext;

public class TestRunner {
	private static SaxContext sc;
	
	public static float runTest( int prime, int num, File content, File schema) {
		sc = null;
		long time = 0; 
		for (int i = 0; i < num; i++) {			
			long dif = doTest(content, schema, null);
			if (i >= prime) {
				time += dif;
			}
		}
		float average = new Float( (float) time / (num-prime));
		if (schema == null) {
			System.out.println("averaging over [" + (num-prime) + "] runs, parsing of [" + content.getAbsolutePath() + "] took [" + average + "] ms");
		}
		else {
			System.out.println("averaging over [" + (num-prime) + "] runs, parsing of [" + content.getAbsolutePath() + "] while validating with [" + schema.getAbsolutePath() + "] took [" + average + "] ms");
		}
		return average;
	}

	@SuppressWarnings("rawtypes")
	private static long doTest(File content, File schema, Class expectedException) {
		if (sc == null) {
			sc = SaxParser.parse();
			if (schema != null) {
				sc.schema(schema);
			} 
			
			else {
				sc.setValidating();
				sc.setNamespaceAware();
			}			
			sc.setHandler( new TestContentHandler());
		}		
		Date before = new Date();
		boolean exceptionThrown = false;
		try {
			sc.parse(content);
		} catch (SaxParserException e) {
			if (expectedException == null) {
				Assert.fail( "Exception thrown " + e.getMessage());
				return 0L;
			}
			else {
				if (e.getClass() != expectedException) {
					Assert.fail( "unexpected exception thrown " + e.getMessage());
				}
				else {
					exceptionThrown = true;
				}
			}
		}
		
		if (expectedException != null && exceptionThrown == false) {
			Assert.fail("expected exception not thrown :" +  expectedException.getName());
		}
		Date after = new Date();
		long dif = after.getTime() - before.getTime();		
		return dif;
	}
	
	public static void runTest( File content, File schema, Class expectedException) {
		sc = null;
		long dif = doTest(content, schema, expectedException);
		if (schema == null) {
			System.out.println("parsing of [" + content.getAbsolutePath() + "] took [" + dif + "] ms");
		}
		else {
			System.out.println("parsing of [" + content.getAbsolutePath() + "] while validating with [" + schema.getAbsolutePath() + "] took [" + dif + "] ms");
		}
	}
}
