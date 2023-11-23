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
package tribefire.platform.impl.rpc;

import java.util.List;
import java.util.Set;

import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;

public interface TestService {

	String collection = "collection";
	String entity = "entity";
	String entityCollection = "entityCollection";
	String mixed = "mixed";
	String mixedWithString = "mixedWithString";
	String mixedWithCollection = "mixedWithCollection";

	int noarg();

	int primitive(int i);

	int primitive(int i, int j);

	TestResponse collection(Set<String> request);

	TestResponse entity(TestRequest request) throws AuthenticationException;

	TestResponse entityCollection(List<TestRequest> request);

	TestResponse mixed(TestRequest request, boolean testBoolean) throws AuthenticationException;

	TestResponse mixed(TestRequest request, boolean testBoolean, String testString) throws AuthenticationException;

	TestResponse mixed(TestRequest request, boolean testBoolean, Set<String> testCollection) throws AuthenticationException;

}
