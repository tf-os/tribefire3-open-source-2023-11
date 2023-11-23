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
package tribefire.platform.wire.space.cortex;

import static com.braintribe.wire.api.util.Lists.list;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.model.ModelNotificationProcessor;
import tribefire.platform.impl.session.InternalModelNotificationProcessor;

@Managed
public class ModelNotificationSpace implements WireSpace {

	@Import
	private GmSessionsSpace gmSessions;

	@Managed
	public ModelNotificationProcessor processor() {
		ModelNotificationProcessor bean = new ModelNotificationProcessor();
		return bean;
	}

	@Managed
	public InternalModelNotificationProcessor internalProcessor() {
		InternalModelNotificationProcessor bean = new InternalModelNotificationProcessor();
		// @formatter:off
		bean.setModelChangeListeners(
			list(
				gmSessions.modelEssentialsSupplier(),
				gmSessions.modelAccessorySupplier()
				
			)
		);
		bean.setModelAccessoryFactories(
			list(
				gmSessions.systemModelAccessoryFactory(), 
				gmSessions.userModelAccessoryFactory()
			)
		);
		// @formatter:on
		return bean;
	}

}
