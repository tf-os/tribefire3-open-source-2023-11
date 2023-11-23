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
package com.braintribe.model.access.hibernate.tests;

import static com.braintribe.model.access.hibernate.base.model.simple.BasicEntity.localizedString;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.HibernateBaseModelTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.Localize;

/**
 * Tests for the {@link Localize} function, both in the SELECT and WHERE clause.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class LocalizedString_HbmTest extends HibernateBaseModelTestBase {

	private static final String enLocale = "en";
	private static final String enValue = "ENG";
	private static final String missingLocale = "missing";
	private static final String needsEscapeLocale = "'needsEscape";
	private static final String defaultValue = "DEFAULT";

	private BasicEntity be;

	@Test
	public void localizedString_Selection() throws Exception {
		createEntityWithLocalizedString();

		assertLocalizedString(enLocale, enValue);
		assertLocalizedString(LocalizedString.LOCALE_DEFAULT, defaultValue);
		assertLocalizedString(missingLocale, defaultValue);
		assertLocalizedString(needsEscapeLocale, defaultValue);
	}

	private void assertLocalizedString(String locale, String expectedValue) {
		String actualValue = queryLocalizedString(locale);

		assertThat(actualValue).isEqualTo(expectedValue);
	}

	private String queryLocalizedString(String locale) {
		// @formatter:off
		SelectQuery sq = new SelectQueryBuilder()
				.select()
					.localize(locale).property("be", localizedString)
				.from(BasicEntity.T, "be")
				.done();
		// @formatter:on

		return session.query().select(sq).unique();
	}

	/**
	 * As default String length is 255, this tests that setting the length to 1000 had the desired effect.  
	 */
	@Test
	public void localizedString_BigValue() {
		String bigString = times256("abc");

		LocalizedString ls = session.create(LocalizedString.T);
		ls.putDefault(bigString);

		session.commit();
		resetGmSession();

		LocalizedString ls2 = session.query().entity(ls).require();

		assertThat(ls2).isNotSameAs(ls); // double check the session is a new one
		assertThat(ls2.value()).isEqualTo(bigString);
	}

	private static String times256(String s) {
		for (int i = 0; i < 8; i++)
			s += s;
		return s;
	}

	@Test
	public void localizedString_Condition() throws Exception {
		createEntityWithLocalizedString();

		be = queryBeByLocalizedString(enLocale, enValue);
		assertThat(be).isNotNull();

		be = queryBeByLocalizedString(LocalizedString.LOCALE_DEFAULT, defaultValue);
		assertThat(be).isNotNull();

		be = queryBeByLocalizedString(missingLocale, defaultValue);
		assertThat(be).isNotNull();

		// We are testing that the single quote gets escaped properly
		be = queryBeByLocalizedString(needsEscapeLocale, defaultValue);
		assertThat(be).isNotNull();
	}

	private void createEntityWithLocalizedString() {
		LocalizedString name = session.create(LocalizedString.T);
		name.putDefault(defaultValue);
		name.put(enLocale, enValue);
		name.put("extraLocale", "extraValue"); // just so the test is more complex

		be = session.create(BasicEntity.T);
		be.setLocalizedString(name);
		session.commit();

		resetGmSession();
	}

	private BasicEntity queryBeByLocalizedString(String locale, String name) {
		SelectQuery query = queryBeByLocalizedStr(locale, name);

		return queryUnique(query);
	}

	private SelectQuery queryBeByLocalizedStr(String locale, String name) {
		// @formatter:off
		return new SelectQueryBuilder()
				.from(BasicEntity.T, "be")
				.where()
					.localize(locale).property("be", "localizedString").eq(name)
				.done();
		// @formatter:on
	}

	/** There was a bug that no and was added between the extra conditions needed for the {@link Localize} function. */
	@Test
	public void localizedString_DisjunctionConditions() throws Exception {
		// value is ENG
		createEntityWithLocalizedString();

		be = queryBeByLocalizedStringWithDisjunction(enLocale, "A", "B");
		assertThat(be).isNull();

		be = queryBeByLocalizedStringWithDisjunction(enLocale, "E", "B");
		assertThat(be).isNotNull();

		be = queryBeByLocalizedStringWithDisjunction(enLocale, "A", "NG");
		assertThat(be).isNotNull();

		be = queryBeByLocalizedStringWithDisjunction(enLocale, "E", "NG");
		assertThat(be).isNotNull();
	}

	private BasicEntity queryBeByLocalizedStringWithDisjunction(String locale, String pattern1, String pattern2) {
		SelectQuery query = queryBeByLocalizedStrWithDisjunction(locale, pattern1, pattern2);

		return queryUnique(query);
	}

	private SelectQuery queryBeByLocalizedStrWithDisjunction(String locale, String pattern1, String pattern2) {
		// @formatter:off
		return new SelectQueryBuilder()
				.from(BasicEntity.T, "be")
				.where()
					.disjunction()
						.localize(locale).property("be", "localizedString").like("*" + pattern1 + "*")
						.localize(locale).property("be", "localizedString").like("*" + pattern2 + "*")
					.close()
				.done();
		// @formatter:on
	}

	/** There was a bug that no and was added between the extra conditions needed for the {@link Localize} function. */
	@Test
	public void localizedString_Conjunction() throws Exception {
		// value is ENG
		createEntityWithLocalizedString();

		be = queryBeByLocalizedStringWithConjunction(enLocale, "A", "B");
		assertThat(be).isNull();

		be = queryBeByLocalizedStringWithConjunction(enLocale, "E", "B");
		assertThat(be).isNull();

		be = queryBeByLocalizedStringWithConjunction(enLocale, "A", "NG");
		assertThat(be).isNull();

		be = queryBeByLocalizedStringWithConjunction(enLocale, "E", "NG");
		assertThat(be).isNotNull();
	}

	private BasicEntity queryBeByLocalizedStringWithConjunction(String locale, String pattern1, String pattern2) {
		SelectQuery query = queryBeByLocalizedStrWithConjunction(locale, pattern1, pattern2);

		return queryUnique(query);
	}

	private SelectQuery queryBeByLocalizedStrWithConjunction(String locale, String pattern1, String pattern2) {
		// @formatter:off
		return new SelectQueryBuilder()
				.from(BasicEntity.T, "be")
				.where()
					.conjunction()
						.localize(locale).property("be", "localizedString").like("*" + pattern1 + "*")
						.localize(locale).property("be", "localizedString").like("*" + pattern2 + "*")
					.close()
				.done();
		// @formatter:on
	}

	private BasicEntity queryUnique(SelectQuery query) {
		return session.query().select(query).unique();
	}

}
