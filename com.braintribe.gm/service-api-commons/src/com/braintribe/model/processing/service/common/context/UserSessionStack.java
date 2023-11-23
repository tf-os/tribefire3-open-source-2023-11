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
package com.braintribe.model.processing.service.common.context;

import com.braintribe.model.processing.service.api.aspect.IsAuthorizedAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorSessionIdAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.provider.Hub;
import com.braintribe.utils.collection.api.MinimalStack;
import com.braintribe.utils.collection.impl.AttributeContexts;

public class UserSessionStack implements Hub<UserSession>, MinimalStack<UserSession> {
	
	@Override
	public void push(UserSession session) {
		AttributeContexts.push(AttributeContexts.peek().derive() //
		.set(RequestorSessionIdAspect.class, session.getSessionId()) // 
		.set(RequestorUserNameAspect.class, session.getUser().getName()) //
		.set(IsAuthorizedAspect.class, true) //
		.set(UserSessionAspect.class, session) //
		.build());
	}

	@Override
	public UserSession peek() {
		return AttributeContexts.peek().findAttribute(UserSessionAspect.class).orElse(null);
	}

	@Override
	public UserSession pop() {
		return AttributeContexts.pop().findAttribute(UserSessionAspect.class).orElse(null);
	}
	
	/**
	 * TODO: use MinimalStack
	 * @deprecated use {@link #push(UserSession)} and {@link #pop()} instead 
	 */
	@Override
	@Deprecated
	public void accept(UserSession session) {
		if (session == null) {
			if (isEmpty()) // to be backwards compatible
				return;
			
			pop();
		} else {
			push(session);
		}
	}

	@Override
	public UserSession get() {
		return peek();
	}

	@Override
	public boolean isEmpty() {
		return AttributeContexts.peek() == null;
	}

}
