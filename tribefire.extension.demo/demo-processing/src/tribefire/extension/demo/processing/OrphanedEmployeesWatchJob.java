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
package tribefire.extension.demo.processing;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;

import tribefire.extension.demo.model.api.GetOrphanedEmployees;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.OrphanedAuditRecord;
import tribefire.extension.demo.model.data.Person;
import tribefire.extension.job_scheduling.api.api.JobRequest;
import tribefire.extension.job_scheduling.api.api.JobResponse;
import tribefire.extension.job_scheduling.deployment.model.JobScheduling;

/**
 * A job implementation that checks for employees which are not assigned to any {@link Company}. In such case, a new {@link OrphanedAuditRecord } is
 * created. If the employee gets assigned to a company within the next {@link JobScheduling} run, the record is removed again.
 */
public class OrphanedEmployeesWatchJob implements ServiceProcessor<JobRequest, JobResponse> {

	private String accessId;
	private PersistenceGmSessionFactory sessionFactory;

	@Required
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public JobResponse process(ServiceRequestContext context, JobRequest request) {
		PersistenceGmSession session = sessionFactory.newSession(accessId);

		Set<Person> orphanIds = GetOrphanedEmployees.T.create().eval(session).get();
		updateOrphanAuditRecords(orphanIds, session);

		return JobResponse.T.create();
	}

	/**
	 * All {@link OrphanedAuditRecord OrphanedAuditRecords} which have become deprecated in the meanwhile are deleted. Those who are up to date are
	 * not touched (the {@link Person} is still not assigned to any company. If after those checks person ids are left, new records are created.
	 */
	private void updateOrphanAuditRecords(Set<Person> orphanEmployees, PersistenceGmSession session) {
		EntityQuery recordQuery = EntityQueryBuilder.from(OrphanedAuditRecord.T).done();
		List<OrphanedAuditRecord> records = session.query().entities(recordQuery).list();

		for (OrphanedAuditRecord record : records) {
			boolean removed = orphanEmployees.remove(record.getPerson());

			if (!removed) {
				session.deleteEntity(record);
				session.commit();
			}
		}

		orphanEmployees.forEach((oe) -> {
			OrphanedAuditRecord rec = session.create(OrphanedAuditRecord.T);
			rec.setPerson(oe);
			rec.setInfo(oe.getFirstName() + " " + oe.getLastName() + " (id " + oe.getId() + ") is not assigned to any Company");
			rec.setDate(new Date());
			session.commit();
		});
	}

}
