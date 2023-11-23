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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.doc.meta.CustomAssetMetaData;
import com.braintribe.doc.meta.FileDisplayInfo;
import com.braintribe.model.asset.PlatformAsset;

public class ContentHierarchy {
	
	private final Map<PlatformAsset, Asset> assets = new HashMap<>();
	private final DisplayInfos displayInfos;
	
	public ContentHierarchy(DisplayInfos displayInfos) {
		this.displayInfos = displayInfos;
	}
	
	public void register(PlatformAsset platformAsset) {
		CustomAssetMetaData customAssetMetaData = displayInfos.assetDisplayInfos.get(platformAsset);
		Asset asset = new Asset(customAssetMetaData);
		assets.put(platformAsset, asset);
	}
	
	public void register(MarkdownFile markdownFile) {
		MdFile mdFile = new MdFile(markdownFile);
		
		UniversalPath parentPath = markdownFile.getAssetRelativeLocation().getParent();
		Asset asset = assets.get(markdownFile.getAsset());
		
		Folder folder = asset.ensureFolder(markdownFile.getAsset(), parentPath);
		folder.getSubElements().add(mdFile);
		
	}
	
	public Folder getAssetElement(PlatformAsset platformAsset) {
		Asset asset = assets.get(platformAsset);
		if (asset == null)
			return null;
		
		return asset.asFolder();
	}
	
	public abstract class Element {
		private FileDisplayInfo fileDisplayInfo;
		public Element(FileDisplayInfo fileDisplayInfo) {
			this.fileDisplayInfo = fileDisplayInfo;
		}
		public abstract String getTitle();
		public abstract String getUrl();
		
		public FileDisplayInfo getDisplayInfo() {
			return fileDisplayInfo;
		}
		
		public boolean isHidden() {
			return getDisplayInfo().getHidden();
		}
	}
	
	private class Asset {
		private CustomAssetMetaData asset;
		private Map<UniversalPath, Folder> folders = new LinkedHashMap<>();

		public Asset(CustomAssetMetaData asset) {
			this.asset = asset;
			String title = getTitle();
			Folder rootFolder = new Folder(title, FileDisplayInfo.T.create());
			folders.put(UniversalPath.empty(), rootFolder);
		}
		
		public String getTitle() {
			return asset.getDisplayTitle();
		}
		
		Folder ensureFolder(PlatformAsset platformAsset, UniversalPath assetRelativeFolderPath) {
			Folder parent = folders.get(assetRelativeFolderPath); 
			if (parent != null) {
				return parent;
			}
			
			UniversalPath parentPath = assetRelativeFolderPath.getParent();
			parent = ensureFolder(platformAsset, parentPath);
			
			UniversalPath assetPath = DocUtils.getPathFromAsset(platformAsset);
			UniversalPath docRelativeFolderPath = assetPath.push(assetRelativeFolderPath);
			FileDisplayInfo folderDisplayInfo = displayInfos.getFileDisplayInfo(docRelativeFolderPath);
			String title = folderDisplayInfo.getDisplayTitle();
			if (title == null) {
				title = assetRelativeFolderPath.getName();
			}
			Folder folder = new Folder(title, folderDisplayInfo);
			parent.subElements.add(folder);
			folders.put(assetRelativeFolderPath, folder);

			return folder;
		}
		
		public Folder asFolder() {
			Folder folder = folders.get(UniversalPath.empty());
			return folder;
		}
		
	}
	
	public class Folder extends Element {
		public List<Element> subElements = new ArrayList<>();
		private String title;

		public Folder(String title, FileDisplayInfo fileDisplayInfo) {
			super(fileDisplayInfo);
			this.title = title;
		}
		
		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getUrl() {
			return null;
		}
		
		public List<Element> getSubElements() {
			return subElements;
		}
	}
	
	public class MdFile extends Element {
		private MarkdownFile markdownFile;

		public MdFile(MarkdownFile markdownFile) {
			super(markdownFile.getDisplayInfo());
			this.markdownFile = markdownFile;
		}
		
		public MarkdownFile getMarkdownFile() {
			return markdownFile;
		}

		@Override
		public String getTitle() {
			return markdownFile.getTitle();
		}

		@Override
		public String getUrl() {
			return markdownFile.getDocRelativeHtmlFileLocation().toSlashPath();
		}
		
	}
}
