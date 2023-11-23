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
package com.braintribe.doc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.junit.Test;

import com.braintribe.testing.test.AbstractTest;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;

public class DocumentLoaderTest extends AbstractTest {
	private static final Path ROOT_PATH = testDir(DocumentLoaderTest.class).toPath();
	private static final String ASSET_ID = "tribefire.group:asset-name";
	private static final String ASSET_PATH = ASSET_ID.replace(':', '/') + "/"; 
	
	@Test
	public void testAbsolutePathUrls() {
		///////////  In the following tests the link stays the same but the context it is resolved against changes ///////////
		
		// .md file in documentation root
		assertLinkUrlChangedCorrectly("/a.md", "", "a.html");
		
		// .md file in group folder (first level from document root)
		assertLinkUrlChangedCorrectly("/a.md", "sub", "a.html");
		
		// .md file in asset root
		assertLinkUrlChangedCorrectly("/a.md", ASSET_PATH, "a.html");
		
		// .md file in subfolder of asset
		assertLinkUrlChangedCorrectly("/a.md", ASSET_PATH + "sub", "../a.html");
		
		////////////// The same again but with the target file in a subfolder /////////////
		
		// .md file in documentation root
		assertLinkUrlChangedCorrectly("/sub1/sub2/a.md", 
				"", 
				"sub1/sub2/a.html");
		
		// .md file in group folder (first level from document root)
		assertLinkUrlChangedCorrectly("/sub1/sub2/a.md", 
				"sub", 
				"sub1/sub2/a.html");
		
		// .md file in asset root
		assertLinkUrlChangedCorrectly("/sub1/sub2/a.md", 
				ASSET_PATH, 
				"sub1/sub2/a.html");
		
		// .md file in subfolder of asset
		assertLinkUrlChangedCorrectly("/sub1/sub2/a.md", 
				ASSET_PATH + "sub", 
				"../sub1/sub2/a.html");
		
		/////////// Testing non-.md files. The extension should not change here //////////////
		
		// .css file in subfolder of asset
		assertLinkUrlChangedCorrectly("/sub1/sub2/a.css", 
				ASSET_PATH + "sub", 
				"../sub1/sub2/a.css");
		
		// file without extension in subfolder of asset
		assertLinkUrlChangedCorrectly("/sub1/sub2/a", 
				ASSET_PATH + "sub", 
				"../sub1/sub2/a");
	}
	

	@Test
	public void testAssetSchemedUrls() {
		// .md file in other asset's root, context is in our asset's root
		assertLinkUrlChangedCorrectly("asset://" + ASSET_ID + "/a.md", 
				ASSET_PATH, 
				"../../" + ASSET_PATH + "a.html");
		
		// .md file in other asset's root, context is in a subfolder of our asset
		assertLinkUrlChangedCorrectly("asset://" + ASSET_ID + "/a.md", 
				ASSET_PATH + "sub/", 
				"../../../" + ASSET_PATH + "a.html");
		
		// .md file in other asset's root, context is in a subfolder in a subfolder of our asset
		assertLinkUrlChangedCorrectly("asset://" + ASSET_ID + "/a.md", 
				ASSET_PATH + "sub/sub",
				"../../../../" + ASSET_PATH + "a.html");
		
		// .md file in subfolder of other asset's root, context is in our asset's root
		assertLinkUrlChangedCorrectly("asset://" + ASSET_ID + "/sub/a.md", 
				ASSET_PATH, 
				"../../" + ASSET_PATH + "sub/a.html");
		
		// .md file in subfolder of other asset's root, context is in a subfolder of our asset
		assertLinkUrlChangedCorrectly("asset://" + ASSET_ID + "/sub/a.md", 
				ASSET_PATH + "sub/", 
				"../../../" + ASSET_PATH + "sub/a.html");
		
		// .md file in subfolder of other asset's root, context is in our asset's root
		assertLinkUrlChangedCorrectly("asset://" + ASSET_ID + "/sub/a.md", 
				ASSET_PATH + "sub/sub", 
				"../../../../" + ASSET_PATH + "sub/a.html");
		
		// Without scheme it's actually a relative URL looking for a file of that name in the same folder. 
		// However this behavior could be changed by some settings in flexmark
		assertLinkUrlChangedCorrectly("www.google.com", 
				ASSET_PATH,
				"www.google.com");
	}
	
