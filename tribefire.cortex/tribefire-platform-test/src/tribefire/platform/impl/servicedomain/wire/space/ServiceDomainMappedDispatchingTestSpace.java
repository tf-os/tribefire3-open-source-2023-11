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
package tribefire.platform.impl.servicedomain.wire.space;

import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.extensiondeployment.HardwiredServiceAroundProcessor;
import com.braintribe.model.extensiondeployment.HardwiredServicePostProcessor;
import com.braintribe.model.extensiondeployment.HardwiredServicePreProcessor;
import com.braintribe.model.extensiondeployment.HardwiredServiceProcessor;
import com.braintribe.model.extensiondeployment.ServiceAroundProcessor;
import com.braintribe.model.extensiondeployment.ServicePostProcessor;
import com.braintribe.model.extensiondeployment.ServicePreProcessor;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.PostProcessWith;
import com.braintribe.model.extensiondeployment.meta.PreProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;
import com.braintribe.testing.tools.gm.session.TestModelAccessoryFactory;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.cortex.model.sdmt.BehaveRequest;
import tribefire.cortex.model.sdmt.DomainAwareRequest;
import tribefire.cortex.model.sdmt.TestRequest1;
import tribefire.cortex.model.sdmt.TestRequest2;
import tribefire.cortex.model.sdmt.TestRequest3;
import tribefire.platform.impl.deployment.BasicDeployRegistry;
import tribefire.platform.impl.deployment.ComponentInterfaceBindingsRegistry;
import tribefire.platform.impl.deployment.ConfigurableDeployedUnit;
import tribefire.platform.impl.deployment.proxy.ProxyingDeployedComponentResolver;
import tribefire.platform.impl.service.ServiceDomainMappedDispatchingInterceptor;
import tribefire.platform.impl.servicedomain.DomainAwareProcessor;
import tribefire.platform.impl.servicedomain.ServiceDomainMappedDispatchingTestCommons;
import tribefire.platform.impl.servicedomain.wire.contract.ServiceDomainMappedDispatchingTestContract;

