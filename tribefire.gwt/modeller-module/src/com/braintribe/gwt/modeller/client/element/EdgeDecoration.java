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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField.QuickAccessTriggerFieldListener;
import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmview.action.client.ParserResult;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.gwt.modeller.client.resources.ModellerModuleResources;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsTypeKind;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellergraph.graphics.AggregationKind;
import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.modellergraph.graphics.Point;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.modellergraph.common.ColorPalette;
import com.braintribe.model.processing.modellergraph.editing.EntityTypeProcessingNew;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

public class EdgeDecoration extends FlowPanel implements QuickAccessTriggerFieldListener, ManipulationListener{
	
	boolean readOnly = UrlParameters.getInstance().getParameter("readOnly") != null || UrlParameters.getInstance().getParameter("offline") != null;
	
	private Edge edge;
	private NodeElement fromNodeElement;
	private NodeElement toNodeElement;
	private ModelGraphConfigurationsNew config;
	private TextBox relationshipName;
	private TextBox associationType;
	private Button delete;
	private Button removeAssociationType;
	FlowPanel bottomWrapper = new FlowPanel();
	
	GmModeller modeller;
	PersistenceGmSession session;
	GmMetaModel model;
	QuickAccessDialog quickAccessDialog;
	Supplier<SpotlightPanel> quickAccessPanelProvider;
	
	boolean newlyCreated = false;
	boolean associationComplete = false;
	
	public EdgeDecoration() {
		FlowPanel topWrapper = new FlowPanel();
		Style topWrapperStyle = topWrapper.getElement().getStyle();
		
		topWrapperStyle.setProperty("display", "flex");
		topWrapperStyle.setProperty("justifyContent", "center");
		topWrapperStyle.setProperty("alignItems", "center");
		topWrapperStyle.setProperty("width", "100%");
		topWrapper.add(getRelationshipName());
		topWrapper.add(getDelete());
		add(topWrapper);
		
		
		Style bottomWrapperStyle = bottomWrapper.getElement().getStyle();
		
		bottomWrapperStyle.setProperty("display", "flex");
		bottomWrapperStyle.setProperty("justifyContent", "center");
		bottomWrapperStyle.setProperty("alignItems", "center");
		bottomWrapperStyle.setProperty("width", "100%");
		bottomWrapper.add(getAssociationType());
		bottomWrapper.add(getRemoveAssociationType());
		add(bottomWrapper);
		
		addStyleName("gmModellerEdge");
	}
	
