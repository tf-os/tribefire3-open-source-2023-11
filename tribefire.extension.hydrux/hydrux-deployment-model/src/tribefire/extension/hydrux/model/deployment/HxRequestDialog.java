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
package tribefire.extension.hydrux.model.deployment;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This is an abstract supertype for an {@link HxView} that serves a very special purpose - evaluating a service request by asking a user for input
 * (in a form of a dialog window).
 * 
 * <h3>Simple example</h3>
 * 
 * Imagine we have a service request called FormatText, with a property "text" of type String. Typically, this would be evaluated on a server, where a
 * processor is registered which modifies the text, e.g. toUpperCase.
 * <p>
 * That's fine, but static. What if we wanted to ask the user what kind of effect we want to apply, e.g. toUpperCase, toLowerCase or say lIkE tHiS?
 * <p>
 * For this purpose we can create a view ({@code FormatTextDialog extends HxRequestDialog}) that shows three buttons for these three options plus a
 * "Cancel" button.
 * <p>
 * We then bind a special service-processor for our FormatText request like this: {@code bindDialogProcessor(FormatText.T, formatTextDialogInstance)}
 * <p>
 * Note this bind method is available in hydrux api on {@code IHxModuleBindingContext.serviceProcessorBinder}.
 * <p>
 * This bind method creates a special service-processor internally which
 * 
 * @author peter.gazdik
 */
@Abstract
public interface HxRequestDialog extends HxView {

	EntityType<HxRequestDialog> T = EntityTypes.T(HxRequestDialog.class);

	String getTitle();
	void setTitle(String title);

	HxWindowCustomizability getWindowCustomizability();
	void setWindowCustomizability(HxWindowCustomizability windowCustomizability);

	/**
	 * If true, the element is rendered as a <a href="https://en.wikipedia.org/wiki/Modal_window">modal window</a>, i.e. it blocks any interaction
	 * with the rest of the application.
	 */
	boolean getModal();
	void setModal(boolean modal);

}
