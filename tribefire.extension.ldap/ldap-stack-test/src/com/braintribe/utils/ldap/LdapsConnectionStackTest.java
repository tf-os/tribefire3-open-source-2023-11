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
package com.braintribe.utils.ldap;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.transport.ssl.impl.EasySslSocketFactoryProvider;

@Category(SpecialEnvironment.class)
public class LdapsConnectionStackTest {

	protected LdapConnectionStack ldapStack = null;

	protected final static String CONN_USER = "CN=Web Connector,OU=ServiceAccounts,OU=Accounts,OU=BTT,DC=Braintribe";
	protected final static String CONN_PASS = "...";


	protected final static String AUTH_USER = "CN=Roman Kurmanowytsch,OU=R&D,OU=Internal,OU=Accounts,OU=BTT,DC=Braintribe";
	protected final static String AUTH_PASS = "...";
	
	protected EasySslSocketFactoryProvider sslSocketFactoryProvider; 

	@Before
	public void prepare() throws Exception {
		this.ldapStack = new LdapConnectionStack();
		this.ldapStack.setConnectionUrl("ldaps://inf-dom-bt.braintribe:636");
		this.ldapStack.setUsername(CONN_USER);
		this.ldapStack.setPassword(CONN_PASS);
		this.sslSocketFactoryProvider = new EasySslSocketFactoryProvider();
		this.sslSocketFactoryProvider.provideSSLSocketFactory();
		this.ldapStack.setSslSocketFactoryProvider(sslSocketFactoryProvider);
	}

	@Test
	public void testConnection() throws Exception {
		DirContext dirContext = this.ldapStack.pop();
		dirContext.close();
	}


	@Test
	public void testSearch() throws Exception {
		DirContext dirContext = this.ldapStack.pop();

		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

		String uidfilter = "(sAMAccountName=roman.kurmanowytsch)";
		String authUserBase = "OU=Accounts,OU=BTT,DC=Braintribe";

		NamingEnumeration<SearchResult> results = dirContext.search(authUserBase, uidfilter, constraints);

		while (results != null && results.hasMoreElements()) {

			SearchResult nextResult = results.next();
			System.out.println("Name: "+nextResult.getNameInNamespace());
		}

		dirContext.close();
	}

	@Test
	public void testAuthentication() throws Exception {
		DirContext dirContext = this.ldapStack.pop();

		try {
			dirContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
			dirContext.addToEnvironment(Context.SECURITY_PRINCIPAL, AUTH_USER);
			dirContext.addToEnvironment(Context.SECURITY_CREDENTIALS, AUTH_PASS);

			dirContext.getAttributes(AUTH_USER, null);
		} finally {
			dirContext.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
			dirContext.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
			dirContext.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
		}
		dirContext.close();
	}

	@Test
	public void testAuthentications() throws Exception {
		LdapContext dirContext = null;

		for (int i=0; i<100; ++i) {
			dirContext = this.ldapStack.pop();
			try {
				dirContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
				dirContext.addToEnvironment(Context.SECURITY_PRINCIPAL, AUTH_USER);
				dirContext.addToEnvironment(Context.SECURITY_CREDENTIALS, AUTH_PASS);

				dirContext.getAttributes(AUTH_USER, null);
			} finally {
				dirContext.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
				dirContext.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
				dirContext.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
			}
			this.ldapStack.push(dirContext);
			dirContext = null;
		}
	}

	@Test
	public void testParallelAuthentications() throws Exception {

		int threads = 20;
		int iterations = 20;

		ExecutorService executor = Executors.newFixedThreadPool(threads);
		Set<Future<Boolean>> futures = new HashSet<Future<Boolean>>();
		
		for (int i=0; i<threads; ++i) {
			
			AuthenticationWorker worker = new AuthenticationWorker(this.ldapStack, i, iterations);
			futures.add(executor.submit(worker));
			
		}
		
		boolean errorDetected = false;
		for (Future<Boolean> future : futures) {
			Boolean result = future.get();
			if (!result.booleanValue()) {
				System.out.println("At least one error was detected.");
				errorDetected = true;
			}
		}
		
		executor.shutdownNow();
		
		Assert.assertFalse(errorDetected);
	}
}
