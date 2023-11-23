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

import static com.braintribe.model.processing.cmd.test.provider.SpecialSelectorMdProvider.JUST_UC;
import static com.braintribe.model.processing.cmd.test.provider.SpecialSelectorMdProvider.NOT_UC;
import static com.braintribe.model.processing.cmd.test.provider.SpecialSelectorMdProvider.NO_UC;
import static com.braintribe.model.processing.cmd.test.provider.SpecialSelectorMdProvider.X_AND_UC;
import static com.braintribe.model.processing.cmd.test.provider.SpecialSelectorMdProvider.X_OR_UC;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelectorExpert;
import com.braintribe.model.processing.cmd.test.meta.selector.StaticContextSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.StaticContextSelectorExpert;
import com.braintribe.model.processing.cmd.test.model.HardwiredAccess;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.provider.SpecialSelectorMdProvider;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;

public class SpecialSelectorTests extends MetaDataResolvingTestBase {

	/** @see SpecialSelectorMdProvider#addSimplePropertyMdWithAccessSelector() */
	@Test
	public void accessSelector() {
		PropertyMetaData md;

		md = pmd("name").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		md = pmd("name").access(SpecialSelectorMdProvider.accessSelector).meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see SpecialSelectorMdProvider#addSimplePropertyMdWithAccessTypeSelector() */
	@Test
	public void accessTypeSelector() {
		PropertyMetaData md;

		md = pmd("age").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		md = pmd("age").with(AccessTypeAspect.class, HardwiredAccess.T.getTypeSignature()).meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see SpecialSelectorMdProvider#addSimplePropertyMdWithAccessTypeSignatureSelector() */
	@Test
	public void accessTypeSignatureSelector() {
		PropertyMetaData md;

		md = pmd("friend").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		md = pmd("friend").with(AccessTypeAspect.class, HardwiredAccess.T.getTypeSignature()).meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see SpecialSelectorMdProvider#addSimplePropertyMdForIgnoreSelectors() */
	@Test
	public void ignoresSelectors() {
		SimplePropertyMetaData md;

		md = pmd("friends").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		md = pmd("friends").ignoreSelectors().meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md, JUST_UC); // highest priority selector

		List<SimplePropertyMetaData> mds;
		mds = pmd("friends").meta(SimplePropertyMetaData.T).list();
		assertEmptyMd(mds);

		mds = pmd("friends").ignoreSelectors().meta(SimplePropertyMetaData.T).list();
		assertThat(mds).hasSize(4);
	}

	/** @see SpecialSelectorMdProvider#addSimplePropertyMdForIgnoreSelectors() */
	@Test
	public void ignoresSelectors_withFork() {
		SimplePropertyMetaData md;

		PropertyMdResolver pmdResolver = pmd("friends");

		md = pmdResolver.meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		md = pmdResolver.fork().ignoreSelectors().meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md, JUST_UC); // highest priority selector

		md = pmdResolver.meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);
	}

	/** @see SpecialSelectorMdProvider#addSimplePropertyMdForIgnoreSelectors() */
	@Test
	public void ignoresSelectors_ExceptUseCase() {
		List<SimplePropertyMetaData> mds;

		mds = pmd("friends").ignoreSelectorsExcept(UseCaseSelector.T).meta(SimplePropertyMetaData.T).list();
		assertMultipleMetaData(SimplePropertyMetaData.T, mds, X_OR_UC, NO_UC);

		mds = pmd("friends").ignoreSelectorsExcept(UseCaseSelector.T).useCase(JUST_UC).meta(SimplePropertyMetaData.T).list();
		assertMultipleMetaData(SimplePropertyMetaData.T, mds, JUST_UC, X_OR_UC, NO_UC);

		mds = pmd("friends").ignoreSelectorsExcept(UseCaseSelector.T).useCase(X_AND_UC).meta(SimplePropertyMetaData.T).list();
		assertMultipleMetaData(SimplePropertyMetaData.T, mds, X_AND_UC, X_OR_UC, NO_UC);

		mds = pmd("otherFriends").ignoreSelectorsExcept(UseCaseSelector.T).meta(SimplePropertyMetaData.T).list();
		assertMultipleMetaData(SimplePropertyMetaData.T, mds, NOT_UC);

		mds = pmd("otherFriends").ignoreSelectorsExcept(UseCaseSelector.T).useCase(NOT_UC).meta(SimplePropertyMetaData.T).list();
		assertEmptyMd(mds);
	}

	// Person's property MD
	private PropertyMdResolver pmd(String propertyName) {
		return getMetaData().entityType(Person.T).property(propertyName);
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new SpecialSelectorMdProvider();
	}

	@Override
	protected void setupCmdResolver(CmdResolverBuilder crb) {
		crb.setSessionProvider(Thread::currentThread);
		crb.addExpert(SimpleSelector.T, new SimpleSelectorExpert());
		crb.addExpert(StaticContextSelector.T, new StaticContextSelectorExpert());
	}

}
