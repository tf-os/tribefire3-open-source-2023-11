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
package com.braintribe.transport.messaging.mq.test;

public class Main {

	public static void main(String[] args) throws Exception {
	
		BasicJmsTests.initTests();
		BasicJmsTests tests = new BasicJmsTests();
		
//		System.out.println("connectTest");
//		tests.initialize();
//		tests.connectTest();
//		tests.destroy();

		System.out.println("sendSingleMessageTest");
		tests.initialize();
		tests.sendSingleMessageTest();
		tests.destroy();

		System.out.println("sendMultipleMessagesTest");
		tests.initialize();
		tests.sendMultipleMessagesTest();
		tests.destroy();

		System.out.println("sendMultipleMessagesTestMultiSessions");
		tests.initialize();
		tests.sendMultipleMessagesTestMultiSessions();
		tests.destroy();

		System.out.println("sendMultipleMessagesTestMultiConnections");
		tests.initialize();
		tests.sendMultipleMessagesTestMultiConnections();
		tests.destroy();

		System.out.println("sendMessagesToTopicTest");
		tests.initialize();
		tests.sendMessagesToTopicTest();
		tests.destroy();

		System.out.println("testTopicMultithreadedMassive");
		tests.initialize();
		tests.testTopicMultithreadedMassive();
		tests.destroy();


		BasicJmsTests.shutdown();
	}
	
}
