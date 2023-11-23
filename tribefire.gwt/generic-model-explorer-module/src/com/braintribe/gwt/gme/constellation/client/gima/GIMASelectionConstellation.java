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
package com.braintribe.gwt.gme.constellation.client.gima;

 import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellation;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellation.SelectionConstellationListener;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellationScope;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel.SpotlightData;
import com.braintribe.gwt.gmview.client.ExpertUI;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmInteractionListenerAdapter;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.InstantiationData;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;

 /**
 * New implementation of the Selection constellation dialog.
 * @author michel.docouto
 *
 */
public class GIMASelectionConstellation implements Function<SelectionConfig, Future<InstanceSelectionData>> {

 	private Future<InstanceSelectionData> future;
	private GIMADialog gimaDialog;
	private GIMASelectionContentView selectionContentView;
	private Object currentSelectedObject;
	private GmCheckListener checkChangeListener;
	private boolean configureInteractionListeners = true;
	private SelectionConfig selectionConfig;
	private List<Object> currentSelectedList = new ArrayList<>();  //support for multiselect
	private GmInteractionListenerAdapter doubleClickListener;
	private List<TetherBarElement> tetherBarElementsWithListeners = new ArrayList<>();
	private boolean closeScope = false;
	private Supplier<GIMADialog> gimaDialogSupplier;
	private Map<Widget, Integer> verticalTabElementSelections;

 	public GIMASelectionConstellation() {
		checkChangeListener = gmSelectionSupport -> {
			List<ModelPath> checkedModels = gmSelectionSupport.getCurrentCheckedItems();
			if (isWidgetHasParent(gmSelectionSupport, selectionContentView.getSelectionConstellation().getCurrentWidget()) || checkedModels == null
					|| checkedModels.isEmpty()) {
				currentSelectedObject = null;
				currentSelectedList.clear();
				if (checkedModels != null && !checkedModels.isEmpty() && areModelsSelectable(checkedModels)) {
					selectionContentView.getAddButton().setEnabled(true);
					selectionContentView.getMainButton().setEnabled(true);
				} else {
					selectionContentView.getAddButton().setEnabled(false);
					selectionContentView.getMainButton().setEnabled(false);
				}
			}
		};

 		doubleClickListener = new GmInteractionListenerAdapter() {
			@Override
			public void onDblClick(GmMouseInteractionEvent event) {
				if (isModelSelectable((ModelPath) event.getElement()))
					performAddAndFinish(false);
			}
		};

 		SelectionConstellationScope scope = new SelectionConstellationScope();
		SelectionConstellationScope.scopeManager.openAndPushScope(scope);
		closeScope = true;
	}

 	@Required
	public void setSelectionContentView(GIMASelectionContentView selectionContentView) {
		this.selectionContentView = selectionContentView;
		addSelectionConstellationListeners(selectionContentView.getSelectionConstellation());
		selectionContentView.configureGIMASelectionConstellation(this);
	}
 	
 	/**
	 * Configures the required supplier for a {@link GIMADialog}, to be used when there is no parent {@link GIMADialog} available.
	 */
	@Required
	public void setGimaDialogSupplier(Supplier<GIMADialog> gimaDialogSupplier) {
		this.gimaDialogSupplier = gimaDialogSupplier;
	}

 	@Override
	public Future<InstanceSelectionData> apply(SelectionConfig config) {
		future = new Future<>();

		if (!config.isReferenceable()) {
			config.setHandlingCollection(config.getMaxSelection() > 1);
			config.setMaxSelection(1);
		}
		
		configureGIMA(config);
 		configureSelectionConfig(config);

 		if (configureInteractionListeners) {
 			selectionContentView.getSelectionConstellation().addClipboardConstellationMasterDetailProvidedListener(masterDetailConstellation -> {
				GmContentView clipboardCurrentView = masterDetailConstellation.getCurrentMasterView();
				if (clipboardCurrentView instanceof GmCheckSupport)
					((GmCheckSupport) clipboardCurrentView).addCheckListener(checkChangeListener);
				if (clipboardCurrentView instanceof GmInteractionSupport)
					((GmInteractionSupport) clipboardCurrentView).addInteractionListener(doubleClickListener);
			});

			selectionContentView.getSelectionConstellation().addChangesConstellationMasterDetailProvidedListener(masterDetailConstellation -> {
				GmContentView changesCurrentView = masterDetailConstellation.getCurrentMasterView();
				if (changesCurrentView instanceof GmCheckSupport)
					((GmCheckSupport) changesCurrentView).addCheckListener(checkChangeListener);
				if (changesCurrentView instanceof GmInteractionSupport)
					((GmInteractionSupport) changesCurrentView).addInteractionListener(doubleClickListener);
			});

 			configureInteractionListeners = false;
		}
 		
 		if (config.getGmType().isEntity() && selectionContentView.isShowPropertyPanel())
 			selectionContentView.handlePropertyPanelToggle(true);
 		else
 			selectionContentView.handleDetailPanelVisibility(false);
 		
 		selectionContentView.setDetailPanelButtonVisibility(config.isSimplified());

 		return future;
	}
 	
