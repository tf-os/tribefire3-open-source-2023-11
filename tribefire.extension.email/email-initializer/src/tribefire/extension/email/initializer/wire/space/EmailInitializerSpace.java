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
package tribefire.extension.email.initializer.wire.space;

import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.braintribe.gm.model.reason.meta.HttpStatusCode;
import com.braintribe.gm.model.reason.meta.LogReason;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.email.deployment.service.EmailServiceProcessor;
import com.braintribe.model.email.deployment.service.HealthCheckProcessor;
import com.braintribe.model.email.service.EmailServiceConstants;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.email.util.EmailConstants;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.email.initializer.DdraMappingsBuilder;
import tribefire.extension.email.initializer.wire.contract.EmailInitializerContract;
import tribefire.extension.email.initializer.wire.contract.EmailInitializerModelsContract;
import tribefire.extension.email.initializer.wire.contract.ExistingInstancesContract;

@Managed
public class EmailInitializerSpace extends AbstractInitializerSpace implements EmailInitializerContract, EmailConstants {

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private EmailInitializerModelsContract models;

	@Override
	@Managed
	public ServiceDomain apiServiceDomain() {
		ServiceDomain bean = create(ServiceDomain.T);
		bean.setExternalId(EmailServiceConstants.SERVICE_DOMAIN);
		bean.setName("Email Services");
		bean.setServiceModel(models.configuredEmailApiModel());
		return bean;
	}

	@Override
	@Managed
	public Set<DdraMapping> ddraMappings() {
		//@formatter:off
			Set<DdraMapping> bean =
						new DdraMappingsBuilder(
							EmailServiceConstants.SERVICE_DOMAIN,
							this::lookup,
							this::create)
						.build();
			//@formatter:on
		return bean;
	}

	@Managed
	@Override
	public EmailServiceProcessor emailServiceProcessor() {
		EmailServiceProcessor bean = create(EmailServiceProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setExternalId(EXTERNAL_ID_EMAIL_SERVICE_PROCESSOR);
		bean.setName("Email Service Processor");

		return bean;
	}

	@Managed
	@Override
	public MetaData processWithEmailServiceProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(emailServiceProcessor());
		return bean;
	}

	@Override
	@Managed
	public CheckBundle connectivityCheckBundle() {

		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName("Email Checks");
		bean.setWeight(CheckWeight.under10s);
		bean.setCoverage(CheckCoverage.connectivity);
		bean.setIsPlatformRelevant(false);

		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setName("Email Check Processor");
		bean.setExternalId(EXTERNAL_ID_EMAIL_HEALTHCHECK_PROCESSOR);
		return bean;
	}

	@Managed
	@Override
	public HttpStatusCode httpStatus500Md() {
		HttpStatusCode bean = create(HttpStatusCode.T);
		bean.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return bean;
	}
	@Managed
	@Override
	public HttpStatusCode httpStatus501Md() {
		HttpStatusCode bean = create(HttpStatusCode.T);
		bean.setCode(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		return bean;
	}
	@Managed
	@Override
	public HttpStatusCode httpStatus502Md() {
		HttpStatusCode bean = create(HttpStatusCode.T);
		bean.setCode(HttpServletResponse.SC_BAD_GATEWAY);
		return bean;
	}
	@Managed
	@Override
	public HttpStatusCode httpStatus404Md() {
		HttpStatusCode bean = create(HttpStatusCode.T);
		bean.setCode(HttpServletResponse.SC_NOT_FOUND);
		return bean;
	}
	@Managed
	@Override
	public LogReason logReasonTrace() {
		LogReason bean = create(LogReason.T);
		bean.setLevel(LogLevel.TRACE);
		return bean;
	}

}
