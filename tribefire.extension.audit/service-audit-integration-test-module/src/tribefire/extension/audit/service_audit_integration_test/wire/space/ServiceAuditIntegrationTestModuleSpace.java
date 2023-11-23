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
package tribefire.extension.audit.service_audit_integration_test.wire.space;

import static com.braintribe.wire.api.util.Lists.list;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.linkedMap;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Maps;

import tribefire.extension.audit.model.integration.test.deployment.ServiceAuditIntegrationTestAuditRecordFactory;
import tribefire.extension.audit.model.integration.test.deployment.ServiceAuditIntegrationTestServiceProcessor;
import tribefire.extension.audit.model.test.data.Person;
import tribefire.extension.audit.service_audit_integration_test.impl.ServiceAuditTestProcessor;
import tribefire.extension.audit.service_audit_integration_test.impl.ServiceAuditTestRecordFactory;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class ServiceAuditIntegrationTestModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// Bind deployment experts for deployable denotation types.
		// Note that the basic component binders (for e.g. serviceProcessor or incrementalAccess) can be found via tfPlatform.deployment().binders().
		bindings.bind(ServiceAuditIntegrationTestServiceProcessor.T) //
			.component(tfPlatform.binders().serviceProcessor()) //
			.expertFactory(this::testProcessor);
		
		bindings.bind(ServiceAuditIntegrationTestAuditRecordFactory.T)
			.component(tfPlatform.binders().serviceProcessor()) //
			.expertFactory(this::testRecordFactory);
	}
	
	@Managed
	private ServiceAuditTestRecordFactory testRecordFactory(ExpertContext<ServiceAuditIntegrationTestAuditRecordFactory> context) {
		ServiceAuditTestRecordFactory bean = new ServiceAuditTestRecordFactory();
		return bean;
	}
	
	@Managed
	private ServiceAuditTestProcessor testProcessor(ExpertContext<ServiceAuditIntegrationTestServiceProcessor> context) {
		ServiceAuditTestProcessor bean = new ServiceAuditTestProcessor();
		bean.setData(linkedMap(
			personEntry(personOne()),
			personEntry(personTwo()),
			personEntry(personX()),
			personEntry(personZ())
		));
		return bean;
	}
	
	private static Maps.Entry<String, Person> personEntry(Person person) {
		return entry(person.getId(), person);
	}
	
	private static Date birthday(int year, int month, int day) {
		return Date.from(LocalDate.of(year, month, day).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
	}
	
	private Person person(String id, String name, String lastName, Date birthday) {
		Person p = Person.T.create();
		p.setId(id);
		p.setBirthday(birthday);
		p.setName(name);
		p.setName(lastName);
		return p;
	}
	
	@Managed
	private Person personOne() {
		Person bean = person("one", "Derek", "Zeddai", birthday(1967, 10, 9));
		bean.setFriends(list(personX(), personZ(), personTwo()));
		return bean;
	}
	
	@Managed
	private Person personTwo() {
		Person bean =  person("two", "Anita", "Xsei", birthday(1929, 1, 1));
		bean.setFriends(list(personZ(), personX(), personOne()));
		return bean;
	}
	
	@Managed
	private Person personX() {
		Person bean = person("x", "TheX", "Xstal", birthday(2000, 2, 14));
		bean.setFriends(list(personOne(), personTwo()));
		bean.setPartner(personZ());
		return bean;
	}
	
	@Managed
	private Person personZ() {
		Person bean = person("z", "TheZ", "Zed", birthday(2011, 7, 14)); 
		bean.setFriends(list(personOne(), personTwo()));
		bean.setPartner(personX());
		return bean;
	}
	
	

}
