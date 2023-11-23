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
package com.braintribe.model.processing.cmd.test.provider;

import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.Arrays;
import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.ConjunctionSelector;
import com.braintribe.model.meta.selector.DisjunctionSelector;
import com.braintribe.model.meta.selector.JunctionSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.NegationSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.cmd.test.meta.ActivableMetaData;
import com.braintribe.model.processing.cmd.test.meta.EntityRelatedMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.StaticContextSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.UnreachableSelector;
import com.braintribe.model.processing.cmd.test.model.CmdTestModelProvider;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;

/**
 * Base class for various {@link GmMetaModel} providers for specific tests.
 */
public abstract class AbstractModelSupplier implements Supplier<GmMetaModel> {

	public static final SimpleSelector TRUE_SELECTOR;
	public static final SimpleSelector FALSE_SELECTOR;

	static {
		TRUE_SELECTOR = SimpleSelector.T.create();
		FALSE_SELECTOR = SimpleSelector.T.create();

		TRUE_SELECTOR.setActive(true);
		FALSE_SELECTOR.setActive(false);

	}

	protected final GmMetaModel model;
	protected final BasicModelOracle baseModelOracle;
	protected final BasicModelOracle fullModelOracle;
	protected final BasicModelMetaDataEditor baseMdEditor; // Person, ServiceProvider
	protected final BasicModelMetaDataEditor fullMdEditor; // Teacher

	private boolean initialized;

	public AbstractModelSupplier() {
		model = CmdTestModelProvider.extended();

		baseModelOracle = new BasicModelOracle(first(model.getDependencies()));
		fullModelOracle = new BasicModelOracle(model);

		baseMdEditor = new BasicModelMetaDataEditor(first(model.getDependencies()));
		fullMdEditor = new BasicModelMetaDataEditor(model);
	}

	@Override
	public GmMetaModel get() {
		if (!initialized) {
			addMetaData();
			initialized = true;
		}

		return model;
	}

	protected abstract void addMetaData();

	protected MetaDataSelector and(MetaDataSelector... selectors) {
		JunctionSelector result = ConjunctionSelector.T.create();
		result.setOperands(Arrays.asList(selectors));

		return result;
	}

	protected MetaDataSelector or(MetaDataSelector... selectors) {
		JunctionSelector result = DisjunctionSelector.T.create();
		result.setOperands(Arrays.asList(selectors));

		return result;
	}

	protected MetaDataSelector not(MetaDataSelector selector) {
		NegationSelector result = NegationSelector.T.create();
		result.setOperand(selector);
		return result;
	}

	protected UseCaseSelector useCase(String name) {
		UseCaseSelector result = UseCaseSelector.T.create();
		result.setUseCase(name);
		return result;
	}

	protected UnreachableSelector unreachable() {
		return UnreachableSelector.T.create();
	}

	protected StaticContextSelector staticContextSelector(String value) {
		StaticContextSelector result = StaticContextSelector.T.create();
		result.setExpectedStaticValue(value);
		return result;
	}

	protected <M extends MetaData> M append(M md, MetaDataSelector mds, int priority) {
		return append(append(md, mds), priority);
	}

	protected <M extends MetaData> M append(M md, MetaDataSelector mds) {
		md.setSelector(mds);
		return md;
	}

	protected <M extends MetaData> M append(M md, int priority) {
		md.setConflictPriority((double) priority);
		return md;
	}

	public static <M extends MetaData & ActivableMetaData> M newMd(EntityType<M> mdEt, boolean isActive) {
		return newMd(mdEt, isActive, null);
	}

	public static <M extends MetaData & ActivableMetaData> M newMd(EntityType<M> mdEt, String activeString) {
		return newMd(mdEt, activeString != null, activeString);
	}

	private static <M extends MetaData & ActivableMetaData> M newMd(EntityType<M> mdEt, boolean isActive, String activeString) {
		M amd = mdEt.create();
		amd.setActive(isActive);
		amd.setActiveString(activeString);

		return amd;
	}

	public static <M extends MetaData & ActivableMetaData & EntityRelatedMetaData> M newMd(EntityType<M> mdEt, boolean isActive,
			EntityType<?> entityType) {

		M amd = mdEt.create();
		amd.setActive(isActive);
		amd.setEntityType(entityType.getShortName());

		return amd;
	}

}
