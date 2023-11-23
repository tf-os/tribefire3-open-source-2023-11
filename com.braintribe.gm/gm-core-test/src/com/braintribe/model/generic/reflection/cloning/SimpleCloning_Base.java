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
package com.braintribe.model.generic.reflection.cloning;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.cloning.model.City;
import com.braintribe.model.generic.reflection.cloning.model.Country;
import com.braintribe.model.generic.session.GmSession;

/**
 * @author peter.gazdik
 */
public abstract class SimpleCloning_Base {

	protected CloningContext cc;

	public abstract void simplyCopying();

	/**
	 * Tests that new type is really created, and a simple property is copied.
	 */
	protected void runSimplyCopying() {
		Country country = Country.T.create();
		country.setName("Wakanda");

		Country cCountry = clone(country);

		assertThat(cCountry).isNotSameAs(country);
		assertThat(cCountry.getName()).isEqualTo(country.getName());
	}

	public abstract void copyOnASession();

	/**
	 * Tests that new type is created on a given session.
	 */
	protected void runCopyOnASession(GmSession session) {
		Country country = Country.T.create();
		country.setName("Wakanda");

		Country cCountry = clone(country);

		assertThat(cCountry).isNotSameAs(country);
		assertThat(cCountry.getName()).isEqualTo(country.getName());
		assertThat(cCountry.session()).isSameAs(session);
	}

	public abstract void doNotCopyIdStuff();

	/**
	 * Tests that identifying properties and globalId are not copied.
	 */
	protected void runDoNotCopyIdStuff() {
		Country country = Country.T.create();
		country.setId(1L);
		country.setPartition("partition");
		country.setGlobalId("globalId");

		Country cCountry = clone(country);

		assertThat(cCountry).isNotSameAs(country);
		assertThat(cCountry.<Object> getId()).isNull();
		assertThat(cCountry.getPartition()).isNull();
		assertThat(cCountry.getGlobalId()).isNull();
	}

	public abstract void referenceOriginalPropertyValue();

	/**
	 * Tests that deeper entity is not copied, but also referenced from the copy of the top level entity.
	 */
	protected void runReferenceOriginalPropertyValue() {
		City city = City.T.create();
		city.setName("Birnin Zana");

		Country country = Country.T.create();
		country.setName("Wakanda");
		country.setCapital(city);

		Country cCountry = clone(country);

		assertThat(cCountry).isNotSameAs(country);
		assertThat(cCountry.getCapital()).isSameAs(country.getCapital());
	}

	public abstract void stringifyIdInPreProcess();

	/**
	 * Tests that id is turned from long to string in pre-processing.
	 */
	protected void runStringifyIdInPreProcess() {
		Country country = Country.T.create();
		country.setId(1L);

		Country cCountry = clone(country);

		assertThat(cCountry).isNotSameAs(country);
		assertThat(country.<String> getId()).isEqualTo("1");
		assertThat(cCountry.<String> getId()).isEqualTo("1");
	}

	public abstract void stringifyIdInPostProcess();

	/**
	 * Tests that id is turned from long to string in post-processing.
	 */
	protected void runStringifyIdInPostProcess() {
		Country country = Country.T.create();
		country.setId(1L);

		Country cCountry = clone(country);

		assertThat(cCountry).isNotSameAs(country);
		assertThat(country.<Long> getId()).isEqualTo(1L);
		assertThat(cCountry.<String> getId()).isEqualTo("1");
	}

	protected GenericEntity stringifyId(GenericEntity e) {
		e.setId("" + e.getId());
		return e;
	}

	private <T> T clone(T t) {
		return Country.T.clone(cc, t, StrategyOnCriterionMatch.partialize);
	}

}
