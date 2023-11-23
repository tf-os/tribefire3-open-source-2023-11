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
package tribefire.extension.artifact.processing_wb_initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.extension.artifact.processing_wb_initializer.wire.contract.ArtifactProcessingWbInitializerContract;
import tribefire.extension.artifact.processing_wb_initializer.wire.contract.ArtifactProcessingWbInitializerMainContract;

@Managed
public class ArtifactProcessingWbInitializerMainSpace implements ArtifactProcessingWbInitializerMainContract {

	@Import
	private DefaultWbContract workbench;
	
	@Import
	private ArtifactProcessingWbInitializerContract artifactProcessingAccessWbInitializerContract;

	@Override
	public DefaultWbContract workbenchContract() {
		return workbench;
	}
	
	@Override
	public ArtifactProcessingWbInitializerContract artifactProcessingWbInitializerContract() {
		return artifactProcessingAccessWbInitializerContract;
	}
	
}
