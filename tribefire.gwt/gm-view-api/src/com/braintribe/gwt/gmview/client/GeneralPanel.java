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
package com.braintribe.gwt.gmview.client;

import java.util.List;

import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.widget.core.client.container.HasLayout;

public class GeneralPanel extends FlowPanel implements GmEntityView, InitializableBean, DisposableBean {
	
	interface GeneralXTemplate extends XTemplates {
		@XTemplate(source = "com/braintribe/gwt/gmview/bt-resources/general/generalPanel.html")
	    SafeHtml general(GeneralContext gc);
		
		@XTemplate(source = "com/braintribe/gwt/gmview/bt-resources/general/generalPanel-collapsed.html")
	    SafeHtml collapsed(GeneralContext gc);
		
		@XTemplate(source = "com/braintribe/gwt/gmview/bt-resources/general/actionPanel.html")
	    SafeHtml actionPanel(GeneralContext gc);
	}
	
	protected class GeneralContext {
		
		private SafeStyles iconStyle;
		private String si;
		private String typeName;
		private String typeSignature;
		private String hint;
		private String expandIconId = HTMLPanel.createUniqueId();
		
		public GeneralContext(SafeStyles iconStyle, String si, String typeName, String typeSignature, String hint) {
			this.iconStyle = iconStyle;
			this.si = si;
			this.typeName = typeName;
			this.typeSignature = typeSignature;
			this.hint = hint;
		}
		
		public SafeStyles getIconStyle() {
			return iconStyle;
		}
		
		public String getSi() {
			return si;
		}
		
		public String getTypeName() {
			return typeName;
		}
		
		public String getTypeSignature() {
			return typeSignature;
		}
		
		public String getHint() {
			return hint;
		}
		
		public String getExpandIconId() {
			return expandIconId;
		}
	}
	
	private ModelPath modelPath;
	private GenericEntity parentEntity;
	private EntityType<GenericEntity> parentEntityType;
	private String useCase;	
	private boolean lenient = true;
	private IconProvider iconProvider;
	private ImageResource defaultIcon;
	private ModelAction action;
	private boolean collapsed;
	private boolean displayShortName = false;
	
	protected PreviewPanel previewPanel;
	protected GeneralXTemplate template = GWT.create(GeneralXTemplate.class);
	
	public GeneralPanel() {
		addHandler(event -> handleResize(), ResizeEvent.getType());
	}

	/**
	 * Configures the required provider which will provide icons.
	 */
	@Required
	public void setIconProvider(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}
	
	public void setPreviewPanel(PreviewPanel previewPanel) {
		this.previewPanel = previewPanel;
		previewPanel.addPreviewListener(this::handleResize);
	}
	
	@Required
	public void setDefaultIcon(ImageResource defaultIcon) {
		this.defaultIcon = defaultIcon;
	}
	
	@Configurable
	public void setAction(ModelAction action) {
		this.action = action;
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void intializeBean() throws Exception {
		init(false);
	}

	@Override
	public void setContent(ModelPath modelPath) {
		this.modelPath = modelPath;
		this.parentEntity = modelPath != null ? (GenericEntity) modelPath.last().getValue() : null;
		this.parentEntityType = this.parentEntity != null ? this.parentEntity.entityType() : null;
		
		init(collapsed);		
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		//NOP
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
		//NOP
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
//		previewPanel.configureGmSession(gmSession);
		if (!(gmSession instanceof ModelEnvironmentDrivenGmSession))
			return;
		
		ModelEnvironmentDrivenGmSession session = (ModelEnvironmentDrivenGmSession) gmSession;
		if (session.getModelEnvironment() == null || session.getModelEnvironment().getWorkbenchConfiguration() == null)
			return;

		displayShortName = session.getModelEnvironment().getWorkbenchConfiguration().getDisplayShortName();
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return null;
	}

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}

	@Override
	public String getUseCase() {
		return useCase;
	}
	
