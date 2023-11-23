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
package com.braintribe.model.messaging.etcd;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.messaging.expert.Messaging;
import com.braintribe.model.plugin.Plugable;

/**
 * <p>
 * A {@link Plugable} denotation type holding the basic configuration properties of a Etcd connection factory.
 * 
 */
public interface EtcdMessaging extends Messaging {

	final EntityType<EtcdMessaging> T = EntityTypes.T(EtcdMessaging.class);

	public final static String project = "project";
	public final static String endpointUrls = "endpointUrls";
	public final static String username = "username";
	public final static String password = "password";

	String getProject();
	void setProject(String project);

	List<String> getEndpointUrls();
	void setEndpointUrls(List<String> endpointUrls);

	String getUsername();
	void setUsername(String username);

	@Confidential
	String getPassword();
	void setPassword(String password);
}
