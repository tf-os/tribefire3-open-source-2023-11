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

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;

/**
 * Extension for the ColumnConfig which adds a max width for the column.
 * @author michel.docouto
 *
 */
public class ColumnConfigWithMaxWidth<M, N> extends ColumnConfig <M, N> {
	
	private int maxWidth;

	public ColumnConfigWithMaxWidth(ValueProvider<? super M, N> valueProvider, int width) {
		super(valueProvider, width);
	}
	
	/**
	 * Configures the maximum width for the column.
	 */
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}
	
	public int getMaxWidth() {
		return maxWidth;
	}

}
