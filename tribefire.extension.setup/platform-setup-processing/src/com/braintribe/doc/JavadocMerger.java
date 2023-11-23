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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeFilter;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

public class JavadocMerger {
	private static final String REMOVE_DOM_ELEMENTS_SELECTOR = ".bottomNav, .subNav, .fixedNav, .topNav, .navPadding, .NavBarCell1, .NavBarCell2, .NavBarCell3, script, noscript, .memberSummary > caption";
	private Map<File, String> scannedClassHTMLs = new ConcurrentHashMap<>();
	private Map<File, UniversalPath> fileToRelativePath = new ConcurrentHashMap<>();
	private Collection<File> scannedHTMLs = new ConcurrentLinkedQueue<>();

	private Map<String, String> simpleClassNames;

	public Optional<String> getSimpleClassName(String fullClassName) {
		return Optional.ofNullable(simpleClassNames.get(fullClassName));
	}

	// TODO: Many htmls are not class docs but e.g. package docs. Treat them differently
	private void scanForJavadocHtmls(Path rootFolder, Path relativeFolder) {
		File currentFolder = rootFolder.resolve(relativeFolder).toFile();
		for (File file : currentFolder.listFiles()) {
			Path filePath = relativeFolder.resolve(file.getName());
			if (file.isDirectory()) {
				scanForJavadocHtmls(rootFolder, filePath);
			} else if (file.getName().endsWith(".html") && relativeFolder.getParent() != null) {

				UniversalPath universalFilePath = UniversalPath.from(filePath);
				String fullyQualifiedClassName = universalFilePath.toDottedPath().replaceAll("\\.html$", "");

				scannedHTMLs.add(file);
				fileToRelativePath.put(file, universalFilePath);

				// technical files have a '-' in the file- or parentfolder name
				if (!fullyQualifiedClassName.contains("-")) {
					scannedClassHTMLs.put(file, fullyQualifiedClassName);
				}
			}
		}
	}

	public void scanForJavadocHtmls(Path rootFolder) {
		System.out.println("Scanning for javadoc in " + rootFolder);
		scanForJavadocHtmls(rootFolder, Paths.get(""));
	}

	private void generateAllClassesHtml(File targetFolder) throws IOException {
		try (FileWriter fileWriter = new FileWriter(new File(targetFolder, "allclasses.html"))) {
			fileWriter.append("<html><head>	<link rel='stylesheet' href='stylesheet.css'/></head><body>");
			fileWriter.append("<ul class='index'>");
			scannedClassHTMLs.entrySet() //
					.stream() //
					.sorted(Comparator.comparing(Map.Entry::getValue)) //
					.forEach(e -> addClassEntry(fileWriter, fileToRelativePath.get(e.getKey()).toSlashPath(), e.getValue()));

			fileWriter.append("</ul>");
			fileWriter.append("</body></html>");
		}
	}
	private void generateIndexHtml(File targetFolder) throws IOException {
		try (FileWriter fileWriter = new FileWriter(new File(targetFolder, "index.html"))) {
			fileWriter.append("<html><head></head>");
			fileWriter.append("<frameset cols=\"20%,80%\" title=\"Documentation frame\">");
			fileWriter.append("<frame src=\"allclasses.html\" name=\"packageFrame\" title=\"All classes and interfaces\">");
			fileWriter.append("<frame src=\"\" name=\"classFrame\" title=\"Class Documentation\">");
			fileWriter.append("</frameset></html>");
		}
	}

	private void addClassEntry(Appendable fileWriter, String href, String simpleName) {
		try {
			fileWriter.append("<li><a href='" + href + "' target='classFrame'>" + simpleName + "</a></li>");
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Could not add class entry to file.");
		}
	}

