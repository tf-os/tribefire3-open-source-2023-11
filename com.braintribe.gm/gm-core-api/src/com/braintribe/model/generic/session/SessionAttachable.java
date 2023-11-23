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
package com.braintribe.model.generic.session;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.GmSystemInterface;

@GmSystemInterface
@Deprecated
public interface SessionAttachable extends GenericEntity {

	/** @deprecated use {@link GenericEntity#attach(GmSession)} */
	@Deprecated
	void attachSession(GmSession session);

	/** @deprecated use {@link GenericEntity#detach()} */
	@Deprecated
	GmSession detachSession();

	/** @deprecated use {@link GenericEntity#session()} */
	@Deprecated
	GmSession accessSession();

}
