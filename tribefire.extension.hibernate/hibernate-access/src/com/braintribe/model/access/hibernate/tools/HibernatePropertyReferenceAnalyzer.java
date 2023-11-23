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
package com.braintribe.model.access.hibernate.tools;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.findrefs.meta.BasicPropertyReferenceAnalyzer;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * @author peter.gazdik
 */
public class HibernatePropertyReferenceAnalyzer extends BasicPropertyReferenceAnalyzer {

	private final HibernateMappingInfoProvider mpip;

	public HibernatePropertyReferenceAnalyzer(ModelOracle modelOracle, HibernateMappingInfoProvider mpip) {
		super(modelOracle, false);

		this.mpip = mpip;
		this.initialize();
	}

	@Override
	protected boolean ignoreEntity(GmEntityType entityType) {
		return !mpip.isEntityMapped(entityType);
	}

	@Override
	protected boolean ignoreProperty(GmEntityType actualOwner, GmProperty property) {
		return !mpip.isPropertyMapped(actualOwner.getTypeSignature(), property.getName());
	}

}
