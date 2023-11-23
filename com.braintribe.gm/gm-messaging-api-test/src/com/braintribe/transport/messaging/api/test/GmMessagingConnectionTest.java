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
package com.braintribe.transport.messaging.api.test;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingException;

/**
 * <p>
 * Tests the {@link MessagingConnectionProvider} and basic {@link MessagingConnection} operations.
 * 
 */
public abstract class GmMessagingConnectionTest extends GmMessagingTest {

	@Test
	public void testOpenClose() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		connection.open();
		
		connection.close();
		
	}
	
	@Test
	public void testNoOpenedClose() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		//no-op close
		connection.close();
		
	}
	
	@Test
	public void testNoOpClose() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		connection.open();
		
		connection.close();
		
		//no-op close
		connection.close();
		
	}
	
	@Test
	public void testNoOpOpen() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		connection.open();
		
		//no-op open
		connection.open();
		
		connection.close();
		
	}

	@Test
	public void testReopen() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		connection.open();
		
		connection.close();
		
		try {
			connection.open();
			
			Assert.fail("Attempt to open a closed connection should have thrown an exception.");
			
		} catch (MessagingException e) {
			
			System.out.println("Expected exception while trying to use closed connection: "+e.getClass().getSimpleName()+": "+e.getMessage());
		
		} catch (Exception e) {
			
			e.printStackTrace();
			Assert.fail("unexpected exception while trying to use a closed connection: "+e.getClass().getName()+": "+e.getMessage());
		
		} finally {
			connection.close();
		}
		
	}

	@Test
	public void testAutoOpen() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		connection.createMessagingSession();
		
		connection.close();
		
	}

	@Test
	public void testAutoOpenAfterClose() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		connection.open();
		
		connection.close();
		
		try {
			connection.createMessagingSession();
			
			Assert.fail("Attempt to use a closed connection should have thrown an exception.");
			
		} catch (MessagingException e) {
			
			System.out.println("Expected exception while trying to use closed connection: "+e.getClass().getSimpleName()+": "+e.getMessage());
		
		} catch (Exception e) {
			
			e.printStackTrace();
			Assert.fail("unexpected exception while trying to use a closed connection: "+e.getClass().getName()+": "+e.getMessage());
		
		} finally {
			connection.close();
		}
		
	}
	
}
