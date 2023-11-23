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

import java.util.HashSet;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeIkpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class CompositeInverseKeyProperty_ManipulationsTests extends AbstractManipulationsTests {

	SmartPersonA p1, p2;
	CompositeIkpaEntity sc1, sc2;

	@Test
	public void changeValue_SingleSingle() throws Exception {
		p1 = newSmartPersonA();
		p1.setNameA("p1");

		CompositeIkpaEntity sc1 = newCompositeIkpaEntity();
		sc1.setDescription("d1");
		commit();

		p1.setCompositeIkpaEntity(sc1);
		commit();

		CompositeIkpaEntityA c1 = compositeIkpaEntityAByDescription("d1");
		BtAssertions.assertThat(c1.getPersonId()).isEqualTo(p1.getId());
		BtAssertions.assertThat(c1.getPersonName()).isEqualTo(p1.getNameA());
	}

	@Test
	public void changeValue_SingleSingle_Nullify() throws Exception {
		changeValue_SingleSingle();

		p1.setCompositeIkpaEntity(null);
		commit();

		CompositeIkpaEntityA c1 = compositeIkpaEntityAByDescription("d1");
		BtAssertions.assertThat(c1.getPersonId()).isNull();
		BtAssertions.assertThat(c1.getPersonName()).isNull();
	}

	@Test
	public void insert_MultiSingle() throws Exception {
		p1 = newSmartPersonA();
		p1.setNameA("p1");

		sc1 = newCompositeIkpaEntity();
		sc1.setDescription("d1");

		sc2 = newCompositeIkpaEntity();
		sc2.setDescription("d2");
		commit();

		p1.setCompositeIkpaEntitySet(new HashSet<CompositeIkpaEntity>());
		p1.getCompositeIkpaEntitySet().add(sc1);
		p1.getCompositeIkpaEntitySet().add(sc2);
		commit();

		assertCompositeIkpaLinkedTo_Set("d1", p1);
		assertCompositeIkpaLinkedTo_Set("d2", p1);
	}

	@Test
	public void remove_MultiSingle() throws Exception {
		insert_MultiSingle();

		p1.getCompositeIkpaEntitySet().remove(sc2);
		commit();

		assertCompositeIkpaLinkedTo_Set("d1", p1);
		assertCompositeIkpaLinkedTo_Set("d2", null);
	}

	@Test
	public void clear_MultiSingle() throws Exception {
		insert_MultiSingle();

		p1.getCompositeIkpaEntitySet().clear();
		commit();

		assertCompositeIkpaLinkedTo_Set("d1", null);
		assertCompositeIkpaLinkedTo_Set("d2", null);
	}

	private void assertCompositeIkpaLinkedTo_Set(String description, SmartPersonA p) {
		CompositeIkpaEntityA c = compositeIkpaEntityAByDescription(description);
		BtAssertions.assertThat(c.getPersonId_Set()).isEqualTo(p == null ? null : p.getId());
		BtAssertions.assertThat(c.getPersonName_Set()).isEqualTo(p == null ? null : p.getNameA());
	}

}
