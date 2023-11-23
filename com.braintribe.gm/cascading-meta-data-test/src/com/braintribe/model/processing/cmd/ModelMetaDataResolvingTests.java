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

import static com.braintribe.model.processing.cmd.test.provider.ModelMdProvider.PRIORITIZED_MD;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.ModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.ActivableMetaData;
import com.braintribe.model.processing.cmd.test.meta.aspects.StaticAspect;
import com.braintribe.model.processing.cmd.test.meta.model.BasicModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.ConjunctionSelectorMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.DisjunctionSelectorMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.InheritedModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.NegationSelectorMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.PriorityMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.SessionScopedMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.SimpleModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.StaticContextMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.TestModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.UseCaseMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelectorExpert;
import com.braintribe.model.processing.cmd.test.meta.selector.StaticContextSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.StaticContextSelectorExpert;
import com.braintribe.model.processing.cmd.test.model.CmdTestModelProvider;
import com.braintribe.model.processing.cmd.test.provider.ModelMdProvider;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.cmd.extended.ModelMdDescriptor;

/**
 * 
 */
public class ModelMetaDataResolvingTests extends MetaDataResolvingTestBase {

	/** @see ModelMdProvider#addSimpleModelMD() */
	@Test
	public void model_NoSelectors() {
		ModelMetaData mmd = getMetaData().meta(BasicModelMetaData.T).exclusive();

		assertThat(mmd).as("Wrong model MetaData.").isInstanceOf(BasicModelMetaData.class);
		assertThat(mmd.getSelector()).isNull();
	}

	/** @see ModelMdProvider#addSimpleModelMD() */
	@Test
	public void model_Simple_Extended() {
		ModelMdDescriptor mmd = getMetaData().meta(BasicModelMetaData.T).exclusiveExtended();

		assertThat(mmd).isNotNull();
		assertThat(mmd.getOwnerModel()).isNotNull();
	}

	/** @see ModelMdProvider#addInheritedMd() */
	@Test
	public void model_InheritedMd() {
		ModelMetaData mmd = getMetaData().meta(InheritedModelMetaData.T).exclusive();

		assertThat(mmd).isNotNull();
	}

	/** @see ModelMdProvider#addInheritedMd() */
	@Test
	public void model_InheritedMd_Extended() {
		ModelMdDescriptor mmd = getMetaData().meta(InheritedModelMetaData.T).exclusiveExtended();

		assertThat(mmd).isNotNull();
		assertThat(mmd.getOwnerModel()).isNotNull();
		assertThat(mmd.getOwnerModel().getName()).isEqualTo(CmdTestModelProvider.CMD_BASE_MODEL_NAME);
		assertThat(mmd.origin()).isEqualTo(CmdTestModelProvider.CMD_BASE_MODEL_NAME);
	}

	/** @see ModelMdProvider#addPriorityModelMD() */
	@Test
	public void model_NoSelectors_Priority() {
		ModelMetaData mmd = getMetaData().meta(PriorityMetaData.T).exclusive();

		assertThat(mmd).as("Wrong model MetaData.").isInstanceOf(PriorityMetaData.class);
		assertThat(((PriorityMetaData) mmd).getPriorityValue()).isEqualTo(PRIORITIZED_MD);
	}

	/** @see ModelMdProvider#addSimpleSelectorMD() */
	@Test
	public void model_Selectors_Simple() {
		checkExacltyOneActiveMetaDataIsFound(SimpleModelMetaData.T);
	}

	/** @see ModelMdProvider#addLogicalSelectorMD() */
	@Test
	public void model_Selectors_Logical() {
		checkExacltyOneActiveMetaDataIsFound(ConjunctionSelectorMetaData.T);
		checkExacltyOneActiveMetaDataIsFound(DisjunctionSelectorMetaData.T);
		checkExacltyOneActiveMetaDataIsFound(NegationSelectorMetaData.T);
	}

	/** @see ModelMdProvider#addSessionModelMD() */
	@Test
	public void model_SessionScope() {
		List<? extends ModelMetaData> mmds = getMetaData().meta(SessionScopedMetaData.T).list();
		assertOneMetaData(SessionScopedMetaData.T, mmds);
	}

	/** @see ModelMdProvider#addStaticContextMD() */
	@Test
	public void model_StaticContext() {
		List<? extends ModelMetaData> mmds = getMetaData().meta(StaticContextMetaData.T).list();
		assertOneMetaData(StaticContextMetaData.T, mmds);
	}

	/** @see ModelMdProvider#addUseCaseMD() */
	@Test
	public void model_UseCaseFlaseIfNothingSpecified() {
		List<? extends ActivableMetaData> mmds = getMetaData().meta(UseCaseMetaData.T).list();
		assertEmptyMd(mmds);
	}

	/** @see ModelMdProvider#addUseCaseMD() */
	@Test
	public void model_UseCaseTrueIfSpecified() {
		List<? extends ActivableMetaData> mmds = getMetaData().useCase("test-case").meta(UseCaseMetaData.T).list();
		assertOneMetaData(UseCaseMetaData.T, mmds);
	}

	/** @see ModelMdProvider#addUseCaseMD() */
	@Test
	public void model_MultipleUseCases() {
		List<? extends ModelMetaData> mmds = getMetaData().useCase("test-case").useCase("other-case").meta(UseCaseMetaData.T).list();
		assertOneMetaData(UseCaseMetaData.T, mmds);
	}

	private void checkExacltyOneActiveMetaDataIsFound(EntityType<? extends TestModelMetaData> mdEt) throws CascadingMetaDataException {
		List<? extends ModelMetaData> mmds = getMetaData().meta(mdEt).list();
		assertOneMetaData(mdEt, mmds);
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new ModelMdProvider();
	}

	@Override
	protected void setupCmdResolver(CmdResolverBuilder crb) {
		crb.addExpert(SimpleSelector.T, new SimpleSelectorExpert());
		crb.addExpert(StaticContextSelector.T, new StaticContextSelectorExpert());

		crb.addStaticAspect(StaticAspect.class, "YES");

		crb.addDynamicAspectProviders(dynamicAspectProviders());

		crb.setSessionProvider(Thread::currentThread);
	}

	private Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectProviders() {
		// @formatter:off
		return asMap( 
				RoleAspect.class, asProvider(asSet("admin")),

				/* This is here as a reminder that setting a provider does not mean selectors will be pre-evaluated (if we set it as) static aspect,
				 * it would be and all MD with a use-case would be thrown out. */
				UseCaseAspect.class, asProvider(asSet("defaultUseCase"))
			);
		// @formatter:on
	}

	static <T> Supplier<T> asProvider(T value) {
		return () -> value;
	}

}
