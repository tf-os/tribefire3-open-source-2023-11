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

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;

import java.io.File;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.doc.meta.FileDisplayInfo;
import com.vladsch.flexmark.ast.Block;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.toc.TocBlock;
import com.vladsch.flexmark.util.sequence.CharSubSequence;

public class DocumentProcessor {
	private static final int ROUGH_LENGTH_SHORT_DESCRIPTION = 100;
	private final boolean checkForLinkValidity;
	private final Path rootPath;
	private final UniversalPath contextPath;
	private final Optional<MarkdownFile> markdownFile;
	private final JavadocMerger javadoc;
	
	private final Set<UniversalPath> includedFiles = new HashSet<>();
	
	private final StringBuilder shortDescriptionBuilder;
	private boolean inFirstLevelHeaderParagraph;

	public Path getRootPath() {
		return rootPath;
	}

	public DocumentProcessor(Path rootPath, UniversalPath relativeDocumentPath) {
		this(rootPath, relativeDocumentPath, null, true);
	}

	public DocumentProcessor(Path rootPath, UniversalPath relativeDocumentPath, MarkdownFile markdownFile, boolean checkForLinkValidity) {
		this(rootPath, relativeDocumentPath, markdownFile, checkForLinkValidity, null);
	}

	public DocumentProcessor(Path rootPath, UniversalPath contextPath, MarkdownFile markdownFile, boolean checkForLinkValidity, JavadocMerger javadocMerger) {
		super();
		this.rootPath = rootPath;
		this.checkForLinkValidity = checkForLinkValidity;
		this.javadoc = javadocMerger;
		this.contextPath = contextPath;
		this.markdownFile = Optional.ofNullable(markdownFile);
		
		if (markdownFile != null && markdownFile.getDisplayInfo().getShortDescription() == null) {
			shortDescriptionBuilder = new StringBuilder();
		} else {
			shortDescriptionBuilder = null;
		}
	}

	private boolean createShortDescription() {
		return shortDescriptionBuilder != null;
	}
	
	public void process(Document document, String string) {
		process(document, contextPath, string);
	}
	
	public void process(Document document, UniversalPath relativeDocumentFolderPath, String locationDescription) {
		try {
			process(document, relativeDocumentFolderPath);
		} catch (MarkdownParsingException e) {
			throw new MarkdownParsingException("There was an error while parsing the markdown file at [" + locationDescription + "], reason :" + e.getMessage(), e);
		}
		
		if (createShortDescription()) {
			shortDescriptionBuilder.append("...");
			markdownFile.ifPresent(md -> md.getDisplayInfo().setShortDescription(shortDescriptionBuilder.toString()));
		}
	}

	private void process(Node node, UniversalPath relativeDocumentFolderPath) {
		if (node instanceof Link) {
			Link linkNode = (Link) node;

			try {
				resolveLink(relativeDocumentFolderPath, linkNode);
			} catch (MarkdownParsingException e) {
				String filepath = markdownFile.map(MarkdownFile::getDocRelativeLocation).map(UniversalPath::toSlashPath).orElse("[Unknown Location]");

				throw new MarkdownParsingException("Error while parsing link at " + filepath + ":" + linkNode.getLineNumber(), e);
			}
		} 
		else if (node instanceof Image) {			

			Image image = (Image) node;
			final String uriStr = image.getUrl().toString();
			MarkdownUri markdownUri = MarkdownUri.resolveLink(relativeDocumentFolderPath, uriStr, contextPath);
			
			URI normalizedUri = markdownUri.getUri().normalize();
			image.setUrl(CharSubSequence.of(normalizedUri.toString()));
		}
		else if (node instanceof Heading) {

			markdownFile.ifPresent(md -> {
				Heading heading = (Heading) node;
				String text = heading.getText().toString();
				text = text.replaceAll("</?[a-zA-Z0-9]+>", "");
				
				md.addHeading(text);
				
				FileDisplayInfo displayInfo = md.getDisplayInfo();
				
				if (displayInfo.getDisplayTitle() == null) {
					displayInfo.setDisplayTitle(text);
				}
				
				if (heading.getLevel() == 1) {
					inFirstLevelHeaderParagraph = true;
				} else {
					inFirstLevelHeaderParagraph = false;
				}
			});

			// The following code is left as an example of how to manually set anchors instead of using the default
			// anchor creation
			//
			// Heading heading = (Heading) node;
			// String headingText = heading.getText().toString();
			// String anchor = headingText.replaceAll(" ", "-");
			// heading.setAnchorRefId(anchor);
		} else if (node instanceof Text) {

			markdownFile.ifPresent(md -> {
				String text = node.getChars().toString();
				// TODO: think of where induced whitespacing is appropriate between text nodes
				md.addContentText(text);
				
				if (inFirstLevelHeaderParagraph && createShortDescription()) {
					if (shortDescriptionBuilder.length() < ROUGH_LENGTH_SHORT_DESCRIPTION) {
						if (node.getAncestorOfType(Paragraph.class) != null)
							shortDescriptionBuilder.append(text);
					}
				}
			});

		}

		for (Node childNode : node.getChildren()) {
			process(childNode, relativeDocumentFolderPath);
		}
		
		// insert whitespaces in lunrjs index after block type nodes to avoid merging of words 
		if (node instanceof Block) {
			markdownFile.ifPresent(md -> md.addContentText("\n"));
		}
	}

