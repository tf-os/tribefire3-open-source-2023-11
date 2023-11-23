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

import static com.braintribe.model.processing.cmd.test.model.CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.EnumTypeMetaData;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.processing.cmd.test.meta.enumeration.GlobalEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.ModelEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.ModelEnumMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumConstantOverrideMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumOverrideMetaData;
import com.braintribe.model.processing.cmd.test.model.CmdTestModelProvider;
import com.braintribe.model.processing.cmd.test.model.Color;
import com.braintribe.model.processing.cmd.test.provider.EnumMdProvider;
import com.braintribe.model.processing.meta.cmd.extended.ConstantMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EnumMdDescriptor;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.EnumMdIndex;
import com.braintribe.model.processing.meta.cmd.resolvers.EnumMdAggregator;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataBox;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * 
 */
public class EnumMetaDataResolvingTests extends MetaDataResolvingTestBase {

	/** @see EnumMdProvider#addSimpleEnumTypeMetaData */
	@Test
	public void enum_NoSelectors() {
		EnumTypeMetaData md = getMetaData().enumClass(Color.class).meta(SimpleEnumMetaData.T).exclusive();
		assertOneMetaData(SimpleEnumMetaData.T, md);
	}

	/** @see EnumMdProvider#addSimpleEnumTypeMetaData */
	@Test
	public void test_Enum_Extended() {
		EnumMdDescriptor md = getMetaData().enumClass(Color.class).meta(SimpleEnumMetaData.T).exclusiveExtended();
		assertOneExtendedMetaData(SimpleEnumMetaData.T, md);
		assertThat(md.origin()).isEqualTo("Enum:" + Color.class.getName() + " of " + CmdTestModelProvider.CMD_BASE_MODEL_NAME);

		GmEnumTypeInfo ownerTypeInfo = md.getOwnerTypeInfo();
		assertThat(ownerTypeInfo).isInstanceOf(GmEnumType.class);
		assertThat(ownerTypeInfo.addressedType().getTypeSignature()).isEqualTo(Color.class.getName());
	}

	/**
	 * Currently we cannot provide information about origin of the MD (i.e. exactly which model element it was attached to), because the underlying
	 * {@link ModelOracle} with methods like {@link ModelOracle#getMetaData()} hide that information. In order to fix this, we need to first change
	 * that API to return pairs - (MD, OwnerElement) and then adjust the MD Resolver as well.
	 * 
	 * Implementation for Enum: The {@link EnumMdIndex} is the place where MD is retrieved from the oracle. We have to adjust the {@link MetaDataBox}
	 * to also accept these pairs (and extra information describing precisely what is the origin of the MD. It can also be attached to a model
	 * directly...) and then adjust everything else that depends on this class. To then add this information to the MdDescriptor,
	 * {@link EnumMdAggregator#extendedFor} method has to be adjusted.
	 * 
	 * @see EnumMdProvider#addSimpleEnumOverrideMetaData
	 */
	@Test
	public void enumOverride_Extended() {
		EnumMdDescriptor md = getMetaData().enumClass(Color.class).meta(SimpleEnumOverrideMetaData.T).exclusiveExtended();
		assertOneExtendedMetaData(SimpleEnumOverrideMetaData.T, md);
		assertThat(md.origin()).isEqualTo("Enum:" + Color.class.getName() + " (override) of " + CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);

		GmEnumTypeInfo ownerTypeInfo = md.getOwnerTypeInfo();
		assertThat(ownerTypeInfo).isInstanceOf(GmEnumTypeOverride.class);
		assertThat(ownerTypeInfo.addressedType().getTypeSignature()).isEqualTo(Color.class.getName());
	}

	/** @see EnumMdProvider#addSimpleEnumConstantMetaData */
	@Test
	public void enumConstant_NoSelectors() {
		EnumConstantMetaData md = getMetaData().enumConstant(Color.GREEN).meta(SimpleEnumConstantMetaData.T).exclusive();
		assertOneMetaData(SimpleEnumConstantMetaData.T, md);
	}

	/** @see EnumMdProvider#addSimpleEnumConstantOverrideMetaData */
	@Test
	public void enumConstant_Extended() {
		ConstantMdDescriptor md = getMetaData().enumConstant(Color.GREEN).meta(SimpleEnumConstantMetaData.T).exclusiveExtended();
		assertOneExtendedMetaData(SimpleEnumConstantMetaData.T, md);
		assertThat(md.origin()).isEqualTo("constant:GREEN of Enum:" + Color.class.getName() + " of " + CmdTestModelProvider.CMD_BASE_MODEL_NAME);

		assertThat(md.getOwnerTypeInfo()).isInstanceOf(GmEnumType.class);
		assertThat(md.getOwnerConstantInfo()).isInstanceOf(GmEnumConstant.class);
	}

