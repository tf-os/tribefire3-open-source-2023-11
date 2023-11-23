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
package com.braintribe.gwt.gme.assemblypanel.client;

import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.TreePropertyModel;
import com.braintribe.model.generic.reflection.Property;
import com.sencha.gxt.core.client.ValueProvider;

public class PropertyTreeModelValueProvider implements ValueProvider<AbstractGenericTreeModel, Object> {
	private final Property property;

	public PropertyTreeModelValueProvider(Property property) {
		this.property = property;
	}

	@Override
	public Object getValue(AbstractGenericTreeModel model) {
		TreePropertyModel treePropertyModel = getTreePropertyModel(model);
		return treePropertyModel == null ? null : treePropertyModel.getValue();
	}

	@Override
	public void setValue(AbstractGenericTreeModel model, Object value) {
		TreePropertyModel treePropertyModel = getTreePropertyModel(model);
		if (treePropertyModel != null)
			treePropertyModel.setValue(value);
	}

	@Override
	public String getPath() {
		return property.getName();
	}
	
	public Property getProperty() {
		return property;
	}
	
	private TreePropertyModel getTreePropertyModel(AbstractGenericTreeModel model) {
		return model.getDelegate().getTreePropertyModel(property);
	}
	
}
