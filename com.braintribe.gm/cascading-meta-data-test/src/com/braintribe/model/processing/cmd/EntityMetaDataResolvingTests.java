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
import static org.junit.Assert.fail;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.processing.cmd.test.meta.entity.AllExpertsMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.EntityTypeRelatedEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.ExtendedEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.ExtendedEntityOverrideMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.InstanceRelatedEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.MultiInheritedMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleInheritedMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleInheritedSelectorMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleNotInheritableMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.InstancePresentSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.InstancePresentSelectorExpert;
import com.braintribe.model.processing.cmd.test.meta.selector.PropertyDeclaredSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.PropertyPresentSelectorExpert;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelectorExpert;
import com.braintribe.model.processing.cmd.test.model.CmdTestModelProvider;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.model.ServiceProvider;
import com.braintribe.model.processing.cmd.test.model.Teacher;
import com.braintribe.model.processing.cmd.test.provider.EntityMdProvider;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.extended.EntityMdDescriptor;
import com.braintribe.model.processing.meta.cmd.result.EntityMdResult;

/**
 * 
 */
public class EntityMetaDataResolvingTests extends MetaDataResolvingTestBase {

	/** @see EntityMdProvider#addSimpleEntityMd() */
	@Test
	public void test_Entity_NoInheritance() {
		List<? extends EntityTypeMetaData> mmds = getMetaData().entityClass(Person.class).meta(SimpleEntityMetaData.T).list();
		assertOneMetaData(SimpleEntityMetaData.T, mmds);
	}

	/** @see EntityMdProvider#addInheritedNoSelectorMd() */
	@Test
	public void test_Entity_Inheritance() {
		EntityMdResult<SimpleInheritedMetaData> emd = getMetaData().entityClass(Teacher.class).meta(SimpleInheritedMetaData.T);

		EntityTypeMetaData exclusive = emd.exclusive();
		assertOneMetaData(SimpleInheritedMetaData.T, exclusive);
		assertTypeSignature(exclusive, Teacher.class);

		List<? extends EntityTypeMetaData> list = emd.list();
		assertMultipleMetaData(SimpleInheritedMetaData.T, list, 2);
		assertTypeSignatures(list, Teacher.class, Person.class);
	}

	/** @see EntityMdProvider#addNotInheritableMd() */
	@Test
	public void test_Entity_InheritanceDisabled() {
		EntityMdResult<SimpleNotInheritableMetaData> emd = getMetaData().entityClass(Teacher.class).meta(SimpleNotInheritableMetaData.T);

		List<? extends EntityTypeMetaData> list = emd.list();
		assertMultipleMetaData(SimpleNotInheritableMetaData.T, list, 1);
		assertTypeSignatures(list, Teacher.class);
	}

	/** @see EntityMdProvider#addInheritedYesSelectorMd() */
	@Test
	public void test_Entity_Inheritance_Selectors() {
		EntityMdResult<SimpleInheritedSelectorMetaData> emd = getMetaData().entityClass(Teacher.class).meta(SimpleInheritedSelectorMetaData.T);

		SimpleInheritedSelectorMetaData exclusive = emd.exclusive();
		assertOneMetaData(SimpleInheritedSelectorMetaData.T, exclusive);
		assertTypeSignature(exclusive, Teacher.class);

		List<? extends EntityTypeMetaData> list = emd.list();
		assertMultipleMetaData(SimpleInheritedSelectorMetaData.T, list, 2);
		assertTypeSignatures(list, Teacher.class, ServiceProvider.class);
	}

	/** @see EntityMdProvider#addMultiInheritedMd() */
	@Test
	public void test_Entity_Inheritance_Multi() {
		EntityMdResult<MultiInheritedMetaData> emd = getMetaData().entityClass(Teacher.class).meta(MultiInheritedMetaData.T);

		List<? extends EntityTypeMetaData> list = emd.list();
		assertMultipleMetaData(MultiInheritedMetaData.T, list, 3);
		assertTypeSignatures(list, Person.class, ServiceProvider.class, GenericEntity.class);
	}

	/** @see EntityMdProvider#addEntityTypeSelectorMd() */
	@Test
	public void test_Entity_WithEntityType() {
		ModelMdResolver metaData = getMetaData();

		List<EntityTypeRelatedEntityMetaData> list;

		list = metaData.entityClass(Person.class).meta(EntityTypeRelatedEntityMetaData.T).list();
		assertOneMetaData(EntityTypeRelatedEntityMetaData.T, list);

		list = metaData.entityClass(Teacher.class).meta(EntityTypeRelatedEntityMetaData.T).list();
		assertEmptyMd(list);
	}

