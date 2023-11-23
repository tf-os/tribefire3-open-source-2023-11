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
package com.braintribe.swagger.util;

import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

@FunctionalInterface
public interface Transformation<V, U> {

	String MIME_TYPE_APPLICATION_ZIP = "application/zip";
	String MIME_TYPE_TEXT_TROFF = "text/troff";

	public U transform(V v, PersistenceGmSession session) throws GmSessionException, GmSessionFactoryBuilderException;

}