 	/**
	 * Returns true if we are currently adding to a map key.
	 */
	public boolean isAddingToMapKey() {
		if (selectionConfig == null)
			return false;
		
		return selectionConfig.isAddingToSet()
				&& ((CollectionType) selectionConfig.getPropertyElement().getType()).getCollectionKind().equals(CollectionKind.map);
	}
 	
 	protected GIMADialog getGimaDialog() {
		return gimaDialog;
	}

 	private void configureGIMA(SelectionConfig config) {
		gimaDialog = getGIMADialog((Widget) config.getParentContentView(), config.getTitle(), config.isSimplified(), config.getDialogWidth(),
				config.getDialogHeight());

		gimaDialog.addSelectionTether(config.getSubTitle(), selectionContentView) //
				.andThen(v -> {
					SelectionConstellation selectionConstellation = selectionContentView.getSelectionConstellation();
					selectionConstellation.configureRequeryNeededAfterHide(true);
					selectionConstellation.closeExpertUIs();

					if (closeScope) {
						SelectionConstellationScope.scopeManager.closeAndPopScope();
						closeScope = false;
					}
				});
	}

 	private GIMADialog getGIMADialog(Widget view, String title, boolean isSimplified, Integer width, Integer height) {
 		if (view == null)
			return prepareNewGIMADialog(title, isSimplified, width, height);
 		
		Widget parentView = view.getParent();
		if (parentView instanceof GIMADialog)
			return (GIMADialog) parentView;

 		return getGIMADialog(parentView, title, isSimplified, width, height);
	}
 	
 	private GIMADialog prepareNewGIMADialog(String title, boolean isSimplified, Integer width, Integer height) {
		GIMADialog gimaDialog = gimaDialogSupplier.get();
		//if (useCase != null)
			//gimaDialog.setUseCase(useCase);
		//if (transientInstantiation)
			//gimaDialog.setGmSession(theTransientSession);
		if (title != null)
			gimaDialog.setHeading(title);
		
		if (width != null || height != null) {
			if (!isSimplified)
				gimaDialog.setDefaultSize();
			
			if (width == null && isSimplified)
				width = 350;
			if (height == null && isSimplified)
				height = 450;
			
			if (width != null)
				gimaDialog.setWidth(width);
			if (height != null)
				gimaDialog.setHeight(height);
		}
		
		if (isSimplified)
			gimaDialog.hideTetherBar();
		else
			gimaDialog.showTetherBar();
		
		gimaDialog.showForSelection();

 		return gimaDialog;
	}

 	private void configureSelectionConfig(SelectionConfig config) {
		selectionConfig = config;
		int maxEntriesToSelect = config.getMaxSelection();

 		SelectionConstellation selectionConstellation = selectionContentView.getSelectionConstellation();
		if (selectionConstellation.getSpotlightPanel() != null) {
			GridSelectionModel<SpotlightData> selectionModel = selectionConstellation.getSpotlightPanel().getGrid().getSelectionModel();
			if (maxEntriesToSelect > 1)
				selectionModel.setSelectionMode(SelectionMode.MULTI);
			else			
				selectionModel.setSelectionMode(SelectionMode.SINGLE);
		}

		selectionContentView.prepareButtonsVisibilityAndNames(maxEntriesToSelect);
		currentSelectedObject = null;
		currentSelectedList.clear();
		selectionConstellation.configureSelectionConfig(config);
		prepareGIMASession(config);
		//backButton.setVisible(previousTypes != null && !previousTypes.isEmpty());
	}
 	
