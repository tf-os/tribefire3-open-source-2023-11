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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.asset.PlatformAsset;

public class DocumentationLinkAnalyzer {

	private Set<UniversalPath> deadLinkTargets;
	private Map<UniversalPath, Set<MarkdownFile>> referencingMdFilesPerPath;
	private Map<PlatformAsset, Set<PlatformAsset>> referencedByAssetsPerAsset;
	private Map<PlatformAsset, Set<PlatformAsset>> referencesToAssetsPerAsset;

	private final DocumentationContent documentationContent;
	
	public DocumentationLinkAnalyzer(DocumentationContent documentationContent) {
		this.documentationContent = documentationContent;
	}

	public void calculate(File targetRootFolder) {
		deadLinkTargets = new HashSet<>();
		referencingMdFilesPerPath = new HashMap<>();
		referencedByAssetsPerAsset = new HashMap<>();
		referencesToAssetsPerAsset = new HashMap<>();

		documentationContent.getAssets().forEach(asset -> {
			referencedByAssetsPerAsset.put(asset, new HashSet<>());
			referencesToAssetsPerAsset.put(asset, new HashSet<>());
		});

		documentationContent.getMarkdownFiles() //
				.forEach(mdFile -> {

					mdFile.getReferencingFiles().forEach(path -> {
						Set<MarkdownFile> referencingFiles = referencingMdFilesPerPath.computeIfAbsent(path, l -> new HashSet<>());
						referencingFiles.add(mdFile);
					});
				});

		referencingMdFilesPerPath.forEach((path, referencedBy) -> {
			MarkdownFile mdFileOfPath = documentationContent.getMarkdownFile(path);

			if (mdFileOfPath == null) {
				UniversalPath mdNameOfMdFile = path.getParent().push(path.getName().replaceAll("\\.html$", ".md"));
				mdFileOfPath = documentationContent.getMarkdownFile(mdNameOfMdFile);
			}

			File targetFile = new File(targetRootFolder, path.toFilePath());

			if (mdFileOfPath == null && !targetFile.exists()) {

				deadLinkTargets.add(path);
			} else {
				// File exists but might not be a md file

				PlatformAsset targetAsset = documentationContent.getAsset(DocUtils.getAssetFromPath(path));

				referencedBy //
						.stream() //
						.map(MarkdownFile::getDocRelativeLocation) //
						.map(DocUtils::getAssetFromPath) //
						.map(documentationContent::getAsset) //
						.forEach(sourceAsset -> {
							referencesToAssetsPerAsset.get(sourceAsset).add(targetAsset);
							referencedByAssetsPerAsset.get(targetAsset).add(sourceAsset);
						});
			}
		});

		// Map<MarkdownFile, List<PlatformAsset>> referencesToAssetsFromMarkdownFile;
		// Map<MarkdownFile, List<PlatformAsset>> referencingAssetsOfMarkdownFile;

	}

	public Set<UniversalPath> getDeadLinkTargets() {
		return deadLinkTargets;
	}

	public Set<MarkdownFile> getReferencingMdFiles(UniversalPath path) {
		return referencingMdFilesPerPath.get(path);
	}

	public Stream<PlatformAsset> getAssetsOrderedByImportance() {
		return documentationContent.getAssets() //
				.stream() //
				.sorted(Comparator.comparing((PlatformAsset a) -> referencedByAssetsPerAsset.get(a).size()) //
						.reversed() //
						.thenComparing(Comparator.comparing(PlatformAsset::qualifiedAssetName)));
	}

	
}
