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
package tribefire.extension.audit.service_audit_integration_test.impl;

import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

import tribefire.extension.audit.model.service.audit.api.CreateServiceAuditRecord;
import tribefire.extension.audit.model.test.TestServiceAuditRecord;
import tribefire.extension.audit.model.test.api.GetPersonData;

public class ServiceAuditTestRecordFactory implements ServiceProcessor<CreateServiceAuditRecord, TestServiceAuditRecord> {

	@Override
	public TestServiceAuditRecord process(ServiceRequestContext requestContext, CreateServiceAuditRecord request) {
		GetPersonData payloadRequest = (GetPersonData) request.getRequest();
		
		TestServiceAuditRecord record = TestServiceAuditRecord.T.create();
		
		record.setPersonId(payloadRequest.getPersonId());
		
		return record;
	}
}
