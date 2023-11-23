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
package com.braintribe.model.processing.securityservice.usersession.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.usersession.UserSessionType;
import com.braintribe.utils.DateTools;

public class UserSessionIdProvider implements Supplier<String>, Function<UserSessionType, String> {

	private Map<UserSessionType, String> typePrefixes = new HashMap<>();

	@Configurable
	public void setTypePrefixes(Map<UserSessionType, String> typePrefixes) {
		this.typePrefixes = typePrefixes;
	}

	@Override
	public String get() {
		//Not using the RandomTools.newStandardUuid() method with the embedded date/time as this 
		//would cryptographically weaken the ID
		//We still want to keep the date/time in the session Id to easier identify problems with expired sessions
		final String datePrefix = DateTools.encode(new Date(), DateTools.TERSE_DATETIME_WITH_MS_FORMAT);
		String uuid = UUID.randomUUID().toString();
		return datePrefix+"-"+uuid;
	}

	@Override
	public String apply(UserSessionType type) {

		String sessionId = get();

		String prefix = typePrefixes.get(type);

		if (prefix != null) {
			sessionId = prefix + sessionId;
		}

		return sessionId;
	}

}
