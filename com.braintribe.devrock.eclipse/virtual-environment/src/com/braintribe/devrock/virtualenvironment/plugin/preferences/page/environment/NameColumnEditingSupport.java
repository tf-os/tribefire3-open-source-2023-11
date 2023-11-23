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
package com.braintribe.devrock.virtualenvironment.plugin.preferences.page.environment;

import org.eclipse.jface.viewers.TableViewer;

import com.braintribe.model.malaclypse.cfg.preferences.ve.EnvironmentOverride;

public class NameColumnEditingSupport extends AbstractOverrideColumnEditingSupport {

	public NameColumnEditingSupport(TableViewer viewer) {
		super(viewer);	
	}
	
	@Override
	protected Object getValue(Object element) {
		EnvironmentOverride override = (EnvironmentOverride) element;
		return override.getName();
	}

	@Override
	protected void setValue(Object element, Object value) {
		EnvironmentOverride override = (EnvironmentOverride) element;
		override.setName( (String) value);
		refresh(element);
	}

}
