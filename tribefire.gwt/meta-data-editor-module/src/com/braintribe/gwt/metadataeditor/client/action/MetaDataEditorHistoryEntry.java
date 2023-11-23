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
package com.braintribe.gwt.metadataeditor.client.action;

import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.model.generic.path.ModelPath;
import com.google.gwt.user.client.ui.Widget;

public class MetaDataEditorHistoryEntry {
	
	public ModelPath modelPath;
	public Widget widget;
	public String tabType;
	public TetherBar tetherBar;
	public TetherBarElement tetherBarElement;
	public WorkWithEntityActionListener listener;
}
