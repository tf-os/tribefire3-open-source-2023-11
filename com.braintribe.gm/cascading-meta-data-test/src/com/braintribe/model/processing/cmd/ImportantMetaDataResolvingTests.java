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
package com.braintribe.model.processing.cmd;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.model.Color;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.model.Teacher;
import com.braintribe.model.processing.cmd.test.provider.ImportantMdProvider;
import com.braintribe.model.processing.meta.cmd.result.ConstantMdResult;
import com.braintribe.model.processing.meta.cmd.result.EntityMdResult;
import com.braintribe.model.processing.meta.cmd.result.PropertyMdResult;

/**
 * Tests that important MDs are resolved correctly.
 */
public class ImportantMetaDataResolvingTests extends MetaDataResolvingTestBase {

	// ######################################
	// ## . . . . . Important MD . . . . . ##
	// ######################################

	/** @see ImportantMdProvider#addImportantEntityMd */
	@Test
	public void test_Entity_ResolvesImportantMd() {
		EntityMdResult<SimpleEntityMetaData> emd = getMetaData().entityClass(Teacher.class).meta(SimpleEntityMetaData.T);

		EntityTypeMetaData exclusive = emd.exclusive();
		assertOneMetaData(SimpleEntityMetaData.T, exclusive);
		assertTypeSignature(exclusive, Person.class);

		List<? extends EntityTypeMetaData> list = emd.list();
		assertMultipleMetaData(SimpleEntityMetaData.T, list, 2);
		assertTypeSignatures(list, Person.class, Teacher.class);
	}

	/** @see ImportantMdProvider#addImportantPropertyMd */
	@Test
	public void test_Property_ResolvesImportantMd_ImportantHasHigherPrio() {
		PropertyMdResult<SimplePropertyMetaData> pmd = getMetaData().entityClass(Teacher.class).property("age").meta(SimplePropertyMetaData.T);

		PropertyMetaData exclusive = pmd.exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, exclusive);
		assertTypeSignature(exclusive, Person.class);

		List<? extends PropertyMetaData> list = pmd.list();
		assertMultipleMetaData(SimplePropertyMetaData.T, list, 2);
		assertTypeSignatures(list, Person.class, Teacher.class);
	}

	/** @see ImportantMdProvider#addImportantPropertyMdWithLowPrio */
	@Test
	public void test_Property_ResolvesImportantMd_LocalHasHigherPrio() {
		PropertyMdResult<SimplePropertyMetaData> pmd = getMetaData().entityClass(Teacher.class).property("name").meta(SimplePropertyMetaData.T);

		PropertyMetaData exclusive = pmd.exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, exclusive);
		assertTypeSignature(exclusive, Teacher.class);

		List<? extends PropertyMetaData> list = pmd.list();
		assertMultipleMetaData(SimplePropertyMetaData.T, list, 2);
		assertTypeSignatures(list, Teacher.class, Person.class);
	}

	/** @see ImportantMdProvider#addImportantEnumConstantMd */
	@Test
	public void test_EnumConstant_ResolvesImportantMd() {
		ConstantMdResult<SimpleEnumConstantMetaData> emd = getMetaData().enumConstant(Color.GREEN).meta(SimpleEnumConstantMetaData.T);

		EnumConstantMetaData exclusive = emd.exclusive();
		assertOneMetaData(SimpleEnumConstantMetaData.T, exclusive);
	}

	// ########################################
	// ## . . . . . . . Setup . . . . . . . .##
	// ########################################

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new ImportantMdProvider();
	}

}
