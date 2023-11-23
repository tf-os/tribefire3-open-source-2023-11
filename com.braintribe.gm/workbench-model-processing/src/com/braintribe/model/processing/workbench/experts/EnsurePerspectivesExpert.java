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
package com.braintribe.model.processing.workbench.experts;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.WorkbenchInstructionContext;
import com.braintribe.model.processing.workbench.WorkbenchInstructionExpert;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessorException;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.workbench.KnownWorkenchPerspective;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.model.workbench.instruction.EnsurePerspectives;
import com.braintribe.utils.i18n.I18nTools;

public class EnsurePerspectivesExpert implements WorkbenchInstructionExpert<EnsurePerspectives> {
	
	private List<String> defaultPerspectives = Arrays.asList(
			KnownWorkenchPerspective.root.toString(),
			KnownWorkenchPerspective.homeFolder.toString(),
			KnownWorkenchPerspective.actionBar.toString(),
			KnownWorkenchPerspective.headerBar.toString(),
			KnownWorkenchPerspective.globalActionBar.toString(),
			KnownWorkenchPerspective.tabActionBar.toString());
	
	@Override
	public void process(EnsurePerspectives instruction, WorkbenchInstructionContext context) throws WorkbenchInstructionProcessorException {
		PersistenceGmSession session = context.getSession();
		
		//@formatter:off
		SelectQuery perspectiveQuery = 
				new SelectQueryBuilder()
					.select("p","name")
					.from(WorkbenchPerspective.class,"p")
				.done();
		//@formatter:on


		try {
			List<Object> existingPerspectives = session.query().select(perspectiveQuery).list();
			for (String perspectiveToEnsure : defaultPerspectives) {
				if (existingPerspectives.contains(perspectiveToEnsure)) {
					continue;
				}
				// perspective does not exist. create it.
				
				WorkbenchPerspective perspective = session.create(WorkbenchPerspective.T);
				perspective.setName(perspectiveToEnsure);
				// @formatter:off
				perspective.setDisplayName(
						I18nTools
							.lsBuilder()
							.factory(() -> session.create(LocalizedString.T))
							.addDefault(perspectiveToEnsure).build());
				// @formatter:on

			}
			
			
		} catch (GmSessionException e) {
			throw new WorkbenchInstructionProcessorException("Error while querying for existing WorkbenchPerspectives.",e);
		}
		
		
	}

}
