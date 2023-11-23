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

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.jms.MQConnection;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQMessageConsumer;
import com.ibm.mq.jms.MQMessageProducer;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQSession;
import com.ibm.msg.client.wmq.WMQConstants;

public class Simple {
	public static void main(String[] args) {
		try {
			MQEnvironment.enableTracing(10);
			MQConnectionFactory connectionFactory = new MQConnectionFactory();
			
			connectionFactory.setHostName("10.202.1.1");
			connectionFactory.setPort(1414);
			connectionFactory.setChannel("SYSTEM.ADMIN.SVRCONN");
			connectionFactory.setQueueManager("MQE");
			connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
			
			String username = null;
			String password = null;
			MQConnection connection = null;

			if ((username != null) && (password != null)) {
				System.out.println(String.format("Creating queue connection for user '%s'", username));
				connection = (MQConnection) connectionFactory.createConnection(username, password);
			} else {
				System.out.println("Creating anonymous queue connection");
				connection = (MQConnection) connectionFactory.createConnection();
			}
			connection.start();
			
			MQSession session = (MQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MQSession session2 = (MQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MQQueue queue = (MQQueue) session.createQueue("tf.dev.queue.remoteToDbl");
			MQQueue queue2 = (MQQueue) session2.createQueue("tf.dev.queue.remoteToDbl");
			MQMessageProducer sender = (MQMessageProducer) session.createProducer(queue);     

			MQMessageConsumer receiver = (MQMessageConsumer) session2.createConsumer(queue2);

			String stt = "Test Message";

			TextMessage message = session.createTextMessage(stt);
			message.setJMSReplyTo(queue2);
			sender.send(message);
			System.out.println("Sent: " + message);

			Message msg1 = receiver.receive(5000);
			if(msg1!=null){
				String responseMsg = ((TextMessage) msg1).getText();
				System.out.println("Received: " + responseMsg);
			}else{
				System.out.println("Message received is null");
			}
		}catch(Exception e){
			System.out.println("Exception caught in program : " + e);
			e.printStackTrace();
		}
	}
}
