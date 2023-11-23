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

import java.util.Comparator;

import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;

public class PriorityColumnConfig {
	private static Comparator<PriorityColumnConfig> comparator;
	
	private Double priority;
	private ColumnConfig<AbstractGenericTreeModel, ?> columnConfig;
	
	public PriorityColumnConfig(Double priority, ColumnConfig<AbstractGenericTreeModel, ?> columnConfig) {
		this.priority = priority;
		this.columnConfig = columnConfig;
	}
	
	public Double getPriority() {
		return priority;
	}
	
	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	public ColumnConfig<AbstractGenericTreeModel, ?> getColumnConfig() {
		return columnConfig;
	}
	
	public void setColumnConfig(ColumnConfig<AbstractGenericTreeModel, ?> columnConfig) {
		this.columnConfig = columnConfig;
	}
	
	public static Comparator<PriorityColumnConfig> getPriorityComparator() {
		if (comparator != null)
			return comparator;
		
		comparator = (o1, o2) -> {
			int priorityComparison;
			if (o1.getPriority() == null && o2.getPriority() == null)
				priorityComparison = 0;
			else if (o1.getPriority() == null && o2.getPriority() != null)
				priorityComparison = 1;
			else if (o1.getPriority() != null && o2.getPriority() == null)
				priorityComparison = -1;
			else {
				priorityComparison = o2.getPriority().compareTo(o1.getPriority());
			}
			if (priorityComparison == 0)
				return o1.getColumnConfig().getHeader().asString().compareToIgnoreCase(o2.getColumnConfig().getHeader().asString());
			return priorityComparison;
		};
		
		return comparator;
	}

}
