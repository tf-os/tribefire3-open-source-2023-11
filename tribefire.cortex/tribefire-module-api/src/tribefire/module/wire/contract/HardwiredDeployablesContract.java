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
package tribefire.module.wire.contract;

import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.extensiondeployment.HardwiredServiceProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.components.ServiceModelExtension;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.impl.ServiceProcessors;
import com.braintribe.model.service.api.PlatformRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.api.HardwiredComponentBinding;
import tribefire.module.api.HardwiredConfigurationModelBinder;
import tribefire.module.api.HardwiredServiceDomainBinder;

/**
 * Contract offering methods for biding hardwired functional components to your platform.
 * <p>
 * <b>All the methods must be called within {@link TribefireModuleContract#bindHardwired()} method.</b>
 * <p>
 * In general, different module-api artifacts can extend this contract with custom bind methods (e.g. <tt>HardwiredMarshallersContract</tt>) and
 * typically a platform offers a contract that unions all the supported contracts, e.g. <tt>WebPlatformHardwiredDeployablesContract</tt>. This
 * platform-specific should only be used in modules targeted for a specific platform, of course.
 * <p>
 * This contract offers two ways how to deploy your deployable. There is a low-level generic way using {@link #bind(HardwiredDeployable)} method,
 * where you can specify your {@link HardwiredDeployable} instance and provide suppliers for the actual expert implementation. The alternative is to
 * use a method dedicated to a given deployable like {@link #bindPlatformServiceProcessor(String, String, EntityType, ServiceProcessor)}.
 * <p>
 * Note that all methods which lead to a new instance of a {@link HardwiredDeployable} accept {@code externalId} and {@code name} as first two
 * parameters, which correspond to the equally-named properties of {@linkplain HardwiredDeployable}. The {@code externalId} has to be unique among all
 * the modules, and thus a value that reflects the module is recommended. The {@code name} is only a nice string to display for the user and is not
 * meant to look technical. We recommend using space separated words starting with uppercase, e.g. "XML Marshaller".
 * 
 * @author peter.gazdik
 */
public interface HardwiredDeployablesContract extends WireSpace {

	/**
	 * Entry point to a fluent API to bind {@link HardwiredDeployable}s generically.
	 * <p>
	 * With this method you can provide your own {@link Deployable denotation} instance and then specify the component-type/expert-factory pairs (via
	 * {@link ComponentBinder} and corresponding Supplier). This is in contrast with other methods that expressively bind a certain component (e.g.
	 * service processor or marshaller) and create the denotation instance internally.
	 * <p>
	 * Note that given deployable must not have it's {@link HardwiredDeployable#getModule() module} set, instead the module is set automatically
	 * inside of this method. If the module is set, this method throws an {@link IllegalArgumentException}.
	 */
	<HD extends HardwiredDeployable> HardwiredComponentBinding<HD> bind(HD deployable);

	/** Eager instantiation version of {@link #bindPlatformServiceProcessor(String, String, EntityType, Supplier)} */
	default <T extends PlatformRequest> HardwiredServiceProcessor bindPlatformServiceProcessor( //
			String externalId, String name, EntityType<T> requestType, ServiceProcessor<? super T, ?> serviceProcessor) {
		return bindPlatformServiceProcessor(externalId, name, requestType, () -> serviceProcessor);
	}

	/**
	 * Binds a service processor on the "platform" domain. Note that given {@code requestType}'s declaring model must be a part (dependency) of the
	 * platform's service model.
	 */
	<T extends PlatformRequest> HardwiredServiceProcessor bindPlatformServiceProcessor( //
			String externalId, String name, EntityType<T> requestType, Supplier<ServiceProcessor<? super T, ?>> serviceProcessorSupplier);

	/**
	 * First step of a fluent API for binding a {@link ServiceDomain}, which consists of one or more service processors, and any number of additional
	 * (service) models. (Adding extra models makes sense if they contain types compatible with configured service processors, i.e. they are extending
	 * the request types which the service processors are bound with.)
	 * <p>
	 * If a ServiceDomain with given domainExternalId already exists, it will be extended with new models and service processors, but the domain name
	 * given here will be ignore If it does not exists, a new one will be created..
	 */
	HardwiredServiceDomainBinder bindOnServiceDomain(String domainExternalId, String domainName);

	/**
	 * Similar to {@link #bindOnServiceDomain}, but requires that no {@link ServiceDomain} with given external id exists yet. If so, exception is
	 * thrown on initialization.
	 */
	HardwiredServiceDomainBinder bindOnNewServiceDomain(String domainExternalId, String domainName);

	/**
	 * Similar to {@link #bindOnServiceDomain}, but requires that a {@link ServiceDomain} with given external id already exists. If not, an exception
	 * is thrown on initialization.
	 */
	HardwiredServiceDomainBinder bindOnExistingServiceDomain(String domainExternalId);

	/**
	 * Allows configuration on a new or existing model. For now just adding dependencies and binding {@link ServiceProcessors} i.e. configuring
	 * ProcessWith meta-data on given {@link ServiceRequest} type with the processosr's denotation instance.
	 * <p>
	 * If you want to use these you need to add this model as a dependency of (most likely) your service domain.
	 * <p>
	 * Every module should clearly state which models it configures.
	 * 
	 * @see ServiceModelExtension
	 */
	HardwiredConfigurationModelBinder bindOnConfigurationModel(String modelName);

	/** Similar to {@link #bindOnConfigurationModel}, but requires no model with given name exists. If so, exception is thrown on initialization. */
	HardwiredConfigurationModelBinder bindOnNewConfigurationModel(String modelName);

	/** Similar to {@link #bindOnConfigurationModel}, but requires the model already exists. If not, an exception is thrown on initialization. */
	HardwiredConfigurationModelBinder bindOnExistingConfigurationModel(String modelName);

}
