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
package com.braintribe.model.platform.setup.api;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

public interface Encrypt extends SetupRequest {
	EntityType<Encrypt> T = EntityTypes.T(Encrypt.class);
	
	@Initializer("'AES/CBC/PKCS5Padding'")
	String getAlgorithm();
	void setAlgorithm(String algorithm);
	
	@Initializer("'c36e99ec-e108-11e8-9f32-f2801f1b9fd1'")
	String getSecret();
	void setSecret(String secret);
	
	@Initializer("'PBKDF2WithHmacSHA1'")
	String getKeyFactoryAlgorithm();
	void setKeyFactoryAlgorithm(String keyFactoryAlgorithm);
	
	@Initializer("128")
	int getKeyLength();
	void setKeyLength(int keyLength);

	@Confidential
	@Mandatory
	String getValue();
	void setValue(String value);
	
	@Override
	EvalContext<String> eval(Evaluator<ServiceRequest> evaluator);
}