	private boolean hasExactlyOneChild(Node node) {
		return node.hasChildren() && !node.hasOrMoreChildren(2);
	}

	private void resolveLink(UniversalPath currentRelativeFolder, Link link) {
		final String uriStr = link.getUrl().toString();
		MarkdownUri markdownUri = MarkdownUri.resolveLink(currentRelativeFolder, uriStr, contextPath);

		// TODO: Clean up the following section
		if (markdownUri.isJavadocSchemed()) {
			String pathString = markdownUri.getUri().getSchemeSpecificPart();
			UniversalPath path = UniversalPath.empty().pushDottedPath(pathString);
			String javadocLocation = contextPath.stream().map(p -> "..").collect(Collectors.joining("/"));

			String newUrl = javadocLocation + "/../javadoc/" + path.toSlashPath() + ".html";

			final String simpleClassName;
			if (javadoc != null) {
				Optional<String> simpleClassNameOptional = javadoc.getSimpleClassName(pathString);

				if (checkForLinkValidity && !simpleClassNameOptional.isPresent()) {
					String currentMdFilePathString = markdownFile //
							.map(MarkdownFile::getDocRelativeHtmlFileLocation) //
							.map(UniversalPath::toFilePath) //
							.orElse("<unknown>");

					println(red("    Found dead javadoc link: '" + pathString + "' in " + currentMdFilePathString
							+ ". This class is not part of the current javadoc build."));
					replaceNodeWithHtmlTag(link, "a", "class='deadlink' href='#' title=\"This link's target does not exist\"");

					return;
				}

				simpleClassName = simpleClassNameOptional.orElse(pathString);
			} else {
				simpleClassName = pathString.substring(pathString.lastIndexOf('.') + 1);
			}

			replaceNodeWithHtmlTag(link, "a", "class='javadoclink' target=_blank href='" + newUrl + "'");
			markdownFile.ifPresent(md -> {
				md.addJavadocLink(new TitledLink(simpleClassName, newUrl, pathString));
			});

			return;
		}

		if (markdownUri.getScheme() != null) {
			// External URI
			String externalUriString = markdownUri.getUri().toString();
			replaceNodeWithHtmlTag(link, "a",
					"class='outsidelink' target=_blank href='" + externalUriString + "' title=\"This links to outside of this documentation\"");
			markdownFile.ifPresent(md -> {
				md.addExternalLink(new TitledLink(link.getText().toString(), externalUriString, externalUriString));
			});

			return;
		}

		if (markdownUri.getPath() == null || markdownUri.getPath().isEmpty()) {

			return;
		}

		try {
			// This is just to validate the path of the link's URI, which must be a valid path on the current file
			// system
			Paths.get(markdownUri.getPath());
		} catch (InvalidPathException e) {
			throw new MarkdownParsingException("Link contains invalid path: '" + markdownUri.getPath() + "'.", e);
		}

		Path currentFolder = rootPath.resolve(contextPath.toPath());
		File targetFile = currentFolder.resolve(markdownUri.getPath()).toFile();
		UniversalPath linkTargetPath = contextPath.pushSlashPath(markdownUri.getPath()).normalize();

		markdownFile.ifPresent(md -> {
			md.addReference(linkTargetPath);
		});
		

		if (markdownUri.isIncludeLink()) {
			resolveIncludeLink(link, markdownUri, targetFile, linkTargetPath);
		} else {

			if (checkForLinkValidity && !targetFile.exists()) {
				// let's allow to reference a .md file via its html name
				String asMdName = markdownUri.getPath().replaceAll("\\.html$", ".md");
				File targetMdFile = currentFolder.resolve(asMdName).toFile();
				if (!targetMdFile.exists()) {
					String page404 = markdownUri.getPath() + ".404.html";

					replaceNodeWithHtmlTag(link, "a", "class='deadlink' href='" + page404 + "' title=\"This link's target does not exist\"");

					return;
				}
			}

			markdownUri.toHtmlName();

			URI normalizedUri = markdownUri.getUri().normalize();

			link.setUrl(CharSubSequence.of(normalizedUri.toString()));

		}
	}

