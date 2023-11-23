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
package tribefire.extension.xml.schemed.test.xsd.analyzer.test.generics;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.xsd.test.util.TestUtil;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.AbstractXsdAnalyzerLab;

public class SimpleChoiceAnalyzerLab extends AbstractXsdAnalyzerLab {
	private static File simple = new File( contents, "simpleChoice");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");
	

	@BeforeClass
	public static void beforeClass() {
		TestUtil.ensure(output);
	}

	@Test
	public void flat_SimpleChoice() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.simple.choices.flat", "simple.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:SimpleChoiceFlatModel#1.0");		
		process( request, output);
	}
	//@Test
	public void structured_SimpleChoice() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.simple.choices.structured", "simple.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:SimpleChoiceStructuredModel#1.0");
		request.setExposeChoice(true);
		request.setExposeSequence(true);
		process( request, output);
	}
	
	@Test
	public void flat_SimplestChoice() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.simplest.choices.flat", "simplest.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:SimplestChoiceFlatModel#1.0");		
		process( request, output);
	}
	//@Test
	public void structured_SimplestChoice() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.simplest.choices.structured", "simplest.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:SimplestChoiceStructuredModel#1.0");
		request.setExposeChoice(true);
		request.setExposeSequence(true);
		process( request, output);
	}
	
	@Test
	public void flat_ReallySimplestChoice() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.really.simplest.choices.flat", "simplest.2.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:ReallySimplestChoiceFlatModel#1.0");		
		process( request, output);
	}
	//@Test
	public void structured_ReallySimplestChoice() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.really.simplest.choices.structured", "simplest.2.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:ReallySimplestChoiceStructuredModel#1.0");
		request.setExposeChoice(true);
		request.setExposeSequence(true);
		process( request, output);
	}
	
	@Test
	public void flat_ReallySimplestMultipleChoice() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.really.simplest.choices.multiple.flat", "simplest.multiple.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:ReallySimplestMultipleChoiceFlatModel#1.0");		
		process( request, output);
	}
}
