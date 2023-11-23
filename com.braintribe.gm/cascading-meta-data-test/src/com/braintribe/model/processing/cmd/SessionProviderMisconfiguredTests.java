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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.ModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.aspects.StaticAspect;
import com.braintribe.model.processing.cmd.test.meta.model.SessionScopedMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.SimpleSelectorExpert;
import com.braintribe.model.processing.cmd.test.meta.selector.StaticContextSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.StaticContextSelectorExpert;
import com.braintribe.model.processing.cmd.test.provider.ModelMdProvider;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * 
 */
public class SessionProviderMisconfiguredTests extends MetaDataResolvingTestBase {

	/**
	 * This is a copy of {@link ModelMetaDataResolvingTests#model_SessionScope()}, but we do not configure any sessionProvider. It still works the
	 * same, but a warning is logged.
	 */
	@Test
	public void test_Model_SessionScope() {
		List<? extends ModelMetaData> mmds = getMetaData().meta(SessionScopedMetaData.T).list();
		assertOneMetaData(SessionScopedMetaData.T, mmds);
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
	}

	private Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectProviders() {
		return CollectionTools2.asMap(RoleAspect.class, new RoleAspectProvider());
	}

	private static class RoleAspectProvider implements Supplier<Set<String>> {
		@Override
		public Set<String> get() throws RuntimeException {
			return new HashSet<String>(Arrays.asList("admin"));
		}
	}

}
