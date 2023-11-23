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
package tribefire.extension.tutorial.initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.tutorial.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.tutorial.initializer.wire.contract.TutorialInitializerContract;
import tribefire.extension.tutorial.initializer.wire.contract.TutorialInitializerMainContract;
import tribefire.extension.tutorial.initializer.wire.contract.TutorialInitializerModelsContract;

@Managed
public class TutorialInitializerMainSpace implements TutorialInitializerMainContract {

	@Import
	private TutorialInitializerContract initializer;
	
	@Import
	private ExistingInstancesContract existingInstances;
	
	@Import
	private CoreInstancesContract coreInstances;
	
	@Import
	private TutorialInitializerModelsContract models;
	
	@Override
	public TutorialInitializerContract initializer() {
		return initializer;
	}

	@Override
	public ExistingInstancesContract existingInstances() {
		return existingInstances;
	}
	
	@Override
	public CoreInstancesContract coreInstances() {
		return coreInstances;
	}

	@Override
	public TutorialInitializerModelsContract models() {
		// TODO Auto-generated method stub
		return models;
	}


}
