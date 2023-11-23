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
package tribefire.extension.okta.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface OktaUserProfile  extends GenericEntity {
	
	EntityType<OktaUserProfile> T = EntityTypes.T(OktaUserProfile.class);
	
	String login = "login";
	String email = "email";
	String secondEmail = "secondEmail";
	String firstName = "firstName";
	String lastName = "lastName";
	String middleName = "middleName";
	String honorificPrefix = "honorificPrefix";
	String honorificSuffix = "honorificSuffix";
	String title = "title";
	String displayName = "displayName";
	String nickName = "nickName";
	String profileUrl = "profileUrl";
	String primaryPhone = "primaryPhone";
	String mobilePhone = "mobilePhone";
	String streetAddress = "streetAddress";
	String city = "city";
	String state = "state";
	String zipCode = "zipCode";
	String countryCode = "countryCode";
	String postalAddress = "postalAddress";
	String preferredLanguage = "preferredLanguage";
	String locale = "locale";
	String timezone = "timezone";
	String userType = "userType";
	String employeeNumber = "employeeNumber";
	String costCenter = "costCenter";
	String organization = "organization";
	String division = "division";
	String department = "department";
	String managerId = "managerId";
	String manager = "manager";
	
	String getLogin();
	void setLogin(String login);
	String getEmail();
	void setEmail(String email);
	String getSecondEmail();
	void setSecondEmail(String secondEmail);
	String getFirstName();
	void setFirstName(String firstName);
	String getLastName();
	void setLastName(String lastName);
	String getMiddleName();
	void setMiddleName(String middleName);
	String getHonorificPrefix();
	void setHonorificPrefix(String honorificPrefix);
	String getHonorificSuffix();
	void setHonorificSuffix(String honorificSuffix);
	String getTitle();
	void setTitle(String title);
	String getDisplayName();
	void setDisplayName(String displayName);
	String getNickName();
	void setNickName(String nickName);
	String getProfileUrl();
	void setProfileUrl(String profileUrl);
	String getPrimaryPhone();
	void setPrimaryPhone(String primaryPhone);
	String getMobilePhone();
	void setMobilePhone(String mobilePhone);
	String getStreetAddress();
	void setStreetAddress(String streetAddress);
	String getCity();
	void setCity(String city);
	String getState();
	void setState(String state);
	String getZipCode();
	void setZipCode(String zipCode);
	String getCountryCode();
	void setCountryCode(String countryCode);
	String getPostalAddress();
	void setPostalAddress(String postalAddress);
	String getPreferredLanguage();
	void setPreferredLanguage(String preferredLanguage);
	String getLocale();
	void setLocale(String locale);
	String getTimezone();
	void setTimezone(String timezone);
	String getUserType();
	void setUserType(String userType);
	String getEmployeeNumber();
	void setEmployeeNumber(String employeeNumber);
	String getCostCenter();
	void setCostCenter(String costCenter);
	String getOrganization();
	void setOrganization(String organization);
	String getDivision();
	void setDivision(String division);
	String getDepartment();
	void setDepartment(String department);
	String getManagerId();
	void setManagerId(String managerId);
	String getManager();
	void setManager(String manager);
	
	
}