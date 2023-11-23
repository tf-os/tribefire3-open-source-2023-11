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
import static com.braintribe.utils.lcd.CollectionTools2.acquireLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.doc.meta.FileDisplayInfo;
import com.braintribe.doc.pages.AllDocsPageRenderer;
import com.braintribe.doc.pages.ContentPageRenderer;
import com.braintribe.doc.pages.DeadLink404Renderer;
import com.braintribe.doc.pages.IndexPageRenderer;
import com.braintribe.doc.pages.OverviewPageRenderer;
import com.braintribe.doc.pages.PageRenderer;
import com.braintribe.doc.pages.PageRenderingContext;
import com.braintribe.doc.pages.SearchPageRenderer;
import com.braintribe.doc.pages.TagsPageRenderer;
import com.braintribe.doc.stop.ParallelStopWatch;
import com.braintribe.doc.stop.SequentialStopWatch;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.Arguments;

public class MarkdownCompiler {

	public static final String MDOC_SUBPATH = ".mdoc/";
	public static final String MDOC_RES_SUBPATH = MDOC_SUBPATH + "res/";
	public static final String MDOC_CONF_SUBPATH = MDOC_SUBPATH + "conf/";
	public static final String MDOC_TEMPLATES_SUBPATH = MDOC_SUBPATH + "templates/";
	public static final String MDOC_TAGS_SUBPATH = MDOC_SUBPATH + "tags/";

	private final File sourceRootFolder;
	private final File targetRootFolder;

	private final DocumentationContent documentationContent;
	private final DisplayInfos displayInfos;

	private boolean verbose;

	private Map<String, Object> dataModelBase;
//	public static Map<PlatformAsset, List<MarkdownFile>> debugLunr = new HashMap<>();
//	public static LunrJsIndexJavascript debugLunrIndex = new LunrJsIndexJavascript();
	private JavadocMerger javadoc;
	
	public final SequentialStopWatch timeMeasure;
	public static ParallelStopWatch renderStopWatch;
	public static SequentialStopWatch lunrTimer;
	
	private final ArrayList<MarkdownFile> untitledAutoHiddenFiles = new ArrayList<>();

	private MarkdownCompiler(File sourceRootFolder, File targetRootFolder, Set<PlatformAsset> markdownDocumentationAssets) {
		super();
		
//		LinearStopWatch.forceJIT();
		timeMeasure = SequentialStopWatch.newTimer("Doc Content");
		
		this.sourceRootFolder = sourceRootFolder;
		this.targetRootFolder = targetRootFolder;

		documentationContent = new DocumentationContent(markdownDocumentationAssets, this::printWarning);
		displayInfos = new DisplayInfos(documentationContent);
	}

	private void verboseOut(String text) {
		verboseOut(ConsoleOutputs.text(text));
	}
	
	private void printWarning(String text) {
		File warningSummaryFile = new File(targetRootFolder, "doc-building-warnings.txt");
		boolean writeIntroduction = !warningSummaryFile.exists();
		try (FileWriter writer = new FileWriter(warningSummaryFile, true)){
			if (writeIntroduction) {
				writer.write("Warnings appeared during building this documentation. This file summarizes them:\n\n");
			}
			writer.write(text);
			writer.write("\n");
		} catch (IOException e) {
			println(ConsoleOutputs.red("Can't write warning summary file " + warningSummaryFile.getAbsolutePath() + ":\n\t" + e.getMessage()));
		}
		
		verboseOut(ConsoleOutputs.sequence(ConsoleOutputs.yellowBg(ConsoleOutputs.black("WARNING:")), ConsoleOutputs.text(" " + text)));
	}

	private void verboseOut(ConsoleOutput text) {
		if (verbose) {
			println(ConsoleOutputs.sequence(ConsoleOutputs.text("  "), text));
		}
	}

	public static void compile(File folder, File targetFolder, Set<PlatformAsset> markdownDocumentationAssets, boolean verboseOutput,
			Map<String, Object> data) {
		compile(folder, targetFolder, markdownDocumentationAssets, verboseOutput, data, null);
	}

