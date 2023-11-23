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
package com.braintribe.model.processing.securityservice.usersession;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cfg.InitializationAware;
import com.braintribe.codec.Codec;
import com.braintribe.codec.string.CommaUrlEscapeCodec;
import com.braintribe.codec.string.ListCodec;
import com.braintribe.codec.string.MapCodec;
import com.braintribe.codec.string.StringCodec;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.user_session_service.MapToUserSession;
import com.braintribe.gm.model.user_session_service.UserSessionRequest;
import com.braintribe.gm.model.usersession.PersistenceUserSession;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;

public class UserSessionServiceProcessor extends AbstractDispatchingServiceProcessor<UserSessionRequest, Object> implements InitializationAware {
	
	private Codec<List<String>, String> listCodec;
	private Codec<Map<String, String>, String> mapCodec = new MapCodec<>();
	
	@Override
	public void postConstruct() {
		ListCodec<String> listCodec = new ListCodec<String>(new StringCodec());
		listCodec.setEscapeCodec(new CommaUrlEscapeCodec());
		this.listCodec = listCodec;
	}
	
	@Override
	protected void configureDispatching(DispatchConfiguration<UserSessionRequest, Object> dispatching) {
		dispatching.register(MapToUserSession.T, this::mapToUserSession);
	}
	
	private List<UserSession> mapToUserSession(@SuppressWarnings("unused") ServiceRequestContext requestContext, MapToUserSession request) {
		return request.getPersistenceUserSessions().stream().map(this::mapToUserSession).collect(Collectors.toList());
	}
	
	private UserSession mapToUserSession(PersistenceUserSession pUserSession) {
		if (pUserSession == null) {
			return null;
		}
		UserSession userSession = UserSession.T.create();
		userSession.setSessionId(pUserSession.getId());
		userSession.setCreationDate(pUserSession.getCreationDate());
		userSession.setFixedExpiryDate(pUserSession.getFixedExpiryDate());
		userSession.setExpiryDate(pUserSession.getExpiryDate());
		userSession.setLastAccessedDate(pUserSession.getLastAccessedDate());
		userSession.setEffectiveRoles(stringToSet(pUserSession.getEffectiveRoles()));
		if (pUserSession.getSessionType() != null) {
			userSession.setType(UserSessionType.valueOf(pUserSession.getSessionType()));
		}
		userSession.setCreationInternetAddress(pUserSession.getCreationInternetAddress());
		userSession.setProperties(stringToMap(pUserSession.getProperties()));
		if (pUserSession.getMaxIdleTime() != null) {
			userSession.setMaxIdleTime(millisToTimeSpan(pUserSession.getMaxIdleTime()));
		}
		User user = User.T.create();
		user.setId("sessionuser-" + pUserSession.getId());
		user.setName(pUserSession.getUserName());
		user.setFirstName(pUserSession.getUserFirstName());
		user.setLastName(pUserSession.getUserLastName());
		user.setEmail(pUserSession.getUserEmail());
		userSession.setUser(user);
		
		return userSession;
	}
	
	private TimeSpan millisToTimeSpan(long millis) {
		TimeSpan maxIdleTime = TimeSpan.T.create();
		maxIdleTime.setUnit(TimeUnit.milliSecond);
		maxIdleTime.setValue(millis);
		return maxIdleTime;
	}
	
	private Map<String, String> stringToMap(String string) {
		if (string == null) {
			return null;
		}
		try {
			return mapCodec.decode(string);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Unable to decode string '" + string + "' to map");
		}
	}
	
	private Set<String> stringToSet(String string) {
		if (string == null) {
			return null;
		}
		try {
			return new HashSet<>(listCodec.decode(string));
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Unable to decode string '" + string + "' to set");
		}
	}

}
