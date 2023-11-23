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
package tribefire.platform.wire.space.cortex.accesses;

import java.util.function.Predicate;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.reflection.Property;

/**
 * A {@link Manipulation} Filter that passes all manipulations except {@link ChangeValueManipulation} for the
 * {@link Deployable#deploymentStatus} property.
 * 
 * @author gunther
 */
public class DeploymentStateManipulationFilter implements Predicate<AtomicManipulation> {

	public static final DeploymentStateManipulationFilter INSTANCE = new DeploymentStateManipulationFilter();

	private static final Property deploymentStateProperty = Deployable.T.getProperty(Deployable.deploymentStatus);

	@Override
	public boolean test(AtomicManipulation m) {
		if (m.manipulationType() == ManipulationType.CHANGE_VALUE) {
			ChangeValueManipulation cvm = (ChangeValueManipulation) m;
			Owner owner = cvm.getOwner();

			return deploymentStateProperty != owner.property();
		}
		return true;

	}
}
