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

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.EnumTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.ModelMetaData;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.processing.cmd.test.meta.ActivableMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.SimpleModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.model.Color;
import com.braintribe.model.processing.cmd.test.model.Teacher;
import com.braintribe.model.processing.cmd.test.provider.AbstractModelSupplier;
import com.braintribe.model.processing.cmd.test.provider.RawModelProvider;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.extended.EntityRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EnumRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.ModelMdDescriptor;

/**
 * Tests that default value is resolved correctly.
 */
public class DefaultMetaDataResolvingTests extends MetaDataResolvingTestBase {

	// #######################################
	// ## . . . . . Default value . . . . . ##
	// #######################################

	@Test
	public void test_Model_ResolvesDefaultValue() {
		ModelMetaData mmd = getMetaData().meta(SimpleModelMetaData.T).exclusive();
		assertOneMetaData(SimpleModelMetaData.T, mmd);
	}

	@Test
	public void test_Enum_ResolvesDefaultValue() {
		EnumTypeMetaData mmd = getMetaData().enumClass(Color.class).meta(SimpleEnumMetaData.T).exclusive();
		assertOneMetaData(SimpleEnumMetaData.T, mmd);
	}

	@Test
	public void test_EnumConstant_ResolvesDefaultValue() {
		EnumConstantMetaData mmd = getMetaData().enumConstant(Color.GREEN).meta(SimpleEnumConstantMetaData.T).exclusive();
		assertOneMetaData(SimpleEnumConstantMetaData.T, mmd);
	}

	@Test
	public void test_Entity_ResolvesDefaultValue() {
		EntityTypeMetaData mmd = getMetaData().entityClass(Teacher.class).meta(SimpleEntityMetaData.T).exclusive();
		assertOneMetaData(SimpleEntityMetaData.T, mmd);
	}

	@Test
	public void test_Property_ResolvesDefaultValue() {
		PropertyMetaData mmd = getMetaData().entityClass(Teacher.class).property("age").meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, mmd);
	}

	// ########################################
	// ## . . . Default extended value . . . ##
	// ########################################

	@Test
	public void test_Model_ResolvesDefaultValue_Extended() {
		ModelMdDescriptor mmd = getMetaData().meta(SimpleModelMetaData.T).exclusiveExtended();
		assertOneDefaultExtendedMetaData(SimpleModelMetaData.class, mmd);
	}

	@Test
	public void test_Enum_ResolvesDefaultValue_Extended() {
		EnumRelatedMdDescriptor mmd = getMetaData().enumClass(Color.class).meta(SimpleEnumMetaData.T).exclusiveExtended();
		assertOneDefaultExtendedMetaData(SimpleEnumMetaData.class, mmd);

		assertThat(mmd.getOwnerTypeInfo()).isNull();
	}

	@Test
	public void test_EnumConstant_ResolvesDefaultValue_Extended() {
		EnumRelatedMdDescriptor mmd = getMetaData().enumConstant(Color.GREEN).meta(SimpleEnumConstantMetaData.T).exclusiveExtended();
		assertOneDefaultExtendedMetaData(SimpleEnumConstantMetaData.class, mmd);

		assertThat(mmd.getOwnerTypeInfo()).isNull();
	}

	@Test
	public void test_Entity_ResolvesDefaultValue_Extended() {
		EntityRelatedMdDescriptor mmd = getMetaData().entityClass(Teacher.class).meta(SimpleEntityMetaData.T).exclusiveExtended();
		assertOneDefaultExtendedMetaData(SimpleEntityMetaData.class, mmd);

		assertThat(mmd.isInherited()).isFalse();
		assertThat(mmd.getOwnerTypeInfo()).isNull();
	}

	@Test
	public void test_Property_ResolvesDefaultValue_Extended() {
		EntityRelatedMdDescriptor mmd = getMetaData().entityClass(Teacher.class).property("age").meta(SimplePropertyMetaData.T).exclusiveExtended();
		assertOneDefaultExtendedMetaData(SimplePropertyMetaData.class, mmd);

		assertThat(mmd.isInherited()).isFalse();
		assertThat(mmd.getOwnerTypeInfo()).isNull();
	}

	// ########################################
	// ## . . . . . . Assertions . . . . . . ##
	// ########################################

	private <T extends MetaData & ActivableMetaData> void assertOneDefaultExtendedMetaData(Class<T> clazz, MdDescriptor mdd) {
		assertThat(mdd).isNotNull();
		MetaData md = mdd.getResolvedValue();
		assertThat(md).isNotNull().isInstanceOf(clazz);
		assertThat(((ActivableMetaData) md).getActive()).isTrue();
		assertThat(mdd.getResolvedAsDefault()).isTrue();
		assertThat(mdd.origin()).isEqualTo("[default]");
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new RawModelProvider();
	}

	@Override
	protected void setupCmdResolver(CmdResolverBuilder crb) {
		Set<MetaData> set = new HashSet<MetaData>();

		set.add(newMd(SimpleModelMetaData.T));
		set.add(newMd(SimpleEntityMetaData.T));
		set.add(newMd(SimplePropertyMetaData.T));
		set.add(newMd(SimpleEnumMetaData.T));
		set.add(newMd(SimpleEnumConstantMetaData.T));

		crb.setDefaultMetaData(set);
	}

	private <M extends MetaData & ActivableMetaData> M newMd(EntityType<M> mdEt) {
		return AbstractModelSupplier.newMd(mdEt, true);
	}
}
