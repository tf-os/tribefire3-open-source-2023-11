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
package com.braintribe.gwt.gmview.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;

public class ViewSituationResolver<T extends ViewContext> {
	
	private GmExpertRegistry gmExpertRegistry;
	private Map<ViewSituationSelector, T> viewSituationSelectorMap;
	private Map<ViewSituationSelector, Function<ModelPathElement, List<T>>> viewSituationSelectorSupplierMap;
	private Comparator<ViewSituationSelector> selectorComparator;
	private boolean priorityReverse = false;
	private MappedSituationSelectorListener mappedSituationSelectorListener;
	
	@Required
	public void setGmExpertRegistry(GmExpertRegistry gmExpertRegistry) {
		this.gmExpertRegistry = gmExpertRegistry;
	}
	
	@Required
	public void setViewSituationSelectorMap(Map<ViewSituationSelector, T> viewSituationSelectorMap) {
		this.viewSituationSelectorMap = viewSituationSelectorMap;
	}
	
	@Configurable
	public void setViewSituationSelectorSupplierMap(Map<ViewSituationSelector, Function<ModelPathElement, List<T>>> viewSituationSelectorSupplierMap) {
		this.viewSituationSelectorSupplierMap = viewSituationSelectorSupplierMap;
	}
	
	/**
	 * Configures whether the priority meta data should be used in reverse order (smaller priorities appear first).
	 * Defaults to false.
	 */
	@Configurable
	public void setPriorityReverse(boolean priorityReverse) {
		this.priorityReverse = priorityReverse;
	}
	
	public List<T> getPossibleContentViews(ModelPathElement modelPathElement) {
		return getPossibleContentViews(modelPathElement, false);
	}
	
	public void setMappedSituationSelectorListener(MappedSituationSelectorListener mappedSituationSelectorListener) {
		this.mappedSituationSelectorListener = mappedSituationSelectorListener;
	}
	
	public List<T> getPossibleContentViews(ModelPathElement modelPathElement, boolean preparingForListView) {
		List<ViewSituationSelector> selectors = new ArrayList<>();
		ViewSelectorExpert<MetaDataSelector> expert = gmExpertRegistry.getExpert(ViewSelectorExpert.class).forType(MetaDataSelector.class);
		for (ViewSituationSelector selector : viewSituationSelectorMap.keySet()) {
			if (expert.doesSelectorApply(selector, modelPathElement))
				selectors.add(selector);
		}
		Collections.sort(selectors, getSelectorComparator());
		List<T> providers = new ArrayList<>();
		for (ViewSituationSelector selector : selectors) {
			T context = viewSituationSelectorMap.get(selector);
			context.setModelPathElement(modelPathElement);
			if (!preparingForListView || context.isListView())
				providers.add(context);
		}
		
		boolean usingMapped = false;
		boolean hideDefaultView = true;
		if (viewSituationSelectorSupplierMap != null) {
			List<ViewSituationSelector> supplierSelectors = new ArrayList<>();
			for (ViewSituationSelector selector : viewSituationSelectorSupplierMap.keySet()) {
				if (expert.doesSelectorApply(selector, modelPathElement))
					supplierSelectors.add(selector);
			}
			
			Collections.sort(supplierSelectors, getSelectorComparator());
			
			for (ViewSituationSelector selector : supplierSelectors) {
				List<T> contexts = viewSituationSelectorSupplierMap.get(selector).apply(modelPathElement);
				for (T context : contexts) {
					context.setModelPathElement(modelPathElement);
					if (!preparingForListView || context.isListView())
						providers.add(context);
					
					if (!context.isHideDefaultView())
						hideDefaultView = false;
				}
				
				usingMapped = true;
			}
		}
		
		fireMappedSituationSelectorListener(usingMapped && hideDefaultView);
		
		return providers;
	}
	
	private Comparator<ViewSituationSelector> getSelectorComparator() {
		if (selectorComparator != null)
			return selectorComparator;
		
		selectorComparator = (o1, o2) -> {
			if (o1.getConflictPriority() != null && o1.getConflictPriority() != null) {					
				if (priorityReverse)
					return o1.getConflictPriority().compareTo(o2.getConflictPriority());
				else
					return o2.getConflictPriority().compareTo(o1.getConflictPriority());
			}
			
			return 0;
		};
		
		return selectorComparator;
	}
	
	private void fireMappedSituationSelectorListener(boolean using) {
		if (mappedSituationSelectorListener != null)
			mappedSituationSelectorListener.setUsingMappedSituationSelectorListener(using);
	}
	
	public interface MappedSituationSelectorListener {
		public void setUsingMappedSituationSelectorListener(boolean using);
	}

}
