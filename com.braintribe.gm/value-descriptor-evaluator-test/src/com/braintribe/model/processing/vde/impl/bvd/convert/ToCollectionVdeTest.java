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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.bvd.convert.ToList;
import com.braintribe.model.bvd.convert.ToSet;
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
public class ToCollectionVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator();

	@Test
	public void testStringToSetConvert() throws Exception {
		String string = "foo";
		
		ToSet convert = $.toSet();
		convert.setOperand(string);

		Object result = evaluate(convert);
		validateSetResult(result, Collections.singleton(string), Set.class);
	}

	@Test
	public void testEntityToSetConvert() throws Exception {
		Person p = person("John", "Doe");
		
		ToSet convert = $.toSet();
		convert.setOperand(p);

		Object result = evaluate(convert);
		validateSetResult(result, Collections.singleton(p), Set.class);
	}

	@Test
	public void testStringToListConvert() throws Exception {
		String string = "foo";
		
		ToList convert = $.toList();
		convert.setOperand(string);

		Object result = evaluate(convert);
		validateSetResult(result, Collections.singletonList(string), List.class);
	}

	@Test
	public void testEntityToListConvert() throws Exception {
		Person p = person("John", "Doe");
		
		ToList convert = $.toList();
		convert.setOperand(p);

		Object result = evaluate(convert);
		validateSetResult(result, Collections.singletonList(p), List.class);
	}

	@Test
	public void testListStringToSetConvert() throws Exception {
		List<String> list = CollectionTools2.asList("foo","bar");
		
		ToSet convert = $.toSet();
		convert.setOperand(list);

		Object result = evaluate(convert);
		validateSetResult(result, new HashSet<>(list), Set.class);
	}

	@Test
	public void testListEntityToSetConvert() throws Exception {
		List<Person> list = CollectionTools2.asList(person("John","Doe"),person("Jane","Doe"));
		
		ToSet convert = $.toSet();
		convert.setOperand(list);

		Object result = evaluate(convert);
		validateSetResult(result, new HashSet<>(list), Set.class);
	}

	@Test
	public void testSetStringToListConvert() throws Exception {
		Set<String> set = CollectionTools2.asSet("foo","bar");
		
		ToList convert = $.toList();
		convert.setOperand(set);

		Object result = evaluate(convert);
		validateSetResult(result, new ArrayList<>(set), List.class);
	}

	@Test
	public void testSetEntityToListConvert() throws Exception {
		Set<Person> set = CollectionTools2.asSet(person("John","Doe"),person("Jane","Doe"));
		
		ToList convert = $.toList();
		convert.setOperand(set);

		Object result = evaluate(convert);
		validateSetResult(result, new ArrayList<>(set), List.class);
	}

	private void validateSetResult(Object result, Class<?> expectedCollectionType) {
		assertThat(result).isNotNull();
		assertTrue(expectedCollectionType.isAssignableFrom(result.getClass()));
	}

	private void validateSetResult(Object result, Collection<Object> expectedResult, Class<?> expectedCollectionType) {
		validateSetResult(result, expectedCollectionType);
		assertThat(expectedResult.size()).isEqualTo(expectedResult.size());
		assertTrue((expectedResult.containsAll(expectedResult)));
	}
	
	
	private Person person(String firstName, String lastName) {
		Name name = Name.T.create();
		name.setFirst(firstName);
		name.setLast(lastName);
		
		Person p = Person.T.create();
		p.setName(name);
		return p;
	}

}
