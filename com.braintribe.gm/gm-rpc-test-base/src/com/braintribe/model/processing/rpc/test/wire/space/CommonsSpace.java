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
package com.braintribe.model.processing.rpc.test.wire.space;

import com.braintribe.model.crypto.key.KeyPair;
import com.braintribe.model.processing.rpc.test.commons.TestClientKeyProvider;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class CommonsSpace implements WireSpace {

	@Import
	private CommonsSpace commons;

	@Import
	private CryptoSpace crypto;

	@Import
	private ServerCommonsSpace serverCommons;

	@Managed
	public InMemorySecurityServiceProcessor securityProcessor() {
		InMemorySecurityServiceProcessor bean = new InMemorySecurityServiceProcessor();
		User user1 = User.T.create();
		user1.setName("testuser");
		user1.setPassword("testuser");
		bean.addUser(user1);
		return bean;
	}

	// <bean id="rpc.crypto.clientKeyProvider"
	// class="com.braintribe.model.processing.rpc.test.commons.TestClientKeyProvider">
	// <property name="cryptorFactory" ref="crypto.cipherCryptorFactory" />
	// <property name="keyPairGenerator" ref="crypto.standardKeyGenerator.keyPair" />
	// <property name="keyPairSpec">
	// <bean class="com.braintribe.model.crypto.key.KeyPair">
	// <property name="keyAlgorithm" value="RSA" />
	// </bean>
	// </property>
	// </bean>
	@Managed
	public TestClientKeyProvider clientKeyProvider() {
		TestClientKeyProvider bean = new TestClientKeyProvider();
		bean.setCryptorFactory(crypto.cipherCryptorFactory());
		bean.setKeyPairGenerator(crypto.standardKeyPairGenerator());
		bean.setKeyPairSpec(clientKeySpec());
		return bean;
	}

	@Managed
	public KeyPair clientKeySpec() {
		KeyPair bean = KeyPair.T.create();
		bean.setKeyAlgorithm("RSA");
		return bean;
	}

}
