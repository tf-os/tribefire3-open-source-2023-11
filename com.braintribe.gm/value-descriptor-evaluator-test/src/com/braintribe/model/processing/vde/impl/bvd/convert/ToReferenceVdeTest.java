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
package com.braintribe.model.processing.vde.impl.bvd.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.convert.ToReference;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToStringVde;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.Person;
import com.braintribe.model.processing.vde.test.VdeTest;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * Provides tests for {@link ToStringVde}.
 * 
 */
public class ToReferenceVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator();

	@Test
	public void testEntityToReferenceConvert() throws Exception {
		Person p = person(1, "John", "Doe");
		
		ToReference convert = $.toReference();
		convert.setOperand(p);

		Object result = evaluate(convert);
		validateResult(result, p.reference());
	}

	@Test
	public void testEntityCollectionToReferenceConvert() throws Exception {
		Person p1 = person(1, "John", "Doe");
		Person p2 = person(2, "Foo", "Bar");
		List<Person> persons = CollectionTools2.asList(p1, p2);
		
		ToReference convert = $.toReference();
		convert.setOperand(persons);

		Object result = evaluate(convert);
		validateResult(result, CollectionTools2.asList(p1.reference(),p2.reference()));
	}
	
	@Test
	public void testStringToReferenceConvert() throws Exception {
		String str = "foo";
		
		ToReference convert = $.toReference();
		convert.setOperand(str);

		Object result = evaluate(convert);
		validateResult(result, str);
	}

	@Test
	public void testStringListToReferenceConvert() throws Exception {
		String str1 = "foo";
		String str2 = "foo";
		
		List<String> strings = CollectionTools2.asList(str1,str2);
		
		ToReference convert = $.toReference();
		convert.setOperand(strings);

		Object result = evaluate(convert);
		validateResult(result, strings);
	}

	private void validateResult(Object result, Object expected) {
		assertThat(result).isNotNull();
		if (expected instanceof List<?>) {
			assertThat(result).isInstanceOf(List.class);
			List<?> resultCollection = (List<?>) result;
			List<?> expectedCollection = (List<?>) expected;
			assertThat(resultCollection.size()).isEqualTo(expectedCollection.size());
			for (int i = 0; i < resultCollection.size(); i++) {
				Object resultElement = resultCollection.get(i);
				Object expectedElement = expectedCollection.get(i);
				validateResult(resultElement, expectedElement);
			}
			
		} else if (expected instanceof EntityReference) {
			assertThat(result).isInstanceOf(EntityReference.class);
			assertTrue(EntRefHashingComparator.INSTANCE.compare((EntityReference)result, (EntityReference)expected));
		} else {
			assertThat(result).isEqualTo(expected);
		}
	}

	
	private Person person(long id, String firstName, String lastName) {
		Name name = Name.T.create();
		name.setFirst(firstName);
		name.setLast(lastName);
		
		Person p = Person.T.create();
		p.setId(id);
		p.setName(name);
		return p;
	}

}
