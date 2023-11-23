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

import static com.braintribe.doc.DocUtils.parseYamlFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.doc.ContentHierarchy.Element;
import com.braintribe.doc.ContentHierarchy.Folder;
import com.braintribe.doc.meta.CustomAssetMetaData;
import com.braintribe.doc.meta.CustomDocMetaData;
import com.braintribe.doc.meta.CustomFolderMetaData;
import com.braintribe.doc.meta.Entrypoint;
import com.braintribe.doc.meta.FileDisplayInfo;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.CommonTools;

public class DisplayInfos {
	public static final String FILE_DISPLAY_INFO_FILENAME = "mdoc-folder-metadata.yml";
	public static final String ASSET_DISPLAY_INFO_FILENAME = "mdoc-asset-metadata.yml";
	public static final String DOC_DISPLAY_INFO_FILENAME = "mdoc-doc-metadata.yml";

	private final Map<UniversalPath, FileDisplayInfo> fileDisplayInfos = new HashMap<>();
	public final Map<PlatformAsset, CustomAssetMetaData> assetDisplayInfos = new HashMap<>();
	
	private final List<PlatformAsset> assetsOrdered = new ArrayList<>();

	public CustomDocMetaData docMetaData;

	public final DocumentationContent documentationContent;
//	private ContentDisplayHierarchy contentDisplayHierarchy;
	private ContentHierarchy contentHierarchy;

	public DisplayInfos(DocumentationContent documentationContent) {
		this.documentationContent = documentationContent;
	}
	
	private void ensureAssetDescription(PlatformAsset asset, CustomAssetMetaData assetMetaData) {
		if (assetMetaData.getShortDescription() == null) {
			Folder assetElement = contentHierarchy.getAssetElement(asset);
			
			List<Element> directSubElements = assetElement.getSubElements();
			int i=0;
			StringBuilder descriptionBuilder = new StringBuilder("Learn about: ");
			for (Element element: directSubElements) {
				if (i>0) {
					descriptionBuilder.append(", \n");
				}
				
				descriptionBuilder.append(element.getTitle());
				
				i++;
				if (i >= 5) {
					descriptionBuilder.append(", ...");
					break;
				}
			}
			
			if (i==0) {
				assetMetaData.setShortDescription("(No content)");
			} else {
				assetMetaData.setShortDescription(descriptionBuilder.toString());
			}
		}
	}
	
	private <T extends GenericEntity> T merge(EntityType<T> entityType, T first, T second) {
		T merged = entityType.create();
		
		for (Property p: entityType.getProperties()) {
			Object currentValue = p.get(first);
			if (currentValue == null) {
				Object newValue = p.get(second);
				p.set(merged, newValue);
			} else {
				p.set(merged, currentValue);					
			}
		}
		
		return merged;
	}
	
	private void putAssetDisplayInfo(PlatformAsset asset, CustomAssetMetaData assetMetaData) {
		CustomAssetMetaData currentAssetMetaData = assetDisplayInfos.get(asset); 
		final CustomAssetMetaData mergedAssetMetaData; 

		if (currentAssetMetaData == null) {
			mergedAssetMetaData = assetMetaData;
		} else {
			mergedAssetMetaData = merge(CustomAssetMetaData.T, currentAssetMetaData, assetMetaData);
		}
		
		if (mergedAssetMetaData.getDisplayTitle() == null) {
			// By convention documentation assets have a "-doc" suffix which we want to ignore here
			String simpleAssetName = asset.getName().replaceAll("-doc$", "");
			String beautifiedAssetName = Arrays.stream(simpleAssetName.split("\\W+")) //
					.map(CommonTools::capitalize) //
					.collect(Collectors.joining(" "));

			mergedAssetMetaData.setDisplayTitle(beautifiedAssetName);
		}
		
		if (mergedAssetMetaData.getImageUrl() != null) {
			UniversalPath pathFromAsset = DocUtils.getPathFromAsset(asset);
			String imageUrl = mergedAssetMetaData.getImageUrl();
			
			MarkdownUri markdownUriImage = MarkdownUri.resolveLink(pathFromAsset, imageUrl);
			mergedAssetMetaData.setImageUrl(markdownUriImage.getUri().toString());
		}

		ensureAssetRegistered(asset);
		assetDisplayInfos.put(asset, mergedAssetMetaData);
	}

	private void ensureAssetRegistered(PlatformAsset asset) {
		if (!assetsOrdered.contains(asset)) {
			assetsOrdered.add(asset);
		}
	}
	
	public List<PlatformAsset> getAssetsOrdered(){
		return assetsOrdered;
	}
	
	public void checkForAssetDisplayInfo(File currentSourceFolder, PlatformAsset asset) {
		File assetMetadataFile = new File(currentSourceFolder, ASSET_DISPLAY_INFO_FILENAME);

		if (assetMetadataFile.exists()) {
			CustomAssetMetaData assetMetaData = parseYamlFile(assetMetadataFile, CustomAssetMetaData.T);

			putAssetDisplayInfo(asset, assetMetaData);
		} else {
			putAssetDisplayInfo(asset, CustomAssetMetaData.T.create());
		}
	}

