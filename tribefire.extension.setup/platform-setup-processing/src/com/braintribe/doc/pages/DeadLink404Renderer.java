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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.doc.MarkdownFile;
import com.braintribe.doc.UniversalPath;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.utils.CollectionTools;

public class DeadLink404Renderer extends AbstractPageRenderer {
	private UniversalPath deadLinkTarget;
	private Set<MarkdownFile> referencingMdFiles;
	
	public DeadLink404Renderer(UniversalPath deadLinkTarget, Set<MarkdownFile> referencingMdFiles) {
		super("file404.ftlh");
		this.deadLinkTarget = deadLinkTarget;
		this.referencingMdFiles = referencingMdFiles;
	}
	
	@Override
	protected Map<String, Object> dataModel(PageRenderingContext context) {
		
		Map<String, List<MarkdownFile>> filesPerAsset = referencingMdFiles.stream()
				.peek(referencingMdFile -> context.verboseOut(ConsoleOutputs.red("  Link target " + deadLinkTarget.toSlashPath()
						+ " does not exist but is referenced by " + referencingMdFile.getDocRelativeLocation())))
				.collect(Collectors.toMap(MarkdownFile::getAssetId, CollectionTools::getList, CollectionTools::addElementsToCollection));

		String relativeFilePathToRoot = "";

		for (int i = 0; i < deadLinkTarget.getNameCount() - 1; i++) {
			relativeFilePathToRoot += "../";
		}

		PlatformAsset assetFromPath = context.getDocumentationContent().getAsset(deadLinkTarget);

//		String menu = menuCompiler.getMenu(assetFromPath, deadLinkTarget);

		Map<String, Object> dataModel = context.newDataModel(relativeFilePathToRoot);
		dataModel.put("referencingMdFiles", referencingMdFiles);
		dataModel.put("referencingMdFilesPerAsset", filesPerAsset);
		dataModel.put("menu", context.getMenu(assetFromPath));
		
		return dataModel;

	}
	
	@Override
	protected UniversalPath targetFile(PageRenderingContext context) {
		
		String targetFileName = deadLinkTarget.getName() + ".404.html";
		
		return deadLinkTarget.getParent().push(targetFileName);

	}
	
}
