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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.doc.ContentHierarchy;
import com.braintribe.doc.DisplayInfos;
import com.braintribe.doc.DocumentationContent;
import com.braintribe.doc.DocumentationLinkAnalyzer;
import com.braintribe.doc.FreemarkerRenderer;
import com.braintribe.doc.MarkdownCompiler;
import com.braintribe.doc.MarkdownFile;
import com.braintribe.model.asset.PlatformAsset;

import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class PageRenderingContext {
	private final DocumentationContent documentationContent;
	private final DocumentationLinkAnalyzer documentationLinkAnalyzer;
	private final DisplayInfos displayInfos;
	private final File targetRootFolder;
	private final FreemarkerRenderer freemarkerRenderer;
	private final Map<String, Object> dataModelBase;

	public PageRenderingContext( //
			DocumentationContent documentationContent, // 
			DocumentationLinkAnalyzer documentationLinkAnalyzer, // 
			DisplayInfos displayInfos, File targetRootFolder, //
			FreemarkerRenderer freemarkerRenderer, //
			Map<String, Object> dataModelBase) {
		this.documentationContent = documentationContent;
		this.documentationLinkAnalyzer = documentationLinkAnalyzer;
		this.displayInfos = displayInfos;
		this.targetRootFolder = targetRootFolder;
		this.freemarkerRenderer = freemarkerRenderer;
		this.dataModelBase = dataModelBase;
	}

	public DocumentationContent getDocumentationContent() {
		return documentationContent;
	}

	public DocumentationLinkAnalyzer getDocumentationLinkAnalyzer() {
		return documentationLinkAnalyzer;
	}

	public DisplayInfos getDisplayInfos() {
		return displayInfos;
	}

	public File getTargetRootFolder() {
		return targetRootFolder;
	}

	public FreemarkerRenderer getFreemarkerRenderer() {
		return freemarkerRenderer;
	}
	
	public Map<String, Object> getDataModelBase() {
		return dataModelBase;
	}
	
	public Map<String, Object> newDataModelWithAssets(String relativeFilePathToRoot) {
		Map<String, Object> dataModel = newDataModel(relativeFilePathToRoot);
		
		List<PlatformAsset> assetsOrdered = displayInfos.getAssetsOrdered()
				.stream()
				.filter(displayInfos::isVisible)
				.collect(Collectors.toList());
		
		dataModel.put("assets", assetsOrdered);
		dataModel.put("metaDataOf", new MapWithNonStringKeys(displayInfos.assetDisplayInfos));
		
		return dataModel;
	}
	
	public Map<String, Object> newDataModel(String relativeFilePathToRoot) {
		Map<String, Object> dataModel = new HashMap<>();

		dataModel.putAll(dataModelBase);

		dataModel.put("relativeFilePathToRoot", relativeFilePathToRoot);
		dataModel.put("resDir", relativeFilePathToRoot + MarkdownCompiler.MDOC_RES_SUBPATH);
		dataModel.put("documentation", displayInfos.docMetaData);
		dataModel.put("isVisible", new TemplateMethodModelEx() {
			@Override
			public Object exec(List arguments) throws TemplateModelException {
				Object firstElement = arguments.get(0);

				if (firstElement instanceof PlatformAsset) {
					PlatformAsset asset = (PlatformAsset) firstElement;
					return displayInfos.isVisible(asset) ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
				} else if (firstElement instanceof MarkdownFile) {
					MarkdownFile mdFile = (MarkdownFile) firstElement;
					return displayInfos.isVisible(mdFile) ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
				}

				throw new IllegalArgumentException("isVisible only takes MarkdownFile and PlatformAsset arguments");
			}

		});
		dataModel.put("javadocLocation", relativeFilePathToRoot + "../javadoc/index.html");
		dataModel.put("headerMenuAssets", displayInfos.getHeaderMenuAssets());
		dataModel.put("metaDataOf", new MapWithNonStringKeys(displayInfos.assetDisplayInfos));

		return dataModel;
	}

	public void verboseOut(ConsoleOutput consoleOutput) {
		ConsoleOutputs.println(consoleOutput);
	}
	
	public ContentHierarchy.Element getMenu(PlatformAsset asset){
		return displayInfos.getContentHierarchy().getAssetElement(asset);
	}
	
	
}
