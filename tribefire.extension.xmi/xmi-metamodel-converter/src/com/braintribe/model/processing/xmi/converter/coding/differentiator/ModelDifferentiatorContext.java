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
package com.braintribe.model.processing.xmi.converter.coding.differentiator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * the {@link ModelDifferentiatorContext} is used as collection-point of the different findings during differentiation
 * @author pit
 *
 */
public class ModelDifferentiatorContext {
	private boolean isDiffering;
	private List<DifferentiationReason> reasons = new ArrayList<>();
	private String accumulatedOldDifferentiations;
	private Date date = new Date();
	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	/**
	 * @return - true if the model has changed 
	 */
	public boolean isDiffering() {
		return isDiffering;
	}
	public void setDiffering(boolean isDiffering) {
		this.isDiffering = isDiffering;
	}

	/**
	 * @return - a {@link List} of {@link DifferentiationReason} why the model is changed
	 */
	public List<DifferentiationReason> getReasons() {
		return reasons;
	}
	public void setReasons(List<DifferentiationReason> reasons) {
		this.reasons = reasons;
	}
	
	/**
	 * @return - accumulated differnentations from the XMI
	 */
	public String getAccumulatedOldDifferentiations() {
		return accumulatedOldDifferentiations;
	}
	public void setAccumulatedOldDifferentiations(String accumulatedOldDifferentiations) {
		this.accumulatedOldDifferentiations = accumulatedOldDifferentiations;
	}
	
	/**
	 * @return - all accumulated {@link DifferentiationReason} collated into a {@link String}
	 */
	public String asString() {
		if (!isDiffering) {
			if (accumulatedOldDifferentiations == null) {
				return "";
			}
			else {
				return accumulatedOldDifferentiations;
			}
		}
		StringBuilder sb = new StringBuilder();
		// 
		sb.append( "changed detected on [" + date() + "]:");
		for (DifferentiationReason reason : reasons) {
			sb.append("\n");
			sb.append( reason.asString());
		}
		
		if (accumulatedOldDifferentiations != null) {
			sb.append("\n");
			sb.append( accumulatedOldDifferentiations);			
		}
		
		return sb.toString();
		
	}
	public String date() { 
		return format.format(date);
	}
	
	
	
	
}
