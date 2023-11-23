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

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.testing.category.SpecialEnvironment;

/**
 * Complex scenario UI test that opens actual Office documents and checks if the WOPI UI opens correctly.
 * 
 *
 */
@Category(SpecialEnvironment.class)
public class ScenarioUITest extends AbstractWopiUITest {

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void testScenarioSimple() throws Exception {
		//@formatter:off
		runParallel(asList(
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT)
			));
		//@formatter:on
	}

	@Test
	public void testScenarioMedium() throws Exception {
		//@formatter:off
		runParallel(asList(
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT)
			));
		//@formatter:on
	}

	@Test
	public void testScenarioComplex() throws Exception {
		//@formatter:off
		runParallel(asList(
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT)
				));
		//@formatter:on
	}

	@Test
	public void testScenarioUltimate() throws Exception {
		//@formatter:off
		runParallel(asList(
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT),
				
				() -> openWopiDocument(DOC, DocumentMode.view, DOC_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.view, DOCX_BUTTON_VIEW),
				() -> openWopiDocument(DOCX, DocumentMode.edit, DOCX_BUTTON_EDIT),
				() -> openWopiDocument(PPT, DocumentMode.view, PPT_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.view, PPTX_BUTTON_VIEW),
				() -> openWopiDocument(PPTX, DocumentMode.edit, PPTX_BUTTON_EDIT)
			));
		//@formatter:on
	}
}
