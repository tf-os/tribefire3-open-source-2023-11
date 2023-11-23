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
package com.braintribe.gwt.simplepropertypanel.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmDetailViewListener;
import com.braintribe.gwt.gmview.client.GmDetailViewSupport;
import com.braintribe.gwt.gmview.client.GmDetailViewSupportContext;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.simplepropertypanel.client.resources.LocalizedText;
import com.braintribe.gwt.simplepropertypanel.client.validation.SimplePropertyValidationPanel;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.prompt.AutoCommit;
import com.braintribe.model.meta.info.GmCustomTypeInfo;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.util.KeyNav;

public class SimplePropertyPanel extends FlowPanel implements GmEntityView, GmDetailViewSupport {

	boolean readOnly = UrlParameters.getInstance().getParameter("readOnly") != null || UrlParameters.getInstance().getParameter("offline") != null;

	protected FlowPanel typeTitelPanel;
	protected FlowPanel mainPanel;
	protected SimplePropertyValidationPanel validationPanel;

	protected Map<String, GmTypeSection> typeSections = new HashMap<>();

	private boolean sectionsVisible = true;

	private ModelPath modelPath;
	private GmType type;
	private PersistenceGmSession session;
	private boolean autoCommit;
	private Action commitAction;
	// private XElement elementToMask;

	// private boolean readOnly = false;

	@SuppressWarnings("unused")
	public SimplePropertyPanel() {
		// setTabIndex(0);
		// setBorders(false);

		addStyleName("gmSimplePropertyPanel");
		add(getTypeTitelPanel());
		add(getMainPanel());
		add(getValidationPanel());

		new KeyNav(this) {
			@Override
			public void onKeyPress(NativeEvent evt) {
				if ((evt.getCtrlKey() || (GXT.isMac() && evt.getMetaKey())) && evt.getKeyCode() == KeyCodes.KEY_N) {
					System.err.println("new simple");
					evt.stopPropagation();
					evt.preventDefault();
				}
			}
		};
	}

	/**
	 * Configures the {@link Action} used for committing when {@link AutoCommit} is available.
	 */
	@Configurable
	public void setCommitAction(Action commitAction) {
		this.commitAction = commitAction;
	}

	public FlowPanel getTypeTitelPanel() {
		if (typeTitelPanel == null) {
			typeTitelPanel = new FlowPanel();
			typeTitelPanel.addStyleName("typeNameTitel");
			typeTitelPanel.addStyleName("clickable");

			typeTitelPanel.addDomHandler(event -> {
				sectionsVisible = !sectionsVisible;
				typeSections.values().forEach(section -> section.setVisible(sectionsVisible));
			}, ClickEvent.getType());

		}
		return typeTitelPanel;
	}