	public void checkForDocumentationDisplayInfo(File overridingResourcesFolder) {
		File docMetadataFile = new File(overridingResourcesFolder, DOC_DISPLAY_INFO_FILENAME);

		if (docMetadataFile.exists()) {

			docMetaData = parseYamlFile(docMetadataFile, CustomDocMetaData.T);

			docMetaData.getAssets().forEach((assetId, metadata) -> {
				PlatformAsset asset = documentationContent.getAsset(assetId);
				
				if (asset == null) {
					throw new IllegalStateException("In the documentation config yaml you tried to configure asset '" + assetId + "' which is not present in your current compilation");
				}
				
				assetDisplayInfos.put(asset, metadata);
				ensureAssetRegistered(asset);
			});
			
			docMetaData.getTags().forEach(documentationContent::registerTag);


		} else {
			docMetaData = CustomDocMetaData.T.create();
			docMetaData.setTitle("Documentation");
			docMetaData.setAutoGenerateEntryPoints(true);
		}
	}

	public CustomFolderMetaData processForMetaData(File currentSourceFolder, UniversalPath docRelativePath) {
		File folderMdFile = new File(currentSourceFolder, FILE_DISPLAY_INFO_FILENAME);

		if (!folderMdFile.exists())
			return CustomFolderMetaData.T.create();

		CustomFolderMetaData folderMd = parseYamlFile(folderMdFile, CustomFolderMetaData.T);

		folderMd.getFiles().forEach((filename, info) -> {
			UniversalPath relativePathToFile = docRelativePath.push(filename);
			fileDisplayInfos.put(relativePathToFile, info);
		});

		return folderMd;
	}
	
	public FileDisplayInfo ensureFileDisplayInfo(CustomFolderMetaData currentFolderDisplayInfos, UniversalPath path) {
		UniversalPath parentPath = path.getParent();
		FileDisplayInfo parentDisplayInfos = getFileDisplayInfo(parentPath);
		
		Set<String> parentTags;
		
		if (parentDisplayInfos != null)
			parentTags = parentDisplayInfos.getTags();
		else
			parentTags = Collections.EMPTY_SET;		
		
		FileDisplayInfo fileDisplayInfo = currentFolderDisplayInfos.getFiles().computeIfAbsent(path.getName(), n -> FileDisplayInfo.T.create());
		fileDisplayInfo.getTags().addAll(parentTags);
		
		fileDisplayInfos.put(path, fileDisplayInfo);
		
		return fileDisplayInfo;
	}

	public List<PlatformAsset> getHeaderMenuAssets(){
		return assetsOrdered.stream()
				.filter(this::isVisible)
				.limit(5)
				.collect(Collectors.toList());
	}
	
	public Map<PlatformAsset, List<MarkdownFile>> getRootLevelMarkdownFilesPerAsset() {
		Map<PlatformAsset, List<MarkdownFile>> markdownFilesPerAssetSorted = new LinkedHashMap<>();

		getMarkdownFilesPerAsset().forEach((asset, mdFiles) -> {
			List<MarkdownFile> rootLevelMdFiles = mdFiles.stream() //
					.filter(mdFile -> mdFile.getAssetRelativeLocation().getParent().isEmpty()) //
					.collect(Collectors.toList());

			markdownFilesPerAssetSorted.put(asset, rootLevelMdFiles);
		});

		return markdownFilesPerAssetSorted;
	}

	public Map<PlatformAsset, List<MarkdownFile>> getMarkdownFilesPerAsset() {
		return getMarkdownFilesPerAsset(documentationContent.getAssets().size());
	}

	public Map<PlatformAsset, List<MarkdownFile>> getMarkdownFilesPerAsset(int limit) {
		Map<PlatformAsset, List<MarkdownFile>> markdownFilesPerAssetSorted = documentationContent.getAssets() //
				.stream() //
				.filter(this::isVisible) //
				.limit(limit) //
				.flatMap(a -> documentationContent.getMarkdownFiles(a).stream()) //
				.filter(this::isVisible) //
				.collect(Collectors.groupingBy(MarkdownFile::getAsset));
		
//		if (!markdownFilesPerAssetSorted.equals(MarkdownCompiler.debugLunr)) {
//			Set<PlatformAsset> currentKeySet = markdownFilesPerAssetSorted.keySet();
//			Set<PlatformAsset> oldKeySet = MarkdownCompiler.debugLunr.keySet();
//			
//			Set<List<MarkdownFile>> currentValues = new HashSet<>(); 
//			currentValues.addAll(markdownFilesPerAssetSorted.values());
//			Set<List<MarkdownFile>> oldValues = new HashSet<>(); 
//			oldValues.addAll(MarkdownCompiler.debugLunr.values());
//			
//			if (currentKeySet.size() != oldKeySet.size())
//				throw new IllegalStateException("DEBUG: key size differ");
//			
//			if (currentValues.size() != oldValues.size())
//				throw new IllegalStateException("DEBUG: value size differ");
//			
//			if (!currentKeySet.equals(oldKeySet))
//				throw new IllegalStateException("DEBUG: key sets differ");
//			
//			if (!currentValues.equals(oldValues))
//				throw new IllegalStateException("DEBUG: value sets differ");
			
//			markdownFilesPerAssetSorted.forEach((k,v)-> {
//				List<MarkdownFile> expected = MarkdownCompiler.debugLunr.get(k);
//				
//				if (v.equals(expected)) {
//					System.out.println("## YES: " + k.qualifiedAssetName());
//				} else {
//					System.out.println("#!! NO !! "  + k.qualifiedAssetName());
//					System.out.println("  Expected size: " + expected.size() + " vs " + v.size());
//					
//					v.stream().filter(m -> !expected.contains(m)).forEach(m -> System.out.println("      " + m.getAssetRelativeLocation()));
//				}
//			});
//		}

		return markdownFilesPerAssetSorted;
	}

