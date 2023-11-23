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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.doc.meta.CustomFolderMetaData;
import com.braintribe.doc.meta.FileDisplayInfo;
import com.braintribe.model.asset.PlatformAsset;
import com.vladsch.flexmark.ast.Document;

public class DocumentationLoader {

	private final File sourceRootFolder;
	private final Map<MarkdownFile, File> compiledBodies = new LinkedHashMap<>();
	private final Map<UniversalPath, File> globalResources = new HashMap<>();
	
	private final Set<Path> staticFiles = new HashSet<>();
	private final Collection<String> wellKnownResourceFilesToCompile = Collections.singleton(MarkdownCompiler.MDOC_CONF_SUBPATH + "/greeting.md");

	private final DisplayInfos displayInfos;

//	public static ParallelStopWatch docProcessing;

	public DocumentationLoader(File sourceRootFolder, DisplayInfos displayInfos) {
		super();
		this.sourceRootFolder = sourceRootFolder;

		this.displayInfos = displayInfos;
	}

	public void crawl(PlatformAsset asset) {
//		MarkdownCompiler.loadStopWatch.start("prepare");
		UniversalPath assetPath = UniversalPath.start(asset.getGroupId()).push(asset.getName());
		File currentSourceFolder = new File(sourceRootFolder, assetPath.toFilePath());

		displayInfos.checkForAssetDisplayInfo(currentSourceFolder, asset);
//		MarkdownCompiler.loadStopWatch.stop("prepare");
		try {
			crawl(asset, assetPath);
		} catch (IOException e) {
			throw new IllegalStateException("Error while compiling markdown files for asset " + asset.qualifiedAssetName(), e);
		}
	}
	
	
	
	private void crawl(PlatformAsset currentAsset, UniversalPath path) throws IOException {

		String relativePath = path.toFilePath();
		File currentSourceFolder = new File(sourceRootFolder, relativePath);

		CustomFolderMetaData currentFolderMd = displayInfos.processForMetaData(currentSourceFolder, path);

		List<File> relevantFilesInOrder = findRelevantSourceFiles(currentSourceFolder, path, currentFolderMd);
		
		for (File file : relevantFilesInOrder) {
			UniversalPath filePath = path.push(file.getName());

			if (file.isDirectory()) {
				crawl(currentAsset, filePath);

			} else if (file.getName().endsWith(".md")) {
				FileDisplayInfo currentDisplayInfo = displayInfos.getFileDisplayInfo(filePath);
				MarkdownFile markdownFile = MarkdownFile.of(filePath, currentFolderMd, currentAsset);
				markdownFile.setDisplayInfo(currentDisplayInfo);
				File targetFile = File.createTempFile("mdoc-markdown-" + file.getName(), "html");
				compiledBodies.put(markdownFile, targetFile);

			} else {
				Path relativeFilePath = path.push(file.getName()).toPath();
				staticFiles.add(relativeFilePath);
			}
		}

	}
	
	public void loadGlobalResources() {
		UniversalPath mdocResPath = UniversalPath.empty().pushSlashPath(MarkdownCompiler.MDOC_RES_SUBPATH);
		loadResources(sourceRootFolder, mdocResPath);
		
		
		for (String resFileName : wellKnownResourceFilesToCompile) {
			UniversalPath mdocPath = UniversalPath.from(sourceRootFolder);
			UniversalPath resFileRelativePath = UniversalPath.empty().pushSlashPath(resFileName);
			File resMdFile = mdocPath.push(resFileRelativePath).toFile();
			if (!resMdFile.exists()) {
				continue;
			}
			
			String targetFileName = DocUtils.toHtmlName(resFileRelativePath.getName());
			UniversalPath targetRelativePath = resFileRelativePath.getParent().push(targetFileName);
			
			File targetFile = UniversalPath.from(sourceRootFolder) //
					.push(targetRelativePath) //
					.toFile();

			try (FileWriter writer = new FileWriter(targetFile)){
				Document document = DocUtils.FLEXMARK_PARSER.parseReader(new FileReader(resMdFile));
				DocUtils.FLEXMARK_RENDERER.render(document, writer);
				globalResources.put(targetRelativePath, targetFile);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
	
	private void loadResources(File sourceRootFolder, UniversalPath path) {
		File folder = new File(sourceRootFolder, path.toFilePath());
		
		if (!folder.exists()) {
			return;
		}
		
		for (File file: folder.listFiles()) {
			UniversalPath relativeFilePath = path.push(file.getName());
			if (file.isDirectory()) {
				loadResources(sourceRootFolder, relativeFilePath);
			} else {
				staticFiles.add(relativeFilePath.toPath());
			}
		}
	}
	
	public Collection<Path> getStaticFiles(){
		return staticFiles;
	}
	
	private List<File> findRelevantSourceFiles(File currentSourceFolder, UniversalPath path, CustomFolderMetaData currentFolderMd) {

		Set<String> fileNamesSet = currentFolderMd.getFiles().keySet();
		List<String> filenamesInOrderOfOccurance = newList(fileNamesSet);

		return Stream.of(currentSourceFolder.listFiles()) //
				.filter(file -> !file.getName().matches("mdoc-.*-metadata\\.yml")) //
				.peek(file -> displayInfos.ensureFileDisplayInfo(currentFolderMd, path.push(file.getName())))
				/*.filter(file -> {
					FileDisplayInfo fileDisplayInfo = currentFolderDisplayInfos.getFiles().get(file.getName());
					
					return fileDisplayInfo == null || !fileDisplayInfo.getHidden();
				})*/
				.sorted( //
						Comparator.comparing((File file) -> {
							FileDisplayInfo fileDisplayInfo = displayInfos.getFileDisplayInfo(path.push(file.getName()));
							return fileDisplayInfo.getPriority();
						}) //
						.thenComparing(file -> fileNamesSet.contains(file.getName()) ? 0 : 1) //
						.thenComparing(file -> filenamesInOrderOfOccurance.indexOf(file.getName())) //  
						.thenComparing(Comparator.comparing(File::getName))) //
				.collect(Collectors.toList());
	}

		
	public Map<MarkdownFile, File> getFoundMdFiles() {
		return compiledBodies;
	}
	
	public File getGlobalResource(UniversalPath path){
		return globalResources.get(path);
	}
	
}