	public static void compile(File folder, File targetFolder, Set<PlatformAsset> markdownDocumentationAssets, boolean verboseOutput,
			Map<String, Object> data, JavadocMerger javadocMerger) {
		
		if (markdownDocumentationAssets.isEmpty()) {
			throw new IllegalStateException("No MarkdownDocumentation assets found. Can't compile documentation.");
		}
		try {
			Arguments.notNullWithNames("folder", folder, "targetFolder", targetFolder, "markdownDocumentationAssets", markdownDocumentationAssets,
					"data", data);
			println("Generating documentation");
			MarkdownCompiler compiler = new MarkdownCompiler(folder, targetFolder, markdownDocumentationAssets);
			compiler.verbose = verboseOutput;
			compiler.dataModelBase = data;
			compiler.javadoc = javadocMerger;

			File mdocSubfolder = new File(compiler.sourceRootFolder, MDOC_SUBPATH);
			compiler.displayInfos.checkForDocumentationDisplayInfo(mdocSubfolder);
			compiler.compile();
			

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while compiling markdown");
		}
	}
	
	private void loadMarkdownFile(MarkdownFile markdownFile, File target, MarkdownDocumentLoader documentLoader, LunrJsIndex lunrJsIndex) {
		documentLoader.compileBody(markdownFile, target);
		
		FileDisplayInfo displayInfo = markdownFile.getDisplayInfo();
		
		if (displayInfos.isVisible(markdownFile)) {
			if (markdownFile.getTitle() == null) {
				untitledAutoHiddenFiles.add(markdownFile);
				displayInfo.setHidden(true);
				return;
			}
			
			displayInfo.getTags().forEach(tag -> documentationContent.tagFile(tag, markdownFile)); 
			lunrJsIndex.add(markdownFile);
		}
	}
	
	private void compile() throws IOException {
		timeMeasure.start("init");
		targetRootFolder.mkdirs();
		File targetResourceFolder = new File(targetRootFolder, MarkdownCompiler.MDOC_RES_SUBPATH);

		verboseOut("Parsing markdown files...");
		
		MarkdownDocumentLoader documentLoader = new MarkdownDocumentLoader(sourceRootFolder, javadoc);
		DocumentationLoader loader = new DocumentationLoader(sourceRootFolder, displayInfos);
		
		timeMeasure.stop("init");

		timeMeasure.start("load");
		documentationContent.getAssets().forEach(loader::crawl);
		Map<MarkdownFile, File> foundMdFiles = loader.getFoundMdFiles();
		
		if (foundMdFiles.isEmpty()) {
			throw new IllegalStateException("Can't create documentation: There isn't a single markdown document in any asset of your setup.");
		}
		
		foundMdFiles.keySet().forEach(documentationContent::addMarkdownFile);
		
		verboseOut("  Found " + foundMdFiles.size() + " documents in " + documentationContent.getAssets().size() + " assets.");
		
		try (LunrJsIndex lunrJsIndex = new LunrJsIndexJava()) {
			foundMdFiles.entrySet()
				.parallelStream()
				.forEach(e -> loadMarkdownFile(e.getKey(), e.getValue(), documentLoader, lunrJsIndex));
			
			if (verbose) {
				printOutUntitledFiles();
			}
			
			assertVisibleDocuments();
			
			timeMeasure.stop("load");
	
			copyStaticFiles(loader, targetResourceFolder);
			
			verboseOut("Creating fulltextsearch index...");
			lunrTimer = timeMeasure.start("lunr");
			File serializedIndexFile = new File(targetResourceFolder, "lunr-index.js");
			lunrJsIndex.serialize(serializedIndexFile);
			timeMeasure.stop("lunr");
		} 
		
		timeMeasure.start("meta");
		verboseOut("Calculating metadata...");
		documentationContent.ensureTagDescriptions();
		displayInfos.finalizeEntrypoints();
		timeMeasure.stop("meta");

		renderDocHtmls(loader);
		
//		System.out.println(SequentialStopWatch.summarize(timeMeasure));
	}

