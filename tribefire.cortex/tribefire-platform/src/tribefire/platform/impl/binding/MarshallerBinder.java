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
package tribefire.platform.impl.binding;

import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.ConfigurableMarshallerRegistry;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.marshallerdeployment.HardwiredMarshaller;
import com.braintribe.model.marshallerdeployment.Marshaller;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployRegistryListener;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.deployment.api.PlainComponentBinder;

/**
 * @author peter.gazdik
 */
public class MarshallerBinder extends PlainComponentBinder<Marshaller, com.braintribe.codec.marshaller.api.Marshaller> implements LifecycleAware {

	private ConfigurableMarshallerRegistry marshallerRegistry;
	private DeployRegistry deployRegistry;
	private MarshallerDeploymentListener deploymentListener;

	@Required
	public void setMarshallerRegistry(ConfigurableMarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}

	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

	public MarshallerBinder() {
		super(Marshaller.T, com.braintribe.codec.marshaller.api.Marshaller.class);
	}

	@Override
	public void postConstruct() {
		deploymentListener = new MarshallerDeploymentListener();
		deployRegistry.addListener(deploymentListener);
	}

	@Override
	public void preDestroy() {
		deployRegistry.removeListener(deploymentListener);
	}

	private class MarshallerDeploymentListener implements DeployRegistryListener {

		@Override
		public void onDeploy(Deployable deployable, DeployedUnit deployedUnit) {
			if (!(deployable instanceof Marshaller))
				return;

			// Maybe temporary solution for the fact that we need to eagerly register core platform marshallers
			if (deployable instanceof HardwiredMarshaller && deployable.getModule() == null)
				return;
			
			Marshaller denotation = (Marshaller) deployable;
			com.braintribe.codec.marshaller.api.Marshaller expert = deployedUnit.getComponent(Marshaller.T);

			for (String mimeType : denotation.getMimeTypes())
				marshallerRegistry.registerMarshaller(mimeType, expert);
		}

		@Override
		public void onUndeploy(Deployable deployable, DeployedUnit deployedUnit) {
			if (!(deployable instanceof Marshaller))
				return;

			Marshaller denotation = (Marshaller) deployable;
			com.braintribe.codec.marshaller.api.Marshaller expert = deployedUnit.getComponent(Marshaller.T);

			for (String mimeType : denotation.getMimeTypes())
				marshallerRegistry.unregisterMarshaller(mimeType, expert);
		}

	}

}
