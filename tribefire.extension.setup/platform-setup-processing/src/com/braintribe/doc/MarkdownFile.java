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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.doc.meta.CustomFolderMetaData;
import com.braintribe.doc.meta.FileDisplayInfo;
import com.braintribe.doc.meta.WizardInfo;
import com.braintribe.model.asset.PlatformAsset;

public class MarkdownFile {
	private final UniversalPath assetRelativeLocation;
	private final CustomFolderMetaData parentFolderMd;
	private final PlatformAsset asset;

	private final List<String> headings = new ArrayList<>();
	private final Set<UrlComparingTitledLinkWrapper> referencingExternalLinks = new HashSet<>();
	private final Set<UrlComparingTitledLinkWrapper> referencingJavadocLinks = new HashSet<>();
	private final Set<UniversalPath> referencingFiles = new HashSet<>();
	private final Set<Tag> tags = new HashSet<>();

	private FileDisplayInfo displayInfo;

	private StringBuilder contentString = new StringBuilder();

	public static MarkdownFile of(UniversalPath relativeLocationToDocRoot, CustomFolderMetaData parentFolderMd, PlatformAsset asset) {
		return new MarkdownFile(relativeLocationToDocRoot, parentFolderMd, asset);
	}

	private MarkdownFile(UniversalPath relativeLocationToDocRoot, CustomFolderMetaData parentFolderMd, PlatformAsset asset) {
		this.assetRelativeLocation = DocUtils.getAssetRelativePath(relativeLocationToDocRoot);
		this.parentFolderMd = parentFolderMd;
		this.asset = asset;
	}

	public String getTitle() {
		return displayInfo.getDisplayTitle();
	}

	public UniversalPath getAssetRelativeLocation() {
		return assetRelativeLocation;
	}

	public UniversalPath getDocRelativeHtmlFileLocation() {
		return getDocRelativeLocation().getParent().push(getHtmlFileName());
	}

	public String getHtmlFileName() {
		return DocUtils.toHtmlName(assetRelativeLocation.getName());
	}

	public String getAssetId() {
		return getAssetGroupId() + ":" + getAssetName();
	}

	public String getAssetGroupId() {
		return asset.getGroupId();
	}

	public String getAssetName() {
		return asset.getName();
	}

	public PlatformAsset getAsset() {
		return asset;
	}

	public WizardInfo getWizard() {
		return parentFolderMd.getWizard();
	}

	public UniversalPath getDocRelativeLocation() {
		return UniversalPath.empty() //
				.push(getAssetGroupId()) //
				.push(getAssetName()) //
				.push(assetRelativeLocation); //
	}

	public void addHeading(String title) {
		headings.add(title);
	}

	public void addReference(UniversalPath docRelativePath) {
		referencingFiles.add(docRelativePath);
	}

	public void addExternalLink(TitledLink titledLink) {
		referencingExternalLinks.add(new UrlComparingTitledLinkWrapper(titledLink));
	}

	public void addJavadocLink(TitledLink titledLink) {
		referencingJavadocLinks.add(new UrlComparingTitledLinkWrapper(titledLink));
	}

	public Set<TitledLink> getReferencingExternalLinks() {
		return Collections.unmodifiableSet( //
				referencingExternalLinks.stream() //
						.map(l -> l.delegate) //
						.collect(Collectors.toSet()));
	}

	public Set<TitledLink> getReferencingJavadocLinks() {
		return Collections.unmodifiableSet( //
				referencingJavadocLinks.stream() //
						.map(l -> l.delegate) //
						.collect(Collectors.toSet()));
	}

	public Set<UniversalPath> getReferencingFiles() {
		return referencingFiles;
	}

	void addContentText(String text) {
		contentString.append(text);
	}

	void setDisplayInfo(FileDisplayInfo displayInfo) {
		this.displayInfo = displayInfo;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	void addTag(Tag tag) {
		tags.add(tag);
	}

	public FileDisplayInfo getDisplayInfo() {
		return displayInfo;
	}

	/**
	 * Returns the text accumulated by {@link #addContentText(String)} and deletes the text afterwards to be memory efficient.
	 */
	String unloadContentText() {
		if (contentString == null) {
			throw new IllegalStateException("You don't want to call unloadContentText twice.");
		}

		String text = contentString.toString();

		contentString = null;
		return text;
	}

	public List<String> getHeadings() {
		return headings;
	}

	/**
	 * This wraps a {@link TitledLink} in a way so that equals() and hashCode() only compare the url string and ignore the rest
	 *
	 * @author Neidhart.Orlich
	 *
	 */
	private class UrlComparingTitledLinkWrapper {
		final TitledLink delegate;

		public UrlComparingTitledLinkWrapper(TitledLink delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}

			if (other instanceof UrlComparingTitledLinkWrapper) {
				UrlComparingTitledLinkWrapper o = (UrlComparingTitledLinkWrapper) other;
				return o.delegate.getUrl().equals(delegate.getUrl());
			}

			return false;
		}

		@Override
		public int hashCode() {
			return delegate.getUrl().hashCode();
		}
	}

	@Override
	public String toString() {
		return getAssetId() + "/" + getAssetRelativeLocation();
	}

	public Set<String> getBoostedSearchTerms() {
		return displayInfo.getBoostedSearchTerms();
	}
}
