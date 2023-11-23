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
package com.braintribe.model.access.smart.test.manipulation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.special.ManualA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ReaderA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartManualA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.utils.junit.assertions.BtAssertions;
import com.braintribe.utils.junit.core.rules.ThrowableChain;
import com.braintribe.utils.junit.core.rules.ThrowableChainRule;

/**
 *  
 */
public class UnmapedTypeReference_Weak_ManipulationTests extends AbstractManipulationsTests {

	private SmartReaderA sr;

	@Rule
	public ThrowableChainRule exceptionChainRule = new ThrowableChainRule();

	@Before
	public void prepareData() {
		sr = newSmartReaderA();
		sr.setName("r");
		commit();
	}

	// #####################################
	// ## . . . . . . . KPA . . . . . . . ##
	// #####################################

	@Test
	public void setValidEntityOk() throws Exception {
		SmartManualA a = newSmartManualA();
		a.setTitle("titleA");
		commit();

		sr.setWeakFavoriteManual(a);
		commit();

		ReaderA r = readerAByName("r");
		BtAssertions.assertThat(r.getFavoriteManualTitle()).isEqualTo(a.getTitle());
	}

	@Test
	@ThrowableChain({ SmartAccessException.class })
	public void setWrongEntityCausesError() throws Exception {
		SmartBookB b = newSmartBookB();
		b.setTitle("titleA");
		commit();

		sr.setWeakFavoriteManual(b);
		session.commit(); // this is expected to throw an exception

		Assert.fail("SmartAccessIllegalManipulationException should have been thrown.");
	}

	@Test
	public void addValidEntityOk() throws Exception {
		SmartManualA a = newSmartManualA();
		a.setTitle("titleA");
		commit();

		sr.getWeakFavoriteManuals().add(a);
		commit();

		ReaderA r = readerAByName("r");
		BtAssertions.assertThat(r.getFavoriteManualTitles()).containsOnly(a.getTitle());
	}

	@Test
	@ThrowableChain({ SmartAccessException.class })
	public void addWrongEntityCausesError() throws Exception {
		SmartBookB b = newSmartBookB();
		b.setTitle("titleA");
		commit();

		sr.getWeakFavoriteManuals().add(b);
		session.commit(); // this is expected to throw an exception

		Assert.fail("SmartAccessIllegalManipulationException should have been thrown.");
	}

	// #####################################
	// ## . . . . . . . IKPA . . . . . . .##
	// #####################################

	@Test
	public void addValidEntityOk_Inverse() throws Exception {
		SmartManualA sm = newSmartManualA();
		sm.setTitle("titleA");
		commit();

		sr.getWeakInverseFavoriteManuals().add(sm);
		commit();

		ManualA m = manualAByTitle(sm.getTitle());
		BtAssertions.assertThat(m.getManualString()).isEqualTo(sr.getName());
	}

	@Test
	@ThrowableChain({ SmartAccessException.class })
	public void addWrongEntityCausesError_Inverse() throws Exception {
		SmartBookB b = newSmartBookB();
		b.setTitle("titleA");
		commit();

		sr.getWeakInverseFavoriteManuals().add(b);
		session.commit(); // this is expected to throw an exception

		Assert.fail("SmartAccessIllegalManipulationException should have been thrown.");
	}

	protected ReaderA readerAByName(String name) {
		return selectByProperty(ReaderA.class, "name", name, smoodA);
	}

	protected ManualA manualAByTitle(String title) {
		return selectByProperty(ManualA.class, "title", title, smoodA);
	}

}
