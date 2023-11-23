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
package tribefire.extension.artifact.processing_initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.artifact.processing_initializer.wire.contract.ArtifactProcessingInitializerContract;
import tribefire.extension.artifact.processing_initializer.wire.contract.ArtifactProcessingInitializerMainContract;
import tribefire.extension.artifact.processing_initializer.wire.contract.ExistingInstancesContract;

@Managed
public class ArtifactProcessingInitializerMainSpace implements ArtifactProcessingInitializerMainContract {

	@Import
	private ArtifactProcessingInitializerContract artifactProcessingInitializerContract;
	
	@Import
	private CoreInstancesContract coreInstancesContract;
	
	@Import
	private ExistingInstancesContract existingInstancesContract;
	
	@Override
	public ArtifactProcessingInitializerContract artifactProcessingInitializerContract() {	
		return artifactProcessingInitializerContract;
	}

	@Override
	public CoreInstancesContract coreInstancesContract() {
		return coreInstancesContract;
	}

	@Override
	public ExistingInstancesContract existingInstancesContract() {		
		return existingInstancesContract;
	}
	
}
