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
package com.braintribe.gwt.gme.constellation.client.gima;

 import com.sencha.gxt.widget.core.client.button.TextButton;

 /**
 * Configuration for {@link GIMAView} buttons position.
 * @author michel.docouto
 *
 */
public class ButtonConfiguration {

 	private TextButton button;
	private boolean afterCancel;

 	public ButtonConfiguration(TextButton button) {
		this.button = button;
	}

 	/**
	 * @param afterCancel - Configures whether the button should be placed after the cancel button. Defaults to false.
	 */
	public ButtonConfiguration(TextButton button, boolean afterCancel) {
		this.button = button;
		this.afterCancel = afterCancel;
	}

 	public TextButton getButton() {
		return button;
	}

 	public void setButton(TextButton button) {
		this.button = button;
	}

 	public boolean isAfterCancel() {
		return afterCancel;
	}

 	public void setAfterCancel(boolean afterCancel) {
		this.afterCancel = afterCancel;
	}
}