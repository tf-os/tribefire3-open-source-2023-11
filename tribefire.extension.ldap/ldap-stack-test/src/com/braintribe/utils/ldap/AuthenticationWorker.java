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

import java.util.concurrent.Callable;

import javax.naming.Context;
import javax.naming.ldap.LdapContext;

import org.junit.Ignore;

@Ignore
public class AuthenticationWorker implements Callable<Boolean> {

	protected LdapConnectionStack ldapStack = null;
	protected int iterations = 20;
	protected int workerId = -1;

	public AuthenticationWorker(LdapConnectionStack ldapStack, int workerId, int iterations) {
		this.ldapStack = ldapStack;
		this.workerId = workerId;
		this.iterations = iterations;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			for (int i=0; i<this.iterations; ++i) {
				LdapContext dirContext = this.ldapStack.pop();
				try {
					dirContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
					dirContext.addToEnvironment(Context.SECURITY_PRINCIPAL, LdapConnectionStackTest.AUTH_USER);
					dirContext.addToEnvironment(Context.SECURITY_CREDENTIALS, LdapConnectionStackTest.AUTH_PASS);

					dirContext.getAttributes(LdapConnectionStackTest.AUTH_USER, null);
					
					System.out.println("Successfully authenticated in thread "+this.workerId+": "+i);
				} finally {
					dirContext.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
					dirContext.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
					dirContext.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
				}
				this.ldapStack.push(dirContext);
				dirContext = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}
