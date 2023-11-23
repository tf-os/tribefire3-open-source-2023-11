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
package com.braintribe.model.processing.query.test.builder;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Date;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.query.test.model.Color;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * 
 */
public class AbstractPersonBuilder<P extends Person, B extends AbstractPersonBuilder<P, B>> extends AbstractBuilder<P, B> {

	public AbstractPersonBuilder(Class<P> clazz, Smood smood) {
		super(clazz, smood);
	}

	public B name(String value) {
		instance.setName(value);
		return self;
	}

	public B indexedName(String value) {
		instance.setIndexedName(value);
		return self;
	}

	public B nicknames(String... values) {
		instance.setNicknames(asSet(values));
		return self;
	}

	public B birthDate(Date value) {
		instance.setBirthDate(value);
		return self;
	}

	public B age(int value) {
		instance.setAge(value);
		return self;
	}

	public B company(Company value) {
		instance.setCompany(value);
		instance.setIndexedCompany(value);
		return self;
	}

	public B companyName(String value) {
		instance.setCompanyName(value);
		return self;
	}

	public B indexedInteger(int value) {
		instance.setIndexedInteger(value);
		return self;
	}

	public B indexedUniqueName(String value) {
		instance.setIndexedUniqueName(value);
		return self;
	}

	public B phoneNumber(String value) {
		instance.setPhoneNumber(value);
		return self;
	}

	public B friend(Person value) {
		instance.setIndexedFriend(value);
		return self;
	}

	public B localizedString(String... os) {
		LocalizedString ls = LocalizedString.T.create();
		ls.setLocalizedValues(CollectionTools2.<String, String> asMap((Object[]) os));

		instance.setLocalizedString(ls);

		return self;
	}

	public B eyeColor(Color eyeColor) {
		instance.setEyeColor(eyeColor);
		return self;
	}

}
