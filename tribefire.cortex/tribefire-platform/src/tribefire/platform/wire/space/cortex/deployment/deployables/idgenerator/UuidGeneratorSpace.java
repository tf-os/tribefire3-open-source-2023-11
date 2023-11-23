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
package tribefire.platform.wire.space.cortex.deployment.deployables.idgenerator;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.idgenerator.basic.UuidGenerator;
import com.braintribe.model.processing.idgenerator.basic.UuidMode;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.wire.space.cortex.deployment.deployables.DeployableBaseSpace;

@Managed
public class UuidGeneratorSpace extends DeployableBaseSpace {

	@Managed
	public UuidGenerator uuidGenerator(ExpertContext<com.braintribe.model.idgendeployment.UuidGenerator> context) {
		com.braintribe.model.idgendeployment.UuidGenerator deployable = context.getDeployable();
		UuidGenerator bean = new UuidGenerator();

		if (deployable != null && deployable.getMode() != null) {
			switch (deployable.getMode()) {
				case compact:
					bean.setMode(UuidMode.compact);
					break;
				case compactWithTimestampPrefix:
					bean.setMode(UuidMode.compactWithTimestampPrefix);
					break;
				case standard:
					bean.setMode(UuidMode.standard);
					break;
				default:
					break;

			}
		}
		return bean;
	}
}