	@Test
	public void testRelativeUrls() {
		assertLinkUrlChangedCorrectly("a.md", 
				ASSET_PATH, 
				"a.html");
		
		assertLinkUrlChangedCorrectly("../a.md", 
				ASSET_PATH, 
				"../a.html");
		
		assertLinkUrlChangedCorrectly("../a.md", 
				"", 
				"../a.html");
		
		assertLinkUrlChangedCorrectly("../a.md", 
				"a/b/c/d/e/f/g", 
				"../a.html");
		
		assertLinkUrlChangedCorrectly("../../../../a.md", 
				ASSET_PATH, 
				"../../../../a.html");
	}
	
	@Test
	public void testNonProprietaryUrls() {
		assertNonProperietaryUrlDidNotChange("https://www.google.com");
		assertNonProperietaryUrlDidNotChange("weirdscheme://weirdhost");
		
		// In the following two cases the .md should NOT be replaced by an .html
		assertNonProperietaryUrlDidNotChange("http://gov.md");
		assertNonProperietaryUrlDidNotChange("http://gov.md/a.md");
		assertNonProperietaryUrlDidNotChange("http://gov.md/a.md?INCLUDE");
	}
	
	@Test
	public void testFragments() {
		// The fragment component of an URI should always remain untouched
		
		// relative URIs
		assertLinkUrlChangedCorrectly("a.md#fragment", 
				ASSET_PATH, 
				"a.html#fragment");
		
		assertLinkUrlChangedCorrectly("sub/a.md#fragment", 
				ASSET_PATH, 
				"sub/a.html#fragment");
		
		assertLinkUrlChangedCorrectly("../a.md#fragment", 
				ASSET_PATH + "sub/sub", 
				"../a.html#fragment");
		
		// absolute URIs
		assertLinkUrlChangedCorrectly("/a.md#fragment", 
				ASSET_PATH + "sub/sub", 
				"../../a.html#fragment");
		
		assertLinkUrlChangedCorrectly("/a.md#fragment", 
				ASSET_PATH, 
				"a.html#fragment");

		// non-proprietary schemed URIs
		assertNonProperietaryUrlDidNotChange("http://a.html#fragment");
		
	}
	
	private void assertNonProperietaryUrlDidNotChange(String url) {
		Document documentWithExternalUrl = generateDocumentWithLink(url, 
				ASSET_PATH + "sub/sub");
		
		HtmlInline link = (HtmlInline)documentWithExternalUrl.getFirstChild().getFirstChild();
		String linkChars = link.getChars().normalizeEOL();
		
		assertThat(linkChars).contains("href='" + url + "'").containsOnlyOnce("href");
		
		// And now with an image
		documentWithExternalUrl = generateDocumentWithImage(url, 
				ASSET_PATH + "sub/sub");
		
		Image image = (Image)documentWithExternalUrl.getFirstChild().getFirstChild();
		linkChars = image.getUrl().normalizeEOL();
		
		assertThat(linkChars).isEqualTo(url);
	}
	
	@Test
	public void testStrangeCases() {
		// from the asset root (absolute), go one more back (relative)
		assertLinkUrlChangedCorrectly("/../a.md", 
				ASSET_PATH + "sub1/sub2/", 
				"../../../a.html");
		
		// from the other asset root (absolute), go one more back (relative) - this brings us to the other asset's group-id folder
		assertLinkUrlChangedCorrectly("asset://" + ASSET_ID + "/../a.md", 
				ASSET_PATH, 
				"../../tribefire.group/a.html");
		
	}
	
	@Test
	public void testSimpleInclude() {
		testInclude("?INCLUDE");
	}
	
