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
package com.braintribe.model.access.impls;

import java.util.Collection;

import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.smood.Smood;

public class ReportTestingAccess extends BasicAccessAdapter {

	private AdapterManipulationReport report;
	private Smood dataSource;

	public ReportTestingAccess(Smood dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	protected Collection<GenericEntity> loadPopulation() throws ModelAccessException {
		return dataSource.getEntitiesPerType(GMF.getTypeReflection().getEntityType(GenericEntity.class));
	}

	@Override
	protected void save(AdapterManipulationReport context) throws ModelAccessException {
		this.report = context; 
	}
	
	public AdapterManipulationReport getReport() {
		return report;
	}

}
