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
package com.braintribe.model.example;

import java.util.Date;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

@SelectiveInformation("${name} ${lastname}")

public interface Person extends StandardIdentifiable {

	EntityType<Person> T = EntityTypes.T(Person.class);

	String name = "name";
	String lastname = "lastname";
	String company = "company";
	String birthday = "birthday";
	String address = "address";
	String contractNumber = "contractNumber";
	String personalNumber = "personalNumber";
	String nationality = "nationality";
	String costLocation = "costLocation";
	String svnr = "svnr";
	String phoneNumber = "phoneNumber";
	String email = "email";
	String position = "position";
	String superior = "superior";
	String probationTime = "probationTime";
	String endOfProbationTime = "endOfProbationTime";
	String evaluation = "evaluation";
	String hiringDate = "hiringDate";
	String resignationDate = "resignationDate";
	String gender = "gender";
	String status = "status";
	String familyStatus = "familyStatus";
	String image = "image";
	String contract = "contract";
	String premium = "premium";
	String payslip = "payslip";

	void setImage(Resource image);
	Resource getImage();

	String getName();
	void setName(String name);

	Company getCompany();
	void setCompany(Company company);

	Address getAddress();
	void setAddress(Address address);

	String getLastname();
	void setLastname(String lastname);

	Date getBirthday();
	void setBirthday(Date birthday);

	String getContractNumber();
	void setContractNumber(String contractNumber);

	String getSvnr();
	void setSvnr(String svnr);

	Person getSuperior();
	void setSuperior(Person superior);

	Boolean getProbationTime();
	void setProbationTime(Boolean probationTime);

	Date getEndOfProbationTime();
	void setEndOfProbationTime(Date endOfProbationTime);

	Date getHiringDate();
	void setHiringDate(Date hiringDate);

	Date getResignationDate();
	void setResignationDate(Date resignationDate);

	Gender getGender();
	void setGender(Gender gender);

	FamilyStatus getFamilyStatus();
	void setFamilyStatus(FamilyStatus familyStatus);

	String getEvaluation();
	void setEvaluation(String evaluation);

	Status getStatus();
	void setStatus(Status status);

	void setPersonalNumber(String personalNumber);
	String getPersonalNumber();

	String getNationality();
	void setNationality(String nationality);

	String getCostLocation();
	void setCostLocation(String costLocation);

	void setContract(String contract);
	String getContract();

	void setPayslip(String payslip);
	String getPayslip();

	void setPremium(String premium);
	String getPremium();

	String getPhoneNumber();
	void setPhoneNumber(String phoneNumber);

	String getEmail();
	void setEmail(String email);

	String getPosition();
	void setPosition(String position);

}
