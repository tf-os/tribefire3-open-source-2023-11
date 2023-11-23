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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.function.BiConsumer;

import com.braintribe.gwt.gmview.client.EditEntityActionListener;
import com.braintribe.model.generic.path.ModelPath;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;

/**
 * Handler responsible for firing the {@link EditEntityActionListener} for handling opening a GIMA.
 * @author michel.docouto
 *
 */
public class GIMASpecialViewHandler implements BiConsumer<Widget, ModelPath> {

	@Override
	public void accept(Widget currentView, ModelPath modelPath) {
		Scheduler.get().scheduleDeferred(() -> {
			EditEntityActionListener listener = getEditEntityActionListener(currentView);
			if (listener != null)
				listener.onEditEntity(modelPath);
		});
	}
	
	private EditEntityActionListener getEditEntityActionListener(Widget widget) {
		if (widget instanceof EditEntityActionListener)
			return (EditEntityActionListener) widget;
		else if (widget.getParent() != null)
			return getEditEntityActionListener(widget.getParent());
		
		return null;
	}

}
