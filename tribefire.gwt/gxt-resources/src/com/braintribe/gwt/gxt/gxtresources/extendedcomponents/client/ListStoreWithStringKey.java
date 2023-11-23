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
package com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;

/**
 * This extension of the ListStore uses a map for storing its data, together with the list. Useful for finding
 * operations.
 * 
 * @author michel.docouto
 */
public class ListStoreWithStringKey<M> extends ListStore<M> {

	private final Map<String, M> allItemsMap;
	private Map<String, M> visibleItemsMap;
	private boolean fireBulkAdd;

	public ListStoreWithStringKey(ModelKeyProvider<? super M> keyProvider) {
		this(keyProvider, false);
	}
	
	/**
	 * If fireBulkAdd is set to true (not default), then when using addAll, we fire only one event.
	 */
	public ListStoreWithStringKey(ModelKeyProvider<? super M> keyProvider, boolean fireBulkAdd) {
		super(keyProvider);

		visibleItemsMap = allItemsMap = new FastMap<>();
		this.fireBulkAdd = fireBulkAdd;
	}

	@Override
	public void add(int index, M item) {
		super.add(index, item);
		String key = getKeyProvider().getKey(item);
		allItemsMap.put(key, item);

		if (isFiltered() && !isFilteredOut(item))
			visibleItemsMap.put(key, item);
	}

	@Override
	public boolean addAll(int index, Collection<? extends M> items) {
		boolean result = super.addAll(index, items);

		boolean filterEnabled = isFiltered();

		for (M item : items) {
			String key = getKeyProvider().getKey(item);
			allItemsMap.put(key, item);
			if (filterEnabled && !isFilteredOut(item))
				visibleItemsMap.put(key, item);
		}

		return result;
	}

	@Override
	public void clear() {
		super.clear();
		allItemsMap.clear();
		visibleItemsMap.clear();
	}

	@Override
	public M findModelWithKey(String key) {
		return visibleItemsMap.get(key);
	}
	
	public M findModelWithKeyEvenIfFiltered(String key) {
		return allItemsMap.get(key);
	}

	@Override
	public M remove(int index) {
		M modelRemoved = super.remove(index);

		if (modelRemoved != null) {
			String key = getKeyProvider().getKey(modelRemoved);
			allItemsMap.remove(key);

			if (isFiltered())
				visibleItemsMap.remove(key);
		}

		return modelRemoved;
	}

	@Override
	public boolean remove(M model) {
		boolean result = super.remove(model);
		if (result)
			allItemsMap.remove(getKeyProvider().getKey(model));

		return result;
	}

	@Override
	public void replaceAll(List<? extends M> newItems) {
		super.replaceAll(newItems);
		allItemsMap.clear();
		visibleItemsMap.clear();

		boolean filterEnabled = isFiltered();

		for (M item : newItems) {
			String key = getKeyProvider().getKey(item);
			allItemsMap.put(key, item);
			if (filterEnabled && !isFilteredOut(item))
				visibleItemsMap.put(key, item);
		}
	}

	@Override
	public void update(M item) {
		String itemKey = getKeyProvider().getKey(item);
		
		int index = indexOf(item);
		M oldItem = get(index);
		
		assert oldItem != null && index != -1 : "Item does not belong to the store, cannot be updated";
		
		getAllItems().set(index, item);
		
		String oldItemKey = null;
		if (oldItem != null) {
			oldItemKey = getKeyProvider().getKey(oldItem);
			allItemsMap.remove(oldItemKey);
		}
		allItemsMap.put(itemKey, item);
		removeOldItem(oldItem);
		
		if (isFiltered() && !isFilteredOut(oldItem)) {
			getVisibleItems().set(indexOf(oldItem), item);
			if (oldItemKey != null)
				visibleItemsMap.remove(oldItemKey);
			visibleItemsMap.put(itemKey, item);
		}
		
		if (!isFiltered() || !isFilteredOut(oldItem))
			fireEvent(new StoreUpdateEvent<M>(Collections.singletonList(item)));
	}
	
	@Override
	protected void applyFilters() {
		super.applyFilters();
		
		visibleItemsMap = new FastMap<>();
		if (isFiltered()) {
			for (Map.Entry<String, M> entry : allItemsMap.entrySet()) {
				if (!isFilteredOut(entry.getValue()))
					visibleItemsMap.put(entry.getKey(), entry.getValue());
			}
		} else if (visibleItemsMap != allItemsMap)
			visibleItemsMap = allItemsMap;
	}
	
	/**
	 * This method was bringing a bad performance... it fires the add one by one, what was SUPER slow for grouping views.
	 * Now firing it all at once, if configured to to so.
	 */
	@Override
	protected void fireSortedAddEvents(Collection<? extends M> items) {
		if (fireBulkAdd)
			fireEvent(new StoreAddEvent<M>(Math.max(getAll().size() - items.size(), 0), new ArrayList<M>(items)));
		else
			super.fireSortedAddEvents(items);
	}
	
	private native List<M> getAllItems() /*-{
		return this.@com.sencha.gxt.data.shared.ListStore::allItems;
	}-*/;
	
	private native List<M> getVisibleItems() /*-{
		return this.@com.sencha.gxt.data.shared.ListStore::visibleItems;
	}-*/;
	
	private native void removeOldItem(M oldItem) /*-{
		this.@com.sencha.gxt.data.shared.Store::remove(*)(oldItem);
	}-*/;

}
