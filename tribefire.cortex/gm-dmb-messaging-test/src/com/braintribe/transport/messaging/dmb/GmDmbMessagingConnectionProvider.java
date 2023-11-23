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
package com.braintribe.transport.messaging.dmb;

import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.model.messaging.expert.Messaging;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.dbm.GmDmbMqMessaging;

public class GmDmbMessagingConnectionProvider {
	
	public static final GmDmbMessagingConnectionProvider instance = new GmDmbMessagingConnectionProvider();
	
	private MessagingConnectionProvider<? extends Messaging> messagingConnectionProvider;
	
	private GmDmbMessagingConnectionProvider() {
		messagingConnectionProvider = getMessagingConnectionProvider();
	}
	
	public MessagingConnectionProvider<? extends Messaging> get() {
		return messagingConnectionProvider;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected com.braintribe.transport.messaging.api.MessagingConnectionProvider<? extends Messaging> getMessagingConnectionProvider() {
		
		Messaging denotationType = createDenotationType();
		
		com.braintribe.transport.messaging.api.Messaging messaging = getExpertByDenotationType(denotationType);
		
		return messaging.createConnectionProvider(denotationType, getMessagingContext());
		
	}
	
	protected Messaging createDenotationType() {
		
		com.braintribe.model.messaging.dmb.GmDmbMqMessaging messagingDenotationType = com.braintribe.model.messaging.dmb.GmDmbMqMessaging.T.create();
		
		return messagingDenotationType;
		
	}
	
	protected com.braintribe.transport.messaging.api.Messaging<? extends Messaging> getExpertByDenotationType(Messaging denotationType) {
		
		if (denotationType instanceof com.braintribe.model.messaging.dmb.GmDmbMqMessaging) {
			GmDmbMqMessaging gmDmbMqMessaging = new GmDmbMqMessaging();
			return gmDmbMqMessaging;
		}
		
		return null;
		
	}
	
	protected MessagingContext getMessagingContext() {
		MessagingContext context = new MessagingContext();
		context.setMarshaller(new Bin2Marshaller());
		return context;
	}
}
