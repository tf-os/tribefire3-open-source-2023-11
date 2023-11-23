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
package tribefire.cortex.check.processing;

import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

import com.braintribe.utils.lcd.LazyInitialized;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;

import tribefire.cortex.model.check.CheckWeight;

public class CheckBundleTest {

	public static final MutableDataHolder FLEXMARK_OPTIONS = new MutableDataSet() //
			.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
	
	private LazyInitialized<Parser> markdownParser = new LazyInitialized<>(() -> Parser.builder(FLEXMARK_OPTIONS).build());
	private LazyInitialized<HtmlRenderer> htmlRenderer = new LazyInitialized<>(() -> HtmlRenderer.builder(FLEXMARK_OPTIONS).build());
	
	@Test
	public void testWeightProcessing() {
		
		CheckWeight under1s = CheckWeight.under1s;
		
		CheckWeight under100ms = CheckWeight.under100ms;
		
		assertTrue(under100ms.ordinal() <= under1s.ordinal());
		
	}
	
	@Test
	public void parseMarkdownTable() {

		String input = "Instance | Type | Id | Thread | Duration\r\n" + 
				"--- | --- | --- | --- | ---\r\n" + 
				"```master@tf@Romans-MacBook-Pro.local#200316140759297a0eb7e67d3340af9d``` | SplitPdfJob | 200316141446208904a54e0c2a4d07ad | ConversionJob-32-thread-4 | 31 s 242 ms\r\n" + 
				"`master@tf@Romans-MacBook-Pro.local#200316140759297a0eb7e67d3340af9d` | CreateDocumentJob | 200316141432802abaf63e8ccc4969a3 | ConversionJob-32-thread-4 | 44 s 589 ms";
		Parser parser = markdownParser.get();
		Document document = parser.parse(input);
		
		HtmlRenderer renderer = htmlRenderer.get();
		
		StringWriter writer = new StringWriter();
		renderer.render(document, writer);
		
		String s = writer.toString();
		System.out.println(s);
	}
	
	@Test
	public void parseMarkdownMixed() {
		
		String input = 
				"```\n" +
				"@Managed\r\n" + 
				"private DemoVitalityChecker demoHealthChecker() {\r\n" + 
				"	DemoVitalityChecker bean = create(DemoVitalityChecker.T);\r\n" + 
				"	\r\n" + 
				"	bean.setModule(existingInstances.demoChecksModule());\r\n" + 
				"	bean.setExternalId(\"serviceProcessor.demoHealthChecker\");\r\n" + 
				"	bean.setName(\"Demo Health Checker\");\r\n" + 
				"	\r\n" + 
				"	return bean;\r\n" + 
				"}\n"
				+ "```";
		Parser parser = markdownParser.get();
		Document document = parser.parse(input);
		
		HtmlRenderer renderer = htmlRenderer.get();
		
		StringWriter writer = new StringWriter();
		renderer.render(document, writer);
		
		String s = writer.toString();
		System.out.println(s);
	}

}
