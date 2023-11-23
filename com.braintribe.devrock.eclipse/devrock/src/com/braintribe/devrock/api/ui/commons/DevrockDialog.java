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
package com.braintribe.devrock.api.ui.commons;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;

import com.braintribe.devrock.plugin.DevrockPlugin;

/**
 * @author peter.gazdik
 */
public abstract class DevrockDialog extends Dialog {

	public DevrockDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	public DevrockDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected final Point getInitialSize() {
		Rectangle shellBounds = DevrockPlugin.instance().uiSupport().getCustomValue(getWindowSizeKey());
		return shellBounds == null ? getDrInitialSize() : new Point(shellBounds.width, shellBounds.height);
	}

	protected abstract Point getDrInitialSize();

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.addListener(SWT.Resize, e -> DevrockPlugin.instance().uiSupport().setCustomValue(getWindowSizeKey(), newShell.getBounds()));
	}

	protected void makeColumnResizableAndSticky(TreeColumn column, String text, int defaultWidth) {
		String key = getWindowSizeKey() + ":" + text;
		UiSupport uiSupport = DevrockPlugin.instance().uiSupport();

		Integer width = uiSupport.getCustomValue(key);

        column.setText(text);
		column.setWidth(width == null ? defaultWidth : width);
		column.addListener(SWT.Resize, e -> uiSupport.setCustomValue(key, column.getWidth()));
        column.setResizable(true);

	}

	protected String getWindowSizeKey() {
		return getClass().getName();
	}

}
