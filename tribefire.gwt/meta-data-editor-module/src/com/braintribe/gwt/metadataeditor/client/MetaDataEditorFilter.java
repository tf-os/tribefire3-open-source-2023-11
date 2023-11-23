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
package com.braintribe.gwt.metadataeditor.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorFilterCheckBox;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorFilterType;
import com.braintribe.gwt.security.client.SecurityService;
import com.braintribe.gwt.security.client.SecurityServiceException;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.CommitListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.core.shared.FastSet;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.ResizeContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

public class MetaDataEditorFilter extends VerticalLayoutContainer implements ManipulationListener {

	//UseCase,Roles, Access  - cfg checkboxes via Edit field (everyone can have more parameters), possible also to delete it
	private PersistenceGmSession gmSession;
	private PersistenceGmSession workbenchSession;
	private final EntityQuery entityQuery;
	private Folder rootFolder;
	private Boolean folderReloaded = false;
	private CssFloatLayoutContainer filterLayoutValues;
	private SimpleContainer boxContaniner;
	private Boolean isInitialized = false;
	private SecurityService securityService = null;
	//private HorizontalLayoutContainer filterLayoutValues;
	
	//private TextField textFieldUseCase;
	//private TextField textFieldRoles;
	//private TextField textFieldAccess;
	private Supplier<SpotlightPanel> spotlightPanelProvider;
	private QuickAccessTriggerField quickAccessTriggerField;
	//private TextField textAddFilter;
	//private CheckBox checkBoxSession;
	private final Map<String, String> mapStringUseCase = new FastMap<>();
	private VerticalLayoutContainer filterLayout;
	private TypeCondition typeCondition;
	private MetaDataEditorFilterCheckBox currentSessionContextCheckbox = null;
	private Boolean staticFilterValueInitialized = false;
	
	//private List<FieldLabel> listFieldUseCase = new ArrayList<FieldLabel>();
	//private List<FieldLabel> listFieldRoles = new ArrayList<FieldLabel>();
	//private List<FieldLabel> listFieldAccess = new ArrayList<FieldLabel>();
	private static final String VALUE_USE_CASE_NAME = "useCaseFilter";
	private static final String VALUE_ROLES_NAME = "rolesFilter";
	private static final String VALUE_ACCESS_NAME = "accessFilter";
	private static final String VALUE_CURRENT_SESSION_CONTEXT_NAME = "currentSessionContext";
	private static final String ROOT_FOLDER_NAME = "metadataeditorfilter";
	private final Map<String,MetaDataEditorFilterCheckBox> checkboxMap = new FastMap<>();
	//private Provider<? extends IndexedProvider<SelectionConfig, Future<List<GMTypeInstanceBean>>>> instanceSelectionFutureProviderProvider;
	//private IndexedProvider<SelectionConfig, Future<List<GMTypeInstanceBean>>> instanceSelectionFutureProvider;
	private NestedTransaction editionNestedTransaction;
		
