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
package com.braintribe.gwt.modeller.client.history;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.model.processing.modellergraph.GmModellerMode;

public class GmModellerHistory {
	
	private GmModeller modeller;
	private List<GmModellerHistoryListener> listeners = new ArrayList<>();
	
	List<GmModellerHistoryEntry> history = new ArrayList<>();
	int index = -1;
	
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	public void addListener(GmModellerHistoryListener listener) {
		listeners.add(listener);
	}
	
	public void fireOnHistoryChanged() {
		listeners.forEach(listener -> {
			listener.onHistoryChanged(this);
		});
	}
	
	public void add(String type) {
		if(canAdd(type)) {
			GmModellerHistoryEntry entry = new GmModellerHistoryEntry();
			entry.type = type;
			entry.mode = GmModellerMode.condensed;
			if(index > -1) 
				history = history.subList(0, index+1);
			history.add(entry);
			index++;
			fireOnHistoryChanged();
		}
//		else
//			index = history.size() - 1;
	}
	
	public void add(String fromType, String toType) {
		if(canAdd(fromType, toType)) {
			GmModellerHistoryEntry entry = new GmModellerHistoryEntry();
			entry.type = fromType;
			entry.type2 = toType;
			entry.mode = GmModellerMode.detailed;
			if(index > -1) 
				history = history.subList(0, index+1);
			history.add(entry);
			
		}
		if(index + 1 < history.size())
			index++;
		fireOnHistoryChanged();
//		else
//			index = history.size() - 1;
		
	}
	
	private boolean canAdd(String type) {
		if(history.size() > 0) {
			GmModellerHistoryEntry entry = history.get(history.size()-1);
			if(entry.mode == GmModellerMode.condensed)
				return !entry.type.equalsIgnoreCase(type);
			else
				return true;
		}
		return true;
	}
	
	private boolean canAdd(String fromType, String toType) {
		if(history.size() > 0) {
			GmModellerHistoryEntry entry = history.get(history.size()-1);
			if(entry.mode == GmModellerMode.detailed)
				return !(entry.type.equalsIgnoreCase(fromType) && entry.type2.equalsIgnoreCase(toType)) || (entry.type2.equalsIgnoreCase(fromType) && entry.type.equalsIgnoreCase(toType));
			else 
				return true;
		}
		return true;
	}
	
	public void next() {
		if(hasNext()) {
			index += 1;
			GmModellerHistoryEntry entry = history.get(index);
			if(entry.mode == GmModellerMode.condensed)
				modeller.focus(entry.type, false);
			else if(entry.mode == GmModellerMode.detailed)
				modeller.detail(entry.type, entry.type2, false);			
		}
		fireOnHistoryChanged();
	}
	
	public void previous(boolean remove) {
		if(hasPrevious()) {
			index -= 1;
			GmModellerHistoryEntry entry = history.get(index);
			
			if(remove) {
				history = history.subList(0, index+1);
			}
			
			if(entry.mode == GmModellerMode.condensed)
				modeller.focus(entry.type, false);
			else if(entry.mode == GmModellerMode.detailed)
				modeller.detail(entry.type, entry.type2, false);			
		}
		fireOnHistoryChanged();
	}
	
	public boolean hasNext() {
		return index + 1 < history.size();
	}
	
	public boolean hasPrevious() {
		return index - 1 >= 0 && history.size() >= (index - 1);
	}

}
