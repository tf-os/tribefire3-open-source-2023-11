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
package com.braintribe.model.processing.vde.impl.bvd.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.bvd.navigation.PropertyPath;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.navigation.PropertyPathVde;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.Person;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link PropertyPathVde}.
 * 
 */
public class PropertyPathVdeTest extends VdeTest {

	/**
	 * Validate that a {@link PropertyPath}, which references a non-null property in a class, will evaluate to the correct property
	 */
	@Test
	public void testFullPropertyPath() throws Exception {
		// init test data
		final Name n = Name.T.create();
		n.setFirst("A");
		n.setMiddle("B");
		n.setLast("C");

		final Person p = Person.T.create();
		p.setName(n);

		final PropertyPath path = $.propertyPath();
		path.setPropertyPath("name.first");
		path.setEntity(p);

		// run the evaluate method
		Object result = evaluate(path);

		// validate output
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("A");
	}

	/**
	 * Validate that a {@link PropertyPath}, which references a null property in a class, will evaluate to null
	 */
	@Test
	public void testEmptyPropertyPath() throws Exception {
		// init test data
		Name n = Name.T.create();
		n.setFirst("A");
		n.setLast("C");

		Person p = Person.T.create();
		p.setName(n);

		PropertyPath path = $.propertyPath();
		path.setPropertyPath("name.middle");
		path.setEntity(p);

		// run the evaluate method
		Object result = evaluate(path);

		// validate output
		assertThat(result).isNull();
	}

	@Test
	public void testNullEntityPropertyPath() throws Exception {

		PropertyPath path = $.propertyPath();
		path.setPropertyPath("name.middle");
		path.setEntity(null);

		// run the evaluate method
		Object result = evaluate(path);

		// validate output
		assertThat(result).isNull();
	}

}
