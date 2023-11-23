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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.processing.cmd.test.meta.ActivableMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.GlobalPropertyMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.GlobalSelectorPMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.InstancePresentSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.InstancePresentSelectorExpert;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelectorExpert;
import com.braintribe.model.processing.cmd.test.model.CmdTestModelProvider;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.model.Teacher;
import com.braintribe.model.processing.cmd.test.provider.PropertyMdProvider;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.extended.PropertyMdDescriptor;

/**
 * 
 */
public class PropertyMetaDataResolvingTests extends MetaDataResolvingTestBase {

	/** @see PropertyMdProvider#addSimplePropertyMd */
	@Test
	public void property_NoInheritance() {
		List<? extends PropertyMetaData> mds = getMetaData().entityClass(Person.class).property("age").meta(SimplePropertyMetaData.T).list();
		assertOneMetaData(SimplePropertyMetaData.T, mds);

		PropertyMetaData md = getMetaData().entityClass(Person.class).property("age").meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see PropertyMdProvider#addMdForExtendedInfo */
	@Test
	public void property_Extended() {
		List<PropertyMdDescriptor> mds = getMetaData().entityType(Teacher.T).property("name").meta(SimplePropertyMetaData.T).listExtended();
		assertExtendedEntityRelatedMetaData(SimplePropertyMetaData.T, mds, 2);

		PropertyMdDescriptor md = getMetaData().entityType(Person.T).property("name").meta(SimplePropertyMetaData.T).exclusiveExtended();
		assertOneExtendedEntityRelatedMetaData(SimplePropertyMetaData.T, md);
		assertThat(md.origin()).isEqualTo("property:name of GE:" + Person.class.getName() + " of " + CmdTestModelProvider.CMD_BASE_MODEL_NAME);

		assertThat(md.getOwnerTypeInfo()).isInstanceOf(GmEntityType.class);
		assertThat(md.getOwnerTypeInfo().addressedType().getTypeSignature()).isEqualTo(Person.class.getName());
		assertThat(md.getOwnerPropertyInfo()).isInstanceOf(GmProperty.class);
	}

	/** @see PropertyMdProvider#addMdForExtendedInfo */
	@Test
	public void propertyOverride_Extended() {
		PropertyMdDescriptor md = getMetaData().entityType(Teacher.T).property("name").meta(SimplePropertyMetaData.T).exclusiveExtended();
		assertOneExtendedEntityRelatedMetaData(SimplePropertyMetaData.T, md);
		assertThat(md.origin())
				.isEqualTo("property:name (override) of GE:" + Teacher.class.getName() + " of " + CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);

		assertThat(md.getOwnerTypeInfo()).isInstanceOf(GmEntityType.class);
		assertThat(md.getOwnerTypeInfo().addressedType().getTypeSignature()).isEqualTo(Teacher.class.getName());
		assertThat(md.getOwnerPropertyInfo()).isInstanceOf(GmPropertyOverride.class);
		assertThat(md.getOwnerPropertyInfo().declaringModel().getName()).isEqualTo(CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);
	}

	/** @see PropertyMdProvider#addGlobalPropertyMd */
	@Test
	public void property_GlobalProperty() {
		List<? extends PropertyMetaData> mds = getMetaData().entityClass(Person.class).property("age").meta(GlobalPropertyMetaData.T).list();
		assertOneMetaData(GlobalPropertyMetaData.T, mds);

		PropertyMetaData md = getMetaData().entityClass(Person.class).property("age").meta(GlobalPropertyMetaData.T).exclusive();
		assertOneMetaData(GlobalPropertyMetaData.T, md);
	}

	/** @see PropertyMdProvider#addGlobalPropertyMd */
	@Test
	public void property_GlobalProperty_CombinedWithLocal() {
		List<? extends PropertyMetaData> mds = getMetaData().entityClass(Person.class).property("name").meta(GlobalPropertyMetaData.T).list();
		assertMultipleMetaData(GlobalPropertyMetaData.T, mds, 2);

		PropertyMetaData md = getMetaData().entityClass(Person.class).property("name").meta(GlobalPropertyMetaData.T).exclusive();
		assertOneMetaData(GlobalPropertyMetaData.T, md);
	}

	/** @see PropertyMdProvider#addGlobalPropertyMd */
	@Test
	public void property_GlobalProperty_Extended() {
		List<PropertyMdDescriptor> mds = getMetaData().entityClass(Person.class).property("name").meta(GlobalPropertyMetaData.T).listExtended();
		assertExtendedEntityRelatedMetaData(GlobalPropertyMetaData.T, mds, 2);

		PropertyMdDescriptor md = getMetaData().entityClass(Person.class).property("age").meta(GlobalPropertyMetaData.T).exclusiveExtended();
		assertOneExtendedEntityRelatedMetaData(GlobalPropertyMetaData.T, md);
		assertThat(md.origin()).isEqualTo("GE:" + GenericEntity.class.getName() + " (override) of " + CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);
	}

	/** @see PropertyMdProvider#addGlobalPropertyMdByName */
	@Test
	public void property_GlobalProperty_ByName() {
		List<? extends ActivableMetaData> mds = getMetaData().entityClass(Person.class).property("color").meta(GlobalSelectorPMetaData.T).list();
		assertMultipleMetaData(GlobalSelectorPMetaData.T, mds, 1, "P_NAME");

		mds = getMetaData().entityClass(Person.class).property("id").meta(GlobalSelectorPMetaData.T).list();
		assertEmptyMd(mds);
	}

	/** @see PropertyMdProvider#addGlobalPropertyMdByRegex */
	@Test
	public void property_GlobalProperty_ByRegex() {
		List<? extends PropertyMetaData> mds = getMetaData().entityClass(Person.class).property("friends").meta(GlobalSelectorPMetaData.T).list();
		assertMultipleMetaData(GlobalSelectorPMetaData.T, mds, 1, "P_REGEX");
	}

	/** @see PropertyMdProvider#addGlobalPropertyMdByType */
	@Test
	public void property_GlobalProperty_ByType() {
		List<? extends PropertyMetaData> mds = getMetaData().entityClass(Person.class).property("friend").meta(GlobalSelectorPMetaData.T).list();
		assertMultipleMetaData(GlobalSelectorPMetaData.T, mds, 1, "P_TYPE");
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new PropertyMdProvider();
	}

	@Override
	protected void setupCmdResolver(CmdResolverBuilder crb) {
		crb.addExpert(SimpleSelector.T, new SimpleSelectorExpert());
		crb.addExpert(InstancePresentSelector.T, new InstancePresentSelectorExpert());
	}
}
