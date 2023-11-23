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
package tribefire.extension.audit.model;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${manipulationType} ${instanceType} ${instanceProperty}")
public interface ManipulationRecord extends GenericEntity {

	EntityType<ManipulationRecord> T = EntityTypes.T(ManipulationRecord.class);

	String user = "user";
	String date = "date";
	String manipulationType = "manipulationType";
	String instanceType = "instanceType";
	String instanceId = "instanceId";
	String instancePartition = "instancePartition";
	String instanceProperty = "instanceProperty";
	String value = "value";
	String previousValue = "previousValue";
	String overflowValue = "overflowValue";
	String overflowPreviousValue = "overflowPreviousValue";
	String transactionId = "transactionId";
	String sequenceNumber = "sequenceNumber";
	String userIpAddress = "userIpAddress";

	String getUser();
	void setUser(String user);

	Date getDate();
	void setDate(Date date);

	/** defines if an Entitiy is a preliminary instance or if the manipulation was persisted to the database */
	boolean getPreliminaryInstance();
	/** defines if an Entitiy is a preliminary instance or if the manipulation was persisted to the database */
	void setPreliminaryInstance(boolean arg);

	ManipulationType getManipulationType();
	void setManipulationType(ManipulationType manipulationType);

	String getInstanceType();
	void setInstanceType(String instanceType);

	String getInstanceId();
	void setInstanceId(String instanceId);
	
	String getInstancePartition();
	void setInstancePartition(String instancePartition);

	String getInstanceProperty();
	void setInstanceProperty(String propertyName);

	String getValue();
	void setValue(String value);
	
	String getPreviousValue();
	void setPreviousValue(String previousValue);
	
	String getOverflowValue();
	void setOverflowValue(String value);
	
	String getOverflowPreviousValue();
	void setOverflowPreviousValue(String value);
	
	String getTransactionId();
	void setTransactionId(String transactionId);

	long getSequenceNumber();
	void setSequenceNumber(long sequenceNumber);

	String getUserIpAddress();
	void setUserIpAddress(String userIpAddress);

	String getAccessId();
	void setAccessId(String accessId);
}