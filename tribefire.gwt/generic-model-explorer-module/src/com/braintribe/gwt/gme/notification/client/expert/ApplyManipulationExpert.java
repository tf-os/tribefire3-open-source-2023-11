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
package com.braintribe.gwt.gme.notification.client.expert;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gmview.client.GmEditionView;
import com.braintribe.gwt.gmview.client.GmEditionViewController;
import com.braintribe.gwt.gmview.client.TemplateSupport;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.proxy.ProxyEnhancedEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.uicommand.ApplyManipulation;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * Expert responsible for the implementation of the {@link ApplyManipulation} command.
 * @author michel.docouto
 *
 */
public class ApplyManipulationExpert implements CommandExpert<ApplyManipulation> {
	
	private static Logger logger = new Logger(ApplyManipulationExpert.class);
	private PersistenceGmSession dataSession;
	private PersistenceGmSession workbenchSession;
	private ExplorerConstellation explorerConstellation;
	private Supplier<GmEditionViewController> gmEditionViewSupportSupplier;
	
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	/**
	 * Configures the required support for editions.
	 */
	@Required
	public void setGmEditionViewSupport(Supplier<GmEditionViewController> gmEditionViewSupportSupplier) {
		this.gmEditionViewSupportSupplier = gmEditionViewSupportSupplier;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setDataSession(PersistenceGmSession dataSession) {
		this.dataSession = dataSession;
		
		/*new Timer() { //Test
			
			@Override
			public void run() {
				ApplyManipulation am = ApplyManipulation.T.create();
				
				EntityType<?> et = session.getModelAccessory().getOracle().getTypes().onlyEntities().asTypes().map(t -> ((EntityType<?>) t)).filter(t -> !t.isAbstract()).findFirst().get();
				Manipulation mani = ManipulationBuilder.instantiation(et.create());
				
//				ChangeValueManipulation mani = ChangeValueManipulation.T.create();
//				mani.setNewValue("acom.braintribe.model.accessapi.AccessDataRequest");
//				EntityProperty ep = EntityProperty.T.create();
//				ep.setPropertyName("typeSignature");
//				PersistentEntityReference reference = PersistentEntityReference.T.create();
//				reference.setTypeSignature("com.braintribe.model.meta.GmEntityType");
//				reference.setRefId("type:com.braintribe.model.accessapi.AccessDataRequest");
//				reference.setRefPartition("cortex");
//				ep.setReference(reference);
//				mani.setOwner(ep);
				
				am.setManipulation(mani);
				handleCommand(am);
			}
		}.schedule(20000);*/
	}
	
	/**
	 * Configures the optional workbench session. If the entities are found in there, then we will apply the manipulations in there.
	 */
	@Configurable
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}

	@Override
	public void handleCommand(ApplyManipulation command) {
		stopCurrentEdition();
		Manipulation manipulation = command.getManipulation();
		
		Set<GenericEntity> touchedEntities = manipulation.touchedEntities().collect(Collectors.toSet());
		boolean useWorkbenchSession = touchedEntities.stream().anyMatch(touchedEntity -> isProxyType(touchedEntity));
		
		touchedEntities = touchedEntities.stream().filter(e -> e.getId() != null).collect(Collectors.toSet());
		touchedEntities = BaseType.INSTANCE.clone(touchedEntities, null, StrategyOnCriterionMatch.reference);
		
		PersistenceGmSession theSession = useWorkbenchSession ? workbenchSession : dataSession;
		
		//We are merging only the touched entities which are not yet present in the session.
		for (GenericEntity touchedEntity : new HashSet<>(touchedEntities)) {
			if (theSession.queryCache().entity(touchedEntity.reference()).find() != null)
				touchedEntities.remove(touchedEntity);
		}

		theSession.merge().suspendHistory(true).adoptUnexposed(true).doFor(touchedEntities, AsyncCallback.of( //
				adoptedEntities -> {
					/* SelectQuery query = new SelectQueryBuilder().from(GenericEntity.T, "e").where().entity("e")
					 * .in(mani.touchedEntities().collect(Collectors.toSet())).done(); */

					Manipulation mani = manipulation;
					if (!manipulation.isRemote())
						mani = ManipulationRemotifier.remotify(manipulation);
					try {
						ManipulationReport report = theSession.manipulate().apply(mani);
						logger.info("Successfully applied the received manipulations.");

						GenericEntity entity = getEntityToEdit(report/* , adoptedEntities */);
						if (entity != null)
							explorerConstellation.onEditEntity(entity, entity.reference() instanceof PreliminaryEntityReference);
					} catch (GmSessionException ex) {
						logger.error("Error while applying the received manipulations.", ex);
					}
				}, e -> logger.error("Error while merging the entities.", e)));
	}
	
	private void stopCurrentEdition() {
		GmEditionView currentEditionView = gmEditionViewSupportSupplier.get().getCurrentEditionView();
		if (currentEditionView != null)
			currentEditionView.stopEditing();
	}

	private GenericEntity getEntityToEdit(ManipulationReport report) {
		return report.getInstantiations().values().stream().findFirst().orElse(null);
	}
	
	private boolean isProxyType(GenericEntity entity) {
		if (entity instanceof ProxyEnhancedEntity)
			return true;
		
		if (entity instanceof EntityReference) {
			String typeSignature = ((EntityReference) entity).getTypeSignature();
			return TemplateSupport.knownCallerClasses.stream().anyMatch(templateSignature -> typeSignature.contains(templateSignature));
		}
		
		return false;
	}
	
	/*private GenericEntity getEntityToEdit(ManipulationReport report, Set<GenericEntity> adoptedEntities) {
		return Stream.concat(report.getInstantiations().values().stream(), adoptedEntities.stream()).findFirst().orElse(null);
	}*/

}