	// TODO: Generate:
	// allclasses-frame.html
	// allclasses-noframe.html
	// constant-values.html
	// deprecated-list.html
	// help-doc.html
	// index-all.html
	// index.html
	// overview-frame.html
	// overview-summary.html
	// overview-tree.html
	// package-list?
	// or remove all links to those
	public void merge(File targetFolder) throws IOException {
		if (!targetFolder.isDirectory()) {
			throw new IllegalArgumentException("You must pass an existing directory as targetFolder");
		}

		List<String> resFileNames = CollectionTools.getList("stylesheet.css", "resources/inherit.gif");
		for (String resFileName : resFileNames) {
			File targetResFile = new File(targetFolder, resFileName);
			URL resource = JavadocMerger.class.getResource("javadoc/" + resFileName);

			if (resource == null) {
				throw new FileNotFoundException("Could not find resource " + resFileName);
			}

			if (!targetResFile.exists()) {
				targetResFile.getParentFile().mkdirs();
				try (InputStream in = resource.openStream(); OutputStream out = new FileOutputStream(targetResFile)) {
					IOTools.transferBytes(in, out);
				}
			}
		}

		simpleClassNames = new HashMap<String, String>();
		for (String fullClassName : scannedClassHTMLs.values()) {
			String simpleName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
			simpleClassNames.put(fullClassName, simpleName);
		}
		// simpleClassNames = scannedClassHTMLs.values().stream() // TODO: Handle classes with same name but different
		// packages
		// .collect(Collectors.toMap(Function.identity(), s -> s.substring(s.lastIndexOf('.') + 1)));
		
		simpleClassNames = Collections.unmodifiableMap(simpleClassNames);

		scannedHTMLs.stream().parallel().forEach(source -> {
			try {
				applyMerging(targetFolder, source);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});

		generateAllClassesHtml(targetFolder);
		generateIndexHtml(targetFolder);
	}

	private void applyMerging(File targetFolder, File source) throws IOException {
		String fullClassName = scannedClassHTMLs.get(source);
		UniversalPath relativePath = fileToRelativePath.get(source);

		File targetFile = new File(targetFolder, relativePath.toFilePath());
		targetFile.getParentFile().mkdirs();

		Document document = Jsoup.parse(source, "UTF-8");
		document.select(REMOVE_DOM_ELEMENTS_SELECTOR) //
				.forEach(e -> e.remove());

		StringBuilder linkBuilder = new StringBuilder();
		relativePath.getParent().forEach(n -> linkBuilder.append("../"));

		Replacer replacer = new Replacer(linkBuilder.toString(), fullClassName, simpleClassNames);
		Element contentContainer = document.selectFirst(".contentContainer");
		if (contentContainer != null) {
			contentContainer.filter(replacer);
		}

		try (FileWriter fileWriter = new FileWriter(targetFile, false)) {
			document.html(fileWriter);
		}
	}

	public static void main(String[] args) throws IOException {

		File dataDir = FileTools.createDirectory("data");

		JavadocMerger javadocMerger = new JavadocMerger();
		System.out.println("Scanning jsoup");
		javadocMerger.scanForJavadocHtmls(Paths.get("C:\\Users\\neidhart.orlich\\Documents\\Fax\\jsoup-1.11.3-javadoc"));
		System.out.println("Scanning ldaptive");
		javadocMerger.scanForJavadocHtmls(Paths.get("C:\\Users\\neidhart.orlich\\Documents\\Fax\\ldaptive-1.2.1-javadoc"));
		System.out.println("Found " + javadocMerger.scannedClassHTMLs.size() + " files.");
		System.out.println("Merging");
		javadocMerger.merge(dataDir);
		System.out.println("DONE.");
	}

	private static class Replacer implements NodeFilter {
		private final String pathToRoot;
		private final String ownClass;

		private TextNode nodeToReplace;
		private Element elementToBeReplacedWith;
		
		private final Map<String, String> simpleClassNames;

		public Replacer(String pathToRoot, String ownClass, Map<String, String> simpleClassNames) {
			this.pathToRoot = pathToRoot;
			this.ownClass = ownClass;
			this.simpleClassNames = simpleClassNames;
		}

		@Override
		public FilterResult head(Node node, int depth) {
			if (node.nodeName().equals("a")) {
				return FilterResult.SKIP_ENTIRELY;
			}

			return FilterResult.CONTINUE;
		}

		@Override
		public FilterResult tail(Node node, int depth) {
			if (nodeToReplace != null) {
				// Workaround because we can't replace the node that's currently visited
				nodeToReplace.replaceWith(elementToBeReplacedWith);
				nodeToReplace = null;
				elementToBeReplacedWith = null;
			}

			if (node instanceof TextNode) {
				TextNode textNode = (TextNode) node;
				String newText = textNode.text();

				Pattern pattern = Pattern.compile("(\\w+\\.)+\\w+");
				Matcher matcher = pattern.matcher(newText);

				while (matcher.find()) {
					String foundClassName = matcher.group();

					if ((ownClass == null || !ownClass.equals(foundClassName)) && simpleClassNames.containsKey(foundClassName)
					// && newText.matches(".*\\b" + foundClassName + "\\b.*")
					) {
						System.out.println("Replacing " + foundClassName + " in " + ownClass);

						String href = pathToRoot + foundClassName.replaceAll("\\.", "/") + ".html";

						newText = newText.replaceAll(foundClassName,
								"<a style='color: red' href='" + href + "'>" + simpleClassNames.get(foundClassName) + "</a>");
						textNode.text(newText);

						nodeToReplace = textNode;
						elementToBeReplacedWith = new Element("span");
						elementToBeReplacedWith.html(newText);
					}
				}
			}

			return FilterResult.CONTINUE;

		}

	}

}
