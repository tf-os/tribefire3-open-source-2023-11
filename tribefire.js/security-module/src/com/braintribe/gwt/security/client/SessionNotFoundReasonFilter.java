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
package com.braintribe.gwt.security.client;

import java.util.function.Predicate;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.security.reason.InvalidSession;
import com.braintribe.gm.model.security.reason.SessionExpired;
import com.braintribe.gm.model.security.reason.SessionNotFound;

/**
 * This filter will match against a {@link SessionNotFound} or a {@link SessionExpired} reason, which are the reasons of a {@link ReasonException}.
 * @author michel.docouto
 *
 */
public class SessionNotFoundReasonFilter implements Predicate<Throwable> {

	@Override
	public boolean test(Throwable exception) {
		if (!(exception instanceof ReasonException))
			return false;
		
		Reason reason = ((ReasonException) exception).getReason();
		if (reason instanceof SessionNotFound || reason instanceof SessionExpired || reason instanceof InvalidSession)
			return true;
		
		return false;
	}

}
