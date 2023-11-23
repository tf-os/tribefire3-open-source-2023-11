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
package com.braintribe.model.usersession;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.user.User;

public interface UserSession extends GenericEntity {

	EntityType<UserSession> T = EntityTypes.T(UserSession.class);

	public static final String accessId = "accessId";
	public static final String sessionId = "sessionId";
	public static final String user = "user";
	public static final String creationDate = "creationDate";
	public static final String fixedExpiryDate = "fixedExpiryDate";
	public static final String expiryDate = "expiryDate";
	public static final String lastAccessedDate = "lastAccessedDate";
	public static final String maxIdleTime = "maxIdleTime";
	public static final String isInvalidated = "isInvalidated";
	public static final String effectiveRoles = "effectiveRoles";
	public static final String referenceCounter = "referenceCounter";
	public static final String type = "type";
	public static final String creationInternetAddress = "creationInternetAddress";
	public static final String creationNodeId = "creationNodeId";
	public static final String properties = "properties";

	String getAccessId();
	void setAccessId(String accessId);

	String getSessionId();
	void setSessionId(String sessionId);

	User getUser();
	void setUser(User user);

	Date getCreationDate();
	void setCreationDate(Date creationDate);

	Date getFixedExpiryDate();
	void setFixedExpiryDate(Date fixedExpiryDate);

	/**
	 * Expiry date is calculated on each access (touch) by adding the {@code maxIdleTime} to the
	 * {@code lastAccessedDate}. If {@code fixedExpiryDate} comes before the mentioned sum, {@code expiryDate} is set to
	 * be equal to {@code fixedExpiryDate}.
	 */
	Date getExpiryDate();
	void setExpiryDate(Date expiryDate);

	Date getLastAccessedDate();
	void setLastAccessedDate(Date lastAccessedDate);

	TimeSpan getMaxIdleTime();
	void setMaxIdleTime(TimeSpan maxIdleTime);

	/**
	 * Signals that the session is no longer valid, i.e. has been closed (e.g. after logout).
	 */
	boolean getIsInvalidated();
	void setIsInvalidated(boolean isInvalidated);

	Set<String> getEffectiveRoles();
	void setEffectiveRoles(Set<String> effectiveRoles);

	int getReferenceCounter();
	void setReferenceCounter(int referenceCounter);

	UserSessionType getType();
	void setType(UserSessionType type);
	
	String getCreationNodeId();
	void setCreationNodeId(String creationNodeId);

	String getCreationInternetAddress();
	void setCreationInternetAddress(String creationInternetAddress);

	/**
	 * Properties can be used to store (unmodifiable) custom data that is available at authentication time and which can
	 * then be retrieved later, e.g. in a custom access. Note that properties are meant to be simple key/value pairs. If
	 * you have to store more (and modeled) data and/or if the data is modifiable (e.g. some state that changes), use
	 * the user session access instead.
	 */
	Map<String, String> getProperties();
	/**
	 * @see #getProperties()
	 */
	void setProperties(Map<String, String> properties);

	/**
	 * @return the locale value if available in the properties. if not available this method returns null.
	 */
	default String locale() {
		Map<String, String> properties = getProperties();
		return properties != null ? properties.get("locale") : null;
	}
}
