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
package com.braintribe.tribefire.cartridge.library.spdx;

import java.io.File;
import java.util.Arrays;

import org.spdx.library.DefaultModelStore;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxConstants;
import org.spdx.library.model.SpdxModelFactory;
import org.spdx.library.model.TypedValue;
import org.spdx.library.model.license.ExtractedLicenseInfo;
import org.spdx.storage.IModelStore;
import org.spdx.storage.simple.InMemSpdxStore;

import junit.framework.TestCase;

/**
 *
 */
public class SpdxLab_CopyOfExtractedLicensingInfoTest extends TestCase {

	/* (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp() */
	protected void setUp() throws Exception {
		super.setUp();
		DefaultModelStore.reset();
	}

	/* (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown() */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	static final String TEST_RDF_FILE_PATH = "TestFiles" + File.separator + "SPDXRdfExample.rdf";
	static final String ID1 = SpdxConstants.NON_STD_LICENSE_ID_PRENUM + "1";
	static final String TEXT1 = "Text1";
	static final String TEXT2 = "Text2";
	static final String COMMENT1 = "Comment1";
	static final String COMMENT2 = "Comment2";
	static final String LICENSENAME1 = "license1";
	static final String LICENSENAME2 = "license2";
	static final String[] SOURCEURLS1 = new String[] { "url1", "url2" };
	static final String[] SOURCEURLS2 = new String[] { "url3", "url4", "url5" };

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#equals(java.lang.Object)}.
	 * 
	 * @throws InvalidSPDXAnalysisException
	 */

