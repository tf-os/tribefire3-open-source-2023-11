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
package com.braintribe.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.utils.PropertyTools.DefaultPropertyGetter;
import com.braintribe.utils.PropertyTools.PropertyGetter;
import com.braintribe.utils.PropertyTools.SortProperty;
import com.braintribe.utils.PropertyTools.SortProperty.Direction;
import com.braintribe.utils.lcd.NullSafe;

/**
 * Provides tests for {@link PropertyTools}.
 *
 * @author michael.lafite
 */
public class PropertyToolsTest {

	@Test
	public void testGetPropertyValue() {
		final Document document = getTestDocumentWithVersions(1);
		final DocumentVersion documentVersion = document.getLastVersion();
		final DocumentVersionInfo documentVersionInfo = documentVersion.getInfo();

		assertThat(PropertyTools.getPropertyValue(document, "id")).isEqualTo(document.getId());
		assertThat(PropertyTools.getPropertyValue(document, "name")).isEqualTo(document.getName());
		assertThat(PropertyTools.getPropertyValue(documentVersion, "id")).isEqualTo(documentVersion.getId());
		assertThat(PropertyTools.getPropertyValue(documentVersionInfo, "lastModified")).isEqualTo(documentVersionInfo.getLastModified());
		assertThat(PropertyTools.getPropertyValue(documentVersionInfo, "version")).isEqualTo(documentVersionInfo.getVersion());

		documentVersionInfo.setLastModified(null);
		assertThat(PropertyTools.getPropertyValue(documentVersionInfo, "lastModified")).isNull();
	}

	@Test
	public void testGetPropertyValueByPath() {
		final Document document = getTestDocumentWithVersions(1);
		final DocumentVersionInfo documentVersionInfo = document.getLastVersion().getInfo();

		assertThat(PropertyTools.getPropertyValueByPath(document, "lastVersion.info.lastModified", false))
				.isEqualTo(documentVersionInfo.getLastModified());
		assertThat(PropertyTools.getPropertyValueByPath(document, "lastVersion.info.name", false)).isEqualTo(documentVersionInfo.getName());
		assertThat(PropertyTools.getPropertyValueByPath(document, "lastVersion.info.version", false)).isEqualTo(documentVersionInfo.getVersion());

		// null check
		documentVersionInfo.setName(null);
		assertThat(PropertyTools.getPropertyValueByPath(document, "lastVersion.info.name", false)).isNull();
		document.setLastVersion(null);

		try {
			PropertyTools.getPropertyValueByPath(document, "lastVersion.info.name", false);
			fail();
		} catch (final Exception e) {
			// expected;
		}

		assertThat(PropertyTools.getPropertyValueByPath(document, "lastVersion.info.name", true)).isNull();
	}

	@Test
	public void testSortByProperties() {
		final Document documentA1 = getTestDocumentWithVersions(2, "A");
		final Document documentA2 = getTestDocumentWithVersions(3, "A");
		final Document documentA3 = getTestDocumentWithVersions(4, "A");
		final Document documentB1 = getTestDocumentWithVersions(10, "B");
		final Document documentB2 = getTestDocumentWithVersions(9, "B");

		final List<Document> documentsInOriginalOrder = CollectionTools.getList(documentA1, documentA2, documentA3, documentB1, documentB2);

		final List<Document> documentsOrderedByLastVersionNumberAscending = CollectionTools.getList(documentA1, documentA2, documentA3, documentB2,
				documentB1);

		final List<Document> documentsOrderedByLastVersionNumberDescending = new ArrayList<>(documentsOrderedByLastVersionNumberAscending);
		Collections.reverse(documentsOrderedByLastVersionNumberDescending);

		final List<Document> documentsOrderedByName = CollectionTools.getList(documentA1, documentA2, documentA3, documentB1, documentB2);

		final List<Document> documentsOrderedByNameAndLastVersion = CollectionTools.getList(documentA1, documentA2, documentA3, documentB2,
				documentB1);

		// just make sure comparison works
		assertThat(PropertyTools.getListSortedByProperties(documentsInOriginalOrder)).isNotEqualTo(documentsOrderedByLastVersionNumberAscending);

		// don't sort at all
		assertThat(PropertyTools.getListSortedByProperties(documentsInOriginalOrder)).isEqualTo(documentsInOriginalOrder);

		// sort by last version
		assertThat(PropertyTools.getListSortedByProperties(documentsInOriginalOrder, new SortProperty("lastVersion.info.version")))
				.isEqualTo(documentsOrderedByLastVersionNumberAscending);
		assertThat(
				PropertyTools.getListSortedByProperties(documentsInOriginalOrder, new SortProperty("lastVersion.info.version", Direction.DESCENDING)))
						.isEqualTo(documentsOrderedByLastVersionNumberDescending);

		// sort by doc name
		assertThat(PropertyTools.getListSortedByProperties(documentsInOriginalOrder, new SortProperty("name"))).isEqualTo(documentsOrderedByName);

		// sort by name and last version
		assertThat(PropertyTools.getListSortedByProperties(documentsInOriginalOrder, new SortProperty("name"),
				new SortProperty("lastVersion.info.version"))).isEqualTo(documentsOrderedByNameAndLastVersion);

		/* test custom property getter (since all have same first version, order shouldn't change compared to original list) */
		assertThat(PropertyTools.getListSortedByProperties(new FirstVersionPropertyGetter(), documentsInOriginalOrder,
				new SortProperty("firstVersion.info.version"))).isEqualTo(documentsInOriginalOrder);
	}

