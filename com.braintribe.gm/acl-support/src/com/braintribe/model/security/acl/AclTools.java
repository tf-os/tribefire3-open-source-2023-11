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
package com.braintribe.model.security.acl;

import com.braintribe.model.acl.Acl;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.data.security.Administrable;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * @author peter.gazdik
 */
public class AclTools {

	public static HasAcl queryAclEntity(PersistenceGmSession session, PersistentEntityReference ref) {
		return (HasAcl) session.query().entity(ref).withTraversingCriterion(AclTcs.HAS_ACL_TC).require();
	}

	public static boolean supportsAcl(PersistenceGmSession session) {
		return supportsAcl(session.getModelAccessory().getOracle());
	}

	public static boolean isHasAclAdministrable(PersistenceGmSession session) {
		return isAdministrable(session, HasAcl.T);
	}

	public static boolean isAclAdministrable(PersistenceGmSession session) {
		return isAdministrable(session, Acl.T);
	}

	private static boolean isAdministrable(PersistenceGmSession session, EntityType<?> et) {
		return isAdministrable(session.getModelAccessory().getCmdResolver(), et);
	}

	public static boolean supportsAcl(ModelOracle modelOracle) {
		return modelOracle.findEntityTypeOracle(HasAcl.T) != null;
	}

	public static boolean isHasAclAdministrable(CmdResolver cmdResolver) {
		return isAdministrable(cmdResolver, HasAcl.T);
	}

	public static boolean isAclAdministrable(CmdResolver cmdResolver) {
		return isAdministrable(cmdResolver, Acl.T);
	}

	private static boolean isAdministrable(CmdResolver cmdResolver, EntityType<?> et) {
		return cmdResolver.getMetaData().entityType(et).useCase("acl").is(Administrable.T);
	}

}