	public void testEqualsObject() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic1 = new ExtractedLicenseInfo(ID1, TEXT1);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(ID1, TEXT2);
		if (!lic1.equals(lic2)) {
			fail("Should equal when ID's equal");
		}
		if (lic1.hashCode() != lic2.hashCode()) {
			fail("Hashcodes should equal");
		}
	}

	/**
	 * Test method for
	 * {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#SPDXNonStandardLicense(org.apache.jena.rdf.model.Model, org.apache.jena.graph.Node)}.
	 * 
	 * @throws InvalidSPDXAnalysisException
	 */

	public void testSPDXNonStandardLicenseModelNode() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setComment(COMMENT1);
		IModelStore modelStore = new InMemSpdxStore();
		ModelCopyManager copyManager = new ModelCopyManager();
		TypedValue copy = copyManager.copy(modelStore, DefaultModelStore.getDefaultDocumentUri(), DefaultModelStore.getDefaultModelStore(),
				DefaultModelStore.getDefaultDocumentUri(), ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO);

		ExtractedLicenseInfo lic2 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, DefaultModelStore.getDefaultDocumentUri(),
				copy.getId(), SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, copyManager);
		assertEquals(copy.getId(), lic2.getLicenseId());
		assertEquals(TEXT1, lic2.getExtractedText());
		assertEquals(COMMENT1, lic2.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#SPDXNonStandardLicense(java.lang.String, java.lang.String)}.
	 * 
	 * @throws InvalidSPDXAnalysisException
	 */

	public void testSPDXNonStandardLicenseStringString() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		assertEquals(ID1, lic.getLicenseId());
		assertEquals(TEXT1, lic.getExtractedText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#setExtractedText(java.lang.String)}.
	 * 
	 * @throws InvalidSPDXAnalysisException
	 */

	public void testSetText() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setComment(COMMENT1);
		ExtractedLicenseInfo lic2 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(DefaultModelStore.getDefaultModelStore(),
				DefaultModelStore.getDefaultDocumentUri(), ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO,
				DefaultModelStore.getDefaultCopyManager());
		lic2.setExtractedText(TEXT2);
		assertEquals(ID1, lic2.getLicenseId());
		assertEquals(TEXT2, lic2.getExtractedText());
		assertEquals(COMMENT1, lic2.getComment());
		IModelStore modelStore = new InMemSpdxStore();
		ExtractedLicenseInfo lic3 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, DefaultModelStore.getDefaultDocumentUri(),
				ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, DefaultModelStore.getDefaultCopyManager());
		lic3.copyFrom(lic);
		assertEquals(TEXT2, lic3.getExtractedText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#setComment(java.lang.String)}.
	 * 
	 * @throws InvalidSPDXAnalysisException
	 */

	public void testSetComment() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setComment(COMMENT1);
		ExtractedLicenseInfo lic2 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(DefaultModelStore.getDefaultModelStore(),
				DefaultModelStore.getDefaultDocumentUri(), ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO,
				DefaultModelStore.getDefaultCopyManager());
		lic2.setComment(COMMENT2);
		assertEquals(ID1, lic2.getLicenseId());
		assertEquals(TEXT1, lic2.getExtractedText());
		assertEquals(COMMENT2, lic2.getComment());

		IModelStore modelStore = new InMemSpdxStore();
		ExtractedLicenseInfo lic3 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, DefaultModelStore.getDefaultDocumentUri(),
				ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, DefaultModelStore.getDefaultCopyManager());
		lic3.copyFrom(lic);
		assertEquals(COMMENT2, lic3.getComment());
	}

	public void testSetLicenseName() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setName(LICENSENAME1);
		IModelStore modelStore = new InMemSpdxStore();

		ExtractedLicenseInfo lic2 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, DefaultModelStore.getDefaultDocumentUri(),
				ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, DefaultModelStore.getDefaultCopyManager());
		lic2.setName(LICENSENAME2);
		assertEquals(LICENSENAME2, lic2.getName());
		ExtractedLicenseInfo lic3 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, DefaultModelStore.getDefaultDocumentUri(),
				ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, DefaultModelStore.getDefaultCopyManager());
		assertEquals(LICENSENAME2, lic3.getName());
	}

	public void testSetSourceUrls() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setSeeAlso(Arrays.asList(SOURCEURLS1));
		IModelStore modelStore = new InMemSpdxStore();

		ExtractedLicenseInfo lic2 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, DefaultModelStore.getDefaultDocumentUri(),
				ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, DefaultModelStore.getDefaultCopyManager());
		lic2.setSeeAlso(Arrays.asList(SOURCEURLS2));
		if (!compareArrayContent(SOURCEURLS2, (String[]) lic2.getSeeAlso().toArray(new String[lic2.getSeeAlso().size()]))) {
			fail("Source URLS not the same");
		}
		ExtractedLicenseInfo lic3 = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, DefaultModelStore.getDefaultDocumentUri(),
				ID1, SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, DefaultModelStore.getDefaultCopyManager());
		if (!compareArrayContent(SOURCEURLS2, (String[]) lic3.getSeeAlso().toArray(new String[lic2.getSeeAlso().size()]))) {
			fail("Source URLS not the same");
		}
	}

	/**
	 * @param strings1
	 * @param strings2
	 * @return true if both arrays contain the same content independent of order
	 */
	private boolean compareArrayContent(String[] strings1, String[] strings2) {
		if (strings1.length != strings2.length) {
			return false;
		}
		for (int i = 0; i < strings1.length; i++) {
			boolean found = false;
			for (int j = 0; j < strings2.length; j++) {
				if (strings1[i].equals(strings2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	public void testEquivalent() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setName(LICENSENAME1);
		lic.setSeeAlso(Arrays.asList(SOURCEURLS1));
		lic.setComment(COMMENT1);
		assertTrue(lic.equivalent(lic));
		IModelStore modelStore = new InMemSpdxStore();
		String docUri2 = "https://second.doc.uri";
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(modelStore, docUri2, ID1, DefaultModelStore.getDefaultCopyManager(), true);
		lic2.setExtractedText(TEXT1 + "    ");
		lic2.setName(LICENSENAME2);
		lic2.setSeeAlso(Arrays.asList(SOURCEURLS2));
		lic2.setComment(COMMENT2);
		assertTrue(lic.equivalent(lic2));
		lic2.setExtractedText(TEXT2);
		assertFalse(lic.equivalent(lic2));
	}
}
