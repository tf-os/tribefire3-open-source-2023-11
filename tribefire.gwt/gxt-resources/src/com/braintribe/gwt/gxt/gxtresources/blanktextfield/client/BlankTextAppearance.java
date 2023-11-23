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
package com.braintribe.gwt.gxt.gxtresources.blanktextfield.client;

import com.google.gwt.core.shared.GWT;
import com.sencha.gxt.theme.base.client.field.TextFieldDefaultAppearance;

public class BlankTextAppearance extends TextFieldDefaultAppearance {
	
	public interface BlankTextResource extends TextFieldResources {
		//@Source({"com/sencha/gxt/theme/base/client/field/ValueBaseField.gss", "com/sencha/gxt/theme/base/client/field/TextField.gss", "blankText.gss"})
		@Override
		@Source({"blankText.gss"})
		BlankTextStyle css();
	}
	
	public interface BlankTextStyle extends TextFieldStyle {
		//NOP
	}
	
	public BlankTextAppearance() {
		super(GWT.<BlankTextResource>create(BlankTextResource.class));
	}

}
