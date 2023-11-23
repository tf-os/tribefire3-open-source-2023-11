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
package com.braintribe.model.messaging.jms;

import com.braintribe.model.messaging.expert.Messaging;

public interface JmsConnection extends Messaging {
		
	String getHostAddress();
	void setHostAddress(String hostAddress);
	
	String getUsername();
	void setUsername(String username);
	
	String getPassword();
	void setPassword(String password);
	
	boolean getTransacted();
	void setTransacted(boolean transacted);
	
	AcknowledgeMode getAcknowledgeMode();
	void setAcknowledgeMode(AcknowledgeMode acknowledgeMode);

}
