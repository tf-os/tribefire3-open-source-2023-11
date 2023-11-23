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
package tribefire.extension.demo.test.wire.space;

import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.gm.service.access.api.AccessProcessingConfiguration;
import com.braintribe.gm.service.access.wire.common.contract.AccessProcessingConfigurationContract;
import com.braintribe.gm.service.access.wire.common.contract.CommonAccessProcessingContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.extension.demo.model.api.EntityMarshallingRequest;
import tribefire.extension.demo.model.api.FindByText;
import tribefire.extension.demo.model.api.GetEmployeesByGender;
import tribefire.extension.demo.model.api.NewEmployee;
import tribefire.extension.demo.model.data.Person;
import tribefire.extension.demo.processing.EntityMarshallingProcessor;
import tribefire.extension.demo.processing.FindByTextProcessor;
import tribefire.extension.demo.processing.FindByTextProcessor.FindByTextExpert;
import tribefire.extension.demo.processing.GetEmployeesByGenderProcessor;
import tribefire.extension.demo.processing.NewEmployeeProcessor;
import tribefire.extension.demo.test.PersonFinder;
import tribefire.extension.demo.test.wire.contract.DemoTestContract;

@Managed
public class DemoTestSpace implements DemoTestContract {
	
	@Import
	private AccessProcessingConfigurationContract accessProcessingConfiguration;
	
	@Import
	private CommonAccessProcessingContract commonAccessProcessing;

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;
	
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		accessProcessingConfiguration.registerAccessConfigurer(this::configureAccesses);
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
	}
	
	private void configureAccesses(AccessProcessingConfiguration configuration) {
		configuration.registerAccess("test.access", Person.T.getModel().getMetaModel());
		configuration.registerAccessRequestProcessor(FindByText.T, findByTextProcessor());
		configuration.registerAccessRequestProcessor(GetEmployeesByGender.T, getEmployeesByGenderProcessor());
		configuration.registerAccessRequestProcessor(NewEmployee.T, newEmployeeProcessor());
	}
	
	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor("auth");
		bean.register(EntityMarshallingRequest.T, entityMarshallingProcessor());
	}
	
	@Managed
	private EntityMarshallingProcessor entityMarshallingProcessor() {
		EntityMarshallingProcessor bean = new EntityMarshallingProcessor();
		bean.setJsonMarshaller(jsonMarshaller());
		bean.setXmlMarshaller(xmlMarshaller());
		bean.setYamlMarshaller(yamlMarshaller());
		
		return bean;
	}
	
	@Managed
	private FindByTextProcessor findByTextProcessor()  {
		FindByTextProcessor bean = new FindByTextProcessor();
		bean.setRegistry(findByTextExpertRegistry());
		
		return bean;
	}
	
	@Managed
	private GetEmployeesByGenderProcessor getEmployeesByGenderProcessor()  {
		return new GetEmployeesByGenderProcessor();
	}
	
	@Managed
	private NewEmployeeProcessor newEmployeeProcessor() {
		return new NewEmployeeProcessor();
	}

	@Managed
	private CharacterMarshaller jsonMarshaller() {
		return new JsonStreamMarshaller();
	}

	@Managed
	private CharacterMarshaller xmlMarshaller() {
		return new StaxMarshaller();
	}

	@Managed
	private CharacterMarshaller yamlMarshaller() {
		return new YamlMarshaller();
	}
	
	@Managed
	private GmExpertRegistry findByTextExpertRegistry() {
		ConfigurableGmExpertRegistry bean = new ConfigurableGmExpertRegistry();
		bean.add(FindByTextExpert.class, Person.class, new PersonFinder());
		
		return bean;
	}
	
	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}
	
	@Override
	public PersistenceGmSessionFactory sessionFactory() {
		return commonAccessProcessing.sessionFactory();
	}
	
}
