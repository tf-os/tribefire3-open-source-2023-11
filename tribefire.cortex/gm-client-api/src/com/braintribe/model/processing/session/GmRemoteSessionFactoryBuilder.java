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
package com.braintribe.model.processing.session;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * Specialization of the {@link GmSessionFactoryBuilder} that creates a {@link com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory} that
 * creates session to a remote tribefire instance. To authenticate the client with the server, either one of the authentication methods have to be used
 * or a user session provider has to be specified.
 */
public interface GmRemoteSessionFactoryBuilder extends GmSessionFactoryBuilder {

	/**
	 * Adds username and password to the builder. This information will be used on creation of a new session for authenticating with the server.
	 * As an alternative, {@link #authentication(Credentials)} could be used to pass a {@link Credentials} object.
	 * @param user The username that should be used for authentication.
	 * @param password The password that should be used for authentication.
	 * @return The {@link GmRemoteSessionFactoryBuilder} instance currently used.
	 */
	GmRemoteSessionFactoryBuilder authentication(String user, String password);
	
	/**
	 * Adds a provided {@link Credentials} object to the builder that will be used to authenticate the client with the remote tribefire instance.
	 * As an alternative, {@link #authentication(String, String)} could be used to pass username and password for authentication.
	 * This method allows the client to use arbitrary ways to authenticate with the server (e.g., SSO, Tokens, etc). 
	 * @param credentials The credentials that should be used for authentication.
	 * @return The {@link GmRemoteSessionFactoryBuilder} instance currently used.
	 */
	GmRemoteSessionFactoryBuilder authentication(Credentials credentials);
	
	/**
	 * Allows to provide a custom user session provider that is used for creating a remote session. When this session provider
	 * is specified, no authentication credentials have to be provided.
	 * @param userSessionProvider The user session provider that should be used for creating remote sessions.
	 * @return The {@link GmRemoteSessionFactoryBuilder} instance currently used.
	 */
	GmRemoteSessionFactoryBuilder userSessionProvider(Supplier<UserSession> userSessionProvider);
	
	/**
	 * Specifies the {@link HttpClientProvider} that should be used to create HTTP connections to the tribefire instance. This method is not
	 * mandatory. If not specified, a default {@link HttpClientProvider} will be used.  
	 * @param httpClientProvider The {@link HttpClientProvider} that should be used to connect to the remote tribefire instance.
	 * @return The {@link GmRemoteSessionFactoryBuilder} instance currently used.
	 */
	GmRemoteSessionFactoryBuilder httpClientProvider(HttpClientProvider httpClientProvider);
	
	/**
	 * Specifies a custom {@link ExecutorService} that should be used for asynchronous service requests. If this is not specified, 
	 * a default executor service will be used instead. With this method, it is possible to have better control on how many 
	 * threads should be used for asynchronous service invocations.
	 * @param executorService The {@link ExecutorService} that should be used.
	 * @return The {@link GmRemoteSessionFactoryBuilder} instance currently used.
	 */
	GmRemoteSessionFactoryBuilder executor(ExecutorService executorService);
	
	/**
	 * An alternative method to {@link #executor(ExecutorService)} where one can specify the core pool size, the maximum pool size
	 * and the maximum keep-alive time for idle threads. The implementation will then take care of creating a thread pool
	 * with these specifications.
	 * @param corePoolSize The core pool size of the executor service.
	 * @param maxPoolSize The maximum pool size of the executor service.
	 * @param keepAliveTime The maximum keep-alive time of idle threads.
	 * @return The {@link GmRemoteSessionFactoryBuilder} instance currently used.
	 */
	GmRemoteSessionFactoryBuilder executor(int corePoolSize, int maxPoolSize, int keepAliveTime);

	/** {@inheritDoc} */
	@Override
	GmRemoteSessionFactoryBuilder resourceAccessFactory(ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory);

	/**
	 * @param streamPipeFactory to be shared by the session factory and the classes that come with it.
	 * @return The {@link GmRemoteSessionFactoryBuilder} instance currently used.
	 */
	GmRemoteSessionFactoryBuilder streamPipeFactory(StreamPipeFactory streamPipeFactory);

}