@Managed
public class ServiceDomainMappedDispatchingTestSpace implements ServiceDomainMappedDispatchingTestContract, ServiceDomainMappedDispatchingTestCommons {

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;
	
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
		serviceProcessingConfiguration.registerSecurityConfigurer(this::configureSecurity);
	}
	
	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.register(TestRequest3.T, (c,r) -> r.getBehaveAlternatively()? RETVAL_HARDWIRED_SERVICE_PROCESSOR_ALT: RETVAL_HARDWIRED_SERVICE_PROCESSOR);
		bean.registerInterceptor("domain-mapped-dispatching").registerWithPredicate(r -> !r.system(), domainMappedDispatchingInterceptor());
	}
	
	private void configureSecurity(InMemorySecurityServiceProcessor bean) {
		User user = User.T.create();
		user.setId("tester");
		user.setName("tester");
		user.setPassword("7357");

		bean.addUser(user);
	}
	
	private ServiceDomainMappedDispatchingInterceptor domainMappedDispatchingInterceptor() {
		ServiceDomainMappedDispatchingInterceptor bean = new ServiceDomainMappedDispatchingInterceptor();
		
		bean.setModelAccessoryFactory(modelAccessoryFactory());
		bean.setDeployedComponentResolver(deployedComponentResolver());
		
		return bean;
	}
	
	private TestModelAccessoryFactory modelAccessoryFactory() {
		TestModelAccessoryFactory bean = new TestModelAccessoryFactory();
		bean.registerServiceModelAccessory(SERVICE_DOMAIN1, model1());
		bean.registerServiceModelAccessory(SERVICE_DOMAIN2, model2());
		bean.registerServiceModelAccessory(SERVICE_DOMAIN3, model3());
		bean.registerServiceModelAccessory(SERVICE_DOMAIN4, model4());
		return bean;
	}
	
	private GmMetaModel model1() {
		GmMetaModel bean = GmMetaModel.T.create();

		GmMetaModel skeletonModel = GMF.getTypeReflection().getModel("tribefire.cortex:service-domain-mapping-test-model").getMetaModel();
		
		bean.setName("tribefire.cortext:configured-service-domain-mapping-test-model");
		bean.getDependencies().add(skeletonModel);
		
		BasicModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(bean).done();
		
		
		mdEditor.onEntityType(TestRequest1.T).addMetaData(processWithTestProcessor1());
		mdEditor.onEntityType(TestRequest2.T).addMetaData(processWithTestProcessor2());
		mdEditor.onEntityType(DomainAwareRequest.T).addMetaData(processWithDomainAwareProcessor());
		
		return bean;
	}
	
	private GmMetaModel model2() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		GmMetaModel skeletonModel = GMF.getTypeReflection().getModel("tribefire.cortex:service-domain-mapping-test-model").getMetaModel();
		
		bean.setName("tribefire.cortext:configured-service-domain-mapping-test-model");
		bean.getDependencies().add(skeletonModel);
		
		BasicModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(bean).done();
		
		mdEditor.onEntityType(TestRequest1.T).addMetaData(processWithTestProcessor1());
		mdEditor.onEntityType(TestRequest2.T).addMetaData(processWithUndeployedTestProcessor2());
		mdEditor.onEntityType(BehaveRequest.T).addMetaData(preProcessWithTestPreProcessor1());
		mdEditor.onEntityType(DomainAwareRequest.T).addMetaData(processWithDomainAwareProcessor());
		
		return bean;
	}
	
	private GmMetaModel model3() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		GmMetaModel skeletonModel = GMF.getTypeReflection().getModel("tribefire.cortex:service-domain-mapping-test-model").getMetaModel();
		
		bean.setName("tribefire.cortext:configured-service-domain-mapping-test-model");
		bean.getDependencies().add(skeletonModel);
		
		BasicModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(bean).done();
		
		mdEditor.onEntityType(TestRequest1.T).addMetaData(processWithTestProcessor1());
		mdEditor.onEntityType(ServiceRequest.T).addMetaData(postProcessWithTestPostProcessor1());
		
		return bean;
	}
	
	private GmMetaModel model4() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		GmMetaModel skeletonModel = GMF.getTypeReflection().getModel("tribefire.cortex:service-domain-mapping-test-model").getMetaModel();
		
		bean.setName("tribefire.cortext:configured-service-domain-mapping-test-model");
		bean.getDependencies().add(skeletonModel);
		
		BasicModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(bean).done();
		
		mdEditor.onEntityType(TestRequest1.T).addMetaData(processWithTestProcessor1());
		mdEditor.onEntityType(ServiceRequest.T).addMetaData(aroundProcessWithTestAroundProcessor1());
		
		return bean;
	}
	
	private ProcessWith processWithTestProcessor1() {
		ProcessWith bean = ProcessWith.T.create();
		bean.setProcessor(testProcessor1());
		return bean;
	}
	
	private ProcessWith processWithDomainAwareProcessor() {
		ProcessWith bean = ProcessWith.T.create();
		bean.setProcessor(domainAwareProcessor());
		return bean;
	}
	
	private PreProcessWith preProcessWithTestPreProcessor1() {
		PreProcessWith bean = PreProcessWith.T.create();
		bean.setProcessor(testPreProcessor1());
		return bean;
	}
	
	private PostProcessWith postProcessWithTestPostProcessor1() {
		PostProcessWith bean = PostProcessWith.T.create();
		bean.setProcessor(testPostProcessor1());
		return bean;
	}
	
	private AroundProcessWith aroundProcessWithTestAroundProcessor1() {
		AroundProcessWith bean = AroundProcessWith.T.create();
		bean.setProcessor(testAroundProcessor1());
		return bean;
	}
	
	private ProcessWith processWithTestProcessor2() {
		ProcessWith processWith = ProcessWith.T.create();
		processWith.setProcessor(testProcessor2());
		return processWith;
	}

	private ProcessWith processWithUndeployedTestProcessor2() {
		ProcessWith processWith = ProcessWith.T.create();
		processWith.setProcessor(undeployedTestProcessor2());
		return processWith;
	}
	
	@Managed
	private HardwiredServiceProcessor testProcessor1() {
		HardwiredServiceProcessor bean = HardwiredServiceProcessor.T.create();
		bean.setDeploymentStatus(DeploymentStatus.deployed);
		bean.setExternalId("testProcessor1");
		bean.setName("Test Processor 1");
		return bean;
	}
	
	@Managed
	private HardwiredServicePreProcessor testPreProcessor1() {
		HardwiredServicePreProcessor bean = HardwiredServicePreProcessor.T.create();
		bean.setDeploymentStatus(DeploymentStatus.deployed);
		bean.setExternalId("testPreProcessor1");
		bean.setName("Test PreProcessor 1");
		return bean;
	}
	
	@Managed
	private HardwiredServicePostProcessor testPostProcessor1() {
		HardwiredServicePostProcessor bean = HardwiredServicePostProcessor.T.create();
		bean.setDeploymentStatus(DeploymentStatus.deployed);
		bean.setExternalId("testPostProcessor1");
		bean.setName("Test PostProcessor 1");
		return bean;
	}
	
	@Managed
	private HardwiredServiceAroundProcessor testAroundProcessor1() {
		HardwiredServiceAroundProcessor bean = HardwiredServiceAroundProcessor.T.create();
		bean.setDeploymentStatus(DeploymentStatus.deployed);
		bean.setExternalId("testAroundProcessor1");
		bean.setName("Test AroundProcessor 1");
		return bean;
	}
	
	@Managed
	private HardwiredServiceProcessor testProcessor2() {
		HardwiredServiceProcessor bean = HardwiredServiceProcessor.T.create();
		bean.setDeploymentStatus(DeploymentStatus.deployed);
		bean.setExternalId("testProcessor2");
		bean.setName("Test Processor 2");
		return bean;
	}
	
	@Managed
	private HardwiredServiceProcessor undeployedTestProcessor2() {
		HardwiredServiceProcessor bean = HardwiredServiceProcessor.T.create();
		bean.setDeploymentStatus(DeploymentStatus.deployed);
		bean.setExternalId("undeployedTestProcessor2");
		bean.setName("Undeployed Test Processor 2");
		return bean;
	}
	
	@Managed
	private HardwiredServiceProcessor domainAwareProcessor() {
		HardwiredServiceProcessor bean = HardwiredServiceProcessor.T.create();
		bean.setDeploymentStatus(DeploymentStatus.deployed);
		bean.setExternalId("domainAwareProcessor");
		bean.setName("Domain Aware Processor");
		return bean;
	}
	
	@Managed
	private DeployRegistry deployRegistry() {
		BasicDeployRegistry bean = new BasicDeployRegistry();
		
		ConfigurableDeployedUnit deployedUnit1 = new ConfigurableDeployedUnit();
		
		deployedUnit1.put(ServiceProcessor.T, 
				(com.braintribe.model.processing.service.api.ServiceProcessor<TestRequest1, Object>)
				(c, r) -> r.getBehaveAlternatively()? RETVAL_SERVICE_PROCESSOR1_ALT: RETVAL_SERVICE_PROCESSOR1);
		
		bean.register(testProcessor1(), deployedUnit1);
		
		ConfigurableDeployedUnit deployedUnit2 = new ConfigurableDeployedUnit();
		
		deployedUnit2.put(ServiceProcessor.T, 
				(com.braintribe.model.processing.service.api.ServiceProcessor<TestRequest2, Object>)
				(c, r) -> RETVAL_SERVICE_PROCESSOR2);
		
		bean.register(testProcessor2(), deployedUnit2);
		
		ConfigurableDeployedUnit deployedUnit3 = new ConfigurableDeployedUnit();
		deployedUnit3.put(ServicePreProcessor.T, (com.braintribe.model.processing.service.api.ServicePreProcessor<BehaveRequest>)(c, r) -> {
			r.setBehaveAlternatively(true);
			return r;
		});
		
		bean.register(testPreProcessor1(), deployedUnit3);

		ConfigurableDeployedUnit deployedUnit4 = new ConfigurableDeployedUnit();
		deployedUnit4.put(ServicePostProcessor.T, (com.braintribe.model.processing.service.api.ServicePostProcessor<String>)(c,s) -> s.toUpperCase());
		bean.register(testPostProcessor1(), deployedUnit4);
		
		ConfigurableDeployedUnit deployedUnit5 = new ConfigurableDeployedUnit();
		deployedUnit5.put(ServiceProcessor.T, new DomainAwareProcessor());
		bean.register(domainAwareProcessor(), deployedUnit5);
		
		ConfigurableDeployedUnit deployedUnit6 = new ConfigurableDeployedUnit();
		deployedUnit6.put(ServiceAroundProcessor.T, (com.braintribe.model.processing.service.api.ServiceAroundProcessor<BehaveRequest, String>)(c,r,p) -> {
			r.setBehaveAlternatively(true);
			String response = p.proceed(r);
			return response.toUpperCase();
		});
		bean.register(testAroundProcessor1(), deployedUnit6);
		
		return bean;
	}
	
	@Managed
	private ProxyingDeployedComponentResolver deployedComponentResolver() {
		ProxyingDeployedComponentResolver bean = new ProxyingDeployedComponentResolver();
		
		bean.setDeployRegistry(deployRegistry());
		bean.setComponentInterfaceBindings(componentInterfaceBindings());
		bean.setProcessingInstanceId(InstanceId.of("testnode", "testapp"));
		
		return bean;
	}
	
	@Managed
	private ComponentInterfaceBindingsRegistry componentInterfaceBindings() {
		ComponentInterfaceBindingsRegistry bean = new ComponentInterfaceBindingsRegistry();
		bean.registerComponentInterfaces(ServiceProcessor.T, com.braintribe.model.processing.service.api.ServiceProcessor.class);
		bean.registerComponentInterfaces(ServicePreProcessor.T, com.braintribe.model.processing.service.api.ServicePreProcessor.class);
		bean.registerComponentInterfaces(ServicePostProcessor.T, com.braintribe.model.processing.service.api.ServicePostProcessor.class);
		bean.registerComponentInterfaces(ServiceAroundProcessor.T, com.braintribe.model.processing.service.api.ServiceAroundProcessor.class);
		return bean;
	}
	
	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}
}