	public void testInclude(String queryParams) {
		String filenameOfTestMd = "a.md";
		String includeUrl = filenameOfTestMd + queryParams;
		String simpleUrl = filenameOfTestMd;
		
		Document generatedDocument = generateDocumentWithLink(includeUrl , "");
		assertThat(generatedDocument.getFirstChild().getFirstChild()).isInstanceOf(Text.class);
		
		generatedDocument = generateDocumentWithLink("../../" + includeUrl , ASSET_PATH);
		assertThat(generatedDocument.getFirstChild().getFirstChild()).isInstanceOf(Text.class);
		
		generatedDocument = generateDocumentWithLink(filenameOfTestMd + "?include" , "");
		assertThat(generatedDocument.getFirstChild().getFirstChild()).isInstanceOf(Link.class);
		assertLinkOrImageUrl(generatedDocument, "a.html?include");
		
		generatedDocument = generateDocumentWithLink(simpleUrl , "");
		assertThat(generatedDocument.getFirstChild().getFirstChild()).isInstanceOf(Link.class);
		assertLinkOrImageUrl(generatedDocument, "a.html");
		
		assertThatThrownBy(() -> generateDocumentWithLink(includeUrl, ASSET_PATH)).hasRootCauseExactlyInstanceOf(MarkdownParsingException.class);
		
		generatedDocument = generateDocumentWithLink("b.md?INCLUDE" , ASSET_PATH);
		assertThat(generatedDocument.getFirstChild().getFirstChild()).isInstanceOf(Text.class);
		
		generatedDocument = generateDocumentWithLink("b.html?INCLUDE" , "");
		assertThat(generatedDocument.getFirstChild().getFirstChild()).isInstanceOf(Link.class);
		assertLinkOrImageUrl(generatedDocument, "b.html?INCLUDE");
		
		generatedDocument = generateDocumentWithLink("asset://" + ASSET_ID + "/b.md?INCLUDE" , ASSET_PATH);
		assertThat(generatedDocument.getFirstChild().getFirstChild()).isInstanceOf(Text.class);
		
	}
	
	@Test
	public void testWeirdInclude() {
		// Also these urls with these query params should behave exactly like "?INCLUDE"
		testInclude("?bla7&INCLUDE");
		testInclude("?level=0&INCLUDE");
	}
	
	@Test
	public void testCollapsingInclude() {
		// md file not starting with header
		Document generatedDocument = generateDocumentWithLink("a.md?INCLUDE&collapsed" , "");
		assertThat(generatedDocument.getFirstChild()).isInstanceOf(HtmlInline.class);
		
		// md file starting with header
		generatedDocument = generateDocumentWithLink("with-header.md?INCLUDE&collapsed" , "");
		Heading heading = (Heading) generatedDocument.getFirstChild();
		assertThat(heading.getLevel()).isEqualTo(2);
		assertThat(heading.getNext()).isExactlyInstanceOf(HtmlInline.class);
	}
	
	@Test
	public void testIncludeWithLevel() {
		String filenameOfTestMd = "with-header.md";
		
		Document generatedDocument = generateDocumentWithLink(filenameOfTestMd + "?INCLUDE&level=1" , "");
		Heading heading = (Heading) generatedDocument.getFirstChild();
		assertThat(heading.getLevel()).isEqualTo(3);
		assertThat(heading.getNext()).isExactlyInstanceOf(Paragraph.class);
		assertThat(heading.getNext().getNext()).isExactlyInstanceOf(Heading.class);
		assertThat(heading.getNext().getNext()).extracting("level").isEqualTo(2);
		
		generatedDocument = generateDocumentWithLink(filenameOfTestMd + "?INCLUDE&level=100" , "");
		heading = (Heading) generatedDocument.getFirstChild();
		assertThat(heading.getLevel()).isEqualTo(102);
		assertThat(heading.getNext()).isExactlyInstanceOf(Paragraph.class);
		assertThat(heading.getNext().getNext()).isExactlyInstanceOf(Heading.class);
		assertThat(heading.getNext().getNext()).extracting("level").isEqualTo(101);
	}
	
