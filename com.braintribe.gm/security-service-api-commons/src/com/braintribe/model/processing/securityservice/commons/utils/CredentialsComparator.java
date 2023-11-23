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

import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.HasUserIdentification;

public class CredentialsComparator implements Comparator<Credentials> {

	public static final CredentialsComparator INSTANCE = new CredentialsComparator();
	
	@Override
	public int compare(Credentials o1, Credentials o2) {
		
		if (o1 == o2) {
			return 0;
		} else if (o1 == null) {
			return -1;
		} else if (o2 == null) {
			return 1;
		}
		
		if (o1 instanceof HasUserIdentification && o2 instanceof HasUserIdentification) {
			return UserIdentificationComparator.INSTANCE.compare(((HasUserIdentification)o1).getUserIdentification(), ((HasUserIdentification)o2).getUserIdentification());
		} else {
			return Integer.compare(o1.hashCode(), o2.hashCode());
		}
	}
	
}
