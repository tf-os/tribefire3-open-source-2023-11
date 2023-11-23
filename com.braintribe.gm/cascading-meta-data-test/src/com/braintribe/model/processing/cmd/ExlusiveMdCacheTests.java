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

import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleInheritedMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.UnreachableSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.UnreachableSelectorExpert;
import com.braintribe.model.processing.cmd.test.model.Teacher;
import com.braintribe.model.processing.cmd.test.provider.ExclusiveMdProvider;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;

/**
 * Tests whether the caching works the right way when resolving exclusive meta-data.
 */
public class ExlusiveMdCacheTests extends MetaDataResolvingTestBase {

	/**
	 * This just shows our {@link UnreachableSelector} and {@link UnreachableSelectorExpert} are setup properly, i.e. that any resolution that tries
	 * to resolve those ends up with an exception.
	 * 
	 * @see ExclusiveMdProvider#addSimpleExclusiveMd()
	 */
	@Test(expected = CascadingMetaDataException.class)
	public void showProperTestSetup() {
		getMetaData().entityType(Teacher.T).meta(SimpleEntityMetaData.T).list();
	}

	/** @see ExclusiveMdProvider#addSimpleExclusiveMd() */
	@Test
	public void test_Entity_StopsOnFirstStaticWithoutLookingToSuper() {
		EntityTypeMetaData md = getMetaData().entityClass(Teacher.class).meta(SimpleEntityMetaData.T).exclusive();
		assertOneMetaData(SimpleEntityMetaData.T, md);
	}

	/** @see ExclusiveMdProvider#addInheritedExclusiveMd() */
	@Test
	public void test_Entity_StopsInFirstSuperWithoutGoingToSecond() {
		EntityTypeMetaData md = getMetaData().entityClass(Teacher.class).meta(SimpleInheritedMetaData.T).exclusive();
		assertOneMetaData(SimpleInheritedMetaData.T, md);
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new ExclusiveMdProvider();
	}

	@Override
	protected void setupCmdResolver(CmdResolverBuilder crb) {
		crb.addExpert(UnreachableSelector.T, new UnreachableSelectorExpert());
	}

}
