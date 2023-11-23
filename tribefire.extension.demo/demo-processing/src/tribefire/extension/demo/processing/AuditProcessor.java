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

import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.BeforeStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

import tribefire.extension.demo.model.data.AuditRecord;
import tribefire.extension.demo.model.data.Person;

public class AuditProcessor implements StateChangeProcessor<Person, AuditRecord> {

	@Override
	public AuditRecord onBeforeStateChange(BeforeStateChangeContext<Person> context) throws StateChangeProcessorException {
		if (context.getManipulation() instanceof DeleteManipulation) {
			AuditRecord auditRecord = AuditRecord.T.create();
			auditRecord.setInfo("deleted Person with id " + context.getProcessEntity().getId());
			return auditRecord;
		}
		return null;
	}

	@Override
	public void onAfterStateChange(AfterStateChangeContext<Person> context, AuditRecord customContext) throws StateChangeProcessorException {
		// record create and delete events
		switch (context.getManipulation().manipulationType()) {
			case ADD:
			case CHANGE_VALUE:
			case CLEAR_COLLECTION:
			case REMOVE:
				PropertyManipulation m = (PropertyManipulation) context.getManipulation();
				AuditRecord changeRecord = context.getSession().create(AuditRecord.T);
				changeRecord.setDate(new Date());
				changeRecord.setInfo("Value of property: "+m.getOwner().getPropertyName()+" of Person with id "+context.getProcessEntity().getId()+" changed.");
				break;
			case INSTANTIATION:
				AuditRecord createRecord = context.getSession().create(AuditRecord.T);
				createRecord.setDate(new Date());
				createRecord.setInfo("created Person with id " + context.getProcessEntity().getId());
				break;
			case DELETE:
				AuditRecord deleteRecord = context.getSession().create(AuditRecord.T);
				deleteRecord.setDate(new Date());
				deleteRecord.setInfo(customContext.getInfo());
				break;
			default:
				break;
		}
	}

	@Override
	public StateChangeProcessorCapabilities getCapabilities() {
		return StateChangeProcessors.capabilities(true, true, false);
	}
}