	private void resolveIncludeLink(Link link, MarkdownUri markdownUri, File targetFile, UniversalPath linkTargetPath) {
		File fileToInclude = targetFile;

		if (!fileToInclude.exists()) {
			throw new MarkdownParsingException("You were trying to include '" + linkTargetPath
					+ "', but it does not exist. Expecting it to be at '" + fileToInclude.getAbsolutePath() + "'.");
		}
		
		if (!includedFiles.add(linkTargetPath)) {
			throw new MarkdownParsingException("Circular include detected: '" + linkTargetPath + "' starts a circle.");
		}
		
		Document includedDocument = DocUtils.simpleParse(fileToInclude);
		process(includedDocument, linkTargetPath.getParent(), linkTargetPath.toSlashPath());
		
		includedFiles.remove(linkTargetPath);
		
		DocUtils.FLEXMARK_PARSER.transferReferences(link.getDocument(), includedDocument);
		

		Node linkContextElement = link;
		Node linkContextElementParent = link.getParent(); // The include link might be the only element of a
															// paragraph in which case we want to remove that
															// paragraph
		
		String levelString = markdownUri.parseQuery().get("level");
		if (levelString != null) {
			try {
				int level = Integer.parseInt(levelString);
				
				includedDocument.getDescendants().forEach(n -> {
					if (n instanceof Heading) {
						Heading heading = (Heading) n;
						heading.setLevel(heading.getLevel() + level);
					}
				});
			} catch (NumberFormatException e) {
				throw new MarkdownParsingException("Expected an integer as value of the 'level' parameter of the include URI but got '" + levelString + "'.", e);
			}
			
		}
		
		Heading extractedHeading = null;
		
		if (markdownUri.hasQueryParamWithoutValue("collapsed")) {
			Node firstChild = includedDocument.getFirstChild();
			
			if (firstChild instanceof Heading) {
				firstChild.unlink();
				extractedHeading = (Heading) firstChild;
			}
			
			String includeAsHTML = DocUtils.FLEXMARK_RENDERER_INCLUDE.render(includedDocument);
			String collapsedDocument = "<div class='collapsed'><div>" + includeAsHTML + "</div></div>";
			HtmlInline htmlNode = new HtmlInline(CharSubSequence.of(collapsedDocument));
			includedDocument.removeChildren();
			includedDocument.appendChild(htmlNode);
		}
		
		Iterable<Node> includedDocumentTopLevelNodes = includedDocument.getChildren();

		if (linkContextElementParent instanceof Paragraph && !linkContextElementParent.hasOrMoreChildren(2)) {
			linkContextElement = linkContextElementParent;
		} else if (hasExactlyOneChild(includedDocument)) {
			Node onlyChild = includedDocument.getFirstChild();

			if (onlyChild instanceof Paragraph) {
				includedDocumentTopLevelNodes = onlyChild.getChildren();
			} else {
				includedDocumentTopLevelNodes = includedDocument.getChildren();
			}
		}

		if (extractedHeading != null) {
			linkContextElement.insertBefore(extractedHeading);
		}
		
		for (Node topNode : includedDocumentTopLevelNodes) {
			linkContextElement.insertBefore(topNode);
		}

		linkContextElement.unlink();
	}
	
	public static void removeSelfLinks(Document document, String selfLinkPath) {
		document.getDescendants().forEach(node -> {
			if (node instanceof Link) {
				Link link = (Link) node;
				String linkTargetString = link.getUrl().toString();

				URI targetUri = URI.create(linkTargetString);

				if (targetUri.getPath().equals(selfLinkPath)) {
					replaceNodeWithHtmlTag(link, "a", "href=# class='selflink'");
				}
			}
		});
	}

	private static void replaceNodeWithHtmlTag(Node nodeToReplace, String tagName, String attributes) {
		HtmlInline deadlink = new HtmlInline(CharSubSequence.of("<" + tagName + " " + attributes + ">"));
		nodeToReplace.insertBefore(deadlink);
		nodeToReplace.getChildren().forEach(nodeToReplace::insertBefore);
		nodeToReplace.insertBefore(new HtmlInline(CharSubSequence.of("</" + tagName + ">")));
		nodeToReplace.unlink();
	}

	public static void ensureToc(Document document) {
		int numSmallHeadings = 0;
		Heading firstSmallHeading = null;

		for (Node node : document.getDescendants()) {
			if (node instanceof Heading) {
				Heading heading = (Heading) node;

				if (heading.getLevel() > 1) {

					if (firstSmallHeading == null) {
						firstSmallHeading = heading;
					}
					numSmallHeadings++;
				}
			}
			if (node instanceof TocBlock) {
				return; // don't insert a Toc if there was one placed already
			}
		}

		if (numSmallHeadings > 2) {
			TocBlock tocBlock = new TocBlock(CharSubSequence.of("[TOC]"));
			firstSmallHeading.insertBefore(tocBlock);
		}
	}
	
	

}