	private void assertVisibleDocuments() {
		boolean anyVisibleDocuments = documentationContent.getMarkdownFiles()
				.stream()
				.anyMatch(displayInfos::isVisible);
		
		if (!anyVisibleDocuments) {
			throw new IllegalStateException("Can't create documentation: All documents in your setup are hidden either directly or by their asset configuration.");
		}
	}

	private void printOutUntitledFiles() {
		final String indentedNewline = "\n    ";
		String untitledMdFiles = untitledAutoHiddenFiles.stream()
			.map(MarkdownFile::getDocRelativeLocation)
			.map(UniversalPath::toFilePath)
			.sorted()
			.collect(Collectors.joining(indentedNewline));
		
		if (!untitledMdFiles.isEmpty()) {
			printWarning("The following untitled documents will not be visible in menus or the tags page. " //
					+ "If this was intended and you would like to remove this warning please explicitly hide them in the respective configuration yaml file. "
					+ "If this is not intended make sure they start with a level 1 heading or add a title in the respective configuration yaml file:"
					+ indentedNewline //
					+ untitledMdFiles);
			
		}
	}

	private long copyStaticFile(Path staticFile) {
		File sourceFile = new File(sourceRootFolder, staticFile.toString());
		File targetFile = new File(targetRootFolder, staticFile.toString());
		targetFile.getParentFile().mkdirs();
		try {
			Files.copy(sourceFile.toPath(), targetFile.toPath());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return sourceFile.length();
	}
	
	private void copyStaticFiles(DocumentationLoader loader, File targetResourceFolder) throws IOException {
		verboseOut("Copy static files...");
		SequentialStopWatch staticCopyTimer = timeMeasure.start("static");

		staticCopyTimer.start("global");
		loader.loadGlobalResources();
		targetResourceFolder.mkdirs();
		staticCopyTimer.stop("global");
		
		staticCopyTimer.start("content");
		long bytesCopied = loader.getStaticFiles().parallelStream().map(this::copyStaticFile).collect(Collectors.summingLong(l->l));
		staticCopyTimer.stop("content");
		verboseOut("  Copied " + bytesCopied/1024 + "Kb and " + loader.getStaticFiles().size() + " files." );
		staticCopyTimer.start("technical");
		
		String staticFiles[] = { //
				"lunr.js", //
				"highlight.pack.js", // see res/highlight.pack.js.HOW_TO_GET.txt 
				"jquery-latest.min.js", //
				"jquery.cookie.min.js", //
				"jquery.navgoco.min.js", //
				"pagination.min.js", //
				"pagination.css", //
				"highlight-custom.css", //
				"jquery.navgoco.css", //
				"general.css", //
				"navigation.css", //
				"index.css", //
				"content.css", //
				"custom.css", //
				"content.js", //
				"search.svg", //
				"javadoclink.svg", //
				"outsidelink.svg", //
				"outsidelink-white.svg", //
				"overview.svg", //
				"tfacademy-white-64x235.svg", //
				"tribefire-logo-orange.svg", //
				"collapse.svg", //
				"expand.svg", //
				"anchor-link.svg", //
				"favicon.png" //
		};

		for (String staticFileName : staticFiles) {
			File targetFile = new File(targetResourceFolder, staticFileName);
			URL resource = MarkdownCompiler.class.getResource("res/" + staticFileName);

			if (resource == null) {
				throw new FileNotFoundException("Could not find resource " + staticFileName);
			}

			if (!targetFile.exists()) {
				targetFile.getParentFile().mkdirs();
				try (InputStream in = resource.openStream(); OutputStream out = new FileOutputStream(targetFile)) {
					IOTools.transferBytes(in, out);
				}
			}
		}
		staticCopyTimer.stop("technical");

		timeMeasure.stop("static");
	}
	
	private void renderDocHtmls(DocumentationLoader loader) {
		timeMeasure.start("prep render");

		verboseOut("Generating HTML files");
		List<PageRenderer> pageRenderers = new ArrayList<>();
		
//		MenuCompiler menuCompiler = new MenuCompiler(displayInfos, loader.getDocumentLoader());

		registerContentPageRenderers(loader.getFoundMdFiles(), pageRenderers);
		
		DocumentationLinkAnalyzer documentationLinkAnalyzer = new DocumentationLinkAnalyzer(documentationContent);
		documentationLinkAnalyzer.calculate(targetRootFolder);

		documentationLinkAnalyzer.getDeadLinkTargets().stream() //
			.peek(t -> printDeadLinkWarning(documentationLinkAnalyzer, t))
			.map(t -> new DeadLink404Renderer(t, documentationLinkAnalyzer.getReferencingMdFiles(t))) //
			.forEach(pageRenderers::add);
		
		documentationContent.getUsedTags().stream()
			.map(TagsPageRenderer::new)
			.forEach(pageRenderers::add);

		UniversalPath greetingHtmlPath = UniversalPath.empty().pushSlashPath(MarkdownCompiler.MDOC_CONF_SUBPATH).push("greeting.html");
		File greetingsHtmlFile = loader.getGlobalResource(greetingHtmlPath);
		
		pageRenderers.add(new IndexPageRenderer(greetingsHtmlFile));
		pageRenderers.add(new SearchPageRenderer());
		pageRenderers.add(new OverviewPageRenderer());
		pageRenderers.add(new AllDocsPageRenderer());
		
		FreemarkerRenderer freemarkerRenderer = new FreemarkerRenderer(new File(sourceRootFolder, MDOC_TEMPLATES_SUBPATH));
		
		PageRenderingContext pageRenderingContext = new PageRenderingContext(documentationContent, documentationLinkAnalyzer, displayInfos,
				targetRootFolder, freemarkerRenderer, dataModelBase);

		displayInfos.createContentHierarchy();
		timeMeasure.stop("prep render");
		renderStopWatch = timeMeasure.startParallel("render");
		pageRenderers.parallelStream().forEach(p -> p.render(pageRenderingContext));
		timeMeasure.stop("render");

	}

	private void registerContentPageRenderers(Map<MarkdownFile, File> foundMdFiles, List<PageRenderer> pageRenderers) {
		if (foundMdFiles.isEmpty())
			return;

		// We first group the files by 
		
		Map<String, Map<MarkdownFile, File>> allMdFiles = newMap();
		for (Entry<MarkdownFile, File> e : foundMdFiles.entrySet()) {
			MarkdownFile mdFile = e.getKey();

			String folderRelativePath = mdFile.getDocRelativeLocation().getParent().toString();
			Map<MarkdownFile, File> folderMdFiles = acquireLinkedMap(allMdFiles, folderRelativePath);

			folderMdFiles.put(mdFile, e.getValue());
		}

		for (Map<MarkdownFile, File> folderMdFiles : allMdFiles.values())
			registerContentPageRenderersForOneDir(folderMdFiles, pageRenderers);
	}

	private void registerContentPageRenderersForOneDir(Map<MarkdownFile, File> folderMdFiles, List<PageRenderer> pageRenderers) {
		int i = 1;
		int n = folderMdFiles.size();

		Iterator<Entry<MarkdownFile, File>> it = folderMdFiles.entrySet().iterator();
		Entry<MarkdownFile, File> prev = null;
		Entry<MarkdownFile, File> current = null;
		Entry<MarkdownFile, File> next = it.next();

		do {
			prev = current;
			current = next;
			next = it.hasNext() ? it.next() : null;

			MarkdownFile prevMd = prev == null ? null : prev.getKey();
			MarkdownFile nextMd = next == null ? null : next.getKey();

			pageRenderers.add(new ContentPageRenderer(current.getKey(), current.getValue(), prevMd, nextMd, i++, n));

		} while (next != null);

	}

	private void printDeadLinkWarning(DocumentationLinkAnalyzer documentationLinkAnalyzer, UniversalPath t) {
		String referencesDescription = documentationLinkAnalyzer.getReferencingMdFiles(t) //
			.stream() //
			.map(MarkdownFile::getDocRelativeLocation) //
			.map(UniversalPath::toFilePath) //
			.collect(Collectors.joining("\n  \t"));
		
		printWarning("File '" + t.toFilePath() + "' does not exist but is linked to by the following files:\n  \t" + referencesDescription);
	}
}