	public FlowPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new FlowPanel();
			mainPanel.addStyleName("simplePropertyPanel");
		}
		return mainPanel;
	}

	public SimplePropertyValidationPanel getValidationPanel() {
		if (validationPanel == null) {
			validationPanel = new SimplePropertyValidationPanel();
			validationPanel.setVisible(false);
		}
		return validationPanel;
	}

	public void handleEntityType(GmEntityType entityType, boolean main, boolean editable) {

		if (main) {
			type = entityType;
			typeSections.clear();
			getMainPanel().clear();
			String name = GmTypeRendering.getTypeName(entityType.getTypeSignature());
			getTypeTitelPanel().getElement().setInnerText(LocalizedText.INSTANCE.propertiesOf(name));
			getTypeTitelPanel().setTitle(name);
		}

		if (entityType.getSuperTypes() != null && !entityType.getSuperTypes().isEmpty()) {
			for (GmEntityType superType : entityType.getSuperTypes()) {
				handleEntityType(superType, false, false);
			}
		}

		if (!typeSections.containsKey(entityType.getTypeSignature())) {
			GmTypeSection typeSection = new GmTypeSection(editable, main);
			typeSection.setSession(session);
			typeSection.setParentPanel(this);
			typeSection.setType(entityType);
			getMainPanel().add(typeSection);
			typeSections.put(entityType.getTypeSignature(), typeSection);
		}

		getValidationPanel().validateEntityType(entityType);

	}

	public void handleEnumType(GmEnumType enumType, boolean editable) {
		type = enumType;
		typeSections.clear();
		getMainPanel().clear();
		String name = GmTypeRendering.getTypeName(enumType.getTypeSignature());
		getTypeTitelPanel().getElement().setInnerText(LocalizedText.INSTANCE.constantsOf(name));
		getTypeTitelPanel().setTitle(name);

		GmTypeSection typeSection = new GmTypeSection(editable, true);
		typeSection.setSession(session);
		typeSection.setParentPanel(this);
		typeSection.setType(enumType);
		getMainPanel().add(typeSection);
		typeSections.put(enumType.getTypeSignature(), typeSection);

		getValidationPanel().validateEnumType(enumType);
	}

	public void validate() {
		if (type instanceof GmEnumType)
			getValidationPanel().validateEnumType((GmEnumType) type);
		else if (type instanceof GmEntityType)
			getValidationPanel().validateEntityType((GmEntityType) type);
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		this.modelPath = modelPath;
		if (modelPath == null) {
			clearSelection();
			return;
		}

		GenericEntity modelPathValue = modelPath.last().getValue();
		refresh(modelPathValue.entityType(), modelPathValue);

		/* try { Object model = modelPath.first().getValue(); GmCustomTypeInfo typeInfo = (GmCustomTypeInfo)modelPath.last().getValue(); boolean
		 * editable = model instanceof GmMetaModel && typeInfo.getDeclaringModel() == model;
		 * 
		 * if (typeInfo instanceof GmEntityType) handleEntityType((GmEntityType) typeInfo, true, editable); else if (typeInfo instanceof GmEnumType)
		 * handleEnumType((GmEnumType)typeInfo, editable); else clearSelection(); } catch(Exception ex) { clearSelection(); } */
	}

	protected void handleAutoCommit() {
		if (autoCommit && commitAction != null && commitAction.getEnabled())
			commitAction.perform(null);
	}

	private void refresh(EntityType<?> type, GenericEntity entity) {
		EntityReference reference = entity.reference();
		if (readOnly || !(reference instanceof PersistentEntityReference)) {
			handleType(entity);
			return;
		}

		// elementToMask = (XElement) getElement().getParentElement().cast();
		// if (elementToMask != null)
		// elementToMask.mask(LocalizedText.INSTANCE.refreshing());
		Object id = type.getIdProperty().get(entity);
		EntityQuery query = EntityQueryBuilder.from(type).where().property(type.getIdProperty().getName()).eq(id).tc().negation().joker().done();
		session.query().entities(query).result(Future.async(this::error, this::handleResult));
	}

	private void handleResult(EntityQueryResultConvenience conv) {
		// if (elementToMask != null)
		// elementToMask.unmask();
		handleType(conv.first());
	}

	private void handleType(GenericEntity entity) {
		try {
			Object first = modelPath.first().getValue();
			boolean editable = true;
			if (first instanceof GmMetaModel) {
				GmCustomTypeInfo typeInfo = (GmCustomTypeInfo) modelPath.last().getValue();
				editable = typeInfo.getDeclaringModel() == first;
			}
			if (GmEntityType.T.isValueAssignable(entity)) {
				handleEntityType((GmEntityType) entity, true, editable);
			} else if (GmEnumType.T.isValueAssignable(entity)) {
				handleEnumType((GmEnumType) entity, editable);
			} else
				clearSelection();
		} catch (Exception ex) {
			ex.printStackTrace();
			clearSelection();
		}
	}

	private void error(Throwable t) {
		t.printStackTrace();
		// if (elementToMask != null)
		// elementToMask.unmask();
	}

	private void clearSelection() {
		type = null;
		typeSections.clear();
		getMainPanel().clear();
		getTypeTitelPanel().getElement().setInnerText(LocalizedText.INSTANCE.noTypeSelected());
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		// NOP
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		// NOP
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return null;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return null;
	}

	@Override
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		// NOP
	}

	@Override
	public GmContentView getView() {
		return null;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.session = gmSession;

		if (gmSession != null) {
			ModelMdResolver mdResolver = gmSession.getModelAccessory().getMetaData();
			autoCommit = mdResolver == null ? false : mdResolver.is(AutoCommit.T);
		}
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return null;
	}

	@Override
	public void configureUseCase(String useCase) {
		// NOP
	}

	@Override
	public String getUseCase() {
		return null;
	}

	@Override
	public void addDetailViewListener(GmDetailViewListener dl) {
		return;
	}

	@Override
	public GmDetailViewSupportContext getGmDetailViewSupportContext() {
		return null;
	}

	@Override
	public void removeDetailViewListener(GmDetailViewListener dl) {
		// NOP
	}

	public void deselectProperty() {
		// NOP
	}

	public void selectProperty() {
		// NOP
	}

}
