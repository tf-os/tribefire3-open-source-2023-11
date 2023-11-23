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
package tribefire.extension.hydrux.processor;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static tribefire.extension.hydrux.model.deployment.prototyping.HxMainView.PROTOTYPING_DOMAIN_ID;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.lcd.StringTools;

import tribefire.extension.hydrux.model.api.HxRequest;
import tribefire.extension.hydrux.model.api.ResolveHxApplication;
import tribefire.extension.hydrux.model.deployment.HxApplication;
import tribefire.extension.hydrux.model.reason.DomainIdMissing;
import tribefire.extension.hydrux.model.reason.NoDomainFound;
import tribefire.extension.hydrux.model.reason.NoHxApplicationConfigured;
import tribefire.extension.hydrux.servlet.HydruxServlet;
import tribefire.extension.js.model.deployment.UxModule;

/**
 * @author peter.gazdik
 */
public class HxRequestProcessor extends AbstractDispatchingServiceProcessor<HxRequest, Object> {

	private ModelAccessoryFactory modelAccessoryFactory;
	private Supplier<PersistenceGmSession> cortexSessionFactory;

	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Required
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<HxRequest, Object> dispatching) {
		dispatching.registerReasoned(ResolveHxApplication.T, (c, r) -> resolveHxApplication(r));
	}

	private Maybe<HxApplication> resolveHxApplication(ResolveHxApplication request) {
		String domainId = request.getTargetDomainId();
		if (domainId == null)
			return DomainIdMissing.create("ResolveHxApplication.targetDomainId is null").asMaybe();

		Maybe<HxApplication> maybeResult = resolveHxApplication(modelAccessoryFactory, domainId, request.getUseCases());
		if (maybeResult.isIncomplete())
			return maybeResult;

		if (!PROTOTYPING_DOMAIN_ID.equals(domainId))
			return maybeResult;

		return fillUxModule(maybeResult.get(), request.getPrototypingModule());
	}

	/**
	 * Returns a {@link Maybe#isSatisfied() satisfied Maybe} if some meta-data was resolved, or an {@link Maybe#isUnsatisfied() unsatisfied} one
	 * otherwise - i.e. if not domainId
	 */
	public static Maybe<HxApplication> resolveHxApplication(ModelAccessoryFactory modelAccessoryFactory, String domainId, Set<String> useCases) {
		Maybe<CmdResolver> cmdResolver = getCmdResolver(modelAccessoryFactory, domainId);
		if (cmdResolver.isUnsatisfied())
			return cmdResolver.cast();

		HxApplication hxApplication = cmdResolver.get().getMetaData() //
				.useCases(useCases) //
				.meta(HxApplication.T) //
				.exclusive();

		if (hxApplication == null)
			return NoHxApplicationConfigured
					.create("HxApplication meta data could not have been resolved for domainId [" + domainId + "], useCases: " + useCases).asMaybe();

		return Maybe.complete(hxApplication);
	}

	private static Maybe<CmdResolver> getCmdResolver(ModelAccessoryFactory modelAccessoryFactory, String domainId) {
		try {
			ModelAccessory modelAccessory = modelAccessoryFactory.getForServiceDomain(domainId);

			return Maybe.complete(modelAccessory.getCmdResolver());

		} catch (Exception e) {
			return NoDomainFound.create(e.getMessage()).asMaybe();
		}
	}

	private Maybe<HxApplication> fillUxModule(HxApplication hxApplication, String prototypingModule) {
		if (prototypingModule == null)
			return InvalidArgument.create("ResolveHxApplication.prototypingModule is null. " + HydruxServlet.prototypingParamExplanation()).asMaybe();

		Maybe<UxModule> uxModule = resolveUxModule(prototypingModule);
		if (uxModule.isUnsatisfied())
			return uxModule.cast();

		HxApplication clone = hxApplication.clone(new StandardCloningContext());
		clone.getView().setModule(uxModule.get());

		return Maybe.complete(clone);
	}

	private Maybe<UxModule> resolveUxModule(String prototypingModule) {
		PersistenceGmSession session = cortexSessionFactory.get();

		if (prototypingModule.contains(":")) {
			String globalId = "js-ux-module://" + prototypingModule;
			UxModule result = session.findEntityByGlobalId(globalId);
			if (result == null)
				return noUxModuleFound(prototypingModule);
			else
				return Maybe.complete(result);
		}

		List<UxModule> list = session.query()
				.entities(EntityQueryBuilder.from(UxModule.T).where().property(GenericEntity.globalId).like("*:" + prototypingModule).done()).list();

		switch (list.size()) {
			case 0:
				return noUxModuleFound(prototypingModule);
			case 1:
				return Maybe.complete(first(list));
			default:
				return InvalidArgument.create("Multiple UX Modules found for name [" + prototypingModule + "]. Modules: " + uxModuleNameList(list))
						.asMaybe();
		}
	}

	private static String uxModuleNameList(List<UxModule> list) {
		return list.stream() //
				.map(UxModule::getGlobalId) //
				.map(gid -> StringTools.removePrefixIfEligible(gid, UxModule.GLOBAL_ID_PREFIX)) //
				.collect(Collectors.joining(", "));
	}

	private static Maybe<UxModule> noUxModuleFound(String pattern) {
		return NotFound.create("No UxModule found: " + pattern).asMaybe();
	}

}