	/** @see EntityMdProvider#addSimpleInstanceMd() */
	@Test
	public void test_Entity_WithInstance() {
		ModelMdResolver metaData = getMetaData();

		List<? extends EntityTypeMetaData> list;

		list = metaData.entity(Person.T.create()).meta(InstanceRelatedEntityMetaData.T).list();
		assertOneMetaData(InstanceRelatedEntityMetaData.T, list);
	}

	/** @see EntityMdProvider#addAllExpertsMD() */
	@Test
	public void test_Entity_AllExpertsInside() {
		/* As long as this does not throw an exception, we are happy (tests that all experts are present (besides Role)) */
		List<? extends EntityTypeMetaData> list = getMetaData().entity(Person.T.create()).useCase("test").meta(AllExpertsMetaData.T).list();

		assertThat(list).isNotNull();
		if (!list.isEmpty())
			fail("Probably an ERROR IN TEST. There should be no MetaData active, but the property meta data may be configured incorrectly.");
	}

	/** @see EntityMdProvider#addAllExpertsMD() */
	@Test
	public void test_Entity_ExpertsWorkEvenIfDataNotInContext() {
		/* as long as this does not throw an exception, we are happy (tests that all experts are present (besides Role)) */
		List<? extends EntityTypeMetaData> list = getMetaData().entityClass(Person.class).useCase("test").meta(AllExpertsMetaData.T).list();

		assertThat(list).isNotNull();
		if (!list.isEmpty())
			fail("Probably an ERROR IN TEST. There should be no MetaData active, but the property meta data may be configured incorrectly.");
	}

	/** @see EntityMdProvider#addMdForExtendedInfo() */
	@Test
	public void test_Entity_Extended() {
		List<EntityMdDescriptor> list = getMetaData().entityClass(Teacher.class).meta(ExtendedEntityMetaData.T).listExtended();
		assertExtendedEntityRelatedMetaData(ExtendedEntityMetaData.T, list, 2);

		EntityMdDescriptor md = getMetaData().entityClass(Teacher.class).meta(ExtendedEntityMetaData.T).exclusiveExtended();
		assertOneExtendedEntityRelatedMetaData(ExtendedEntityMetaData.T, md);
		assertThat(md.origin()).isEqualTo("GE:" + Teacher.class.getName() + " of " + CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);

		GmEntityTypeInfo ownerTypeInfo = md.getOwnerTypeInfo();
		assertThat(ownerTypeInfo).isInstanceOf(GmEntityType.class);
		assertThat(ownerTypeInfo.addressedType().getTypeSignature()).isEqualTo(Teacher.class.getName());
	}

	/**
	 * For explanation on why this is failing see {@link EnumMetaDataResolvingTests#enumOverride_Extended()}
	 * 
	 * @see EntityMdProvider#addEntityOverrideExtendedMd()
	 */
	@Test
	public void test_EntityOverride_Extended() {
		EntityMdDescriptor md = getMetaData().entityType(Person.T).meta(ExtendedEntityOverrideMetaData.T).exclusiveExtended();
		assertOneExtendedMetaData(ExtendedEntityOverrideMetaData.T, md);

		GmEntityTypeInfo ownerTypeInfo = md.getOwnerTypeInfo();
		assertThat(ownerTypeInfo).isInstanceOf(GmEntityTypeOverride.class);
		assertThat(ownerTypeInfo.addressedType().getTypeSignature()).isEqualTo(Person.class.getName());

		assertThat(ownerTypeInfo.declaringModel()).isNotNull();
		assertThat(ownerTypeInfo.declaringModel().getName()).isEqualTo(CmdTestModelProvider.CMD_EXTENDED_MODEL_NAME);
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new EntityMdProvider();
	}

	@Override
	protected void setupCmdResolver(CmdResolverBuilder crb) {
		crb.addExpert(SimpleSelector.T, new SimpleSelectorExpert());
		crb.addExpert(PropertyDeclaredSelector.T, new PropertyPresentSelectorExpert());
		crb.addExpert(InstancePresentSelector.T, new InstancePresentSelectorExpert());
	}

}
