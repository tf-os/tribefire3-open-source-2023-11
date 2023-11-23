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
package tribefire.cortex.services.tribefire_web_platform_test.tests.testrunners;

import static com.braintribe.model.processing.query.fluent.EntityQueryBuilder.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.cortex.services.tribefire_web_platform_test.tests.PlatformHolder;
import tribefire.cortex.testing.junit.runner.AuthorizingParallelRunner;

/**
 * This integration test only tests the authorization of the corresponding thread but not the parallelization itself.
 * There is a dedicated unit test for that.
 * 
 * @author Neidhart.Orlich
 *
 */
@RunWith(AuthorizingParallelRunner.class)
public class ParallelRunnerAuthorizationTest {

	@Test
	public void test1() throws Exception {
		test();
	}

	@Test
	public void test2() throws Exception {
		test();
	}

	@Test
	public void test3() throws Exception {
		test();
	}

	@Test
	public void test4() throws Exception {
		test();
	}

	private void test() throws Exception {
		PersistenceGmSession cortexSession = PlatformHolder.platformContract.requestUserRelated().cortexSessionSupplier().get();

		List<?> models = cortexSession.query() //
				.entities(from(GmMetaModel.T).done()) //
				.list();

		assertThat(models).isNotEmpty();
	}

}
