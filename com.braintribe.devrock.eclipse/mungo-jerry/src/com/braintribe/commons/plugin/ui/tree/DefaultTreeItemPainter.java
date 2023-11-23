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
package com.braintribe.commons.plugin.ui.tree;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public class DefaultTreeItemPainter implements TreeItemPainter {

	private String [] keys;
	
	@Configurable @Required
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	@Override
	public void paint(TreeItem newItem, TreeItem oldItem) {
		for (String key : keys) {
			Object value = oldItem.getData(key);
			if (value == null)
				continue;
			newItem.setData(key, value);
			
			if (value instanceof Color) {
				newItem.setForeground((Color) value);
				continue;
			}
			if (value instanceof Image) {
				newItem.setImage( (Image) value);
			}
			if (value instanceof Font) {
				newItem.setFont((Font) value);
			}			
		}
	}

}
