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
package tribefire.extension.xml.schemed.xml.marshaller.test.include;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import com.braintribe.logging.Logger;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

import tribefire.extension.xml.schemed.test.commons.xsd.test.util.TestUtil;

@Category(KnownIssue.class)
public class IncludeLab {
	private static final File contents = new File("res/include");
	private static final File input = new File (contents, "input");
	private static final File output = new File (contents, "output");
	private static final String XML = "including.xml";
	private static Logger log = Logger.getLogger(IncludeLab.class);
	
	@Before
	public void before() {
		TestUtil.ensure(output);;
	}
	
	//@Test
	public void test() {
		File inputFile = new File( input, XML);
		Document doc;
		try {
			doc = DomParser.load().setIncludeAware().setNamespaceAware().from( inputFile);
		} catch (DomParserException e) {
			throw new IllegalStateException("cannot read file [" + inputFile + "]");
		}
		
		File outputFile = new File( output, XML);
		try {
			DomParser.write().from(doc).to( outputFile);
		} catch (DomParserException e) {
			throw new IllegalStateException( "cannot write file [" + outputFile + "]");
		}
	}

}
