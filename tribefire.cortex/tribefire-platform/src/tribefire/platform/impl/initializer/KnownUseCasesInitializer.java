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
package tribefire.platform.impl.initializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

public class KnownUseCasesInitializer extends SimplePersistenceInitializer {
	
	private final List<KnownUseCase> defaultUseCases = Arrays.asList(KnownUseCase.values());
	private List<String> additionalUserCases = Collections.emptyList();
	
	
	public void setAdditionalUseCase(List<String> additionalUserCases) {
		this.additionalUserCases = additionalUserCases;
	}
	
	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		
		ManagedGmSession session = context.getSession();
		
		for (KnownUseCase useCase : defaultUseCases) {
			String useCaseValue = TribefireRuntime.getProperty(useCase.name(), useCase.getDefaultValue());
			UseCaseSelector useCaseSelector = session.create(UseCaseSelector.T, "selector:useCase/gme."+useCase.name());
			useCaseSelector.setUseCase(useCaseValue);
		}
		
		for (String useCase : additionalUserCases) {

			UseCaseSelector useCaseSelector = session.create(UseCaseSelector.T, "selector:useCase/"+useCase);
			useCaseSelector.setUseCase(useCase);

		}
		
		
	}
	

}
