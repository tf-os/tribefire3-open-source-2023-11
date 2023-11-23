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
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.provider.PropertyValueComparatorMdProvider;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;

public class PropertyValueComparatorTests extends MetaDataResolvingTestBase {

	/** @see PropertyValueComparatorMdProvider#addPropertyMdWithStringEqualComparator() */
	@Test
	public void compareStringPropertyWithEqualComparator() {
		PropertyMetaData md;

		md = getMetaData().entityClass(Person.class).property("name").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		Person person = Person.T.create();
		person.setName("foo");

		md = getMetaData().entityClass(Person.class).property("name").entity(person).meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see PropertyValueComparatorMdProvider#addPropertyMdWithIntGreaterComparator */
	@Test
	public void compareIntPropertyWithExplicitPropertyPathComparator() {
		PropertyMetaData md;

		md = getMetaData().entityClass(Person.class).property("name").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		Person person = Person.T.create();
		person.setAge(1);

		md = getMetaData().entityClass(Person.class).property("name").entity(person).meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see PropertyValueComparatorMdProvider#addPropertyMdWithCollectionFirstElementEqualComparator */
	@Test
	public void compareListPropertyWithFirstElementComparator() {
		PropertyMetaData md;

		md = getMetaData().entityClass(Person.class).property("name").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		Person friend = Person.T.create();
		friend.setName("foo");

		Person person = Person.T.create();
		person.setName("holder");
		person.getFriends().add(friend);

		md = getMetaData().entityClass(Person.class).property("name").entity(person).useCase("firstElement").meta(SimplePropertyMetaData.T)
				.exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see PropertyValueComparatorMdProvider#addPropertyMdWithListSizeComparator */
	@Test
	public void compareListPropertyWithSizeComparator() {
		PropertyMetaData md;

		md = getMetaData().entityClass(Person.class).property("name").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		Person friend = Person.T.create();
		friend.setName("foo");

		Person friend2 = Person.T.create();
		friend2.setName("foo");

		Person person = Person.T.create();
		person.setName("holder");
		person.getFriends().add(friend);
		person.getFriends().add(friend2);

		md = getMetaData().entityClass(Person.class).property("name").entity(person).useCase("size-list").meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see PropertyValueComparatorMdProvider#addPropertyMdWithSetSizeComparator */
	@Test
	public void compareSetPropertyWithSizeComparator() {
		PropertyMetaData md;

		md = getMetaData().entityClass(Person.class).property("name").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		Person friend = Person.T.create();
		friend.setName("foo");

		Person friend2 = Person.T.create();
		friend2.setName("foo");

		Person person = Person.T.create();
		person.setName("holder");
		person.getOtherFriends().add(friend);
		person.getOtherFriends().add(friend2);

		md = getMetaData().entityClass(Person.class).property("name").entity(person).useCase("size-set").meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	/** @see PropertyValueComparatorMdProvider#addPropertyMdWithMapSizeComparator */
	@Test
	public void compareMapPropertyWithSizeComparator() {
		PropertyMetaData md;

		md = getMetaData().entityClass(Person.class).property("name").meta(SimplePropertyMetaData.T).exclusive();
		assertNoMd(md);

		Person person = Person.T.create();
		person.setName("holder");
		person.getProperties().put("foo", "bar");
		person.getProperties().put("foo2", "bar2");

		md = getMetaData().entityClass(Person.class).property("name").entity(person).useCase("size-map").meta(SimplePropertyMetaData.T).exclusive();
		assertOneMetaData(SimplePropertyMetaData.T, md);
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new PropertyValueComparatorMdProvider();
	}

	@Override
	protected void setupCmdResolver(CmdResolverBuilder crb) {
		crb.setSessionProvider(Thread::currentThread);
	}
}
