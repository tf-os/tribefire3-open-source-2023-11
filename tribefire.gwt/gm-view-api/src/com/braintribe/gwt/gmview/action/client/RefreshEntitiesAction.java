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
package com.braintribe.gwt.gmview.action.client;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.not;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.NotificationViewHandler;
import com.braintribe.gwt.gmview.metadata.client.MetaDataEditorPanelHandler;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMETraversingCriterionUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.JunctionBuilder;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.uicommand.Refresh;
import com.braintribe.processing.async.api.AsyncCallback;

@SuppressWarnings("unusable-by-js")
public class RefreshEntitiesAction extends ModelAction {
	
	public static final String COMMAND_PROPERTY = "command";
	
	private boolean useMask;
	private Set<GenericEntity> withinRefreshingEntities;
	private Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion;
	private String useCase;
	private Supplier<GmContentView> currentContentViewProvider;
	private Set<GenericEntity> entitiesToRefresh;
	
	/**
	 * Configures a map containing special traversing criterion for the given entities.
	 * This is used when loading an absent property. Special entities (such as {@link LocalizedString}) require some properties to be loaded.
	 */
	@Configurable
	public void setSpecialEntityTraversingCriterion(Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion) {
		this.specialEntityTraversingCriterion = specialEntityTraversingCriterion;
	}
	
	/**
	 * Configures the useCase where this action is used on.
	 */
	@Configurable
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	@Override
	public void configureGmContentView(GmContentView gmContentView) {
		super.configureGmContentView(gmContentView);
	}
	
	/**
	 * Configures a Provider for getting the selection when no view was configured via {@link #configureGmContentView(GmContentView)}
	 */
	@Configurable
	public void setCurrentContentViewProvider(Supplier<GmContentView> currentContentViewProvider) {
		this.currentContentViewProvider = currentContentViewProvider;
	}
	
	/**
	 * Configures whether we mask while refreshing. Defaults to false.
	 */
	@Configurable
	public void setUseMask(boolean useMask) {
		this.useMask = useMask;
	}
	
	public RefreshEntitiesAction() {
		setName(LocalizedText.INSTANCE.refresh());
		setIcon(GmViewActionResources.INSTANCE.refresh());
		setHoverIcon(GmViewActionResources.INSTANCE.refreshBig());
		setHidden(true);
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}

	@Override
	protected void updateVisibility() {
		if 	(gmContentView instanceof MetaDataEditorPanelHandler || gmContentView instanceof NotificationViewHandler) {
			setHidden(true, true);
			return;
		}		
		
		if (modelPaths == null || modelPaths.isEmpty()) {
			setHidden(true);
			return;
		}
		
		for (List<ModelPath> selection : modelPaths) {
			for (ModelPath modelPath : selection) {
				for (ModelPathElement modelPathElement : modelPath) {
					if (modelPathElement.getValue() != null && modelPathElement.getValue() instanceof GenericEntity) {
						setHidden(false);
						return;
					}
				}
			}
		}
		
		setHidden(true);
	}
	
	private TraversingCriterion prepareTraversingCriterion(GenericEntity entity, EntityType<?> entityType, PersistenceGmSession gmSession) {
		GMEMetadataUtil.CondensationBean bean = GMEMetadataUtil.getEntityCondensationProperty(
				GMEMetadataUtil.getEntityCondensations(entity, entityType, gmSession.getModelAccessory().getMetaData(), useCase), false);
		
		JunctionBuilder<JunctionBuilder<TC>> junctionBuilder = TC.create()
				   .conjunction()
				    .property()
				    .typeCondition(or(isKind(TypeKind.entityType), isKind(TypeKind.collectionType)))
				    .negation()
				     .disjunction();
		
		if (bean != null && (bean.getMode().equals(CondensationMode.auto) || bean.getMode().equals(CondensationMode.forced))) {
			junctionBuilder = junctionBuilder
			  .pattern()
			   .entity(entityType)
			   .property(bean.getProperty())
			  .close();
		}
		
		TraversingCriterion tc = junctionBuilder
				.propertyType(LocalizedString.class)
                .pattern()
                 .entity(LocalizedString.class)
                 .property("localizedValues")
                .close()
                .disjunction() // disjunction 2
			      .pattern()
			       .root()
			       .listElement()
			       .entity()
			       .conjunction()
			        .property()
			        .typeCondition(not(isKind(TypeKind.setType)))
			       .close() // conjunction
			      .close() //pattern
			      .pattern()
			       .root()
			       .entity()
			       .conjunction()
			        .property()
			        .typeCondition(not(isKind(TypeKind.setType)))
			       .close() // conjunction
			      .close() //pattern
			     .close() // disjunction 2
			    .close() // disjunction
			   .close() // conjunction
			   .done();
		
		return GMEUtil.expandTc(tc);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		GmContentView view = gmContentView;
		if (view == null && currentContentViewProvider != null)
			view = currentContentViewProvider.get();
		
		GenericEntity entityToRefresh = null;
		if (triggerInfo != null) {
			Refresh refreshCommand = triggerInfo.get(COMMAND_PROPERTY);
			if (refreshCommand != null) {
				PersistentEntityReference referenceToRefresh = refreshCommand.getReferenceToRefresh();
				if (referenceToRefresh != null)
					entityToRefresh = resolveReference(referenceToRefresh, view);
			}
		}

		String useCase = null;
		if (view != null) {
			if (view instanceof GmAmbiguousSelectionSupport)
				modelPaths = ((GmAmbiguousSelectionSupport) view).getAmbiguousSelection();
			else
				modelPaths = view.transformSelection(view.getCurrentSelection());
			
			useCase = view.getUseCase();
		}
		
		if (entityToRefresh == null && (modelPaths == null || modelPaths.isEmpty()))
			return;
		
		if (useMask)
			GlobalState.mask(LocalizedText.INSTANCE.refreshing());
		
		if (entitiesToRefresh == null)
			entitiesToRefresh = newSet();
		else
			entitiesToRefresh.clear();
		
		if (entityToRefresh != null) {
			refreshEntity(entityToRefresh, useCase);
			return;
		}
		
		for (List<ModelPath> selection : modelPaths) {
			for (ModelPath modelPath : selection) {
				for (ModelPathElement modelPathElement : modelPath) {
					Object value = modelPathElement.getValue();
					if (!(value instanceof GenericEntity))
						continue;
					
					GenericEntity entity = (GenericEntity) value;
					refreshEntity(entity, useCase);
				}
			}
		}		
	}
	