	/** @see EnumMdProvider#addSimpleEnumConstantOverrideMetaData */
	@Test
	public void enumConstantOverride_Extended() {
		ConstantMdDescriptor md = getMetaData().enumConstant(Color.GREEN).meta(SimpleEnumConstantOverrideMetaData.T).exclusiveExtended();
		assertOneExtendedMetaData(SimpleEnumConstantOverrideMetaData.T, md);
		assertThat(md.origin()).isEqualTo(
				"constant:GREEN (override) of Enum:" + Color.class.getName() + " (override) of " + CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);

		assertThat(md.getOwnerTypeInfo()).isInstanceOf(GmEnumTypeOverride.class);
		assertThat(md.getOwnerConstantInfo()).isInstanceOf(GmEnumConstantOverride.class);
	}

	/** @see EnumMdProvider#addGlobalConstantMd */
	@Test
	public void enumConstant_GlobalProperty() {
		List<GlobalEnumConstantMetaData> mds = getMetaData().enumConstant(Color.GREEN).meta(GlobalEnumConstantMetaData.T).list();
		assertMultipleMetaData(GlobalEnumConstantMetaData.T, mds, 2);

		GlobalEnumConstantMetaData md = getMetaData().enumConstant(Color.GREEN).meta(GlobalEnumConstantMetaData.T).exclusive();
		assertOneMetaData(GlobalEnumConstantMetaData.T, md);
	}

	/** @see EnumMdProvider#addGlobalConstantMd */
	@Test
	public void enumConstant_GlobalProperty_Extended() {
		ConstantMdDescriptor md = getMetaData().enumConstant(Color.BLUE).meta(GlobalEnumConstantMetaData.T).exclusiveExtended();
		assertOneExtendedMetaData(GlobalEnumConstantMetaData.T, md);
		assertThat(md.origin()).isEqualTo("Enum:" + Color.class.getName() + " of " + CmdTestModelProvider.CMD_BASE_MODEL_NAME);

		assertThat(md.getOwnerTypeInfo()).isInstanceOf(GmEnumType.class);
	}

	/** @see EnumMdProvider#addModelEnumMd */
	@Test
	public void enum_OnModel() {
		List<ModelEnumMetaData> mds = getMetaData().enumClass(Color.class).meta(ModelEnumMetaData.T).list();
		assertOneMetaData(ModelEnumMetaData.T, mds);

		ModelEnumMetaData md = getMetaData().enumClass(Color.class).meta(ModelEnumMetaData.T).exclusive();
		assertOneMetaData(ModelEnumMetaData.T, md);
	}

	/** @see EnumMdProvider#addModelConstantMd */
	@Test
	public void enumConstant_OnModel() {
		List<ModelEnumConstantMetaData> mds = getMetaData().enumConstant(Color.BLUE).meta(ModelEnumConstantMetaData.T).list();
		assertOneMetaData(ModelEnumConstantMetaData.T, mds);

		ModelEnumConstantMetaData md = getMetaData().enumConstant(Color.BLUE).meta(ModelEnumConstantMetaData.T).exclusive();
		assertOneMetaData(ModelEnumConstantMetaData.T, md);
	}

	/** @see EnumMdProvider#addModelEnumMd */
	@Test
	public void enum_OnModel_Extended() {
		EnumMdDescriptor md = getMetaData().enumClass(Color.class).meta(ModelEnumMetaData.T).exclusiveExtended();
		assertOneExtendedMetaData(ModelEnumMetaData.T, md);
		assertThat(md.origin()).isEqualTo(CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);

		assertThat(md.getOwnerTypeInfo()).isNull();
		assertThat(md.getOwnerModel()).isNotNull();
		assertThat(md.getOwnerModel().getName()).isEqualTo(CMD_EXTENDED_MODEL_NAME);
	}

	/** @see EnumMdProvider#addModelConstantMd */
	@Test
	public void enumConstant_OnModel_Extended() {
		ConstantMdDescriptor md = getMetaData().enumConstant(Color.BLUE).meta(ModelEnumConstantMetaData.T).exclusiveExtended();
		assertOneExtendedMetaData(ModelEnumConstantMetaData.T, md);
		assertThat(md.origin()).isEqualTo(CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);

		assertThat(md.getOwnerTypeInfo()).isNull();
		assertThat(md.getOwnerModel()).isNotNull();
		assertThat(md.getOwnerModel().getName()).isEqualTo(CMD_EXTENDED_MODEL_NAME);
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new EnumMdProvider();
	}

}
