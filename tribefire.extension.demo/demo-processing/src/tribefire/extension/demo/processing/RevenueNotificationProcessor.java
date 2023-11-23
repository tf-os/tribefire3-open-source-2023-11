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
package tribefire.extension.demo.processing;

import java.math.BigDecimal;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Person;

public class RevenueNotificationProcessor implements StateChangeProcessor<Company, GenericEntity> {
	
	private BigDecimal minRevenue = new BigDecimal(1000000);
	private BigDecimal maxRevenue = new BigDecimal(10000000);
	
	public void setMinRevenue(BigDecimal minRevenue) {
		this.minRevenue = minRevenue;
	}
	
	public void setMaxRevenue(BigDecimal maxRevenue) {
		this.maxRevenue = maxRevenue;
	}
	
	@Override
	public StateChangeProcessorCapabilities getCapabilities() {
		return StateChangeProcessors.afterOnlyCapabilities();
	}
	
	@Override
	public void onAfterStateChange(AfterStateChangeContext<Company> context, GenericEntity arg1)
			throws StateChangeProcessorException {
		Company company = context.getProcessEntity();
		BigDecimal currentRevenue = company.getAverageRevenue();
		Person ceo = company.getCeo();
		
		if (currentRevenue == null)
			// ignore the state change if the averageRevenue property of the company is set to null
			return;	
		
		if (currentRevenue.compareTo(minRevenue) < 0) {
			// We fall below the limit
			ceo.getComments().add("Dear CEO! Your Company's revenue fall to: "+currentRevenue);
		} else if (currentRevenue.compareTo(maxRevenue) > 0) {
			ceo.getComments().add("Dear CEO! Your Company's revenue increased to: "+currentRevenue);
		} 
	}

}
