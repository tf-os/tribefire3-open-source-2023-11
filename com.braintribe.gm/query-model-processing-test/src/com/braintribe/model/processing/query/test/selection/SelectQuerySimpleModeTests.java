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
package com.braintribe.model.processing.query.test.selection;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.query.selection.BasicQuerySelectionResolver;

public class SelectQuerySimpleModeTests extends AbstractSelectionResolverTest {
	
	private BasicQuerySelectionResolver resolver = 
			BasicQuerySelectionResolver.create()
									   .aliasMode()
									     .simple()
									   .shorteningMode()
									     .simplified();
	
	@Test
	public void testSingleReferenceJoin() {
		List<QuerySelection> selections = stringifyAndResolve(singleReferenceQuery(), resolver);
		
		assertThat(selections).isNotEmpty();
		assertThat(selections).hasSize(4);
		assertThat(selections.get(0).getAlias()).isEqualTo("Person.name");
		assertThat(selections.get(1).getAlias()).isEqualTo("Person.company");
		assertThat(selections.get(2).getAlias()).isEqualTo("Company");
		assertThat(selections.get(3).getAlias()).isEqualTo("Company.name");
		
	}
	
	@Test
	public void testCollectionReferenceJoin() {
		List<QuerySelection> selections = stringifyAndResolve(collectionReferenceQuery(), resolver);
		
		assertThat(selections).isNotEmpty();
		assertThat(selections).hasSize(6);
		assertThat(selections.get(0).getAlias()).isEqualTo("Person.name");
		assertThat(selections.get(1).getAlias()).isEqualTo("Person.company");
		assertThat(selections.get(2).getAlias()).isEqualTo("Company");
		assertThat(selections.get(3).getAlias()).isEqualTo("Company.name");
		assertThat(selections.get(4).getAlias()).isEqualTo("LocalizedString.id");
		assertThat(selections.get(5).getAlias()).isEqualTo("LocalizedString");

	}

	
	@Test
	public void testFunctions() {
		List<QuerySelection> selections = stringifyAndResolve(functionQuery(), resolver);
		
		assertThat(selections).isNotEmpty();
		assertThat(selections).hasSize(5);
		assertThat(selections.get(0).getAlias()).isEqualTo("Person");
		assertThat(selections.get(1).getAlias()).isEqualTo("count(_Person)");
		assertThat(selections.get(2).getAlias()).isEqualTo("toString(_Person)");
		assertThat(selections.get(3).getAlias()).isEqualTo("avg(_Person.age)");
		assertThat(selections.get(4).getAlias()).isEqualTo("concatenation(_Person, '.', _Person.name, ':', _Person, '.', _Person.companyName)");

		
	}
	

	@Test
	public void testWildcardSelection() {
		List<QuerySelection> selections = stringifyAndResolve(wildcardsQuery(), resolver);
		
		assertThat(selections).isNotEmpty();
		assertThat(selections).hasSize(4);
		assertThat(selections.get(0).getAlias()).isEqualTo("Person");
		assertThat(selections.get(1).getAlias()).isEqualTo("Company");
		assertThat(selections.get(2).getAlias()).isEqualTo("Person");
		assertThat(selections.get(3).getAlias()).isEqualTo("LocalizedString");
		//assertThat(selections.get(4).getAlias()).isEqualTo("Company");
		
	}

	
}
