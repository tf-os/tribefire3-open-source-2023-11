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
package tribefire.extension.elasticsearch.test.service;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.VerySlow;

import tribefire.extension.elasticsearch.model.api.request.admin.CreateIndexByName;
import tribefire.extension.elasticsearch.model.api.request.admin.DeleteIndexByName;
import tribefire.extension.elasticsearch.model.api.response.SuccessResult;
import tribefire.extension.elasticsearch.test.AbstractElasticsearchTest;

@Category(VerySlow.class)
public class AdminTest extends AbstractElasticsearchTest {

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void testCreateIndexByName() {
		String uniqueName = UUID.randomUUID().toString();

		CreateIndexByName createRequest = CreateIndexByName.T.create();
		createRequest.setIndexName(uniqueName);

		SuccessResult result = (SuccessResult) createRequest.eval(cortexSession).get();
		assertEquals(true, result.getSuccess());

		DeleteIndexByName deleteRequest = DeleteIndexByName.T.create();
		deleteRequest.setIndexName(uniqueName);

		result = (SuccessResult) deleteRequest.eval(cortexSession).get();
		assertEquals(true, result.getSuccess());
	}

}
