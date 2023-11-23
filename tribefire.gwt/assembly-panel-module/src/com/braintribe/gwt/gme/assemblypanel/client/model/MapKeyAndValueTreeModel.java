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
package com.braintribe.gwt.gme.assemblypanel.client.model;

import com.braintribe.model.generic.reflection.GenericModelType;

/**
 * This model represents the Entries for a Map.
 * @author michel.docouto
 *
 */
public class MapKeyAndValueTreeModel extends AbstractGenericTreeModel {
	
	private MapKeyOrValueEntryTreeModel mapKeyEntryTreeModel;
	private MapKeyOrValueEntryTreeModel mapValueEntryTreeModel;
	private int entryNumber;
	
	public MapKeyAndValueTreeModel(MapKeyOrValueEntryTreeModel mapKeyEntryTreeModel, MapKeyOrValueEntryTreeModel mapValueEntryTreeModel,
			int entryNumber) {
		add(mapKeyEntryTreeModel);
		add(mapValueEntryTreeModel);
		this.mapValueEntryTreeModel = mapValueEntryTreeModel;
		this.mapKeyEntryTreeModel = mapKeyEntryTreeModel;
		this.entryNumber = entryNumber;
	}

	@Override
	public AbstractGenericTreeModel getDelegate() {
		return this;
	}

	@Override
	public <X extends GenericModelType> X getElementType() {
		return mapValueEntryTreeModel.getElementType();
	}
	
	public GenericModelType getKeyElementType() {
		return mapKeyEntryTreeModel.getElementType();
	}
	
	public MapKeyOrValueEntryTreeModel getMapKeyEntryTreeModel() {
		return mapKeyEntryTreeModel;
	}
	
	public MapKeyOrValueEntryTreeModel getMapValueEntryTreeModel() {
		return mapValueEntryTreeModel;
	}
	
	public int getEntryNumber() {
		return entryNumber;
	}
	
	public void setEntryNumber(int entryNumber) {
		this.entryNumber = entryNumber;
	}

}
