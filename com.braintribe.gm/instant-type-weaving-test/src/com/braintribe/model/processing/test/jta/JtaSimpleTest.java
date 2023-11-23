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
package com.braintribe.model.processing.test.jta;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.test.itw.entity.trans.TransientPropertyEntity;
import com.braintribe.model.processing.test.jta.model.EntityWithStaticGetSetMethods;
import com.braintribe.model.processing.test.jta.model.EnumTypeWithLiteral;
import com.braintribe.model.processing.test.jta.model.OverriddenIdEntity;
import com.braintribe.model.processing.test.jta.model.errors.EntityWithInvalidSetter;
import com.braintribe.model.processing.test.jta.model.errors.EntityWithWrongNonGmEnumEval;
import com.braintribe.model.processing.test.jta.model.errors.EntityWithWrongNonGmEnumProperty;

/**
 * @author peter.gazdik
 */
public class JtaSimpleTest {

	@Test
	public void overriddenPropertyIsHandledCorrectly() {
		GenericEntity.T.getTypeSignature(); // init JTA

		GmEntityType type = (GmEntityType) new JavaTypeAnalysis().getGmType(OverriddenIdEntity.class);

		assertThat(type.getPropertyOverrides()).isNotEmpty();
	}

	/**
	 * DEVCX-531: There was a bug that the "T" literal was recognized as one of the constants.
	 */
	@Test
	public void enumTypeLiteralIsHandledCorrectly() {
		GmEnumType type = (GmEnumType) new JavaTypeAnalysis().getGmType(EnumTypeWithLiteral.class);

		List<String> constantNames = type.getConstants().stream() //
				.map(GmEnumConstant::getName) //
				.collect(Collectors.toList());

		assertThat(constantNames).containsExactly(EnumTypeWithLiteral.enumValue.name());
	}

	@Test(expected = IllegalArgumentException.class)
	public void enumWithoutEnumBaseAsPropertyIsWrong() {
		new JavaTypeAnalysis().getGmType(EntityWithWrongNonGmEnumProperty.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void enumWithoutEnumBaseAsEvalTypeIsWrong() {
		new JavaTypeAnalysis().getGmType(EntityWithWrongNonGmEnumEval.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void deafultMethodCalledLikeSettterIsWrong() {
		new JavaTypeAnalysis().getGmType(EntityWithInvalidSetter.class);
	}

	@Test
	public void staticGetSetMethodsAreOk() {
		GmEntityType type = (GmEntityType) new JavaTypeAnalysis().getGmType(EntityWithStaticGetSetMethods.class);

		assertThat(type.getProperties()).isNullOrEmpty();
	}

	@Test
	public void transientPropertyIsIgnored() {
		GmEntityType type = (GmEntityType) new JavaTypeAnalysis().getGmType(TransientPropertyEntity.class);

		assertThat(type.getProperties()).isNullOrEmpty();
	}
}
