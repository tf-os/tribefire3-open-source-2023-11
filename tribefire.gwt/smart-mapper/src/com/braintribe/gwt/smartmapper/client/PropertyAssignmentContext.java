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
package com.braintribe.gwt.smartmapper.client;

import java.util.function.Supplier;

import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class PropertyAssignmentContext {

	public GmEntityTypeInfo entityType;
	public GmEntityType mappedToEntityType;
	
	public String propertyName;
	public GmPropertyInfo parentProperty;
	public GenericEntity parentEntity;
	
	public PersistenceGmSession session;
	public Supplier<SpotlightPanel> spotlightPanelProvider;
	public SmartMapper smartMapper;
	
	public boolean inherited = false;
	
}