	public void setConfig(ModelGraphConfigurationsNew config) {
		this.config = config;
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
	
	public void setModel(GmMetaModel model) {
		this.model = model;
	}
	
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public void afterPropertySet(boolean nameSet, boolean isMap) {
		if(isMap) {
			GmMapType gmt = (GmMapType) edge.getGmProperty().getType();
			if(gmt.getKeyType() != null && gmt.getValueType() != null && nameSet) {
				if(newlyCreated)
					modeller.previous();
				newlyCreated = false;
			}			
		}else {
			if(newlyCreated)
				modeller.previous();
			newlyCreated = false;
		}
	}
	
	public void afterRemove() {
		boolean noRemaininEdges = modeller.getCurrentModelGraphState().getEdges().isEmpty();		
		if(noRemaininEdges)
			modeller.previous();
	}
	
	public TextBox getRelationshipName() {
		if(relationshipName == null) {
			relationshipName = new TextBox();
			
			Style style = relationshipName.getElement().getStyle();
			style.setProperty("background", "none");
			style.setProperty("border", "none");
			
			relationshipName.addKeyUpHandler(event -> {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					relationshipName.setFocus(false);
			});
			
			relationshipName.addChangeHandler(event -> {
				if(relationshipName.getText() != null) {
					String text = relationshipName.getText();
					GmEntityType entityType = edge.getGmProperty().getDeclaringType();
					if(EntityTypeProcessingNew.isValidPropertyName(text) && EntityTypeProcessingNew.isNameAvailable(text, entityType)) {
						edge.getGmProperty().setName(text);							
						afterPropertySet(true, edge.getGmProperty().getType() instanceof GmMapType);
					}
					else
						relationshipName.setValue(edge.getGmProperty().getName(), false);
				}else
					relationshipName.setValue(edge.getGmProperty().getName(), false);
			});
			relationshipName.setEnabled(!readOnly);
		}
		return relationshipName;
	}
	
	public Button getDelete() {
		if(delete == null) {
			String img = "<img src='"+ModellerModuleResources.INSTANCE.delete().getSafeUri().asString()+"'>";
			delete = new Button(img,(ClickHandler) event -> {
				if(edge.getGmProperty() != null) {
					GmEntityType type1 = edge.getGmProperty().getDeclaringType();
					type1.getProperties().remove(edge.getGmProperty());
				}else {
					GmEntityType type2 = (GmEntityType) modeller.getType(edge.getFromNode().getTypeSignature());
					GmEntityType superType = (GmEntityType) modeller.getType(edge.getToNode().getTypeSignature());
					type2.getSuperTypes().remove(superType);
				}
			});
			delete.setStyleName("modellerButton");
			delete.setVisible(!readOnly);
		}
		return delete;
	}
	
	public TextBox getAssociationType() {
		if(associationType == null) {
			associationType = new TextBox();
			Style style = associationType.getElement().getStyle();
			style.setProperty("background", "none");
			style.setProperty("border", "none");
			associationType.getElement().setAttribute("placeholder", "Choose a key type...");
			associationType.addKeyUpHandler(event -> {
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
			});
		}
		return associationType;
	}
	
	public Button getRemoveAssociationType() {
		if(removeAssociationType == null) {
			String img = "<img src='"+ModellerModuleResources.INSTANCE.delete().getSafeUri().asString()+"'>";
			removeAssociationType = new Button(img,(ClickHandler) event -> {
				GmMapType mt = (GmMapType)edge.getGmProperty().getType();
				if(edge.getStartAggregationKind() == AggregationKind.key_association) {
					mt.setValueType(null);
				}else if(edge.getStartAggregationKind() == AggregationKind.value_association) {
					mt.setKeyType(null);
				}	
				adapt();
			});
			removeAssociationType.setStyleName("modellerButton");
		}
		return removeAssociationType;
	}
	
	private void adapt() {
		adapt(this.fromNodeElement, this.toNodeElement, this.edge);
	}
	
	public void adapt(NodeElement fromNode, NodeElement toNode, Edge edge) {
		this.edge = edge;
		this.fromNodeElement = fromNode;
		this.toNodeElement = toNode;
		try {
			Point fromCenter = fromNode.getNode().getCenter();
			Point toCenter = toNode.getNode().getCenter();			
			double x1 = fromCenter.getX() + fromNode.getNode().getRadius();
			double x2 = toCenter.getX() - toNode.getNode().getRadius();
			
			Style style = getElement().getStyle();
			style.setPosition(Position.ABSOLUTE);
			style.setTop(edge.getAbove() ? edge.getTurning().getY() : edge.getTurning().getY() - 25, Unit.PX);
			style.setLeft(Math.min(x1,x2), Unit.PX);
//			style.setHeight(25, Unit.PX);
			style.setWidth(Math.abs(Math.max(x1,x2) - Math.min(x1,x2)), Unit.PX);
			style.setOpacity(edge.getColor().getAlpha());
			style.setProperty("display", "flex");
			style.setProperty("justifyContent", "center");
			style.setProperty("flexDirection", "column");
			
			style = getRelationshipName().getElement().getStyle();
			style.setColor(ColorPalette.toHex(edge.getColor()));
			if(edge.getGmProperty() != null) {
				GmProperty property = edge.getGmProperty();
				getRelationshipName().setEnabled(!readOnly);
				getRelationshipName().setText(property.getName());
				
				if(property.getType() instanceof GmMapType) {
					add(bottomWrapper);
					GmMapType mt = (GmMapType)property.getType();
					session.listeners().entity(mt).add(this);
					if(edge.getStartAggregationKind() == AggregationKind.key_association) {
						getAssociationType().getElement().setAttribute("placeholder", "Choose a value type...");
						getRemoveAssociationType().setVisible(mt.getValueType() != null);
						getAssociationType().setEnabled(mt.getValueType() == null);
						if(mt.getValueType() != null)
							getAssociationType().setValue(getTypeName(mt.getValueType().getTypeSignature()), false);
						else
							getAssociationType().setValue("", false);
					}else if(edge.getStartAggregationKind() == AggregationKind.value_association) {
						getAssociationType().getElement().setAttribute("placeholder", "Choose a key type...");
						getRemoveAssociationType().setVisible(mt.getKeyType() != null);
						getAssociationType().setEnabled(mt.getKeyType() == null);
						if(mt.getKeyType() != null)
							getAssociationType().setValue(getTypeName(mt.getKeyType().getTypeSignature()), false);
						else
							getAssociationType().setValue("", false);
					}
					
				}else
					remove(bottomWrapper);
				
				if(edge.getColor().getAlpha() == 1 && edge.getGmProperty() == config.currentAddedProperty) {
					select();
					config.currentAddedProperty = null;
				}
			}
			else {
				getRelationshipName().setEnabled(false);
				getRelationshipName().setText("derives from");
				remove(bottomWrapper);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void select() {
		Timer timer = new Timer() {
			@Override
			public void run() {
				newlyCreated = true;
				associationComplete = false;
				getRelationshipName().setFocus(true);
				getRelationshipName().selectAll();
			}
		};
		timer.schedule(50);
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		adapt();
	}
	
	@Override
	public void onQuickAccessResult(QuickAccessResult result) {
		if(result != null) {
			Object o = null;
			if(result.getObject() instanceof GmType)
				o = result.getObject();
			GmType t = o != null ? (GmType) o : result.getType();
			GmMapType mt = (GmMapType)edge.getGmProperty().getType();
			
			if(edge.getStartAggregationKind() == AggregationKind.key_association) {
				mt.setValueType(t);
			}else if(edge.getStartAggregationKind() == AggregationKind.value_association) {
				mt.setKeyType(t);
			}
			adapt();
			afterPropertySet(!newlyCreated, edge.getGmProperty().getType() instanceof GmMapType);			
		}
	}
	
	private void showQuickAccess() throws RuntimeException{
		QuickAccessDialog quickAccessDialog = getQuickAccessDialog();
		
		TypeConditionDisjunction typeCondition = TypeConditionDisjunction.T.createPlain();
		List<TypeCondition> conditions = new ArrayList<>();
		
		typeCondition.setOperands(conditions);
		
		IsTypeKind entityTypeCondition = IsTypeKind.T.createPlain();
		entityTypeCondition.setKind(TypeKind.entityType);
		
		conditions.add(entityTypeCondition);
		
		IsTypeKind enumTypeCondition = IsTypeKind.T.createPlain();
		enumTypeCondition.setKind(TypeKind.simpleType);
		
		conditions.add(enumTypeCondition);
		
		quickAccessDialog.getQuickAccessResult(typeCondition, getAssociationType(), getAssociationType().getText())
				.andThen(result -> onQuickAccessResult(result)).onError(e -> ErrorDialog.show("Error while providing quickAccess", e));
	}
	
	private QuickAccessDialog getQuickAccessDialog() throws RuntimeException {
		if(quickAccessDialog == null){
			quickAccessDialog = new QuickAccessDialog();
			quickAccessDialog.setShadow(false);
			quickAccessDialog.setUseApplyButton(false);
			quickAccessDialog.setUseNavigationButtons(false);
			quickAccessDialog.setInstantiateButtonLabel("Add");			
			quickAccessDialog.setFocusWidget(getAssociationType());
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
			spotlightPanel.configureGmMetaModel(null);
			spotlightPanel.configureGmMetaModel(model);
			spotlightPanel.setTextField(getAssociationType());
			spotlightPanel.setMinCharsForFilter(3);
			spotlightPanel.setUseApplyButton(false);
			spotlightPanel.setLoadTypes(true);
			spotlightPanel.setLoadExistingValues(false);
			spotlightPanel.setSimpleTypesValuesProvider(() -> new SimpleTypesProvider());
			return spotlightPanel;
		};
	}
	
	private String getTypeName(String typeSignature) {
		if(typeSignature.endsWith(config.doubleTypeSuffix))
			typeSignature = typeSignature.substring(0, typeSignature.lastIndexOf(config.doubleTypeSuffix));
		return typeSignature.substring(typeSignature.lastIndexOf(".") + 1, typeSignature.length());
	}
	
	class SimpleTypesProvider implements Function<ParserArgument, List<ParserResult>>{

		@Override
		public List<ParserResult> apply(ParserArgument pa) {
			List<ParserResult> results = new ArrayList<>();
			if (pa.hasValue()) {
				modeller.getOracle().getGmSimpleTypes().forEach(gmSimpleType -> {
					if(gmSimpleType.getTypeSignature().toLowerCase().contains(pa.getValue().toLowerCase())) {
						ParserResult pr = new ParserResult(gmSimpleType.getTypeSignature(), gmSimpleType.getTypeSignature(), gmSimpleType);
						results.add(pr);
					}
				});
			}
			return results;
		}
		
	}

}
