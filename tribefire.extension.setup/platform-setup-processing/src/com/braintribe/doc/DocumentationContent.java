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

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.doc.meta.TagDisplayInfo;
import com.braintribe.model.asset.PlatformAsset;
import com.vladsch.flexmark.util.collection.Consumer;

public class DocumentationContent {
	private final Map<PlatformAsset, List<MarkdownFile>> markdownFiles = newLinkedMap();
	private final Map<UniversalPath, MarkdownFile> pathToMdFile = newLinkedMap();
	private final Map<String, PlatformAsset> assets = newLinkedMap();
	private final Map<String, Tag> tags = newLinkedMap();
	private final Consumer<String> warningOut;
	
	DocumentationContent(Set<PlatformAsset> markdownDocumentationAssets, Consumer<String> warningOut) {
		this.warningOut = warningOut;
		
		markdownDocumentationAssets.forEach(asset -> {
			assets.put(asset.getGroupId() + ":" + asset.getName(), asset);
			markdownFiles.put(asset, new ArrayList<>());
		});
	}
	
	public PlatformAsset getAsset(String assetId) {
		return assets.get(assetId);
	}
	
	public void addMarkdownFile(MarkdownFile markdownFile) {
		PlatformAsset currentAsset = markdownFile.getAsset();
		markdownFiles.get(currentAsset).add(markdownFile);
		pathToMdFile.put(markdownFile.getDocRelativeLocation(), markdownFile);
		
	}
	
	public void tagFile(String tagId, MarkdownFile markdownFile) {
		Tag tag = tags.get(tagId);
		
		if (tag == null) {
			warningOut.accept(markdownFile.getDocRelativeLocation() + ": Ignoring unexpected tag: '" + tagId + "'");
			return;
		}
		
		tag.addTaggedFile(markdownFile);
		markdownFile.addTag(tag);
	}
	
	public Tag registerTag(TagDisplayInfo tagConfig) {
		String tagId = tagConfig.getTagId();
		if (tags.containsKey(tagId)) {
			throw new IllegalStateException("Tag with id '" + tagId + "' alreadyRegistered. Duplicate entries not allowed.");
		}
		
		Tag tag = new Tag(tagConfig);
		tags.put(tagId, tag);
		
		return tag;
	}

	public PlatformAsset getAsset(UniversalPath path) {
		return getAsset(DocUtils.getAssetFromPath(path));
	}

	public List<MarkdownFile> getMarkdownFiles(PlatformAsset asset) {
		return Collections.unmodifiableList(markdownFiles.get(asset));
	}

	public Collection<PlatformAsset> getAssets() {
		return assets.values();
	}
	
	public MarkdownFile getMarkdownFile(UniversalPath path) {
		return pathToMdFile.get(path);
	}

	public Map<UniversalPath, MarkdownFile> getPathToMdFile() {
		return pathToMdFile;
	}
	
	public Collection<MarkdownFile> getMarkdownFiles() {
		return pathToMdFile.values();
	}
	
	public Collection<Tag> getUsedTags() {
		return tags.values().stream()
				.filter(t -> !t.getTaggedFiles().isEmpty())
				.sorted(Comparator.comparing(Tag::getName))
				.collect(Collectors.toList());
	}
	
	public void ensureTagDescriptions() {
		getUsedTags().forEach(this::ensureTagDescription);
	}
	
	private void ensureTagDescription(Tag tag) {
		if (tag.getDescription() == null) {
			final int MAX_DOCS = 5;
			
			StringBuilder stringBuilder = new StringBuilder("Contained topics: ");
			int i=0;
			Iterator<MarkdownFile> it = tag.getTaggedFiles().iterator();
			while(it.hasNext()) {
				MarkdownFile mdFile = it.next();
				if (i > 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(mdFile.getTitle());
				
				if (i >= MAX_DOCS) {
					break;
				}
				
				i++;
			}
			
			if (i == MAX_DOCS) {
				stringBuilder.append(", ...");
			}
			
			tag.setDescription(stringBuilder.toString());
		}
	}
}
