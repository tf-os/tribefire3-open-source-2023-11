<#import '/imports/context.ftl' as context>
${template.relocate(context.relocation(context.wireSpaceFull))}
package ${context.wireSpacePackage};

import com.braintribe.gm.service.access.api.AccessProcessingConfiguration;
import com.braintribe.gm.service.access.wire.common.contract.AccessProcessingConfigurationContract;
import com.braintribe.gm.service.access.wire.common.contract.CommonAccessProcessingContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

<#if request.serviceProcessorSample>
import ${context.abstractRequestFull};
import ${context.requestProcessorFull};
</#if>
import ${context.wireContractFull};

@Managed
public class ${context.wireSpaceSimple} implements ${context.wireContractSimple} {

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
		serviceProcessingConfiguration.registerSecurityConfigurer(this::configureSecurity);
	}

	private void configureAccesses(AccessProcessingConfiguration configuration) {
		// TODO register accesses and tested access service request processors
		/* 
			configuration.registerAccess("some.access", someModel());
			configuration.registerAccessRequestProcessor(SomeAccessServiceRequest.T, someAccessServiceRequestProcessor());
		*/
	}

	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor(CommonServiceProcessingContract.AUTH_INTERCEPTOR_ID);
		// TODO register or remove interceptors and register tested service processors
		/*
			bean.registerInterceptor("someInterceptor");
			bean.removeInterceptor("someInterceptor");
			bean.register(SomeServiceRequest.T, someServiceProcessor());
		*/
<#if request.serviceProcessorSample>
		bean.register(${context.abstractRequestSimple}.T, ${context.requestProcessorMethod}());
</#if>
	}

<#if request.serviceProcessorSample>
	@Managed
	private ${context.requestProcessorSimple} ${context.requestProcessorMethod}() {
		return new ${context.requestProcessorSimple}();		
	}

</#if>
	private void configureSecurity(InMemorySecurityServiceProcessor bean) {
		// TODO add users IF your requests are to be authorized while testing
		// (make sure the 'auth' interceptor is NOT REMOVED in that case in the 'configureServices' method)
		/* 
			User someUser = User.T.create();
			user.setId("someUserId");
			user.setName("someUserName");
			user.setPassword("somePassword");

			bean.addUser(someUser);
		*/
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