	@Test
	public void testIncludeMixed() {
		String filenameOfTestMd = "with-header.md";
		
		Document generatedDocument = generateDocumentWithLink(filenameOfTestMd + "?INCLUDE&level=1&collapsed" , "");
		Heading heading = (Heading) generatedDocument.getFirstChild();
		assertThat(heading.getLevel()).isEqualTo(3);
		assertThat(heading.getNext()).isExactlyInstanceOf(HtmlInline.class);
	}
	
	@Test
	public void testNestedInclude() {
		Document generatedDocument = generateDocumentWithLink("asset://tribefire.group:asset-name/including2.md?INCLUDE" , "");
		Heading heading = (Heading) generatedDocument.getFirstChild();
		assertThat(heading.getText().toString()).isEqualTo("C");
		Link link = (Link) heading.getNext().getFirstChild();
		assertThat(link.getUrl().toString()).isEqualTo("tribefire.group/asset-name/including.html");
		link = (Link) link.getNext().getNext();
		assertThat(link.getUrl().toString()).isEqualTo("#c");
	}
	
	@Test
	public void testCircularInclude() {
		assertThatThrownBy(() -> generateDocumentWithLink("self-including.md?INCLUDE" , "")).isExactlyInstanceOf(MarkdownParsingException.class);
		assertThatThrownBy(() -> generateDocumentWithLink("circular/a.md?INCLUDE" , "")).isExactlyInstanceOf(MarkdownParsingException.class);
		
		// Including the same document multiple times and/or via multiple paths is no circular include and thus should pass
		generateDocument("", "[](a.md?INCLUDE) [](a.md?INCLUDE) [](a.md?INCLUDE) [](a.md?INCLUDE) [](asset://tribefire.group:asset-name/including2.md?INCLUDE) [](asset://tribefire.group:asset-name/including.md?INCLUDE) [](asset://tribefire.group:asset-name/c.md?INCLUDE)");
	}
	
	@Test
	public void testImages() {
		///////////  In the following tests the link stays the same but the context it is resolved against changes ///////////
		
		// file in documentation root
		assertImageUrlChangedCorrectly("/a.gif", "", "a.gif");
		
		// file in group folder (first level from document root)
		assertImageUrlChangedCorrectly("/a.gif", "sub", "a.gif");
		
		// file in asset root
		assertImageUrlChangedCorrectly("/a.gif", ASSET_PATH, "a.gif");
		
		// file in subfolder of asset
		assertImageUrlChangedCorrectly("/a.gif", ASSET_PATH + "sub", "../a.gif");
		
		////////////// The same again but with the target file in a subfolder /////////////
		
		// file in documentation root
		assertImageUrlChangedCorrectly("/sub1/sub2/a.gif", 
				"", 
				"sub1/sub2/a.gif");
		
		// file in group folder (first level from document root)
		assertImageUrlChangedCorrectly("/sub1/sub2/a.gif", 
				"sub", 
				"sub1/sub2/a.gif");
		
		// file in asset root
		assertImageUrlChangedCorrectly("/sub1/sub2/a.gif", 
				ASSET_PATH, 
				"sub1/sub2/a.gif");
		
		// file in subfolder of asset
		assertImageUrlChangedCorrectly("/sub1/sub2/a.gif", 
				ASSET_PATH + "sub", 
				"../sub1/sub2/a.gif");
		
		
		// file without extension in subfolder of asset
		assertImageUrlChangedCorrectly("/sub1/sub2/a", 
				ASSET_PATH + "sub", 
				"../sub1/sub2/a");

		// file in other asset's root, context is in our asset's root
		assertImageUrlChangedCorrectly("asset://" + ASSET_ID + "/a.gif", 
				ASSET_PATH, 
				"../../" + ASSET_PATH + "a.gif");
		
		// file in other asset's root, context is in a subfolder of our asset
		assertImageUrlChangedCorrectly("asset://" + ASSET_ID + "/a.gif", 
				ASSET_PATH + "sub/", 
				"../../../" + ASSET_PATH + "a.gif");
		
		//  file in other asset's root, context is in a subfolder in a subfolder of our asset
		assertImageUrlChangedCorrectly("asset://" + ASSET_ID + "/a.gif", 
				ASSET_PATH + "sub/sub",
				"../../../../" + ASSET_PATH + "a.gif");
		
		// file in subfolder of other asset's root, context is in our asset's root
		assertImageUrlChangedCorrectly("asset://" + ASSET_ID + "/sub/a.gif", 
				ASSET_PATH, 
				"../../" + ASSET_PATH + "sub/a.gif");
		
		// file in subfolder of other asset's root, context is in a subfolder of our asset
		assertImageUrlChangedCorrectly("asset://" + ASSET_ID + "/sub/a.gif", 
				ASSET_PATH + "sub/", 
				"../../../" + ASSET_PATH + "sub/a.gif");
		
		// file in subfolder of other asset's root, context is in our asset's root
		assertImageUrlChangedCorrectly("asset://" + ASSET_ID + "/sub/a.gif", 
				ASSET_PATH + "sub/sub", 
				"../../../../" + ASSET_PATH + "sub/a.gif");
		
		// Without scheme it's actually a relative URL looking for a file of that name in the same folder. 
		// However this behavior could be changed by some settings in flexmark
		assertImageUrlChangedCorrectly("www.google.com", 
				ASSET_PATH,
				"www.google.com");

		assertImageUrlChangedCorrectly("a.gif", 
				ASSET_PATH, 
				"a.gif");
		
		assertImageUrlChangedCorrectly("../a.gif", 
				ASSET_PATH, 
				"../a.gif");
		
		assertImageUrlChangedCorrectly("../a.gif", 
				"", 
				"../a.gif");
		
		assertImageUrlChangedCorrectly("../a.gif", 
				"a/b/c/d/e/f/g", 
				"../a.gif");
		
		assertImageUrlChangedCorrectly("../../../../a.gif", 
				ASSET_PATH, 
				"../../../../a.gif");
		
		// from the asset root (absolute), go one more back (relative)
		assertImageUrlChangedCorrectly("/../a.gif", 
				ASSET_PATH + "sub1/sub2/", 
				"../../../a.gif");
		
		// from the other asset root (absolute), go one more back (relative) - this brings us to the other asset's group-id folder
		assertImageUrlChangedCorrectly("asset://" + ASSET_ID + "/../a.gif", 
				ASSET_PATH, 
				"../../tribefire.group/a.gif");
	}
	
