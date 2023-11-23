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
package com.braintribe.utils.html;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Test;

import com.braintribe.utils.xml.XmlTools;

public class HtmlToolsTest {


	@Test
	public void testGuessHtml() throws Exception {
		
		assertThat(HtmlTools.guessHTML("<html><body>Hello<br>world</body>")).isTrue();
		assertThat(HtmlTools.guessHTML("Hello, world")).isFalse();
		
	}
	
	@Test
	public void testNormalizeHtml() throws Exception {
		
		String html = "<html><body>Hello<br>world</body>";
		String xhtml = HtmlTools.normalizeHTML(html);
		
		try {
			XmlTools.parseXML(html);
			fail("Expected an exception here.");
		} catch(Exception e) {
			//expected
		}
		XmlTools.parseXML(xhtml);
		
	}
	
	@Test
	public void testNormalizeHtmlJTidy() throws Exception {
		
		String html = "<html><body>Hello<br>world</body>";
		String xhtml = HtmlTools.normalizeHTMLWithJTidy(html);
		
		try {
			XmlTools.parseXML(html);
			fail("Expected an exception here.");
		} catch(Exception e) {
			//expected
		}
		XmlTools.parseXML(xhtml);
		
	}
	
	@Test
	public void testNormalizeHtmlTagSoup() throws Exception {
		
		String html = "<html><body>Hello<br>world</body>";
		String xhtml = HtmlTools.normalizeHTMLWithTagSoup(html);
		
		try {
			XmlTools.parseXML(html);
			fail("Expected an exception here.");
		} catch(Exception e) {
			//expected
		}
		XmlTools.parseXML(xhtml);
		
	}
	
	@Test
	public void testEscapeHtml() throws Exception {
		
		assertThat(HtmlTools.escapeHtml("hello, world")).isEqualTo("hello, world");
		assertThat(HtmlTools.escapeHtml("hello\nworld")).isEqualTo("hello<br />world");
		assertThat(HtmlTools.escapeHtml("hello&world")).isEqualTo("hello&amp;world");
		assertThat(HtmlTools.escapeHtml("hello\"world")).isEqualTo("hello&quot;world");
	}

	@Test
	public void testEscapeXml() throws Exception {
		
		assertThat(HtmlTools.escapeXml("hello, world")).isEqualTo("hello, world");
		assertThat(HtmlTools.escapeXml("hello\nworld")).isEqualTo("hello\nworld");
		assertThat(HtmlTools.escapeXml("hello&world")).isEqualTo("hello&amp;world");
		assertThat(HtmlTools.escapeXml("hello\"world")).isEqualTo("hello&quot;world");
	}

	@Test
	public void testUnescapeHtml() throws Exception {
		
		assertThat(HtmlTools.unescapeHtml("hello, world")).isEqualTo("hello, world");
		assertThat(HtmlTools.unescapeHtml("hello&#x0020;world")).isEqualTo("hello world");
		assertThat(HtmlTools.unescapeHtml("hello&#0032;world")).isEqualTo("hello world");
		assertThat(HtmlTools.unescapeHtml("hello&amp;world")).isEqualTo("hello&world");
		assertThat(HtmlTools.unescapeHtml("hello&quot;world")).isEqualTo("hello\"world");
	}
}
