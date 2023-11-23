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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.utils.junit.assertions.BtAssertions;
import com.braintribe.utils.junit.core.rules.ThrowableChainRule;

public class UnmappedValue_ManipulationsTests extends AbstractManipulationsTests {

	private SmartPersonA p;

	@Rule
	public ThrowableChainRule exceptionChainRule = new ThrowableChainRule(SmartAccessException.class);

	@Before
	public void initalize() throws Exception {
		p = newSmartPersonA();
		commit();

		BtAssertions.assertThat(p.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countPersonA()).isEqualTo(1);
	}

	@Test
	public void unmappedString() throws Exception {
		p.setUnmappedString("value");
		session.commit();
	}

	@Test
	public void unmappedEntity() throws Exception {
		p.setUnmappedParent(p);
		session.commit();
	}

	@Test
	public void unmappedCollection() throws Exception {
		p.setUnmappedParents(asSet(p));
		session.commit();
	}

}
