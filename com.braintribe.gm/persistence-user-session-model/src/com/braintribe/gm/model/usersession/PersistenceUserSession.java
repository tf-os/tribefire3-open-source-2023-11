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
package com.braintribe.gm.model.usersession;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Indexed;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface PersistenceUserSession extends GenericEntity {

	EntityType<PersistenceUserSession> T = EntityTypes.T(PersistenceUserSession.class);

	String acquirationKey = "acquirationKey";
	String userName = "userName";
	String userFirstName = "userFirstName";
	String userLastName = "userLastName";
	String userEmail = "userEmail";
	String creationDate = "creationDate";
	String fixedExpiryDate = "fixedExpiryDate";
	String expiryDate = "expiryDate";
	String lastAccessedDate = "lastAccessedDate";
	String maxIdleTime = "maxIdleTime";
	String effectiveRoles = "effectiveRoles";
	String sessionType = "sessionType";
	String creationInternetAddress = "creationInternetAddress";
	String creationNodeId = "creationNodeId";
	String properties = "properties";
	String blocksAuthenticationAfterLogout = "blocksAuthenticationAfterLogout";
	String closed = "closed";

	@Indexed
	String getAcquirationKey();
	void setAcquirationKey(String acquirationKey);

	boolean getBlocksAuthenticationAfterLogout();
	void setBlocksAuthenticationAfterLogout(boolean blocksAuthenticationAfterLogout);

	boolean getClosed();
	void setClosed(boolean closed);

	String getUserName();
	void setUserName(String name);

	String getUserFirstName();
	void setUserFirstName(String firstName);

	String getUserLastName();
	void setUserLastName(String lastName);

	String getUserEmail();
	void setUserEmail(String email);

	Date getCreationDate();
	void setCreationDate(Date creationDate);

	@Indexed
	Date getFixedExpiryDate();
	void setFixedExpiryDate(Date fixedExpiryDate);

	/**
	 * Expiry date is calculated on each access (touch) by adding the {@code maxIdleTime} to the {@code lastAccessedDate}.
	 * If {@code fixedExpiryDate} comes before the mentioned sum, {@code expiryDate} is set to be equal to
	 * {@code fixedExpiryDate}.
	 */
	@Indexed
	Date getExpiryDate();
	void setExpiryDate(Date expiryDate);

	Date getLastAccessedDate();
	void setLastAccessedDate(Date lastAccessedDate);

	/**
	 * Expressed in milliseconds.
	 */
	Long getMaxIdleTime();
	void setMaxIdleTime(Long maxIdleTime);

	@MaxLength(4000)
	String getEffectiveRoles();
	void setEffectiveRoles(String effectiveRoles);

	String getSessionType();
	void setSessionType(String sessionType);

	@MaxLength(1000)
	String getCreationInternetAddress();
	void setCreationInternetAddress(String creationInternetAddress);

	@MaxLength(1000)
	String getCreationNodeId();
	void setCreationNodeId(String creationNodeId);

	@MaxLength(4000)
	String getProperties();
	void setProperties(String properties);

}
