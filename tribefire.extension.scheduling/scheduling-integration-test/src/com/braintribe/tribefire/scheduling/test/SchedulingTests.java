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
package com.braintribe.tribefire.scheduling.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.extension.scheduling.model.Scheduled;
import tribefire.extension.scheduling.model.action.NopAction;
import tribefire.extension.scheduling.model.api.Cancel;
import tribefire.extension.scheduling.model.api.GetList;
import tribefire.extension.scheduling.model.api.ListResult;
import tribefire.extension.scheduling.model.api.PurgeRegistry;
import tribefire.extension.scheduling.model.api.RegistryPurged;
import tribefire.extension.scheduling.model.api.Schedule;
import tribefire.extension.scheduling.model.api.ScheduleResult;

public class SchedulingTests extends AbstractSchedulingTest {

	private boolean initialized = false;

	// -----------------------------------------------------------------------
	// SETUP & TEARDOWN
	// -----------------------------------------------------------------------

	@Override
	@Before
	public void before() throws Exception {
		super.before();
		if (!initialized) {
			initialized = true;
		}
	}

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void testRegisterAndExecution() throws Exception {

		PersistenceGmSession session = getSession();

		PurgeRegistry purge = PurgeRegistry.T.create();
		RegistryPurged purged = purge.eval(session).get();
		System.out.println("Purged " + purged.getRemovedEntryCount() + " entries");

		GetList getList = GetList.T.create();
		ListResult listBefore = getList.eval(session).get();
		assertThat(listBefore.getList()).isEmpty();

		Schedule schedule = Schedule.T.create();
		schedule.setAction(NopAction.T.create());
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.SECOND, 10);
		schedule.setScheduledDate(cal.getTime());
		ScheduleResult scheduleResult = schedule.eval(session).get();
		assertThat(scheduleResult.getScheduledId()).isNotNull();

		ListResult listAfter = getList.eval(session).get();
		assertThat(listAfter.getList()).hasSize(1);
		assertThat((String) listAfter.getList().get(0).getId()).isEqualTo(scheduleResult.getScheduledId());

		Thread.sleep(15000L);

		PersistenceGmSession session2 = getSession();
		Scheduled checkScheduled = session2.query().entity(Scheduled.T, scheduleResult.getScheduledId()).require();
		assertThat(checkScheduled.getExecutionDate()).isNotNull();
		assertThat(checkScheduled.getExecutionSuccess()).isTrue();

		ListResult finalList = getList.eval(session).get();
		assertThat(finalList.getList()).isEmpty();

	}

	@Test
	public void testRegisterAndCancel() throws Exception {

		PersistenceGmSession session = getSession();

		PurgeRegistry purge = PurgeRegistry.T.create();
		RegistryPurged purged = purge.eval(session).get();
		System.out.println("Purged " + purged.getRemovedEntryCount() + " entries");

		GetList getList = GetList.T.create();
		ListResult listBefore = getList.eval(session).get();
		assertThat(listBefore.getList()).isEmpty();

		Schedule schedule = Schedule.T.create();
		schedule.setAction(NopAction.T.create());
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.SECOND, 10);
		schedule.setScheduledDate(cal.getTime());
		ScheduleResult scheduleResult = schedule.eval(session).get();
		assertThat(scheduleResult.getScheduledId()).isNotNull();

		ListResult listAfter = getList.eval(session).get();
		assertThat(listAfter.getList()).hasSize(1);
		assertThat((String) listAfter.getList().get(0).getId()).isEqualTo(scheduleResult.getScheduledId());

		Cancel cancel = Cancel.T.create();
		cancel.setScheduledId(scheduleResult.getScheduledId());
		cancel.eval(session).get();

		PersistenceGmSession session2 = getSession();
		Scheduled checkScheduled = session2.query().entity(Scheduled.T, scheduleResult.getScheduledId()).find();
		assertThat(checkScheduled).isNull();

	}
}
