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
package com.braintribe.model.processing.shiro.bootstrapping;

import java.io.Serializable;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.utils.RandomTools;

public class NodeSessionIdGenerator implements SessionIdGenerator {

	public static NodeSessionIdGenerator INSTANCE;

	protected String instanceIdAsString;

	@Override
	public Serializable generateId(Session session) {
		return INSTANCE.generateIdInternal(session);
	}

	public Serializable generateIdInternal(Session session) {
		String id = RandomTools.newStandardUuid() + "@" + instanceIdAsString;
		if (session instanceof SimpleSession) {
			((SimpleSession) session).setId(id);
		}

		return null;
	}
	@Configurable
	@Required
	public void setInstanceIdAsString(String instanceIdAsString) {
		this.instanceIdAsString = instanceIdAsString;
	}

}
