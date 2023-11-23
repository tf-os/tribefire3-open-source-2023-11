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
package com.braintribe.model.platformreflection.check.cpu;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Cpu extends GenericEntity {

	EntityType<Cpu> T = EntityTypes.T(Cpu.class);

	String getFamily();
	void setFamily(String family);

	String getIdentifier();
	void setIdentifier(String identifier);

	String getModel();
	void setModel(String model);

	String getName();
	void setName(String name);

	int getLogicalProcessorCount();
	void setLogicalProcessorCount(int logicalProcessorCount);

	int getPhysicalProcessorCount();
	void setPhysicalProcessorCount(int physicalProcessorCount);

	CpuLoad getCpuLoad();
	void setCpuLoad(CpuLoad cpuLoad);
	
	String getSystemSerialNumber();
	void setSystemSerialNumber(String systemSerialNumber);

	Date getSystemBootTime();
	void setSystemBootTime(Date systemBootTime);

	String getVendor();
	void setVendor(String vendor);

	long getVendorFreq();
	void setVendorFreq(long vendorFreq);

	Double getVendorFreqInGh();
	void setVendorFreqInGh(Double vendorFreqInGh);

	long getMaxFreq();
	void setMaxFreq(long maxFreq);

	Double getMaxFreqInGh();
	void setMaxFreqInGh(Double maxFreqInGh);

	List<String> getCurrentFrequencies();
	void setCurrentFrequencies(List<String> currentFrequencies);

	String getProcessorId();
	void setProcessorId(String processorId);

	boolean getCpu64bit();
	void setCpu64bit(boolean cpu64bit);

	double getCpuTemperature();
	void setCpuTemperature(double cpuTemperature);

	double getCpuVoltage();
	void setCpuVoltage(double cpuVoltage);

	List<Integer> getFanSpeeds();
	void setFanSpeeds(List<Integer> fanSpeeds);
}