	private void refreshEntity(GenericEntity entity, String useCase) {
		if (withinRefreshingEntities == null)
			withinRefreshingEntities = new HashSet<>();
		withinRefreshingEntities.add(entity);

		if (!(entity.session() instanceof PersistenceGmSession) || !entitiesToRefresh.add(entity)) {
			handleSuccess(entity);
			return;
		}
				
 		PersistenceGmSession gmSession = (PersistenceGmSession) entity.session();
		
		final EntityType<GenericEntity> entityType = entity.entityType();
		TraversingCriterion tc = prepareTraversingCriterion(entity, entityType, gmSession);
		tc = GMETraversingCriterionUtil.prepareForDepthTC(tc, null, entityType.getTypeSignature(), gmSession, useCase);
		gmSession.query().entity(entity).withTraversingCriterion(tc).refresh(AsyncCallback.of( //
				future -> {
					List<Property> propertiesToRefresh = new ArrayList<>();
					if (entityType.getProperties() != null) {
						for (Property property : entityType.getProperties()) {
							if (property.getType().isCollection()
									&& ((CollectionType) property.getType()).getCollectionKind().equals(CollectionKind.set))
								propertiesToRefresh.add(property);
						}
					}

					if (propertiesToRefresh.isEmpty()) {
						handleSuccess(entity);
						return;
					}

					refreshProperties(entity, entityType, propertiesToRefresh, gmSession) //
							.andThen(v -> handleSuccess(entity)) //
							.onError(e -> {
								handleSuccess(entity);
								e.printStackTrace();
							});
				}, e -> {
					handleSuccess(entity);
					e.printStackTrace();
				}));
	}
	
	private Future<Void> refreshProperties(GenericEntity entity, EntityType<GenericEntity> entityType, List<Property> properties, PersistenceGmSession gmSession) {
		final Future<Void> future = new Future<>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		
		int i = 0;
		for (Property property : properties)
			multiLoader.add(Integer.toString(i++), refreshProperty(entity, entityType, property, gmSession));
		
		multiLoader.load(AsyncCallbacks.of(v -> future.onSuccess(null), future::onFailure));
		
		return future;
	}
	
	private Loader<Void> refreshProperty(final GenericEntity entity, final EntityType<GenericEntity> entityType, final Property property, final PersistenceGmSession gmSession) {
		return asyncCallback -> {
			EntityReference reference = entity.reference();
			if (!(reference instanceof PersistentEntityReference)) {
				asyncCallback.onSuccess(null);
				return;
			}
			
			TraversingCriterion tc = getSpecialTraversingCriterion(property.getType().getJavaType());
			tc = GMETraversingCriterionUtil.prepareForDepthTC(tc, null, entityType.getTypeSignature(), gmSession, useCase);
			
			ModelMdResolver modelMdResolver = gmSession.getModelAccessory().getMetaData();
			Integer pageSize = GMEMetadataUtil.getMaxLimit(gmSession.getModelAccessory().getMetaData().lenient(true).entity(entity).useCase(useCase), property, 10);
			if (pageSize < 0)
				pageSize = null;
			else
				pageSize++;
			
			final PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) reference, property.getName(), pageSize,
					tc, true, modelMdResolver, useCase);
			
			gmSession.query().property(propertyQuery).result(AsyncCallback.of( //
					propertyQueryResult -> {
						GmSessionException exception = null;
						try {
							PropertyQueryResult result = propertyQueryResult.result();
							gmSession.suspendHistory();
							Object value = result != null ? result.getPropertyValue() : null;
							value = GMEUtil.transformIfSet(value, property.getName(), entityType);

							if (value instanceof EnhancedCollection)
								((EnhancedCollection) value).setIncomplete(result.getHasMore());

							property.set(entity, value);
						} catch (GmSessionException e) {
							exception = e;
						} finally {
							gmSession.resumeHistory();
							if (exception == null)
								asyncCallback.onSuccess(null);
							else
								asyncCallback.onFailure(exception);
						}
					}, asyncCallback::onFailure));
		};
	}
	
	private void handleSuccess(GenericEntity entity) {
		if (withinRefreshingEntities != null) {
			withinRefreshingEntities.remove(entity);
			if (withinRefreshingEntities.isEmpty() && useMask)
				GlobalState.unmask();
		} else if (useMask)
			GlobalState.unmask();
	}
	
	private TraversingCriterion getSpecialTraversingCriterion(Class<?> clazz) {
		if (specialEntityTraversingCriterion != null)
			return specialEntityTraversingCriterion.get(clazz);
		
		return null;
	}
	
	private GenericEntity resolveReference(EntityReference entityReference, GmContentView view) {
		if (entityReference == null || view == null)	
			return null;
		
		PersistenceGmSession gmSession = view.getGmSession();
		if (gmSession == null)
			return null;
		
		try {
			return gmSession.queryCache().entity(entityReference).require();
		} catch (Exception e) {
			return null;
		}
	}
	
}
