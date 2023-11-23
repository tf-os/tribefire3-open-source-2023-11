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

import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertThat;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.cmd.test.meta.ActivableMetaData;
import com.braintribe.model.processing.cmd.test.meta.EntityRelatedMetaData;
import com.braintribe.model.processing.cmd.test.provider.AbstractModelSupplier;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.extended.EntityRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * Base class for resolver tests. This class caches the {@link CmdResolverImpl} for each test case, so all tests in one test case use the same
 * instance. To disable the caching, change the value of {@link #ALWAYS_NEW_RESOLVER} flag to true;
 * 
 * The MetaModel supplier ({@link AbstractModelSupplier} is provided through {@link #getModelProvider()}, the context is initialized using
 * {@link #setupCmdResolver(CmdResolverBuilder)} method.
 */
abstract class MetaDataResolvingTestBase {

	private static Class<?> testClass;
	private static GmMetaModel metaModel;
	private static ModelOracle modelOracle;
	private static CmdResolver cmdResolver;
	public static boolean ALWAYS_NEW_RESOLVER = false;

	// @Rule
	// public LoopRule loopRule = new LoopRule(30);

	// @Rule
	// public ConcurrentRule concurrentRule = new ConcurrentRule(10);

	protected ModelMdResolver getMetaData() {
		return resolverForModel().getMetaData();
	}

	private synchronized CmdResolver resolverForModel() {
		if (testClass != getClass()) {
			testClass = getClass();
			metaModel = getModelProvider().get();
			cmdResolver = null;
		}

		if (ALWAYS_NEW_RESOLVER || cmdResolver == null) {
			modelOracle = new BasicModelOracle(metaModel);
			cmdResolver = newCmdResolver();
		}

		return cmdResolver;
	}

	protected abstract Supplier<GmMetaModel> getModelProvider();

	private CmdResolver newCmdResolver() {
		CmdResolverBuilder crb = CmdResolverImpl.create(modelOracle);
		setupCmdResolver(crb);

		return crb.done();
	}

	/** Subclasses may override this method! */
	protected void setupCmdResolver(@SuppressWarnings("unused") CmdResolverBuilder crb) {
		// do nothing
	}

	protected <T extends ActivableMetaData> void assertOneMetaData(EntityType<T> mdEt, MetaData md) {
		assertMultipleMetaData(mdEt, asList(md), 1);
	}

	protected <T extends ActivableMetaData> void assertOneMetaData(EntityType<T> mdEt, MetaData md, String activeString) {
		assertMultipleMetaData(mdEt, asList(md), 1, activeString);
	}

	protected void assertOneMetaData(EntityType<? extends ActivableMetaData> mdEt, List<? extends MetaData> mds) {
		assertMultipleMetaData(mdEt, mds, 1);
	}

	protected void assertNoMd(MetaData md) {
		assertThat(md).isNull();
	}

	protected void assertEmptyMd(List<? extends ActivableMetaData> mds) {
		assertThat(mds).isNotNull().isEmpty();
	}

	protected void assertMultipleMetaData(EntityType<? extends ActivableMetaData> mdEt, List<? extends MetaData> list, int count) {
		assertMultipleMetaData(mdEt, list, count, null);
	}

	protected void assertMultipleMetaData(EntityType<? extends ActivableMetaData> mdEt, List<? extends MetaData> list, int count,
			String activeString) {

		assertThat(list).isNotNull().hasSize(count);
		for (MetaData md : list) {
			ActivableMetaData amd = (ActivableMetaData) md;

			assertThat(md).isNotNull().isInstanceOf(mdEt.getJavaType());
			assertThat(amd.getActive()).isTrue();

			if (activeString != null)
				assertThat(amd.getActiveString()).isEqualTo(activeString);
		}
	}

	protected void assertMultipleMetaData(EntityType<? extends ActivableMetaData> mdEt, List<? extends ActivableMetaData> list,
			String... activeStrings) {
		assertThat(list).isNotNull().hasSize(activeStrings.length);

		int i = 0;
		for (ActivableMetaData amd : list) {
			assertThat(amd).isNotNull().isInstanceOf(mdEt);
			assertThat(amd.getActive()).isTrue();
			assertThat(amd.getActiveString()).isEqualTo(activeStrings[i++]);
		}
	}

	protected <T extends ActivableMetaData> void assertOneExtendedMetaData(EntityType<? extends T> et, MdDescriptor md) {
		assertExtendedMetaData(et, Arrays.asList(md), 1);
	}

	protected <T extends ActivableMetaData> void assertExtendedMetaData(EntityType<? extends T> et, List<? extends MdDescriptor> list, int count) {
		assertThat(list).isNotNull().hasSize(count);
		for (MdDescriptor mdd : list) {
			MetaData md = mdd.getResolvedValue();
			assertThat(md).isNotNull().isInstanceOf(et.getJavaType());
			assertThat(((ActivableMetaData) md).getActive()).isTrue();
			assertThat(mdd.getResolvedAsDefault()).isFalse();
		}
	}

	protected <T extends ActivableMetaData> void assertOneExtendedEntityRelatedMetaData(EntityType<? extends T> et, EntityRelatedMdDescriptor md) {
		assertExtendedMetaData(et, Arrays.asList(md), 1);
	}

	protected <T extends ActivableMetaData> void assertExtendedEntityRelatedMetaData(EntityType<? extends T> et,
			List<? extends EntityRelatedMdDescriptor> list, int count) {

		assertExtendedMetaData(et, list, count);

		boolean first = true;
		for (EntityRelatedMdDescriptor mdd : list) {
			// first is not inherited, others are
			assertThat(mdd.isInherited()).isEqualTo(!first);
			first = false;
		}
	}

	protected void assertTypeSignature(MetaData exclusive, Class<? extends GenericEntity> entityClass) {
		assertThat(((EntityRelatedMetaData) exclusive).getEntityType()).isEqualTo(entityClass.getSimpleName());
	}

	protected void assertTypeSignatures(List<? extends MetaData> list, Class<?>... entityClasses) {
		int count = 0;
		for (MetaData emd : list) {
			Class<?> entityClass = entityClasses[count++];
			assertThat(((EntityRelatedMetaData) emd).getEntityType()) //
					.as("Wrong MD owner type at position " + (count - 1)) //
					.isEqualTo(entityClass.getSimpleName());
		}
	}

	protected Map<Class<? extends SelectorContextAspect<?>>, Object> staticContext() {
		return Collections.emptyMap();
	}

}
