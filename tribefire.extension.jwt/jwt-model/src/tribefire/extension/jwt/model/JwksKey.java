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
package tribefire.extension.jwt.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface JwksKey extends GenericEntity {

	EntityType<JwksKey> T = EntityTypes.T(JwksKey.class);

	String alg = "alg";
	String e = "e";
	String kid = "kid";
	String kty = "kty";
	String n = "n";
	String use = "use";

	String getAlg();
	void setAlg(String alg);

	String getE();
	void setE(String e);

	String getKid();
	void setKid(String kid);

	String getKty();
	void setKty(String kty);

	String getN();
	void setN(String n);

	String getUse();
	void setUse(String use);

	@Override
	default String asString() {
		StringBuilder sb = new StringBuilder();
		sb.append("alg: " + getAlg());
		sb.append(", e: " + getE());
		sb.append(", kid: " + getKid());
		sb.append(", kty: " + getKty());
		sb.append(", n: " + getN());
		sb.append(", use: " + getUse());
		return sb.toString();
	}
}
