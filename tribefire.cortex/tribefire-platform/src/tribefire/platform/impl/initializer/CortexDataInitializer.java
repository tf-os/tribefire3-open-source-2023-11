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

import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.traverse.EntityCollector;

/**
 * Takes a supplier of an assembly ({@link #setDataSupplier(Supplier)} and pre-processes and imports the entities from that assembly into an access
 * which should be the cortex. The pre-processing of the entities includes clearance of Deployable.deploymentStatus as well as ensuring the globalId
 * @author Dirk Scheffler
 *
 */
public class CortexDataInitializer extends SimplePersistenceInitializer {

	protected static Logger logger = Logger.getLogger(CortexDataInitializer.class);

	private Supplier<?> dataSupplier;
	private String dataOrigin;
	private int idSequence = 1;
	
	/**
	 * Defines a name for the origin of the data which is used in logging and globalId ensurance as scheme prefix if a globalId is not given on an entity.
	 */
	@Required
	public void setDataOrigin(String dataOrigin) {
		this.dataOrigin = dataOrigin;
	}

	/**
	 * Configures the data supplier for the assembly that is imported during {@link #initializeData(PersistenceInitializationContext)} 
	 */
	@Required
	public void setDataSupplier(Supplier<?> dataSupplier) {
		this.dataSupplier = dataSupplier;
	}
	
	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		logger.info("Start of import of data from " + dataOrigin + " into access " + context.getAccessId());

		EntityPreProcessor globalIdEnsurer = new EntityPreProcessor(context);
		
		ConfigurableCloningContext cc = ConfigurableCloningContext.build() //
				.supplyRawCloneWith(context.getSession()) //
				.withOriginPreProcessor(globalIdEnsurer::preprocessEntity) // 
				.withCanTransferPropertyTest(globalIdEnsurer::canTransferProperty) //
				.done();
		
		EntityCollector entityCollector = new EntityCollector();
		
		entityCollector.visit(dataSupplier.get());
		entityCollector.getEntities() //
			.stream() // 
			.forEach(d -> {
					logger.debug(() -> "Importing entity " + d.entityType().getShortName() + " with globalId = " + d.getGlobalId() + " from "
							+ dataOrigin + " into access " + context.getAccessId());
					d.clone(cc);
			}); //
	}
	
	private class EntityPreProcessor {
		private final PersistenceInitializationContext context;
		
		public EntityPreProcessor(PersistenceInitializationContext context) {
			this.context = context;
		}

		@SuppressWarnings("unused")
		private boolean canTransferProperty(EntityType<?> entityType, Property property, GenericEntity origin, GenericEntity clone, AbsenceInformation originAi) {
			return !(Deployable.T.isAssignableFrom(entityType) && Deployable.deploymentStatus.equals(property.getName()));
		}

		private GenericEntity preprocessEntity(GenericEntity e) {
			if (e.getGlobalId() != null)
				return e;
			
			StringBuilder globalId = new StringBuilder(dataOrigin);
			globalId.append(':');
			
			if (e instanceof HasExternalId) {
				HasExternalId hasExternalId = (HasExternalId)e;
				
				if (hasExternalId.getExternalId() == null) {
					throw new IllegalStateException("Import from " + dataOrigin + " into " + context.getAccessId() + " failed because of missing externalId on " + e);
				}
				
				globalId.append(hasExternalId.getExternalId());
			} else {
				globalId.append(e.entityType().getShortName());
				globalId.append('/');
				globalId.append(idSequence++);
			}
			
			e.setGlobalId(globalId.toString());
			
			return e;
		}
	}
	
}
