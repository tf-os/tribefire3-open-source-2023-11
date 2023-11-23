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
package tribefire.extension.hydrux.demo.model.ux.deployment;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.hydrux.demo.model.ux.deployment.client.HxDemoChooseTextProcessingMethodRequestDialog;
import tribefire.extension.hydrux.model.deployment.HxRequestDialog;
import tribefire.extension.hydrux.model.deployment.HxView;

/**
 * Demo for a service-request which is evaluated locally, and the evaluation involves asking a user for input via {@link HxRequestDialog}.
 * <p>
 * The request is <tt>HxDemoProcessText</tt> (from <tt>hx-demo-api-model</tt>).<br>
 * the dialog is {@link HxDemoChooseTextProcessingMethodRequestDialog}
 * <p>
 * This view is bound as a normal view of course, and contains an input and a button. When the button is clicked, a <code>HxDemoProcessText</code> is
 * evaluated on the local evaluator. For this requests we use
 * <code>context.serviceProcessorBinder().bindDialogProcessor(HxDemoProcessText, dialog)</code>, where <code>dialog</code> is an instance of
 * {@link HxDemoChooseTextProcessingMethodRequestDialog}.
 * <p>
 * 
 * @author peter.gazdik
 */
public interface HxDemoModalDialogView extends HxView {

	EntityType<HxDemoModalDialogView> T = EntityTypes.T(HxDemoModalDialogView.class);

}
