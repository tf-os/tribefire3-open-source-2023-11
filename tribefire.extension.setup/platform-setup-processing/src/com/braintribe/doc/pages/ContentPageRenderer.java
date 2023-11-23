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
package com.braintribe.doc.pages;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.braintribe.doc.DisplayInfos;
import com.braintribe.doc.DocumentationContent;
import com.braintribe.doc.DocumentationLinkAnalyzer;
import com.braintribe.doc.MarkdownFile;
import com.braintribe.doc.UniversalPath;
import com.braintribe.doc.meta.WizardInfo;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.NullSafe;

public class ContentPageRenderer extends AbstractPageRenderer {

	private final MarkdownFile mdFile;
	private final File bodyFile;
	private final int index; // Number of page within folder, beginning with 1
	private final int totalFiles;
	private final MarkdownFile prevMd;
	private final MarkdownFile nextMd;

	public ContentPageRenderer(MarkdownFile mdFile, File bodyFile, MarkdownFile prevMd, MarkdownFile nextMd, int index, int totalFiles) {
		super("content.ftlh");
		this.mdFile = mdFile;
		this.bodyFile = bodyFile;
		this.prevMd = prevMd;
		this.nextMd = nextMd;
		this.index = index;
		this.totalFiles = totalFiles;
	}

	@Override
	protected Map<String, Object> dataModel(PageRenderingContext context) {
		DocumentationContent documentationContent = context.getDocumentationContent();
		DisplayInfos displayInfos = context.getDisplayInfos();
		DocumentationLinkAnalyzer documentationLinkAnalyzer = context.getDocumentationLinkAnalyzer();

		UniversalPath docRelativeHtmlFileLocation = mdFile.getDocRelativeHtmlFileLocation();
		UniversalPath path = docRelativeHtmlFileLocation.getParent();

		String relativeFilePathToRoot = "";

		for (int i = 0; i < path.getNameCount(); i++) {
			relativeFilePathToRoot += "../";
		}

		PlatformAsset currentAsset = documentationContent.getAsset(path);

		UniversalPath pathOfMdFile = mdFile.getDocRelativeLocation();

		List<MarkdownFile> referencingMdFiles = NullSafe.collection(documentationLinkAnalyzer.getReferencingMdFiles(pathOfMdFile)) //
				.stream() //
				.filter(displayInfos::isVisible) //
				.sorted(Comparator.comparing(m -> m.getDocRelativeLocation().toSlashPath())) //
				.collect(Collectors.toList());

		List<MarkdownFile> referencedMdFiles = mdFile.getReferencingFiles() //
				.stream() //
				.map(documentationContent::getMarkdownFile) //
				.filter(Objects::nonNull) //
				.filter(displayInfos::isVisible) //
				.collect(Collectors.toList());

		Map<String, Object> dataModel = context.newDataModel(relativeFilePathToRoot);
		try {
			dataModel.put("content", IOTools.slurp(bodyFile, "UTF-8"));
			bodyFile.delete();
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read compiled markdown at " + bodyFile.getAbsolutePath(), e);
		}
		dataModel.put("menu", context.getMenu(currentAsset));
		dataModel.put("mdFile", mdFile);
		dataModel.put("referencingMdFiles", referencingMdFiles);
		dataModel.put("referencedMdFiles", referencedMdFiles);
		dataModel.put("wizard", wizardInfo());

		return dataModel;
	}

	private ExtendedWizardInfo wizardInfo() {
		WizardInfo wizard = mdFile.getWizard();
		return wizard == null ? null : new ExtendedWizardInfo(wizard, index, totalFiles, prevMd, nextMd);
	}

	@Override
	protected UniversalPath targetFile(PageRenderingContext context) {
		return mdFile.getDocRelativeHtmlFileLocation();
	}

	public static class ExtendedWizardInfo {
		public ExtendedWizardInfo(WizardInfo wizard, int index, int total, MarkdownFile prevFile, MarkdownFile nextFile) {
			this.prevFile = prevFile;
			this.nextFile = nextFile;
			this.displayTitle = wizard.getDisplayTitle();
			this.index = index;
			this.total = total;
		}

		private final String displayTitle;
		private final int index;
		private final int total;
		private final MarkdownFile prevFile;
		private final MarkdownFile nextFile;

		// @formatter:off
		public String getDisplayTitle() { return displayTitle; }
		public int getIndex() { return index; }
		public int getTotal() { return total; }
		public MarkdownFile getPrevFile() { return prevFile; }
		public MarkdownFile getNextFile() { return nextFile; }
		// @formatter:on
	}
}
