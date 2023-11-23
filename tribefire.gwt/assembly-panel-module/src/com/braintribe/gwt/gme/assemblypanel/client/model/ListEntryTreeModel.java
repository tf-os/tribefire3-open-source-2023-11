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

import java.util.function.Function;

public class ListEntryTreeModel extends DelegatingTreeModel {
	
	private ListOrSetEntry listEntry;
	
	public ListEntryTreeModel(ObjectAndType objectAndType, Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory) {
		ObjectAndType subObjectAndType = new ObjectAndType();
		listEntry = (ListOrSetEntry)objectAndType.getObject();
		subObjectAndType.setObject(listEntry.getObject());
		subObjectAndType.setType(objectAndType.getType());
		subObjectAndType.setDepth(objectAndType.getDepth());
		delegate = modelFactory.apply(subObjectAndType);
	}
	
	@Override
	public <X> X get(String property) {
		return (X)delegate.get(property);
	}
	
	public int getListEntryIndex() {
		return listEntry.getIndex();
	}
	
	public void setListEntryIndex(int index) {
		listEntry.setIndex(index);
	}

}
