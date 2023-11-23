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
package tribefire.extension.demo.model.data.process;

import java.util.Date;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.process.Process;

import tribefire.extension.demo.model.data.Person;

public interface HolidayRequestProcess extends Process {
	
	EntityType<HolidayRequestProcess> T = EntityTypes.T(HolidayRequestProcess.class);
	
	
	/*
	 * Constants for each property name.
	 */
	public static final String employee = "employee";
	public static final String from = "from";
	public static final String to = "to";
	public static final String comment = "comment";
	public static final String approvalStatus = "approvalStatus";
	public static final String assignee = "assignee";
	
	@Mandatory
	Person getEmployee();
	void setEmployee(Person employee);
	
	@Mandatory
	Date getFrom();
	void setFrom(Date from);
	
	@Mandatory
	Date getTo();
	void setTo(Date to);
	
	String getComment();
	void setComment(String comment);
	
	String getApprovalStatus();
	void setApprovalStatus(String approvalStatus);
	
	Person getAssignee();
	void setAssignee(Person assignee);
	
	
}
