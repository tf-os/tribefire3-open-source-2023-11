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
package tribefire.platform.wire.space.common;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.common.OptionsEnrichingMarshaller;
import com.braintribe.gm.marshaller.resource.aware.ResourceAwareMarshaller;
import com.braintribe.gm.marshaller.threshold.ThresholdPersistenceMarshaller;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.messaging.dmb.GmDmbMqMessaging;
import com.braintribe.model.processing.deployment.api.SchrodingerBean;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.transport.messaging.dbm.GmDmbMqConnectionProvider;
import com.braintribe.transport.messaging.impl.StandardMessagingSessionProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.MessagingContract;
import tribefire.platform.wire.contract.MessagingRuntimePropertiesContract;
import tribefire.platform.wire.space.SchrodingerBeansSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;

/**
 * @author peter.gazdik
 */
@Managed
public class MessagingSpace implements MessagingContract {

	public static final String DEFAULT_MESSAGING_EXTERNAL_ID = "default.Messaging";

	@Import
	private BindersSpace binders;

	@Import
	private CartridgeInformationSpace cartridgeInformation;

	@Import
	private DeploymentSpace deployment;

	@Import
	private MarshallingSpace marshalling;

	@Import
	private MessagingDestinationsSpace messagingDestinations;

	@Import
	private RpcSpace rpc;

	@Import
	private SchrodingerBeansSpace schrodingerBeans;

	@Import
	private MessagingRuntimePropertiesContract messagingRuntimeProperties;

	@Override
	public MessagingDestinationsSpace destinations() {
		return messagingDestinations;
	}

	@Managed
	@Override
	public MessagingSessionProvider sessionProvider() {
		StandardMessagingSessionProvider bean = new StandardMessagingSessionProvider();
		bean.setMessagingConnectionProvider(messagingSchrodingerBean().proxy());
		return bean;
	}

	@Managed
	public SchrodingerBean<MessagingConnectionProvider<?>> messagingSchrodingerBean() {
		return schrodingerBeans.newBean("Messaging", CortexConfiguration::getMessaging, binders.messaging());
	}

	@Managed
	public MessagingConnectionProvider<?> defaultMessagingConnectionSupplier() {
		GmDmbMqConnectionProvider bean = new GmDmbMqConnectionProvider();
		bean.setConnectionConfiguration(GmDmbMqMessaging.T.create());
		bean.setMessagingContext(context());

		return bean;
	}

	@Override
	@Managed
	public MessagingContext context() {
		MessagingContext bean = new MessagingContext();
		bean.setMarshaller(messageMarshaller());
		bean.setApplicationId(cartridgeInformation.applicationId());
		bean.setNodeId(cartridgeInformation.nodeId());
		return bean;
	}

	@Managed
	private Marshaller messageMarshaller() {
		OptionsEnrichingMarshaller bean = new OptionsEnrichingMarshaller();
		bean.setDelegate(thresholdPersistenceMarshaller());
		bean.setDeserializationOptionsEnricher(o -> o.derive().setRequiredTypesReceiver(marshalling.requiredTypeEnsurer()).build());

		return bean;
	}

	@Managed
	private ThresholdPersistenceMarshaller thresholdPersistenceMarshaller() {
		ThresholdPersistenceMarshaller bean = new ThresholdPersistenceMarshaller();
		bean.setDelegate(resourceAwareMarshaller());
		bean.setSubstituteResourceMarshaller(marshalling.binMarshaller());
		bean.setThreshold(messagingRuntimeProperties.TRIBEFIRE_MESSAGING_TRANSIENT_PERSISTENCE_THRESHOLD());
		bean.setAccessId(TribefireConstants.ACCESS_TRANSIENT_MESSAGING_DATA);
		bean.setEvaluator(rpc.systemServiceRequestEvaluator());

		return bean;
	}

	@Managed
	private ResourceAwareMarshaller resourceAwareMarshaller() {
		ResourceAwareMarshaller bean = new ResourceAwareMarshaller();
		bean.setGmDataMimeType("application/gm");
		bean.setMarshaller(marshalling.binMarshaller());
		return bean;
	}

}
