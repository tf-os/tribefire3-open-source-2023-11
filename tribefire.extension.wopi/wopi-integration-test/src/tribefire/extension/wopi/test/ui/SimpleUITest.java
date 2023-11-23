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
package tribefire.extension.wopi.test.ui;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.testing.category.SpecialEnvironment;

/**
 * Simple UI tests that opens actual Office documents and checks if the WOPI UI opens correctly.
 * 
 *
 */
@Category(SpecialEnvironment.class)
public class SimpleUITest extends AbstractWopiUITest {

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void testDocView() throws Exception {
		openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW);
	}
	@Test(expected = AssertionError.class)
	public void testDocEdit() throws Exception {
		openWopiDocument(DOC, DocumentMode.edit, NOT_NECESSARY);
	}

	@Test
	public void testDocxView() throws Exception {
		openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW);
	}
	@Test
	public void testDocxEdit() throws Exception {
		openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT);
	}

	@Test
	public void testXlsView() throws Exception {
		openWopiDocument(XLS, DocumentMode.view, XLS_BUTTON_VIEW);
	}
	@Test(expected = AssertionError.class)
	public void testXlsEdit() throws Exception {
		openWopiDocument(XLS, DocumentMode.edit, NOT_NECESSARY);
	}

	@Test
	public void testXlsxView() throws Exception {
		openWopiDocument(XLSX, DocumentMode.view, XLSX_BUTTON_VIEW);
	}
	@Test
	public void testXlsxEdit() throws Exception {
		openWopiDocument(XLSX, DocumentMode.edit, XLSX_BUTTON_EDIT);
	}

	@Test
	public void testPptView() throws Exception {
		openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW);
	}
	@Test(expected = AssertionError.class)
	public void testPptEdit() throws Exception {
		openWopiDocument(PPT, DocumentMode.edit, NOT_NECESSARY);
	}

	@Test
	public void testPptxView() throws Exception {
		openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW);
	}
	@Test
	public void testPptxEdit() throws Exception {
		openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT);
	}

}
