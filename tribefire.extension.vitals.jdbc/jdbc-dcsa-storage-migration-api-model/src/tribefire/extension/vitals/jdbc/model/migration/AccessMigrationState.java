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
package tribefire.extension.vitals.jdbc.model.migration;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
@SelectiveInformation("${accessId} Migration")
public interface AccessMigrationState extends GenericEntity {

	EntityType<AccessMigrationState> T = EntityTypes.T(AccessMigrationState.class);

	String getAccessId();
	void setAccessId(String accessId);

	Date getStart();
	void setStart(Date start);

	Date getEnd();
	void setEnd(Date end);

	int getOpsDone();
	void setOpsDone(int opsDone);

	int getOpsTotal();
	void setOpsTotal(int opsTotal);

	int getResDone();
	void setResDone(int resDone);

	long getResSizeDone();
	void setResSizeDone(long resSizeDone);

	String getTimeTotal();
	void setTimeTotal(String timeTotal);

	String getTimeOpsDownload();
	void setTimeOpsDownload(String timeOpsDownload);

	String getTimeResDownload();
	void setTimeResDownload(String timeResDownload);

}
