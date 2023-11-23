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
package tribefire.extension.demo.processing.tools;

import java.util.Collection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.Resource;

import tribefire.extension.demo.model.data.Department;

/**
 * 
 * 
 * @author peter.gazdik
 */
public class DemoCsaPopulationInitializer extends SimplePersistenceInitializer {

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		ManagedGmSession session = context.getSession();

		Collection<GenericEntity> population = DemoPopulationBuilder.newInstance().noIdGenerator().build();
		for (GenericEntity entity : population) {
			if (entity instanceof Resource)
				/* Resources are not unique using SelectiveInformation - not sure why though. But I also do not think
				 * they need to be addressed here, as they are not likely to be modified by a user. */
				continue;

			String id = getIdFor(entity);

			entity.setId(id);
			entity.setGlobalId(id);

			session.attach(entity);
		}
	}

	private String getIdFor(GenericEntity entity) {
		String id = entity.toSelectiveInformation();

		if (entity instanceof Department)
			id = ((Department) entity).getCompany().getName() + "->" + id; // since different companies can have
																			// departments with same name

		id = id.replaceAll("\\s", "-");

		return id;
	}

}
