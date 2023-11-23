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
package com.braintribe.gwt.modeller.client.element;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField.QuickAccessTriggerFieldListener;
import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.modeller.client.GmModellerRenderer;
import com.braintribe.gwt.modeller.client.GmModellerTypeSource;
import com.braintribe.gwt.modeller.client.resources.ModellerModuleResources;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellergraph.graphics.Node;
import com.braintribe.model.modellergraph.graphics.Point;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class NodeDecoration extends FlowPanel implements QuickAccessTriggerFieldListener{
	
	private final static String HOVER_COLOR = "#ffefd9";
	private final static String NORMAL_COLOR = "white";
	private final static String SELECTED_COLOR = "#ffddad";
	
	private final static String TYPE_CHOICE = "?";
	
	private GmModellerRenderer renderer;
	private Node node;
	private ModelGraphConfigurationsNew config;
	private Label typeName;
	private Label modelName;
	private Label typeKind;
	private boolean selected = false;	
	
	private GmModellerTypeSource typeSource;
	private QualifiedEntityAssignment qualifiedEntityAssignment;	
	
	FlowPanel mappingWrapper;
	TextBox mappingType;
	Button removeMapping;	
	Button pin;
	
	QuickAccessDialog quickAccessDialog;
	Supplier<SpotlightPanel> quickAccessPanelProvider;
	
	//private int minFontSize = 8;
	//private int maxFontSize = 22;
	
	FlowPanel top = new FlowPanel();
	FlowPanel center = new FlowPanel();
	FlowPanel bottom = new FlowPanel();
	
	public NodeDecoration() {		
		addStyleName("gmModellerNode");
		
		
		top.addStyleName("gmModellerNode-top");
		
		
		center.addStyleName("gmModellerNode-center");
		
		
		bottom.addStyleName("gmModellerNode-bottom");
		
		add(top);
		add(center);
		add(bottom);
		
		top.add(getPin());
		
		center.add(getTypeKind());
		center.add(getTypeName());
		center.add(getModelName());
		
		bottom.add(getMappingWrapper());
	}
	
	public void setRenderer(GmModellerRenderer renderer) {
		this.renderer = renderer;
	}
	
	public void setConfig(ModelGraphConfigurationsNew config) {
		this.config = config;
	}
	
	public void setTypeSource(GmModellerTypeSource typeSource) {
		this.typeSource = typeSource;
		//center.add(typeSource);
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
	
	public void adapt(Node node) {
		this.node = node;		
		String typeSig = node.getTypeSignature();
		if(typeSig.endsWith(config.doubleTypeSuffix))
			typeSig = typeSig.substring(0, typeSig.lastIndexOf(config.doubleTypeSuffix));
		GmType type = typeSource.getType(typeSig);
		if(!node.getTypeSignature().equals(TYPE_CHOICE)) {
			
			getPin().setVisible(node.getPinned());			
			Style pinStyle = getPin().getElement().getStyle();
			pinStyle.setPaddingLeft(node.getRadius()/5, Unit.PX);
			pinStyle.setPaddingTop(node.getRadius()/5, Unit.PX);
			
			getModelName().setVisible(config.modellerView.getSettings().getShowAdditionalInfos());
			getTypeKind().setVisible(config.modellerView.getSettings().getShowAdditionalInfos());
			
			if(type != null) {
				if(type.isGmEntity())
					getElement().setAttribute("draggable", "true");
				else
					getElement().setAttribute("draggable", "false");
				
//				getTypeName().setText(getTypeName(node.getTypeSignature()));
				getTypeName().setText(node.getText());
				
				GmMetaModel model = type.getDeclaringModel();
				if(model != null)
					getModelName().setText(getModelName(model.getName()));
				
				if(type.isGmEntity()) {
					GmEntityType entityType = (GmEntityType)type;
					if(entityType.getIsAbstract()) {
						setStyleDependentName("abstract", true);
						getTypeKind().setText("Abstract");
					}
					else {
						setStyleDependentName("entity", true);
						getTypeKind().setText("");
					}
				}
				else if(type.isGmEnum()) {
					setStyleDependentName("enum", true);
					getTypeKind().setText("Enum");
				}
			}
		}else {
			getPin().setVisible(false);
			getTypeName().setVisible(false);
			getModelName().setVisible(false);
			getTypeKind().setVisible(false);
			getElement().setAttribute("draggable", "false");
			center.add(typeSource);
			typeSource.setText("");
			if(node.getColor().getAlpha() == 1) {
				Scheduler.get().scheduleFixedDelay(() -> {
					typeSource.setFocus(true);
					typeSource.selectAll();
					return false;
				}, 50);
			}
		}
		
		if(type != null && type.isGmEntity()) {
			if(config.modellerView.getSettings().getUseMapper()) {
				getMappingWrapper().setVisible(true);
				
				qualifiedEntityAssignment = typeSource.getResolver().entityTypeSignature(node.getTypeSignature()).meta(QualifiedEntityAssignment.T).exclusive();
				if(qualifiedEntityAssignment != null) {
					String typeName = getTypeName(qualifiedEntityAssignment.getEntityType().getTypeSignature());
					getMappingType().setValue(typeName, false);
					getMappingType().setReadOnly(true);
					getMappingType().addStyleName("readOnly");
					getRemoveMapping().setVisible(true);
				}else {
					getMappingType().setValue("", false);
					getMappingType().setReadOnly(false);
					getMappingType().removeStyleName("readOnly");
					getRemoveMapping().setVisible(false);
				}
			}
			else {
				getMappingWrapper().setVisible(false);
			}
		}else
			getMappingWrapper().setVisible(false);		
		
		double opacity = node.getColor().getAlpha();
		Point p = node.getCenter();
		Style style = getElement().getStyle();
		style.setTop(p.getY() - node.getRadius(), Unit.PX);
		style.setLeft(p.getX() - node.getRadius(), Unit.PX);
		style.setHeight(node.getRadius() * 2, Unit.PX);
		style.setWidth(node.getRadius() * 2, Unit.PX);
		style.setOpacity(opacity);
		if(selected)
			style.setBackgroundColor(SELECTED_COLOR);
		else
			style.setBackgroundColor(NORMAL_COLOR);
		
		//int width = (int) (node.getRadius() * 2);
//		String calc = "calc(" + minFontSize + "px + " + "(" + maxFontSize + " - " + minFontSize + ") * ((100vw - " + (width/2) + "px) / (" + width + " - " + (width/2) + ")))";
//		getTypeName().getElement().getStyle().setProperty("fontSize", calc);
		
		//calc([minimum size] + ([maximum size] - [minimum size]) * ((100vw - [minimum viewport width]) / ([maximum viewport width] - [minimum viewport width])));
		//calc(8px + (22 - 8) * ((125px - 62px) / (125 - 62)))
		
	}
	
	public void reset() {
		adapt(node);
	}
	
	public void downplay() {
		Style style = getElement().getStyle();
		style.setOpacity(GmModellerElement.DOWNPLAY_OPACITY);
	}
	
	public Label getTypeName() {
		if(typeName == null) {
			typeName = new Label();
			typeName.addStyleName("gmModellerNodeTypeName");
		}
		return typeName;
	}	
	
	public Label getModelName() {
		if(modelName == null) {
			modelName = new Label();
			modelName.addStyleName("gmModellerNodeModelName");
		}
		return modelName;
	}
	
	public Label getTypeKind() {
		if(typeKind == null) {
			typeKind = new Label();
			typeKind.addStyleName("gmModellerNodeModelName");
		}
		return typeKind;
	}
	
	public FlowPanel getMappingWrapper() {
		if(mappingWrapper == null) {
			mappingWrapper = new FlowPanel();
			mappingWrapper.addStyleName("mappingWrapper");
			mappingWrapper.add(getMappingType());
			mappingWrapper.add(getRemoveMapping());
		}
		return mappingWrapper;
	}
	
	public Button getRemoveMapping() {
		if(removeMapping == null) {
			String img = "<img src='"+ModellerModuleResources.INSTANCE.delete().getSafeUri().asString()+"'>";
			removeMapping = new Button(img,(ClickHandler) event -> {
				if(qualifiedEntityAssignment != null) {
					NestedTransaction nt = typeSource.getSession().getTransaction().beginNestedTransaction();
					typeSource.getEditor().onEntityType(node.getTypeSignature()).removeMetaData(m -> {
						return m == qualifiedEntityAssignment;
					});
					
					//typeSource.getOracle().findEntityTypeOracle(node.getTypeSignature()).getProperties().asProperties().forEach(property -> {
					for(GmProperty p : getProperties((GmEntityType) typeSource.getType(node.getTypeSignature()))){						
						ModelMetaDataEditor mmde = typeSource.getEditor();
						mmde.onEntityType(node.getTypeSignature()).removePropertyMetaData(p.getName(), pm -> {
							return pm instanceof PropertyAssignment;
						});
					}
					nt.commit();
				}
			});
		}
		return removeMapping;
	}
	
	public Button getPin() {
		if(pin == null) {
			String img = "<img src='"+ModellerModuleResources.INSTANCE.pin().getSafeUri().asString()+"'>";
			pin = new Button(img,(ClickHandler) event -> {
				Style pinStyle = getPin().getElement().getStyle();	
//					Set<String> addedTypes = config.addedTypes;
				Set<String> addedTypes = config.modellerView.getIncludesFilterContext().getAddedTypes();
				if(addedTypes.contains(node.getTypeSignature())) {
					addedTypes.remove(node.getTypeSignature());
					pinStyle.setOpacity(0.3);
				}
				else {	
					addedTypes.add(node.getTypeSignature());
					pinStyle.setOpacity(1);
				}
				renderer.rerender();
			});
			pin.addStyleName("gmModellerNodePin");
		}
		return pin;
	}
	
	public void select() {
		selected = true;
		over();
	}
	
	public void deselect() {
		selected = false;
		out();
	}
	
	public void out() {
		Style style = getElement().getStyle();
		if(selected)
			style.setBackgroundColor(SELECTED_COLOR);
		else
			style.setBackgroundColor(NORMAL_COLOR);
		style.setZIndex(0);
		if(!node.getTypeSignature().equals(TYPE_CHOICE))
			getPin().setVisible(node.getPinned());
		else
			getPin().setVisible(false);
		renderer.hideTooltip();
	}
	
	public void over() {
		Style style = getElement().getStyle();
		if(selected)
			style.setBackgroundColor(SELECTED_COLOR);
		else
			style.setBackgroundColor(HOVER_COLOR);
		style.setZIndex(99);
		
		getPin().setVisible(!node.getTypeSignature().equals(TYPE_CHOICE));
		
		Style pinStyle = getPin().getElement().getStyle();
		pinStyle.setOpacity(node.getPinned() ? 1 : 0.3);
		
		String typeSig = node.getTypeSignature();
		if(typeSig.endsWith(config.doubleTypeSuffix))
			typeSig = typeSig.substring(0, typeSig.lastIndexOf(config.doubleTypeSuffix));
		renderer.showTooltip(typeSig);
	}
	
	public void show() {
		Style style = getElement().getStyle();
		style.setProperty("display", "flex");
		style.setProperty("pointerEvents", "all");
	}
	
	public void hide() {
		Style style = getElement().getStyle();
		style.setProperty("display", "none");
		style.setProperty("pointerEvents", "none");
	}
	
	public void ensureDragHandlers(NodeElement nodeElement) {
		addDomHandler(nodeElement, DragStartEvent.getType());
		addDomHandler(nodeElement, DragEvent.getType());
		addDomHandler(nodeElement, DragEndEvent.getType());
		
		addDomHandler(nodeElement, DragEnterEvent.getType());
		addDomHandler(nodeElement, DragLeaveEvent.getType());
		addDomHandler(nodeElement, DragOverEvent.getType());		
		
		addDomHandler(nodeElement, DropEvent.getType());		
	}
	
	public void ensureMouseHandlers(NodeElement nodeElement) {
		addDomHandler(nodeElement, ClickEvent.getType());	
		addDomHandler(nodeElement, MouseOverEvent.getType());	
		addDomHandler(nodeElement, MouseOutEvent.getType());	
		
		getTypeName().addDomHandler(nodeElement, ClickEvent.getType());
	}
	
	private String getTypeName(String typeSignature) {
		if(typeSignature.endsWith(config.doubleTypeSuffix))
			typeSignature = typeSignature.substring(0, typeSignature.lastIndexOf(config.doubleTypeSuffix));
		return typeSignature.substring(typeSignature.lastIndexOf(".") + 1, typeSignature.length());
	}
	
	private String getModelName(String modelName) {
		return modelName.contains(":") ? modelName.split(":")[1] : modelName;
	}
	
	public TextBox getMappingType() {
		if(mappingType == null) {
			mappingType = new TextBox();
			mappingType.addStyleName("gmModellerNodeMappingTypeName");
			mappingType.getElement().setAttribute("placeholder", "Choose mapping type...");
			mappingType.addKeyUpHandler(event -> {
				if(!mappingType.isReadOnly()) {
					int keyCode = event.getNativeKeyCode();
					if (keyCode == KeyCodes.KEY_ESCAPE) {
						onQuickAccessResult(null);
						return;
					}
					
					try {
						if (keyCode != KeyCodes.KEY_MAC_FF_META && 
								keyCode != KeyCodes.KEY_ENTER && 
								keyCode != KeyCodes.KEY_DOWN && keyCode != KeyCodes.KEY_UP && keyCode != 
								KeyCodes.KEY_LEFT && keyCode != KeyCodes.KEY_RIGHT)
							showQuickAccess();
					} catch(Exception ex) {
						ErrorDialog.show("Error while providing quickAccessDialog", ex);
					}
				}
			});
			
			mappingType.addClickHandler(event -> {
				if(mappingType.isReadOnly())
					typeSource.showMapper((GmEntityType) typeSource.getType(node.getTypeSignature()));
			});
		}
		return mappingType;
	}
	
	private void showQuickAccess() throws RuntimeException{
		QuickAccessDialog quickAccessDialog = getQuickAccessDialog();
		quickAccessDialog
				.getQuickAccessResult(quickAccessDialog.getQuickAccessPanel().prepareTypeCondition(GmEntityType.T), getMappingType(),
						getMappingType().getText()) //
				.andThen(result -> onQuickAccessResult(result)) //
				.onError(e -> ErrorDialog.show("Error while providing quickAccess", e));
	}
	
	private QuickAccessDialog getQuickAccessDialog() throws RuntimeException {
		if(quickAccessDialog == null){
			quickAccessDialog = new QuickAccessDialog();
			quickAccessDialog.setShadow(false);
			quickAccessDialog.setUseApplyButton(false);
			quickAccessDialog.setUseNavigationButtons(false);
			quickAccessDialog.setInstantiateButtonLabel("Add");			
			quickAccessDialog.setFocusWidget(getMappingType());
			quickAccessDialog.setQuickAccessPanelProvider(getQuickAccessPanelProvider());
			quickAccessDialog.addStyleName(WorkbenchResources.INSTANCE.css().border());
			try {
				quickAccessDialog.intializeBean();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return quickAccessDialog;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider() {
		return () -> {
			SpotlightPanel spotlightPanel = quickAccessPanelProvider.get();
			spotlightPanel.setTextField(getMappingType());
			spotlightPanel.setMinCharsForFilter(3);
			spotlightPanel.setUseApplyButton(false);
			spotlightPanel.setLoadTypes(false);
			spotlightPanel.setLoadExistingValues(true);
			return spotlightPanel;
		};
	}
	
	@Override
	public void onQuickAccessResult(QuickAccessResult result) {
		if(result != null) {
			GmEntityType mappingType = (GmEntityType) result.getObject();
			if(qualifiedEntityAssignment != null)
				qualifiedEntityAssignment.setEntityType(mappingType);
			else
				typeSource.addMapping(node.getTypeSignature(), mappingType);
			adapt(node);
		}
			
	}
	
	private static Set<GmProperty> getProperties(GmEntityType gmEntityType){
		Set<GmProperty> properties = new HashSet<>();
		if(gmEntityType.getProperties() != null)
			properties.addAll(gmEntityType.getProperties());
		if(gmEntityType.getSuperTypes() != null && !gmEntityType.getSuperTypes().isEmpty()){
			for(GmEntityType superType : gmEntityType.getSuperTypes())
				properties.addAll(getProperties(superType));
		}
		return properties;
	}
}
