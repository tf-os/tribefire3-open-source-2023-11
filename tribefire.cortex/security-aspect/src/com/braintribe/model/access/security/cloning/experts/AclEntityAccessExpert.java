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
package com.braintribe.model.access.security.cloning.experts;

import com.braintribe.model.access.security.query.PostQueryExpertContextImpl;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.security.query.context.EntityExpertContext;
import com.braintribe.model.processing.security.query.expert.EntityAccessExpert;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;

/**
 * Verifies whether a given {@link HasAcl} entity is accessible.
 */
public class AclEntityAccessExpert implements EntityAccessExpert {

	/**
	 * @return <tt>false</tt> iff we are visiting a {@link HasAcl} instance, for which the ACL condition implies access
	 *         denial
	 */
	@Override
	public boolean isAccessGranted(EntityExpertContext expertContext) {
		GenericEntity entity = expertContext.getEntity();

		if (!(entity instanceof HasAcl))
			return true;

		/* TODO improve, this is a quick hack for now to avoid repetitive MD resolution here. Context should have some
		 * way to store information for these experts, maybe some aspects like with CMD context... */
		if (expertContext instanceof PostQueryExpertContextImpl)
			if (!((PostQueryExpertContextImpl) expertContext).needsHasAclChecks())
				return true;

		HasAcl hasAcl = (HasAcl) entity;
		SessionAuthorization sa = expertContext.getSession().getSessionAuthorization();

		return hasAcl.isOperationGranted(AclOperation.READ, sa.getUserName(), sa.getUserRoles());
	}

}
