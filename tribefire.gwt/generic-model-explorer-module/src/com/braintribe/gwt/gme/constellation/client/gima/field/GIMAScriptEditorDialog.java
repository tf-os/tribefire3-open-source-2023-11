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
package com.braintribe.gwt.gme.constellation.client.gima.field;

import com.braintribe.gwt.aceeditor.client.GmScriptEditorDialog;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

public class GIMAScriptEditorDialog extends GmScriptEditorDialog {

	private GIMAScriptEditorView gimaView;
	private GIMADialog gimaDialog;
	
	@Override
	public void show() {
		AbstractGridEditing<?> parentGridEditing = getParentGridEditing();
		gimaDialog = parentGridEditing == null ? null : getParentGimaDialog(parentGridEditing.getEditableGrid());
		
		if (gimaDialog == null || !gimaDialog.isVisible()) {
			super.updateContainer();
			super.show();
			return;
		}

		//initSettings();
		gimaDialog.addEntityFieldView(this.getCaption(), prepareGIMAView(gimaDialog));
	}
	
	@Override
	public void applyChanges() {
		if (gimaDialog != null) {			
			applyChanges = true;
			fireEvent(new HideEvent());
		} else {
			super.applyChanges();
			super.hide();
		}
	}	
	
	@Override
	public void cancelChanges() {
		if (gimaDialog != null)
			cancelChanges = true;
		else
			super.cancelChanges();
	}

	private GIMAScriptEditorView prepareGIMAView(GIMADialog gimaDialog) {
		if (gimaView != null) {
			Widget widget = getView();
			if (widget.getParent() != gimaView)
				gimaView.add(getView());
			gimaView.configureGimaDialog(gimaDialog);
			return gimaView;
		}
		
		gimaView = new GIMAScriptEditorView(this, gimaDialog);
		return gimaView;
	}

	private GIMADialog getParentGimaDialog(Widget widget) {
		if (widget == null)
			return null;
		
		if (widget instanceof GIMADialog)
			return (GIMADialog) widget;
		
		return getParentGimaDialog(widget.getParent());
	}
		
	@Override
	public boolean canFireDialog() {
		AbstractGridEditing<?> parentGridEditing = getParentGridEditing();
		GIMADialog gimaDialog = parentGridEditing == null ? null : getParentGimaDialog(parentGridEditing.getEditableGrid());
		
		return (gimaDialog != null);
	}	
}
