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

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CpuLoad extends GenericEntity {

	EntityType<CpuLoad> T = EntityTypes.T(CpuLoad.class);

	Long getContextSwitches();
	void setContextSwitches(Long contextSwitches);
	
	Long getInterrupts();
	void setInterrupts(Long interrupts);
	
	Double getSystemCpuLoad();
	void setSystemCpuLoad(Double systemCpuLoad);

	Double getSystemLoadAverage1Minute();
	void setSystemLoadAverage1Minute(Double systemLoadAverage1Minute);

	Double getSystemLoadAverage5Minutes();
	void setSystemLoadAverage5Minutes(Double systemLoadAverage5Minutes);

	Double getSystemLoadAverage15Minutes();
	void setSystemLoadAverage15Minutes(Double systemLoadAverage15Minutes);

	List<Double> getSystemLoadPerProcessor();
	void setSystemLoadPerProcessor(List<Double> systemLoadPerProcessor);
	
	Double getUser();
	void setUser(Double user);

	Double getNice();
	void setNice(Double nice);

	Double getSys();
	void setSys(Double sys);

	Double getIdle();
	void setIdle(Double idle);

	Double getIoWait();
	void setIoWait(Double ioWait);

	Double getIrq();
	void setIrq(Double irq);

	Double getSoftIrq();
	void setSoftIrq(Double softIrq);

	Double getSteal();
	void setSteal(Double steal);

	Double getTotalCpu();
	void setTotalCpu(Double totalCpu);

}
