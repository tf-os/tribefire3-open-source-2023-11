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

public class TypeNameClashAnalyzerLab extends AbstractXsdAnalyzerLab {
	private static File simple = new File( contents, "typeClash");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");
	
	@BeforeClass
	public static void beforeClass() {
		TestUtil.ensure(output);
	}

	@Test
	public void structured_TypeClash() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.typeclash.structured", "typeclash.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:TypeClashModel#1.0");
		request.setExposeChoice(true);
		request.setExposeSequence(true);
		process( request, output);
	}
	
	@Test
	public void flat_TypeClash() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.typeclash.flat", "typeclash.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:TypeClashFlatModel#1.0");
		process( request, output);
	}
	
	@Test
	public void structured_TypeAndAttrClash() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.typeattr.clash.structured", "typeAndAttributeClash.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:TypeAndAttrClashModel#1.0");
		request.setExposeChoice(true);
		request.setExposeSequence(true);
		process( request, output);
	}
	
	@Test
	public void flat_TypeAndAttrClash() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.typeattr.clash.flat", "typeAndAttributeClash.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:TypeAndAttrClashFlatModel#1.0");
		process( request, output);
	}
	
	@Test
	public void structured_TypeAndDivAttrClash() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.typeattrdiv.clash.structured", "typeAndAttributeClash2.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:TypeAndAttrDivClashModel#1.0");
		request.setExposeChoice(true);
		request.setExposeSequence(true);
		process( request, output);
	}
	
	@Test
	public void flat_TypeAndDivAttrClash() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.typeattrdiv.clash.flat", "typeAndAttributeClash2.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:TypeAndAttrDivClashFlatModel#1.0");
		process( request, output);
	}
	
	@Test
	public void structured_SingleTypeAndDivAttrClash() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.singletypeattrdiv.clash.structured", "typeAndAttributeClash3.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:SingleTypeAndAttrDivClashModel#1.0");
		request.setExposeChoice(true);
		request.setExposeSequence(true);
		process( request, output);
	}
	
	@Test
	public void flat_SingleTypeAndDivAttrClash() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.singletypeattrdiv.clash.flat", "typeAndAttributeClash3.xsd", java.util.Collections.emptyList(), "com.braintribe.xsd:SingleTypeAndAttrDivClashFlatModel#1.0");
		process( request, output);
	}
}