	private Document generateDocumentWithLink(String linkUrl, String resolvingAgainst) {
		return generateDocument(resolvingAgainst, "[linktext](" + linkUrl + ")");

	}

	private Document generateDocumentWithImage(String linkUrl, String resolvingAgainst) {
		return generateDocument(resolvingAgainst, "![linktext](" + linkUrl + ")");
	}
	
	private Document generateDocument(String resolvingAgainst, String content) {
		UniversalPath resolvingAgainstPath = UniversalPath.empty().pushSlashPath(resolvingAgainst);
		DocumentProcessor documentLoader = new DocumentProcessor(ROOT_PATH, resolvingAgainstPath, null, false);

		Document document = DocUtils.FLEXMARK_PARSER.parse(content);
		documentLoader.process(document, "<TEST>");
		return document;
	}
	
	private void assertLinkOrImageUrl(Document document, String expectedUrl) {
		InlineLinkNode link = (InlineLinkNode)document.getFirstChild().getFirstChild();
		String url = link.getUrl().normalizeEOL();
		assertThat(url).as("DocumentLoader resolved different URL than expected").isEqualTo(expectedUrl);
	}
	
	private void assertLinkUrlChangedCorrectly(String urlInMarkdown, String pathToResolveAgainst, String expectedResolvedUrl) {
		Document document = generateDocumentWithLink(urlInMarkdown, pathToResolveAgainst);
		assertLinkOrImageUrl(document, expectedResolvedUrl);
	}
	
	private void assertImageUrlChangedCorrectly(String urlInMarkdown, String pathToResolveAgainst, String expectedResolvedUrl) {
		Document document = generateDocumentWithImage(urlInMarkdown, pathToResolveAgainst);
		assertLinkOrImageUrl(document, expectedResolvedUrl);
	}
}