	public void init(boolean collapsed) {
		this.collapsed = collapsed;
		clear();
		
		if (action != null) {
			Scheduler.get().scheduleDeferred(() -> {
				GmContentView parentView = getParentView(GeneralPanel.this);
				if (parentView != null)
					action.configureGmContentView(parentView);
			});
		}
		
		if (parentEntity == null) {
			if (action == null || action.getHidden()) {
				add(new HTMLPanel(""));
				handleResize();
				return;
			}
			GeneralContext gc = new GeneralContext(new SafeStylesBuilder().toSafeStyles(),
					action.getIcon() != null ? action.getIcon().getSafeUri().asString() : "", "", "", "");
			add(new HTMLPanel(template.actionPanel(gc)));
			Scheduler.get().scheduleDeferred(() -> prepareActionClickHandler(gc.getExpandIconId()));
			handleResize();
			return;
		}
		
		double iconOpacity = 0.5;
		ImageResource icon = defaultIcon;
		IconAndType iconAndType = iconProvider.apply(modelPath);
		if (iconAndType != null && iconAndType.getIcon() != null) {
			icon = iconAndType.getIcon();
			iconOpacity = 1;
		}
		
		String entityString = "";
		String selectiveInformation = SelectiveInformationResolver.resolve(parentEntityType, parentEntity, (ModelMdResolver) null, useCase, lenient/* , null */);
		if (selectiveInformation != null && !selectiveInformation.trim().isEmpty())
			entityString = selectiveInformation;
		
		Image img = new Image(icon);
		SafeStyles iconStyle = new SafeStylesBuilder().backgroundImage(UriUtils.fromTrustedString(img.getUrl())).opacity(iconOpacity).toSafeStyles();

		boolean actionEnabled = true;
		if (action == null || action.getHidden())
			actionEnabled = false;
		
		String typeSignature = displayShortName ? parentEntityType.getShortName() : parentEntityType.getTypeSignature();
		GeneralContext gc = new GeneralContext(iconStyle, entityString, parentEntityType.getShortName(), typeSignature,
				prepareTypeHint(parentEntityType, parentEntity));
		if (collapsed)
			add(new HTMLPanel(template.collapsed(gc)));
		else
			add(new HTMLPanel(template.general(gc)));
		
		if (actionEnabled)
			Scheduler.get().scheduleDeferred(() -> prepareActionClickHandler(gc.getExpandIconId()));
		handleResize();
		
//		Preview preview = GmSessions.getMetaData(parentEntity).entity(parentEntity).meta(Preview.T).exclusive();		
		
		if(!this.collapsed) {
			previewPanel.setParentEntity(parentEntity);
			previewPanel.configureUseCase(getUseCase());
			add(previewPanel);
		}
	}
	
	private String prepareTypeHint(EntityType<?> type, GenericEntity entity) {
		StringBuilder builder = new StringBuilder();
		if (entity != null && entity.session() instanceof ModelEnvironmentDrivenGmSession) {
			ModelEnvironmentDrivenGmSession session = (ModelEnvironmentDrivenGmSession) entity.session();
			if (session.getModelEnvironment() != null && session.getModelAccessory() != null && session.getModelAccessory().getOracle() != null) {
				GmType gmType = session.getModelAccessory().getOracle().findGmType(type);
				if (gmType != null) {
					GmMetaModel declaringModel = gmType.getDeclaringModel();
					if (declaringModel != null) {
						builder.append(declaringModel.getName()).append("#");
						builder.append(declaringModel.getVersion()).append(":");
					}
				}
			}
		}
		builder.append(type.getTypeSignature());
		
		return builder.toString();
	}
	
	private void prepareActionClickHandler(String expandIconId) {
		Element element = DOM.getElementById("expandIcon-" + expandIconId);
		if (element == null)
			return;
		
		Event.sinkEvents(element, Event.ONCLICK);
		DOM.setEventListener(element, event -> {
		      if (Event.ONCLICK == event.getTypeInt())
		    	  action.perform(null);
		});
	}
	
	private void handleResize() {
		Scheduler.get().scheduleDeferred(() -> {
			Widget parent = getParent();
			HasLayout parentPanel = getParentGmContentView(parent);
			if (parentPanel != null)
				parentPanel.forceLayout();
			else if (parent instanceof HasLayout)
				((HasLayout) parent).forceLayout();
		});
	}
	
	private GmContentView getParentView(Widget view) {
		Widget parent = view.getParent();
		if (parent instanceof GmContentView)
			return (GmContentView) parent;
		else if (parent != null)
			return getParentView(parent);
		
		return null;
	}
	
	private HasLayout getParentGmContentView(Widget widget) {
		if (widget == null)
			return null;
		
		if (widget instanceof GmEntityView && widget instanceof HasLayout)
			return (HasLayout) widget;
		
		return getParentGmContentView(widget.getParent());
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (previewPanel != null)
			previewPanel.disposeBean();
	}

}
