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
package com.braintribe.model.processing.meta.cmd.context.experts;

import static java.util.Collections.emptySet;

import java.util.Collection;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.attribute.common.UserInfo;
import com.braintribe.common.attribute.common.UserInfoAttribute;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.selector.AclSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityAspect;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools;
import com.braintribe.utils.collection.impl.AttributeContexts;

/** @see AclSelector */
public class AclSelectorExpert implements CmdSelectorExpert<AclSelector> {

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(AclSelector selector) throws Exception {
		return MetaDataTools.aspects(EntityAspect.class);
	}

	@Override
	public boolean matches(AclSelector selector, SelectorContext context) throws Exception {
		GenericEntity entity = context.get(EntityAspect.class);
		if (entity == null)
			return false;

		if (!(entity instanceof HasAcl))
			return true;

		HasAcl aclEntity = (HasAcl) entity;
		String op = selector.getOperation();

		UserInfo ui = resolveUserInfo();

		if (ui == null)
			return aclEntity.isOperationGranted(op, null, emptySet());
		else
			return aclEntity.isOperationGranted(op, ui.userName(), ui.roles());
	}

	private UserInfo resolveUserInfo() {
		AttributeContext ac = AttributeContexts.peek();
		return ac == null ? null : ac.findOrNull(UserInfoAttribute.class);
	}

}
