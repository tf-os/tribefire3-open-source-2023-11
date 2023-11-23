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
package com.braintribe.utils.genericmodel;

import java.util.Comparator;
import java.util.function.Function;

import com.braintribe.model.meta.GmProperty;

public class GmPropertyComparator implements Comparator<GmProperty>{
	
	private Function<GmProperty, Double> priorityResolver;
	private Function<GmProperty, Boolean> mandatoryResolver;

	public GmPropertyComparator() {
		
	}
	public GmPropertyComparator(Function<GmProperty, Double> priorityResolver) {
		this.priorityResolver = priorityResolver;
	}

	public GmPropertyComparator(Function<GmProperty, Double> priorityResolver, Function<GmProperty, Boolean> mandatoryResolver) {
		this.priorityResolver = priorityResolver;
		this.mandatoryResolver = mandatoryResolver;
	}

	public void setPriorityResolver(Function<GmProperty, Double> priorityResolver) {
		this.priorityResolver = priorityResolver;
	}
	
	public void setMandatoryResolver(Function<GmProperty, Boolean> mandatoryResolver) {
		this.mandatoryResolver = mandatoryResolver;
	}
	
	@Override
	public int compare(GmProperty p1, GmProperty p2) {
		if (p1 == p2) return 0;
	
		Double prio1 = priorityResolver.apply(p1);
		Double prio2 = priorityResolver.apply(p2);
		
		if (prio1 == null) {
			prio1 = 0d;
		}
		if (prio2 == null) {
			prio2 = 0d;
		}

		if (prio1.equals(prio2)) {
			
			// Same Priority:
			// Now, we prefer mandatory properties before optional ones (only if according resolver is given).
			// If that still doesn't decide we sort by name.
			
			if (mandatoryResolver != null) {
				Boolean mandatory1 = this.mandatoryResolver.apply(p1);
				Boolean mandatory2 = this.mandatoryResolver.apply(p2);
				
				if (mandatory1 != mandatory2) {
					return mandatory2.compareTo(mandatory1);
				}
			}
			
			return p1.getName().compareTo(p2.getName());
		}
		
		return prio2.compareTo(prio1); // we want order descending by priority. highest prio first. 
	}

}
