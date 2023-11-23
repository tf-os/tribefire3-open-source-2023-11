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
package tribefire.extension.drools.pd_processing.test;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.condition.impl.BasicConditionProcessorContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.lcd.StopWatch;

import tribefire.extension.drools.model.test.TestProcess;
import tribefire.extension.drools.pe.DroolsConditionProcessor;
import tribefire.extension.drools.pe.DroolsTransitionProcessor;

public class ConditionProcessorTest extends DroolsPdProcessingTestBase {

	@Test
	public void testConditionProcessor() {
		StopWatch stopWatch = new StopWatch();
		DroolsConditionProcessor conditionProcessor = testContract.conditionProcessor();
		
		System.out.println("condition processor instantiation in " + stopWatch.getElapsedTime() + "ms");
		
		stopWatch = new StopWatch();
		DroolsTransitionProcessor transitionProcessor = testContract.transitionProcessor();
		
		System.out.println("transition processor instantiation in " + stopWatch.getElapsedTime() + "ms");
		
		PersistenceGmSession session = testContract.sessionFactory().newSession("access.test");
		
		TestProcess testProcess1 = session.query().entity(TestProcess.T, 1L).require();
		TestProcess testProcess2 = session.query().entity(TestProcess.T, 2L).require();

		BasicConditionProcessorContext<GenericEntity> context1 = new BasicConditionProcessorContext<>(session, session, testProcess1, testProcess1);
		BasicConditionProcessorContext<GenericEntity> context2 = new BasicConditionProcessorContext<>(session, session, testProcess2, testProcess2);
		
		Assertions.assertThat(conditionProcessor.matches(context1)).isTrue();
		Assertions.assertThat(conditionProcessor.matches(context2)).isFalse();
	}
}
