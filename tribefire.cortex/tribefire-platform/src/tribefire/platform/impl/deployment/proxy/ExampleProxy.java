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
package tribefire.platform.impl.deployment.proxy;

import com.braintribe.model.processing.deployment.api.ConfigurableDcProxyDelegation;
import com.braintribe.model.processing.deployment.api.DcProxy;

/**
 * @author peter.gazdik
 */
class ExampleProxy extends ExampleClass implements DcProxy {

	public final DcProxyDelegationImpl delegateManager = new DcProxyDelegationImpl();

	@Override
	public ConfigurableDcProxyDelegation $_delegatorAligator() {
		return delegateManager;
	}

	@Override
	public int foo() {
		return ((ExampleClass) delegateManager.getDelegate()).foo();
	}

}


class ExampleClass {
	
	int foo() {
		return 0;
	}
}