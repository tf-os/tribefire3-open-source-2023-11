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
package com.braintribe.model.crypto.configuration;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.generic.GenericEntity;

@Abstract
public interface HasSaltingOptions extends GenericEntity {

	EntityType<HasSaltingOptions> T = EntityTypes.T(HasSaltingOptions.class);

	static final String enablePublicSalt = "enablePublicSalt";
	static final String publicSalt = "publicSalt";
	static final String enableRandomSalt = "enableRandomSalt";
	static final String randomSaltSize = "randomSaltSize";
	static final String randomSaltAlgorithm = "randomSaltAlgorithm";
	static final String randomSaltAlgorithmProvider = "randomSaltAlgorithmProvider";

	@Name("Enable Public Salt")
	@Description("Specifies whether the Public Salt should be used. When this is set to false, a Random Salt will be used instead (when enabled).")
	boolean getEnablePublicSalt();
	void setEnablePublicSalt(boolean enablePublicSalt);
	
	@Name("Public Salt")
	@Description("The salt that should be used. When this is not set, a Random Salt will be used instead.")
	String getPublicSalt();
	void setPublicSalt(String publicSalt);

	@Name("Enable Random Salt")
	@Description("Specifies whether a random salt should be used. When this is set to false, no salt will be used at all.")
	boolean getEnableRandomSalt();
	void setEnableRandomSalt(boolean enableRandomSalt);
	
	@Name("Random Salt Size")
	@Description("The size of the random salt that should be created.")
	Integer getRandomSaltSize();
	void setRandomSaltSize(Integer randomSaltSize);

	@Name("Random Salt Algorithm")
	@Description("The algorithm that should be used for creating random salts. When this is not set, a default will be used. See https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SecureRandom for a list of possivle algorithms.")
	String getRandomSaltAlgorithm();
	void setRandomSaltAlgorithm(String randomSaltAlgorithm);

	@Name("Random Salt Algorithm Provider")
	@Description("The algorithm provider that should be used for creating random salts. When this is not set, a default will be used. See https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SecureRandom for a list of possivle algorithms.")
	String getRandomSaltAlgorithmProvider();
	void setRandomSaltAlgorithmProvider(String randomSaltAlgorithmProvider);
}
