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
package com.braintribe.gwt.tribefirejs.client.remote;

import java.util.function.Supplier;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.AccessDescriptor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.processing.async.api.JsPromise;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.remote)
@SuppressWarnings("unusable-by-js")
public interface ServicesSession extends ServicesConnection {

	String sessionId();

	UserSession userSession();

	ModelAccessoryBuilder modelAccessory(GmMetaModel model);

	JsPromise<Supplier<PersistenceGmSession>> accessSessionFactory(String accessId);
	SessionFactoryBuilder accessSessionFactoryBuilder(AccessDescriptor accessDescriptor);

	JsPromise<Supplier<PersistenceGmSession>> serviceSessionFactory(String domainId);
	SessionFactoryBuilder serviceSessionFactoryBuilder(String domainId, GmMetaModel model);

	<T> JsPromise<T> decodeJse(String jseValue);

}
