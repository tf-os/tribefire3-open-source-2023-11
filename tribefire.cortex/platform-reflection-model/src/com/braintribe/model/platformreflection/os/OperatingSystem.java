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
package com.braintribe.model.platformreflection.os;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface OperatingSystem extends GenericEntity {

	EntityType<OperatingSystem> T = EntityTypes.T(OperatingSystem.class);

	String getFamily();
	void setFamily(String family);

	String getManufacturer();
	void setManufacturer(String manufacturer);

	Integer getProcessCount();
	void setProcessCount(Integer processCount);

	int getThreadCount();
	void setThreadCount(int threadCount);

	int getBitness();
	void setBitness(int bitness);

	String getVersion();
	void setVersion(String version);

	String getCodeName();
	void setCodeName(String codeName);

	String getBuildNumber();
	void setBuildNumber(String buildNumber);

	String getArchitecture();
	void setArchitecture(String architecture);

	String getHostSystem();
	void setHostSystem(String hostSystem);

	Date getSystemTime();
	void setSystemTime(Date systemTime);

	String getSystemTimeAsString();
	void setSystemTimeAsString(String systemTimeAsString);

	Locale getDefaultLocale();
	void setDefaultLocale(Locale defaultLocale);

	int getNumberOfAvailableLocales();
	void setNumberOfAvailableLocales(int numberOfAvailableLocales);
}