 	private void prepareGIMASession(SelectionConfig selectionConfiguration) {
		if (gimaDialog.getSessionForTransactionAndCMD().getModelAccessory().getOracle().findGmType(selectionConfiguration.getGmType()) == null)
			gimaDialog.setGmSession(selectionConfiguration.getGmSession());
	}

 	protected Object getCurrentSelectedObject() {
		return currentSelectedObject;
	}

 	protected void setCurrentSelectedObject(Object currentSelectedObject) {
		this.currentSelectedObject = currentSelectedObject;
	}

 	private void addSelectionConstellationListeners(final SelectionConstellation selectionConstellation) {
		addSelectionConstellationStoreListeners(selectionConstellation);

 		selectionConstellation.addSelectionConstellationListener(prepareSelectionConstellationListener());

 		final SpotlightPanel spotlightPanel = selectionConstellation.getSpotlightPanel();
		spotlightPanel.getGrid().getSelectionModel().addSelectionChangedHandler(event -> {
			List<SpotlightData> selectedList = event.getSelection();
			currentSelectedList.clear();				

 			selectionContentView.getMainButton().setEnabled(!selectedList.isEmpty());
			//addAndFinishButton.setEnabled(!selectedList.isEmpty());
 			selectionContentView.handleAddButtonVisibility(selectedList);
			//typeFilterButton.setVisible(!selectedList.isEmpty() && spotlightPanel.prepareModelTypeOrAction() instanceof GmEntityType);

 			if (selectedList.isEmpty())
 				return;
 			
			for (SpotlightData selectSpotlightModel : selectedList) {
				ObjectAndType objectAndType = spotlightPanel.prepareObjectAndType(selectSpotlightModel);

				if (objectAndType != null) {
					currentSelectedObject = objectAndType.getObject();
					if (currentSelectedObject != null)
						currentSelectedList.add(currentSelectedObject);
				}
			}					
		});

 		selectionConstellation.getVerticalTabPanel().addVerticalTabListener(new VerticalTabListener() {
			@Override
			public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement) {
				if (previousVerticalTabElement != null) {
					if (verticalTabElementSelections == null)
						verticalTabElementSelections = new HashMap<>();
					Widget previousWidget = previousVerticalTabElement.getWidget();
					if (previousWidget instanceof SpotlightPanel) {
						Grid<SpotlightData> grid = ((SpotlightPanel) previousWidget).getGrid();
						GridSelectionModel<SpotlightData> selectionModel = grid.getSelectionModel();
						int index = -1;
						SpotlightData selectedItem = selectionModel.getSelectedItem();
						if (selectedItem != null)
							index = grid.getStore().indexOf(selectedItem);
						verticalTabElementSelections.put(previousWidget, index);
						
						selectionModel.deselectAll();
						currentSelectedObject = null;
						currentSelectedList.clear();
					} else if (previousWidget instanceof GmCheckSupport) {
						int index = -1;
						if (previousWidget instanceof GmSelectionSupport)
							index = ((GmSelectionSupport) previousWidget).getFirstSelectedIndex();
						verticalTabElementSelections.put(previousWidget, index);
						
						((GmCheckSupport) previousWidget).uncheckAll();
						//typeFilterButton.setVisible(false);
						selectionContentView.getSelectionConstellation().clearDetailPanel();
					}
				}
				
				if (verticalTabElement == null)
					return;
				
				Widget currentWidget = verticalTabElement.getWidget();
				Integer indexToSelect = verticalTabElementSelections == null ? null : verticalTabElementSelections.get(currentWidget);
				if (indexToSelect == null || indexToSelect == -1)
					return;
				
				if (currentWidget instanceof SpotlightPanel)
					((SpotlightPanel) currentWidget).getGrid().getSelectionModel().select(indexToSelect, false);
				else if (currentWidget instanceof GmSelectionSupport)
					((GmSelectionSupport) currentWidget).select(indexToSelect, false);
			}

 			@Override
			public void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements) {
				//NOP
			}

 			@Override
			public void onHeightChanged(int newHeight) {
				//NOP
			}
		});
	}

 	private void addSelectionConstellationStoreListeners(final SelectionConstellation selectionConstellation) {
		ListStore<GMTypeInstanceBean> store = selectionConstellation.getSelectionGrid().getStore();
		store.addStoreAddHandler(event -> selectionContentView.renameButtonsAfterAdd(true));
		store.addStoreRemoveHandler(event -> {
			if (selectionConstellation.getSelectionGrid().getStore().getAll().isEmpty())
				selectionContentView.renameButtonsAfterAdd(false);
		});
		store.addStoreClearHandler(event -> selectionContentView.renameButtonsAfterAdd(false));
	}

 	private SelectionConstellationListener prepareSelectionConstellationListener() {
		return new SelectionConstellationListener() {
			@Override
			public void onBrowsingConstellationSet(BrowsingConstellation browsingConstellation) {
				browsingConstellation.getTetherBar().addTetherBarListener(getTetherBarListener());
			}

 			@Override
			public void onEntityTypeChanged(EntityType<?> entityType) {
				InstantiationData instantiationData = new InstantiationData(entityType, null, true, true, null, null,
						selectionConfig.isHandlingCollection(), gimaDialog.getSessionForTransactionAndCMD() instanceof TransientPersistenceGmSession);
				instantiationData.setPathElement(selectionConfig.getPropertyElement());
				Future<GenericEntity> future = new Future<>();
				gimaDialog.showForInstantiation(instantiationData, future);
				future.andThen(result -> {
					if (result != null)
						onObjectSelected(Collections.singletonList((Object) result), selectionConfig.getMaxSelection() == 1 || gimaDialog.isApplyingAll(), false);
					else
						Scheduler.get().scheduleDeferred(() -> handleCancel(true, true));
				});
			}

 			@Override
			public void onObjectSelected(List<Object> objects, boolean finish, boolean handleInstantiation) {
 				currentSelectedObject = objects.get(objects.size() - 1);
 				currentSelectedList.clear();
 				
 				if (selectionConfig.isAddingToSet()) {
					SelectionConstellation selectionConstellation = selectionContentView.getSelectionConstellation();
					Set<Object> selections = selectionConstellation.getSelections().stream().map(bean -> bean.getInstance()).collect(Collectors.toSet());
					objects.stream().filter(o -> !selections.contains(o)).forEach(o -> currentSelectedList.add(o));
				} else
					currentSelectedList.addAll(objects);
 				
 				if (finish)
					performAddAndFinish(handleInstantiation);
				else
					performAdd(handleInstantiation);
			}

 			private TetherBarListener getTetherBarListener() {
				return new TetherBarListener() {
					@Override
					public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
						for (TetherBarElement element : tetherBarElementsRemoved) {
							GmContentView contentViewRemoved = element.getContentViewIfProvided();
							boolean removeElementWithListener = false;
							if (contentViewRemoved instanceof GmCheckSupport)
								((GmCheckSupport) contentViewRemoved).removeCheckListener(checkChangeListener);
							removeElementWithListener = true;
							if (contentViewRemoved instanceof GmInteractionSupport)
								((GmInteractionSupport) contentViewRemoved).removeInteractionListener(doubleClickListener);
							removeElementWithListener = true;
							if (removeElementWithListener)
								tetherBarElementsWithListeners.remove(element);
						}
					}

 					@Override
					public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
 						if (tetherBarElementsWithListeners.contains(tetherBarElement))
 							return;
 							
						GmContentView contentView = tetherBarElement.getContentViewIfProvided();
						boolean addElementWithListener = false;

						if (contentView instanceof GmCheckSupport)
							((GmCheckSupport) contentView).addCheckListener(checkChangeListener);
						addElementWithListener = true;
						if (contentView instanceof GmInteractionSupport)
							((GmInteractionSupport) contentView).addInteractionListener(doubleClickListener);
						addElementWithListener = true;
						if (addElementWithListener)
							tetherBarElementsWithListeners.add(tetherBarElement);
					}

 					@Override
					public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
						//NOP
					}
				};
			}
		};
	}

 	//multi Add
	private boolean areModelsSelectable(List<ModelPath> modelList) {
		for (ModelPath model : modelList) {
			if (!isModelSelectable(model))
				return false;
		}

 		return true;
	}

 	//single Add
	private boolean isModelSelectable(ModelPath model) {
		if (model == null)
			return false;

 		ModelPathElement element = model.get(model.size() - 1);
		GenericModelType elementType = element.getType();
		GenericModelType selectionType = selectionConfig.getGmType();
		if (elementType.isEntity() && selectionType.isEntity()) {
			if (((EntityType<?>) selectionConfig.getGmType()).isAssignableFrom(elementType)) {
				currentSelectedObject = element.getValue();					
				if (currentSelectedObject != null) {
					currentSelectedList.add(currentSelectedObject);
					return true;
				}
			}
		} else if (elementType == selectionType || selectionType.isBase()) {
			currentSelectedObject = element.getValue();
			currentSelectedList.add(currentSelectedObject);
			return true;
		}

 		return false;
	}

 	protected void performAddAndFinish(boolean handleInstantiation) {
 		SelectionConstellation selectionConstellation = selectionContentView.getSelectionConstellation();
 		
		if (selectionConstellation.getCurrentWidget() == selectionConstellation.getSpotlightPanel()
				&& (currentSelectedObject == null || currentSelectedObject instanceof ExpertUI)) {
			selectionConstellation.handleObjectAndType(selectionConstellation.getSpotlightPanel().prepareObjectAndType(), true);
			currentSelectedObject = null;
			currentSelectedList.clear();
		} else {
			selectionContentView.disableUpdateButtons();
			currentSelectedList.forEach(object -> selectionConstellation.addObject(object));
			selectionContentView.enableUpdateButtons();
			performFinish(handleInstantiation);
		}
	}
 	
 	protected void performAdd(boolean handleInstantiation) {
		SelectionConstellation selectionConstellation = selectionContentView.getSelectionConstellation();
		if (selectionConstellation.getCurrentWidget() == selectionConstellation.getSpotlightPanel()
				&& (currentSelectedObject == null || currentSelectedObject instanceof ExpertUI)) {
			selectionConstellation.handleObjectAndType(selectionConstellation.getSpotlightPanel().prepareObjectAndType(), false);
			currentSelectedObject = null;
			currentSelectedList.clear();
		} else {
			currentSelectedList.forEach(object -> selectionConstellation.addObject(object));
			gimaDialog.getToolBar().forceLayout();
		}

		if (handleInstantiation)
			gimaDialog.showForInstantiation(new InstantiationData(new RootPathElement((GenericEntity) currentSelectedObject), true, true, null, true,
					gimaDialog.getSessionForTransactionAndCMD() instanceof TransientPersistenceGmSession), new Future<>());
	}

 	protected void handleCancel(boolean handleFuture, boolean performGoBack) {
 		if (handleFuture)
 			future.onSuccess(null);
		
		if (performGoBack)
			gimaDialog.handleHideOrBack(false, true);
	}

 	protected void performFinish(boolean handleInstantiation) {
		List<GMTypeInstanceBean> selections = selectionContentView.getSelectionConstellation().getSelections();
		if (handleInstantiation)
			selections.forEach(bean -> bean.setHandleInstantiation(handleInstantiation));

 		//EnumTypes need a special handling. We change the selected value from GmEnumConstant to the actual enum.
		if (selectionConfig.getGmType().isEnum()) {
			EnumType enumType = (EnumType) selectionConfig.getGmType();

			selections.stream().filter(bean -> bean.getInstance() instanceof GmEnumConstant).forEach(bean -> {
				GmEnumConstant enumConstant = (GmEnumConstant) bean.getInstance();
				for (Enum<?> enumValue : enumType.getEnumValues()) {
					if (enumConstant.getName().equalsIgnoreCase(enumValue.name())) {
						bean.setInstance(enumValue);
						break;
					}
				}

				bean.setGenericModelType(selectionConfig.getGmType());
			});
		}

		future.onSuccess(new InstanceSelectionData(gimaDialog, selections));
		gimaDialog.handleHideOrBack(false, false);
	}

 	private boolean isWidgetHasParent(Object view, Widget parent) {
		if (view == parent)
			return true;
		
		if (view instanceof Widget)
			return isWidgetHasParent(((Widget) view).getParent(), parent);
		
		return false;
	}

 }