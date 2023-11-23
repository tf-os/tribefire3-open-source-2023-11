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
package tribefire.cortex.check.processing;

import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.check.CheckProcessor;

/**
 * A context used to bundle contextualized check bundle information.
 */
public class CheckBundlesContext {
	public CheckProcessor processor;
	public CheckRequest request;
	public CheckBundle bundle;
	
	public CheckBundlesContext(CheckProcessor processor, CheckRequest request, CheckBundle bundle) {
		this.processor = processor;
		this.request = request;
		this.bundle = bundle;
	}
}