	public boolean isVisible(PlatformAsset asset) {
		return assetDisplayInfos.get(asset) == null || assetDisplayInfos.get(asset).getHidden() == false;
	}
	
	public boolean isVisible(MarkdownFile mdFile) {
		
		if (!isVisible(mdFile.getAsset()))
			return false;
		
		UniversalPath elementPath = mdFile.getDocRelativeLocation();
		do {
			FileDisplayInfo fileDisplayInfo = fileDisplayInfos.get(elementPath);
			if (fileDisplayInfo != null && fileDisplayInfo.getHidden() == true) {
				return false;
			}
			elementPath = elementPath.getParent();
		} while (!elementPath.isEmpty());
		
		return true;
	}

	public CustomAssetMetaData getAssetDisplayInfo(PlatformAsset asset) {
		return assetDisplayInfos.get(asset);
	}
	
	public FileDisplayInfo getFileDisplayInfo(UniversalPath path) {
		return fileDisplayInfos.get(path);
	}

	public void createContentHierarchy() {
		documentationContent.getAssets().forEach(this::ensureAssetRegistered);
		contentHierarchy = new ContentHierarchy(this);

		getAssetsOrdered()
		.stream()
		.filter(this::isVisible)
		.forEach(asset -> {
			List<MarkdownFile> markdownFiles = documentationContent.getMarkdownFiles(asset);

			// TODO: This does not really belong here...
			if (assetDisplayInfos.get(asset).getStartingPoint() == null) {
				Optional<MarkdownFile> first = markdownFiles.stream().filter(this::isVisible).findFirst();
				if (!first.isPresent()) {
					// If there is no visible file then hide the whole asset
					assetDisplayInfos.get(asset).setHidden(true);
					return;
				}
				MarkdownFile firstMarkdownFile = first.get();
				String assetRelativeHtmlLocation = DocUtils.toHtmlName(firstMarkdownFile.getAssetRelativeLocation().toSlashPath());
				assetDisplayInfos.get(asset).setStartingPoint(assetRelativeHtmlLocation);
			}
			
			contentHierarchy.register(asset);
			markdownFiles.stream().forEach(md -> contentHierarchy.register(md));
			
			// TODO: This does not really belong here...
			CustomAssetMetaData assetMeta = assetDisplayInfos.get(asset);
			ensureAssetDescription(asset, assetMeta);
		});
		
	}

	public ContentHierarchy getContentHierarchy() {
		return contentHierarchy;
	}
	
	public void finalizeEntrypoints() {
		List<Entrypoint> entrypoints = docMetaData.getEntrypoints();
		
		if (docMetaData.getAutoGenerateEntryPoints()) {

			getMarkdownFilesPerAsset(4).forEach((asset, files) -> {
				Entrypoint entrypoint = Entrypoint.T.create();

				entrypoint.setAssetId(asset.getGroupId() + ":" + asset.getName());
				entrypoints.add(entrypoint);
			});

		}
		
		for (Entrypoint entrypoint: entrypoints) {
			PlatformAsset asset = documentationContent.getAsset(entrypoint.getAssetId());
			
			if (asset == null) {
				throw new IllegalStateException("Entrypoint for asset '" + entrypoint.getAssetId() + "' configured but this asset is not part of the compilation.");
			}
			
			CustomAssetMetaData currentAssetMetaData = assetDisplayInfos.get(asset);
			
			CustomAssetMetaData mergedAssetMetaData;
			
			if (entrypoint.getDisplayInfo() == null) {
				mergedAssetMetaData = currentAssetMetaData;
			} else {
				mergedAssetMetaData = merge(CustomAssetMetaData.T, entrypoint.getDisplayInfo(), currentAssetMetaData);
			}
			
			entrypoint.setDisplayInfo(mergedAssetMetaData);
			
		}
		
	}

}
