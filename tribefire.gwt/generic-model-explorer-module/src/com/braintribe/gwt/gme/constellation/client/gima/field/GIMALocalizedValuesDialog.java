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

import com.braintribe.gwt.genericmodelgxtsupport.client.field.LocalizedValuesDialog;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

/**
 * Extension of the {@link LocalizedValuesDialog} prepared for being used within a {@link GIMADialog}.
 * @author michel.docouto
 *
 */
public class GIMALocalizedValuesDialog extends LocalizedValuesDialog {
	
	private GIMALocalizedValuesView gimaView;
	private GIMADialog gimaDialog;
	
	@Override
	public void show() {
		AbstractGridEditing<?> parentGridEditing = getParentGridEditing();
		gimaDialog = parentGridEditing == null ? null : getParentGimaDialog(parentGridEditing.getEditableGrid());
		
		if (gimaDialog == null || !gimaDialog.isVisible()) {
			addToolBar();
			super.show();
			return;
		}
		
		removeToolBar();
		
		gimaDialog.addEntityFieldView(LocalizedText.INSTANCE.localization(), prepareGIMAView(gimaDialog));
		gridView.refresh(true);
	}
	
	@Override
	public void applyChanges() {
		if (gimaDialog != null)
			fireEvent(new HideEvent());
		else
			GIMALocalizedValuesDialog.super.applyChanges();
	}
	
	@Override
	public void cancelChanges() {
		if (gimaDialog != null)
			cancelChanges = true;
		else
			GIMALocalizedValuesDialog.super.cancelChanges();
	}

	private GIMALocalizedValuesView prepareGIMAView(GIMADialog gimaDialog) {
		if (gimaView != null) {
			Widget widget = getView();
			if (widget.getParent() != gimaView)
				gimaView.add(getView());
			gimaView.configureGimaDialog(gimaDialog);
			return gimaView;
		}
		
		gimaView = new GIMALocalizedValuesView(this, gimaDialog);
		return gimaView;
	}

	private GIMADialog getParentGimaDialog(Widget widget) {
		if (widget == null)
			return null;
		
		if (widget instanceof GIMADialog)
			return (GIMADialog) widget;
		
		return getParentGimaDialog(widget.getParent());
	}

}