	//set sessions
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}
			
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
		this.workbenchSession.listeners().add(new CommitListener() {				
			@Override
			public void onBeforeCommit(PersistenceGmSession session, Manipulation manipulation) {
				//Do nothing
			}
			
			@Override
			public void onAfterCommit(PersistenceGmSession session, Manipulation manipulation,	Manipulation inducedManipluation) {
				//RVE - check if root folder is created - if is already exists than notice manipulation is geenrated, ptherwise need to check creating of folder
				if (rootFolder == null)
					for (Manipulation manupulationItem : manipulation.inline())
						if (manupulationItem instanceof InstantiationManipulation)
							noticeManipulation(manupulationItem);
			}
		});		
	}
	
	public void setQuickAccessTriggerField(QuickAccessTriggerField quickAccessTriggerField) {
		this.quickAccessTriggerField = quickAccessTriggerField;
	}

	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> spotlightPanelProvider) {
		this.spotlightPanelProvider = spotlightPanelProvider;
	}
	
	public void setTypeCondition(TypeCondition typeCondition) {
		this.typeCondition = typeCondition;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;		
	}	
	
	//public void setInstanceSelectionFutureProvider(Provider<? extends IndexedProvider<SelectionConfig, Future<List<GMTypeInstanceBean>>>> instanceSelectionFutureProvider) {
	//	this.instanceSelectionFutureProviderProvider = instanceSelectionFutureProvider;
	//}
	
	public MetaDataEditorFilter() {
		super();
		this.entityQuery = EntityQueryBuilder.from(Folder.class).tc(TC.create().negation().joker().done()).where().property("name").eq(ROOT_FOLDER_NAME).done();		
		//this.entityQuery = EntityQueryBuilder.from(Folder.class).tc(TC.create().negation().joker().done()).done();		
		addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().north());
		setScrollMode(ScrollMode.AUTOY);
		
		configureDefaultTypeCondition();		
	}

	public void configureDefaultTypeCondition() {
		this.typeCondition = GMEUtil.prepareTypeCondition(GMF.getTypeReflection().getEntityType(IncrementalAccess.class));	
	}
	
	public void initFilterData() {
		showFilterData();
	}
			
	public Boolean isLoadingReady() {
		return this.isInitialized;
	}
	
	public void refresh() {
		fillFilter();
	}
	
	/*
	private Provider<SpotlightPanel> getQuickAccessPanelProvider(final Widget textField) {
		return new Provider<SpotlightPanel>() {
			@Override
			public SpotlightPanel provide() throws ProviderException {
				SpotlightPanel quickAccessPanel = quickAccessPanelProvider.provide();
				//quickAccessPanel.setMinCharsForFilter(MIN_CHARS_FOR_FILTER);
				//quickAccessPanel.setUseCase(this.useCase);
				//quickAccessPanel.setUseApplyButton(false);
				//quickAccessPanel.setLoadExistingValues(false);
				//quickAccessPanel.setSimpleTypesValuesProvider(relationshipFilterValuesProvider);
				//quickAccessPanel.setEntitiesFutureProvider(null);
				return quickAccessPanel;
			}
		};
	}	
	*/
	
	public QuickAccessTriggerField getQuickAccessTriggerField() {
		if (this.quickAccessTriggerField == null) {
			this.quickAccessTriggerField = new QuickAccessTriggerField();
			this.quickAccessTriggerField.setEmptyText(LocalizedText.INSTANCE.selectFilterType());
			this.quickAccessTriggerField.setBorders(false);
			this.quickAccessTriggerField.setWidth(450);
			this.quickAccessTriggerField.setTypeCondition(this.typeCondition);			
			this.quickAccessTriggerField.setQuickAccessPanel(this.spotlightPanelProvider);
			this.quickAccessTriggerField.addQuickAccessTriggerFieldListener(result -> {
				if (result != null) {
					ObjectAndType objectAndType = result.getObjectAndType();
					addFilterValue(objectAndType);
				}
			}); 
			//this.quickAccessTriggerField.setSize("100%", "25px");
		}	
		return this.quickAccessTriggerField;
	}	
	
	//return List of UseCase, Roles or Accesses depending on Filter type
	public Set<String> getFilterList(MetaDataEditorFilterType filterType) {
		Set<String> result = new FastSet();		
		
		if ((currentSessionContextCheckbox != null) && (currentSessionContextCheckbox.getChecked()) && (filterType == MetaDataEditorFilterType.Role)) {
			try {
				if (this.securityService != null && this.securityService.getSession() != null)
					result.addAll(this.securityService.getSession().getRoles());
			} catch (SecurityServiceException e) {
				e.printStackTrace();
			}
		}
		
		for (Entry<String, String> entry : this.mapStringUseCase.entrySet()) {
			String idString = entry.getKey();
			MetaDataEditorFilterCheckBox checkBox = this.checkboxMap.get(idString);
			if (checkBox.getChecked() && checkBox.getFilterType() == filterType) {				
				result.add(entry.getValue());
			}
		}
		
		/*
		for (String idString : this.mapStringUseCase.keySet()) {
			MetaDataEditorFilterCheckBox checkBox = this.checkboxMap.get(idString);
			if (checkBox.getChecked() && checkBox.getFilterType() == filterType) {				
					value = this.mapStringUseCase.get(idString);
					list.add(value);
			}
		}
		*/
		return result;
	}
	
	//public Boolean getUseSessionResolver () {
	//	return this.checkBoxSession.getValue();
	//}
	
	//private void addFilterValue(List<GMTypeInstanceBean> result, NestedTransaction nestedTransaction) 
	private void addFilterValue(ObjectAndType objectAndType) {
		String value = null;
		String filterType = null;

		if (objectAndType.getObject() instanceof String) {
			value = (String) objectAndType.getObject();
			if (objectAndType.getDescription().equals("useCase")) {
				filterType = VALUE_USE_CASE_NAME;
			} else if (objectAndType.getDescription().equals("role")) {
				filterType = VALUE_ROLES_NAME; 
			}
			
		} else if (objectAndType.getObject() instanceof IncrementalAccess) {
			IncrementalAccess access = (IncrementalAccess) objectAndType.getObject();
			value = access.getExternalId();
			filterType = VALUE_ACCESS_NAME;
		}
		
		if (value != null && filterType != null) {
			this.mapStringUseCase.put(filterType + "$" + value, value);
			saveFilterValues();
			this.folderReloaded = true;
			fillFilter();
			if (!this.checkboxMap.isEmpty())
				DomEvent.fireNativeEvent(Document.get().createChangeEvent(), this.checkboxMap.values().iterator().next());
			
		} 							
	}
	
	/*
	private void getGimaFilterValue() {
		
		try {
			if (this.instanceSelectionFutureProvider == null) {
				this.instanceSelectionFutureProvider = this.instanceSelectionFutureProviderProvider.provide();
			}
			
			//final NestedTransaction nestedTransaction = workbenchSession.getTransaction().beginNestedTransaction();			
			//TypeCondition stringCondition = GMEUtil.prepareTypeCondition(GMF.getTypeReflection().getSimpleType(String.class));
						
			GenericModelType gmType = null;
			
			this.instanceSelectionFutureProvider.provide(new SelectionConfig(this.typeCondition, gmType, 1, null, this.workbenchSession, this.workbenchSession)).get(new com.google.gwt.user.client.rpc.AsyncCallback<List<GMTypeInstanceBean>>() {
				@Override
				public void onFailure(Throwable e) {
					
					//try {
					//	nestedTransaction.rollback();
					//} catch (TransactionException e1) {
					//	e1.printStackTrace();
					//}
					e.printStackTrace();
					ErrorDialog.show(LocalizedText.INSTANCE.errorAddFilter(), e);
				}

				@Override
				public void onSuccess(List<GMTypeInstanceBean> result) {
					//addFilterValue(result, nestedTransaction);
					addFilterValue(result);
				}
			});
		} catch (ProviderException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorAddFilter(), e);
			e.printStackTrace();
		}		
	}
	*/
		
	private void showFilterData() {
		if (this.editionNestedTransaction != null)
			rollbackTransaction();
		
		//add Field
		HorizontalLayoutContainer layoutAddFiled = new HorizontalLayoutContainer();
		layoutAddFiled.setWidth("auto");
		/*
		this.textAddFilter = new TextField();
		this.textAddFilter.setEmptyText( LocalizedText.INSTANCE.addFilter());
		this.textAddFilter.addFocusHandler(new FocusHandler() {				
			public void onFocus(FocusEvent event) {
				getGimaFilterValue();					
			}
		});			
		layoutAddFiled.add(this.textAddFilter, new HorizontalLayoutData(250, 1, new Margins(0,0,0,0)));
		*/

		//Filter add edit field
		getQuickAccessTriggerField();
		if (this.quickAccessTriggerField != null) {
			layoutAddFiled.add(this.quickAccessTriggerField, new HorizontalLayoutData(450, 22, new Margins(0,4,0,4)));
		}			
		
		/*
		this.checkBoxSession = new CheckBox();
		this.checkBoxSession.setBoxLabel("use Session Resolver");
		this.checkBoxSession.addChangeHandler(new ChangeHandler() {				
			@Override
			public void onChange(ChangeEvent event) {
				fireEvent(event);
				//DomEvent.fireNativeEvent(Document.get().createChangeEvent(), checkBoxSession);
			}
		});
		layoutAddFiled.add(this.checkBoxSession, new HorizontalLayoutData(1, 1, new Margins(4)));
		*/
		
		add(layoutAddFiled, new VerticalLayoutData(1d, 25d, new Margins(6)));

		//add horizontal layout
		this.filterLayout = new VerticalLayoutContainer();
		//filterLayout.setScrollMode(ScrollMode.AUTOY);
		add(this.filterLayout);
		
		//filterLayout.add(imagesListView);					
		
		this.filterLayout.add(createCssFloatLayoutContainerExample(), new VerticalLayoutData(1, 1, new Margins(6)));
		//filterLayoutValues = new HorizontalLayoutContainer();
		//filterLayoutValues = createCssFloatLayoutContainerExample();
		//filterLayoutValues.setWidth("auto");
		//filterLayoutValues.setHeight(250);
		//filterLayoutValues.setStyleFloat(Style.Float.RIGHT);
		//filterLayoutValues.setBorders(true);
		//filterLayout.add(filterLayoutValues, new VerticalLayoutData(1, 1, new Margins(6)));

		loadFilterValues();
	}
	
	private Widget createCssFloatLayoutContainerExample() {
		  this.filterLayoutValues = new CssFloatLayoutContainer();
		  //filterLayoutValues.setStyleFloat(Style.Float.RIGHT);
		  this.filterLayoutValues.setBorders(true);
		  this.filterLayoutValues.setStyleName(MetaDataEditorResources.INSTANCE.constellationCss().filterMetaDataContainer());
		  this.filterLayoutValues.setWidth("auto");
		  this.filterLayoutValues.setDeferHeight(true);

		  /*
		  for (int i = 1; i < 17; i++) {
		    ContentPanel cp = new ContentPanel();
		    cp.setHeadingText(Integer.toString(i));

		    if (i % 2 == 0) {
		      cp.setPixelSize(50, 50);
		    } else {
		      cp.setPixelSize(70,70);
		    }

		    SimpleContainer boxWithMargin = new SimpleContainer();
		    boxWithMargin.add(cp, new MarginData(5));

		    container.add(cp);
		  }
		  */

		  this.boxContaniner = new SimpleContainer();
		  this.boxContaniner.setBorders(true);
		  this.boxContaniner.setWidth("auto");		  
		  this.boxContaniner.setResize(true);
		  //this.boxContaniner.setDeferHeight(true);
		  this.boxContaniner.setStyleName(MetaDataEditorResources.INSTANCE.constellationCss().filterMetaDataBox());
		  this.boxContaniner.setHeight("auto");
		  this.boxContaniner.add(this.filterLayoutValues);
		  		  
		  return this.boxContaniner;
	}			

	private void clearFilterLayout() {
		if (this.filterLayoutValues == null)
			return;
		
		Set<Widget> widgetSet = new HashSet<>();
		
		for (int i=0; i<this.filterLayoutValues.getWidgetCount(); i++) {
			Widget widget = this.filterLayoutValues.getWidget(i);
			
			if (widget instanceof ContentPanel) {
				ContentPanel cp = (ContentPanel) widget;
				if (cp.getId().equals(VALUE_CURRENT_SESSION_CONTEXT_NAME))
					continue;
			}

			widgetSet.add(widget);			
		}
		
		for (Widget widget : widgetSet) {
			this.filterLayoutValues.remove(widget);
		}
	}
	
	/*
	private String getTimeStamp () {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	*/
	
	private void fillFilter() {		

		if (!this.isVisible())
				return;
		
		this.boxContaniner.setHeight("250px");
		addStaticFilterValue();
		
		if (!this.folderReloaded)
			    return;
		//Date beginDate = new Date();		
		//textAddFilter.setText("FillFilter B:" + getTimeStamp());
		
		clearFilterLayout();		
		this.checkboxMap.clear();
		this.folderReloaded = false;
		
		Map<String, Long> intMap = new FastMap<>();
	    this.mapStringUseCase.clear();
	    	    		
		if (this.rootFolder != null && this.rootFolder.getSubFolders() != null) {
			for (Folder subFolder : this.rootFolder.getSubFolders()) {	
				addEntityListener(subFolder);
				String value =  subFolder.getName();
				value = value.replaceFirst( VALUE_USE_CASE_NAME , "");
				value = value.replaceFirst( VALUE_ROLES_NAME , "");
				value = value.replaceFirst( VALUE_ACCESS_NAME , "");
				value = value.replace( "$" , "");
				
				String id = subFolder.getName();
				this.mapStringUseCase.put( id, value);
				if (subFolder.getTags().contains("1")) {
					intMap.put(id, (long) 1);										
				} else {
					intMap.put(id, (long) 0);																				
				}
			}						
		}
		
		//FieldLabel fieldLabel;
		//CheckBox checkBoxField;
		Long i = (long) 0;					
		for (Entry<String, String> entry : this.mapStringUseCase.entrySet()) {
			String valueKey = entry.getKey();
			String valueString = entry.getValue();
			MetaDataEditorFilterType filterType = MetaDataEditorFilterType.UseCase;
			
			Long intValue = intMap.get(valueKey);
			
			if (valueKey.startsWith(VALUE_USE_CASE_NAME)) {
				filterType = MetaDataEditorFilterType.UseCase;
			}
			if (valueKey.startsWith(VALUE_ROLES_NAME)) {					
				filterType = MetaDataEditorFilterType.Role;
			}
			if (valueKey.startsWith(VALUE_ACCESS_NAME)) { 
				filterType = MetaDataEditorFilterType.Access;
			}
			
			if (valueString == null || intValue == null)
			{
				continue;
			}
			
		    
		    //cp.setPixelSize(200, 24);
		    
			//filterLayoutValues.add(checkboxFilter, new HorizontalLayoutData(-1, -1, new Margins(0, 4, 0, (x))));
			//filterLayoutValues.add(checkboxFilter, new CssFloatData(1, new Margins(0, 4, 0, (x))));
			//filterLayoutValues.add(checkboxFilter);
			
			//x = checkboxFilter.getOffsetWidth();
			//y = checkboxFilter.getOffsetHeight();
			this.checkboxMap.put(valueKey, createCheckBoxFilter(valueKey, valueString, intValue == 1, filterType));
			i++;
		}			

		//super.forceLayout();
		
		super.doLayout();
		if (this.getParent() instanceof ResizeContainer)
			((ResizeContainer) this.getParent()).forceLayout();
		
		//Date endDate = new Date();
		//long milisec = endDate.getTime() - beginDate.getTime();		
		//textAddFilter.setText(textAddFilter.getText() + " - " + "FillFilter:" + milisec );
	}

	private MetaDataEditorFilterCheckBox createCheckBoxFilter(String valueKey, String valueString, Boolean checked, MetaDataEditorFilterType filterType) {
			MetaDataEditorFilterCheckBox checkboxFilter = new MetaDataEditorFilterCheckBox(valueKey, valueString, checked, filterType);
			checkboxFilter.addChangeHandler(event -> {
				if (event.getSource() instanceof MetaDataEditorFilterCheckBox) {
					uncheckAccessFields((MetaDataEditorFilterCheckBox) event.getSource());
					saveFilterValues();
					fireEvent(event);
				}
			});	
			checkboxFilter.addHideHandler(event -> {
				final MetaDataEditorFilterCheckBox filterCheckbox = (MetaDataEditorFilterCheckBox) event.getSource();
				final Boolean isChecked = filterCheckbox.getChecked();
				new Timer() {
					@Override
					public void run() {								
													
						deleteFilterValue (filterCheckbox.getId());
						MetaDataEditorFilter.this.folderReloaded = true;
						fillFilter();
						//RVE - need only in case of checked, othewise not need refresh Resolver because value is not in filter list
						if (isChecked)
							DomEvent.fireNativeEvent(Document.get().createChangeEvent(), currentSessionContextCheckbox);							
					}
				}.schedule(10);																
			});
										
		    ContentPanel cp = new ContentPanel();
		    cp.setHeaderVisible(false);
		    cp.setBorders(false);
		    cp.setBodyBorder(false);
		    cp.setStyleName("filterMetaDataContentPanel");
		    cp.add(checkboxFilter);
		    //cp.setData(valueKey, checkboxFilter);
		    cp.setId(valueKey);
		    SimpleContainer boxWithMargin = new SimpleContainer();
		    boxWithMargin.setBorders(false);
			boxWithMargin.setStyleName("filterMetaDataMargin");			    
		    boxWithMargin.add(cp, new MarginData(2));
		    this.filterLayoutValues.add(cp);
		    cp.setPixelSize(checkboxFilter.getOffsetWidth(), checkboxFilter.getOffsetHeight());
		    
		    cp.forceLayout();
		    boxWithMargin.forceLayout();
		    
		    return checkboxFilter;
	}
	
	
	protected void uncheckAccessFields(MetaDataEditorFilterCheckBox source) {
		// Uncheck all rest Access fields - allow checked only one Access!!!
		if (source == null)
			return;
		
		if (source.getChecked() && source.getFilterType() == MetaDataEditorFilterType.Access) {
			for (MetaDataEditorFilterCheckBox checkBoxFilter : this.checkboxMap.values()) {
				if (checkBoxFilter != source && checkBoxFilter.getFilterType() == MetaDataEditorFilterType.Access && checkBoxFilter.getChecked()) {
					checkBoxFilter.setChecked(false);
				}
			}
		}
		
	}

	/*private Element getActionElement(Element clickedElement, int depth, String className) {
		if (depth > 0) {
			if (clickedElement != null) {	
				
				if (className.equals(clickedElement.getClassName())) {
					return clickedElement;
				}
				else
					return getActionElement(clickedElement.getParentElement(), --depth, className);
			}
		}		
		return null;
	}*/		
	
	// if filterId == null  -> delete all for UseCase,Roles and Access
	// if filterId != null -> delete Exact value in UseCase or Roles or Access
	private void deleteFilterValue (String filterId) {
		if (this.rootFolder == null)
			return;

		
		//final Date beginDate = new Date();
	    if (filterId == null) {
			this.editionNestedTransaction = this.workbenchSession.getTransaction().beginNestedTransaction();
	    	this.rootFolder.getSubFolders().clear();
	    	this.mapStringUseCase.clear();
			this.editionNestedTransaction.commit();
			this.editionNestedTransaction = null;	
	    } else {
			List<Folder> removeFolderList = new ArrayList<> ();
			
			//need remove later, baceause can not directly delete in loop
			for (Folder subFolder : this.rootFolder.getSubFolders()) {
					if (subFolder.getName().equals(filterId)) {
						removeFolderList.add(subFolder);
			    		this.mapStringUseCase.remove(filterId);
					}		    					
			}
			if (!removeFolderList.isEmpty()) {
				this.editionNestedTransaction = this.workbenchSession.getTransaction().beginNestedTransaction();
				for (Folder removeFolder : removeFolderList)
					this.rootFolder.getSubFolders().remove(removeFolder);
				this.editionNestedTransaction.commit();
				this.editionNestedTransaction = null;	
			}		    					
	    }			 
		    
	    //final Date middleDate = new Date();
	    
	    /*
		this.workbenchSession.commit(new AsyncCallback<ManipulationResponse>() {
			@Override
			public void onSuccess(ManipulationResponse future) {
				//GlobalState.showSuccess(LocalizedText.INSTANCE.headerBarCreated());
				//Date endDate = new Date();
				//long milisec = endDate.getTime() - beginDate.getTime();							
				//long milisec2 = endDate.getTime() - middleDate.getTime();							
				//textAddFilter.setText(textAddFilter.getText() + " - " + "DeleteFilterValue:" + milisec + " load:"+ milisec2);							
			}
			
			@Override
			public void onFailure(Throwable t) {
				t.printStackTrace();
			}
		});
		*/
	    	    
	}
	
    private void addStaticFilterValue() {
    	if (!staticFilterValueInitialized) {
    		//add always visible Filter value "Current Session Context"
    		currentSessionContextCheckbox = createCheckBoxFilter(VALUE_CURRENT_SESSION_CONTEXT_NAME, VALUE_CURRENT_SESSION_CONTEXT_NAME, false, MetaDataEditorFilterType.CurrentSessionContext);
    	}
    	staticFilterValueInitialized = true;
    }
	
	private void loadFilterValues () {
		if ((this.workbenchSession == null) || (this.workbenchSession.getAccessId() == null)) {
			fillFilter();			
			//ShowFilterData();	
			return;

		}
		this.rootFolder = null;
		
		workbenchSession.query().entities(this.entityQuery).result(AsyncCallback.of(entityQueryResult -> {
			if (entityQueryResult != null) {
				EntityQueryResult result = null;
				try {
					result = entityQueryResult.result();
				} catch (GmSessionException e) {
					handleEntityQueryError(e);
				}
				
				if (result != null && result.getEntities() != null && !result.getEntities().isEmpty()) {
					MetaDataEditorFilter.this.rootFolder = (Folder) result.getEntities().get(0);
					MetaDataEditorFilter.this.folderReloaded = true;
					addEntityListener(MetaDataEditorFilter.this.rootFolder);
				}
			}
			MetaDataEditorFilter.this.isInitialized = true;
			//FillFilter();
			//ShowFilterData();
		}, e -> {
			handleEntityQueryError(e);
		}));
	}

	private void handleEntityQueryError(Throwable e) {
		e.printStackTrace();
		fillFilter();
		//ShowFilterData();
	}
		
	private static Map<String, Folder> indexFolders(List<Folder> folders) {
		if (folders == null)
			return Collections.emptyMap();
		
		Map<String, Folder> result = new FastMap<Folder>();
		for (Folder folder : folders) {
			result.put(folder.getName(), folder);
		}
		return result;
	}
	
	//save ShowCase,Roles and Acces Values to Session Folders
	private void saveFilterValues () {
			if ((this.workbenchSession != null) && (this.workbenchSession.getAccessId() != null)) {
				//final Date beginDate = new Date();
				
				boolean hasChanges = false;
				List<Folder> subFolders = null;
	
				this.editionNestedTransaction = this.workbenchSession.getTransaction().beginNestedTransaction();
				
				if (this.rootFolder == null) {
				   this.rootFolder = this.workbenchSession.create(Folder.T);
				   this.rootFolder.setName(ROOT_FOLDER_NAME);
				   LocalizedString displayName = this.workbenchSession.create(LocalizedString.T);
				   displayName.getLocalizedValues().put("default", "metaDataEditorFilterValue");				   
				   this.rootFolder.setDisplayName(displayName);
				   hasChanges = true;
				}
				
				subFolders = this.rootFolder.getSubFolders();
				if (subFolders == null) {
					subFolders = new ArrayList<Folder>();
					this.rootFolder.setSubFolders(subFolders);
					hasChanges = true;
				}
				
				//subFolders.clear();
				
				//for (int i=0; i<j ; i++) {
				
				   /*
				   if (filterString.isEmpty())	{
					   if (i == 0) {filterNameString = ValueUseCaseName;}
					   if (i == 1) {filterNameString = ValueRolesName;}
					   if (i == 2) {filterNameString = ValueAccessName;}						   						   
				   } else {
					   filterNameString = filterString;  
				   }

				   //save only one filter part setting
				   Folder filterFolder = null;
				   for (Folder subFolder : subFolders) {
					  if (subFolder.getName().equals(filterNameString)) {
						  filterFolder = subFolder;
					  }
				   }
				   
				   if (filterFolder == null) {
					   filterFolder = workbenchSession.create(Folder.T);
					   filterFolder.setName(filterNameString);
					   this.rootFolder.getSubFolders().add(filterFolder);
					   hasChanges = true;
				   }
				   */
				   
				   //save to Folder also if checkbox is checked - Tag "0" or "1"
				   for (String stringKey : this.mapStringUseCase.keySet()) {
					   MetaDataEditorFilterCheckBox checkBox = this.checkboxMap.get(stringKey);
					   Boolean checkboxChecked = (checkBox != null) ?  checkBox.getChecked() : true;
					   String tag = (checkboxChecked) ? "1" : "0";	   
					   
					   Folder subFolder = null;
					   Map<String, Folder> rootSubFolders = indexFolders(MetaDataEditorFilter.this.rootFolder.getSubFolders());					   
					   if (rootSubFolders != null)
						   subFolder = rootSubFolders.get(stringKey);
					   /*
					   for (Folder subFilterFolder : this.rootFolder.getSubFolders()) {
						   if (subFilterFolder.getName().equals(stringKey)) {
							   subFolder = subFilterFolder;							  
							   break;
						   }
					   }
					   */
					   if (subFolder == null) {
						   subFolder = this.workbenchSession.create(Folder.T);
						   subFolder.setName(stringKey);
						   LocalizedString displayName = this.workbenchSession.create(LocalizedString.T);
						   displayName.getLocalizedValues().put("default", "metaDataEditorFilterValue");
						   subFolder.setDisplayName(displayName);
						   this.rootFolder.getSubFolders().add(subFolder);
						   Set<String> tags = new FastSet();
						   tags.add(tag);
						   subFolder.setTags(tags);
						   hasChanges = true;						   
					   } else if (!subFolder.getTags().contains(tag)) {							   						   
						   subFolder.getTags().clear();
						   subFolder.getTags().add(tag);
						   hasChanges = true;
					   }
				   }
				//}
								
				if (hasChanges) {	
					this.editionNestedTransaction.commit();
					this.editionNestedTransaction = null;	
					
					/*
					//final Date middleDate = new Date();
					this.workbenchSession.commit(new AsyncCallback<ManipulationResponse>() {
						@Override
						public void onSuccess(ManipulationResponse future) {
							//GlobalState.showSuccess(LocalizedText.INSTANCE.headerBarCreated());
							//Date endDate = new Date();
							//long milisec = endDate.getTime() - beginDate.getTime();							
							//long milisec2 = endDate.getTime() - middleDate.getTime();							
							//textAddFilter.setText(textAddFilter.getText() + " - " + "SaveFilterValues:" + milisec + " load:"+ milisec2);
						}
						
						@Override
						public void onFailure(Throwable t) {
							t.printStackTrace();
							GlobalState.showError("Error while creating MetaDataEditor filter.", t);
						}
					});
					*/
				} else {
					rollbackTransaction();
				}
			}
		}
	
	private void addEntityListener(GenericEntity entity) {
		if (entity != null) {			
	    	this.workbenchSession.listeners().entity(entity).remove(this);		
	    	this.workbenchSession.listeners().entity(entity).add(this);		
		}
	}	
	
	private void rollbackTransaction() {
		try {
			if (this.editionNestedTransaction != null) {
				this.editionNestedTransaction.rollback();
			}
			this.editionNestedTransaction = null;
		} catch (TransactionException e) {
			//ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), e);
			e.printStackTrace();
		} catch (IllegalStateException ex) {
			//Nothing to do: the PP was used within some widget which rolled back the parent transaction already. This may happen within GIMA when canceling it while editing.
		}
	}
	
	@Override
	public void noticeManipulation(final Manipulation manipulation) {
		if (manipulation instanceof PropertyManipulation) {
			new Timer() {
				@Override
				public void run() {
					//Object parentObject = GMEUtil.getParentObject((PropertyManipulation) manipulation);
					GenericEntity entity = null;
					//String propertyName = null;
					Owner manipulationOwner = ((PropertyManipulation) manipulation).getOwner();
					if (manipulationOwner instanceof LocalEntityProperty) {
						entity = ((LocalEntityProperty) manipulationOwner).getEntity();
						//propertyName = ((LocalEntityProperty) manipulationOwner).getPropertyName();
					}
					
					if (entity instanceof Folder && rootFolder != null &&  entity.equals(rootFolder)) {
						switch (manipulation.manipulationType()) {
						case CHANGE_VALUE:
							loadFilterValues ();
							break;
						case ADD:
						case REMOVE:
						case CLEAR_COLLECTION:
							loadFilterValues ();
							break;
						default:
							break;
						}
					} /*else if (entity instanceof Folder && rootFolder != null &&  rootFolder.getSubFolders().contains(entity)) {
						switch (manipulation.manipulationType()) {
						//RVE - only ADD, not Remove, because Tag is removed and than add new String value - and we need check it only once	
						case CHANGE_VALUE:
						case ADD:
							loadFilterValues ();
							break;
						default:
							break;
						}
						
					}*/
				}
			}.schedule(10); //needed, so the value in the entity was the correct one
		}		
			
		if (manipulation instanceof InstantiationManipulation) {
			new Timer() {
					@Override
					public void run() {						
						GenericEntity entity = ((InstantiationManipulation) manipulation).getEntity();
						if (entity == null)
							return;
				
						if (entity instanceof PreliminaryEntityReference)
							if (((PreliminaryEntityReference) entity).getTypeSignature().equals("com.braintribe.model.folder.Folder"))
								loadFilterValues ();
					}
			}.schedule(10);
		}
	}
}
