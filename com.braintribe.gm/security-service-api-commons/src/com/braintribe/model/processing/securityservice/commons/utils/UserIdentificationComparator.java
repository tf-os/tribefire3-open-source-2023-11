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
package com.braintribe.model.processing.securityservice.commons.utils;

import java.util.Comparator;

import com.braintribe.model.securityservice.credentials.identification.EmailIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;

public class UserIdentificationComparator implements Comparator<UserIdentification> {

	public static final UserIdentificationComparator INSTANCE = new UserIdentificationComparator();

	@Override
	public int compare(UserIdentification o1, UserIdentification o2) {

		if (o1 == o2) {
			return 0;
		}

		if (o1 == null) {
			return -1;
		}

		if (o2 == null) {
			return 1;
		}

		if (o1 instanceof UserNameIdentification && o2 instanceof UserNameIdentification) {
			return compareUserNameIdentification((UserNameIdentification) o1, (UserNameIdentification) o2);
		}

		if (o1 instanceof EmailIdentification && o2 instanceof EmailIdentification) {
			return compareEmailIdentification((EmailIdentification) o1, (EmailIdentification) o2);
		}

		return -1;
	}

	private static int compareUserNameIdentification(UserNameIdentification o1, UserNameIdentification o2) {
		return compare(o1.getUserName(), o2.getUserName());
	}

	private static int compareEmailIdentification(EmailIdentification o1, EmailIdentification o2) {
		return compare(o1.getEmail(), o2.getEmail());
	}

	private static int compare(String a, String b) {
		if (a == null && b == null) {
			return 0;
		} else if (a == null) {
			return -1;
		} else if (b == null) {
			return 1;
		} else {
			return a.compareTo(b);
		}
	}

}