	/**
	 * A custom {@link PropertyGetter} that adds the virtual property 'firstVersion'.
	 *
	 * @author michael.lafite
	 */
	private static class FirstVersionPropertyGetter extends DefaultPropertyGetter {

		@Override
		public Object getPropertyValue(final Object object, final String propertyName) {
			if (object instanceof Document && propertyName.equals("firstVersion")) {
				final Document document = (Document) object;
				for (final DocumentVersion version : document.getVersions()) {
					if (version.getInfo().getVersion() == 1) {
						return version;
					}
				}
				// shouldn't happen with the test data
				throw new RuntimeException("first version not found!");
			}

			return super.getPropertyValue(object, propertyName);
		}
	}

	private static Document getTestDocumentWithVersions(final int numberOfVersions) {
		return getTestDocumentWithVersions(numberOfVersions, "docName_" + RandomTools.newStandardUuid());
	}

	private static Document getTestDocumentWithVersions(final int numberOfVersions, final String docName) {
		final Document document = new Document();
		document.setName(docName);
		document.setId("docId_" + RandomTools.newStandardUuid());
		for (int versionNumber = 1; versionNumber <= numberOfVersions; versionNumber++) {
			final DocumentVersion documentVersion = new DocumentVersion();
			documentVersion.setId("versionId_" + RandomTools.newStandardUuid());

			final DocumentVersionInfo documentVersionInfo = new DocumentVersionInfo();
			documentVersion.setInfo(documentVersionInfo);
			documentVersionInfo.setLastModified(new Date());
			documentVersionInfo.setName("versionName_" + RandomTools.newStandardUuid());
			documentVersionInfo.setVersion(versionNumber);

			document.getVersions().add(documentVersion);
			document.setLastVersion(documentVersion);
		}
		return document;
	}

	/**
	 * Simple test class used by the test cases of this unit test.
	 *
	 * @author michael.lafite
	 */
	private static class Document {
		private String id;
		private String name;
		private final Set<DocumentVersion> versions = new HashSet<>();
		DocumentVersion lastVersion;

		Document() {
			// nothing to do
		}

		String getName() {
			return this.name;
		}

		void setName(final String name) {
			this.name = name;
		}

		String getId() {
			return this.id;
		}

		void setId(final String id) {
			this.id = id;
		}

		Set<DocumentVersion> getVersions() {
			return this.versions;
		}

		DocumentVersion getLastVersion() {
			return this.lastVersion;
		}

		void setLastVersion(final DocumentVersion lastVersion) {
			this.lastVersion = lastVersion;
		}

		@Override
		public String toString() {
			return "Document [id=" + this.id + ", name=" + this.name + ", versions count=" + NullSafe.size(this.versions) + ", lastVersion="
					+ this.lastVersion + "]";
		}

	}

	/**
	 * Simple test class used by the test cases of this unit test.
	 *
	 * @author michael.lafite
	 */
	private static class DocumentVersion {
		private String id;

		private DocumentVersionInfo info;

		DocumentVersion() {
			// nothing to do
		}

		String getId() {
			return this.id;
		}

		void setId(final String id) {
			this.id = id;
		}

		DocumentVersionInfo getInfo() {
			return this.info;
		}

		void setInfo(final DocumentVersionInfo info) {
			this.info = info;
		}

		@Override
		public String toString() {
			return "DocumentVersion [id=" + this.id + ", info=" + this.info + "]";
		}

	}

	/**
	 * Simple test class used by the test cases of this unit test.
	 *
	 * @author michael.lafite
	 */
	private static class DocumentVersionInfo {
		private String name;
		private Date lastModified;
		private int version;

		DocumentVersionInfo() {
			// nothing to do
		}

		String getName() {
			return this.name;
		}

		void setName(final String name) {
			this.name = name;
		}

		Date getLastModified() {
			return this.lastModified;
		}

		void setLastModified(final Date lastModified) {
			this.lastModified = lastModified;
		}

		int getVersion() {
			return this.version;
		}

		void setVersion(final int version) {
			this.version = version;
		}

		@Override
		public String toString() {
			return "DocumentVersionInfo [name=" + this.name + ", lastModified=" + this.lastModified + ", version=" + this.version + "]";
		}

	}
}
