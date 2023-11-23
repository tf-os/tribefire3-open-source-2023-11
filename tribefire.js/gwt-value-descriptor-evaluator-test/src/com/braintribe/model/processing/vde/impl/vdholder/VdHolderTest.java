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
package com.braintribe.model.processing.vde.impl.vdholder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.impl.misc.VdeTestTemplate;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Test for entities that have a VD set as a VD holder
 */
public class VdHolderTest extends VdeTest {

	private static final EntityType<VdeTestTemplate> et = VdeTestTemplate.T;

	private final VdeTestTemplate template = et.create();

	@Test
	public void testEvaluatesVdParams() throws Exception {
		Property prop = template.entityType().getProperty("stringParam");
		prop.setAbsenceInformation(template, GMF.absenceInformation());

		VdeTestTemplate result = evalTemplate();

		assertThat(prop.getAbsenceInformation(result)).isNotNull();
	}

	private VdeTestTemplate evalTemplate() {
		VdeTestTemplate result = (VdeTestTemplate) evaluateWithEvaluationMode(template, VdeEvaluationMode.Preliminary);
		assertThat(result).isNotNull().isNotSameAs(template);
		return result;
	}

}
