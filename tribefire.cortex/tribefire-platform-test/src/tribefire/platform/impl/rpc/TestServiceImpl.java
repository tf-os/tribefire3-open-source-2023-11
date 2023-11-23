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

public class TestServiceImpl implements TestService {

	@Override
	public int noarg() {
		return 0;
	}

	@Override
	public int primitive(int i) {
		return i;
	}

	@Override
	public int primitive(int i, int j) {
		return i + j;
	}

	@Override
	public TestResponse collection(Set<String> request) {
		TestResponse response = TestResponse.T.create();
		response.setProcessedBy(TestService.collection);
		return response;
	}

	@Override
	public TestResponse entity(TestRequest request) throws AuthenticationException {
		TestResponse response = TestResponse.T.create();
		response.setProcessedBy(TestService.entity);
		return response;
	}

	@Override
	public TestResponse entityCollection(List<TestRequest> request) {
		TestResponse response = TestResponse.T.create();
		response.setProcessedBy(TestService.entityCollection);
		return response;
	}

	@Override
	public TestResponse mixed(TestRequest request, boolean testBoolean) throws AuthenticationException {
		TestResponse response = TestResponse.T.create();
		response.setProcessedBy(TestService.mixed);
		return response;
	}

	@Override
	public TestResponse mixed(TestRequest request, boolean testBoolean, String testString) throws AuthenticationException {
		TestResponse response = TestResponse.T.create();
		response.setProcessedBy(TestService.mixedWithString);
		return response;
	}

	@Override
	public TestResponse mixed(TestRequest request, boolean testBoolean, Set<String> testCollection) throws AuthenticationException {
		TestResponse response = TestResponse.T.create();
		response.setProcessedBy(TestService.mixedWithCollection);
		return response;
	}

}
