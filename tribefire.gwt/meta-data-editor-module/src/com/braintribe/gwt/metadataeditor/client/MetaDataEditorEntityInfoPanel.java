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

import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorEntityInfoProvider;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.info.GmCustomTypeInfo;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

//public class MetaDataEditorEntityInfoPanel extends ContentPanel implements InitializableBean, MetaDataEditorEntityInfoProvider {
public class MetaDataEditorEntityInfoPanel extends BorderLayoutContainer implements InitializableBean, MetaDataEditorEntityInfoProvider {
	//private static final Logger logger = new Logger(MetaDataEditorEntityInfoPanel.class);
	private static int gppId = 0;
	private int localQppId = 0;
	
	private PersistenceGmSession gmSession;
	//private PersistenceGmSession workbenchSession;
	private Image iconImage;
	private Label titleLabel;
	private Label titleLabel2;
	private Label titleLabel3;
	private Label titleDeclaredLabel;
	private Label titleDeclaredLabel2;
	private Label editedModelLabel;
	private Label editingLabel;
	private HTMLPanel panelLeft; 
	private StringBuilder htmlLeft;
	private HTMLPanel panelRight; 
	private StringBuilder htmlRight;
	private ContentPanel panelRightContent;
	//private Label descriptionLabel = new Label();
	private GenericEntity parentEntity;
	//private ModelPath contentPath;
	private IconProvider iconProvider;
	private String useCase;
		
	//set sessions
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		if (this.iconProvider != null)
			this.iconProvider.configureGmSession(gmSession);
	}
		
	@Override
	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}
	
	/*public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}*/

	@Required
	public void setIconProvider(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		if (this.iconProvider != null)
			this.iconProvider.configureUseCase(useCase);
	}

	@Override
	public String getUseCase() {
		return this.useCase;
	}
	
	@Override
	public void intializeBean() throws Exception {
		gppId++;
		this.localQppId = gppId;
	    //setHeaderVisible(false);
		setBorders(false);
		//setBodyBorder(false);
			
		this.htmlLeft = new StringBuilder();
		this.htmlLeft.append("<html><body>");
			this.htmlLeft.append("<table style='margin-top: 0px; height: 40px;'><tr>");
				this.htmlLeft.append("<td class='gxtReset' style='vertical-align: top;'><div id='generalPropertyPanelImage" + this.localQppId + "' class='");
				this.htmlLeft.append(PropertyPanelResources.INSTANCE.css().entityIcon()).append("'></div></td>");
				this.htmlLeft.append("<td class='gxtReset'><table>");
					this.htmlLeft.append("<tr><td class='gxtReset'><div style='display: inline-block;' id='generalPropertyPanelTitleLeft" + this.localQppId + "' class='").append(MetaDataEditorResources.INSTANCE.constellationCss().entityTypeLabel()).append("'></div>")
					             .append("<div style='display: inline-block;' id='generalPropertyPanelTitleMiddle" + this.localQppId + "'></div>")
					             .append("<div style='display: inline-block;' id='generalPropertyPanelTitleRight" + this.localQppId + "' class='").append(MetaDataEditorResources.INSTANCE.constellationCss().entityTypeLabel()).append("'></div>")
					             .append("</td></tr>");
					//this.htmlLeft.append("<tr><td class='gxtReset'><div style='display: inline-block;' id='generalPropertyPanelTitleDeclared" + this.localQppId + "'></div>")
					this.htmlLeft.append("<tr><td class='gxtReset'><div style='display: inline-block;' id='generalPropertyPanelTitleDeclared" + this.localQppId + "' class='").append(MetaDataEditorResources.INSTANCE.constellationCss().entityTypeLabel()).append("'></div>")
					             .append("<div style='display: inline-block;' id='generalPropertyPanelTitleDeclaredMiddle" + this.localQppId + "'></div>")
								 .append("</td></tr>");
				this.htmlLeft.append("</table></td>");
			this.htmlLeft.append("</tr></table>");
		this.htmlLeft.append("</body></html>");
		
		this.htmlRight = new StringBuilder();
		this.htmlRight.append("<html><body style='height: auto;'>");
			this.htmlRight.append("<table style='margin-top: 0px; height: auto;'><tr>");
				this.htmlRight.append("<td class='gxtReset'><table>");
					this.htmlRight.append("<tr><td class='gxtReset'><div id='generalPropertyPanelEditingText" + this.localQppId + "' class='").append(MetaDataEditorResources.INSTANCE.constellationCss().entityTypeLabel()).append("'></div></td></tr>");
					this.htmlRight.append("<tr><td class='gxtReset'><div style='display: inline-block;' id='generalPropertyPanelEditingModel" + this.localQppId + "' class='").append("'></div></td></tr>");
				this.htmlRight.append("</table></td>");
			this.htmlRight.append("</tr></table>");
		this.htmlRight.append("</body></html>");
								
		this.panelLeft = new HTMLPanel(this.htmlLeft.toString());
		this.panelRight = new HTMLPanel(this.htmlRight.toString());
		this.panelRight.setStyleName("MetaDataEditorInfoRightPanel");
		this.panelRight.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().autoSize());
		//preparePanel();
		
		this.panelRightContent = new ContentPanel();
		this.panelRightContent.setHeaderVisible(false);
		this.panelRightContent.setBodyBorder(false);
		this.panelRightContent.setBodyStyleName(MetaDataEditorResources.INSTANCE.constellationCss().rightContentBodyPanel());
		this.panelRightContent.setStyleName("MetaDataEditorInfoRightContentPanel");
		this.panelRightContent.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().rightContentPanel());
		this.panelRightContent.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().autoSize());
		this.panelRightContent.add(this.panelRight);
		
		this.setHeight("auto");
		this.setEastWidget(panelRightContent, new BorderLayoutData(350));
		//this.setEastWidget(this.panelRight);
		this.setCenterWidget(this.panelLeft);
		//add(this.panelLeft);
		//add(this.panelRight);
		
		addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().entityInfo());
		//descriptionLabel.addStyleName(PropertyPanelResources.INSTANCE.css().entityDescriptionLabel());
	}

	private void preparePanel() {
		this.panelLeft.clear();
		this.panelRight.clear();
		
		this.iconImage = new Image();
		this.titleLabel = new Label();
		this.titleLabel2 = new Label();
		this.titleLabel3 = new Label();
		this.titleDeclaredLabel = new Label();
		this.titleDeclaredLabel2 = new Label();
		this.editedModelLabel = new Label();
		this.editingLabel = new Label();

		this.panelLeft.add(this.iconImage, "generalPropertyPanelImage" + this.localQppId);
		this.panelLeft.add(this.titleLabel, "generalPropertyPanelTitleLeft" + this.localQppId);
		this.panelLeft.add(this.titleLabel2, "generalPropertyPanelTitleMiddle" + this.localQppId);
		this.panelLeft.add(this.titleLabel3, "generalPropertyPanelTitleRight" + this.localQppId);
		this.panelLeft.add(this.titleDeclaredLabel, "generalPropertyPanelTitleDeclared" + this.localQppId);
		this.panelLeft.add(this.titleDeclaredLabel2, "generalPropertyPanelTitleDeclaredMiddle" + this.localQppId);
		
		this.titleLabel.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().entityTitleLabel());
		this.titleLabel2.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().entityTitleLabel());
		this.titleLabel3.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().entityTypeLabel());
		this.titleDeclaredLabel.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().entityTypeLabel());		
		this.titleDeclaredLabel2.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().entityTypeLabelBlack());		
		
		this.panelRight.add(this.editingLabel, "generalPropertyPanelEditingText" + this.localQppId);
		this.panelRight.add(this.editedModelLabel, "generalPropertyPanelEditingModel" + this.localQppId);
		
		this.editingLabel.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().entityTitleLabel());
		this.editedModelLabel.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().entityTypeLabelBlack());
	}	
		
	@Override
	public void setContent(ModelPath modelPath) {
		preparePanel();
		
		ModelPathElement pathElement = (modelPath != null) ?  modelPath.last() : null; 
		this.parentEntity = pathElement != null ? (GenericEntity) pathElement.getValue() : null;
				
		if ((this.parentEntity == null) || (modelPath == null) || (pathElement == null)) {
			this.iconImage.setResource(PropertyPanelResources.INSTANCE.clear());
			this.titleLabel.setText("");
			this.titleLabel2.setText("");
			this.titleLabel3.setText("");
			this.titleDeclaredLabel.setText("");
			this.titleDeclaredLabel2.setText("");
			this.editingLabel.setText("");
			this.editedModelLabel.setText("");
			//descriptionLabel.setText("");
			return;
		} 			
		
		GmMetaModel editModel = null;
		GmEntityType editEntity = null;
		for (ModelPathElement element : modelPath)
			if (element.getValue() instanceof GmMetaModel) {
				editModel = element.getValue();
			} else if (element.getValue() instanceof GmEntityType ) {
				editEntity = element.getValue();
			}
				
		
		EntityType<?> entityType = pathElement.getType();
		String typeSignature = entityType.getTypeSignature();
		if (pathElement.getValue() instanceof GmType) {
			typeSignature = ((GmType) pathElement.getValue()).getTypeSignature();
		} else if (pathElement.getValue() instanceof GmMetaModel) {
			typeSignature = ((GmMetaModel) pathElement.getValue()).getName();
		} else if (pathElement.getValue() instanceof GmProperty) {
			GmProperty property = pathElement.getValue();
			if (editEntity != null) {
				typeSignature = editEntity.getTypeSignature() + "/" + property.getName();				
			} else {
				typeSignature = property.getDeclaringType().getTypeSignature() + "/" + property.getName();
			}
		} else if (pathElement.getValue() instanceof GmEnumConstant) {
			GmEnumConstant enumConstant = pathElement.getValue();
			typeSignature = enumConstant.getDeclaringType().getTypeSignature() + "/" + enumConstant.getName();
		}
		
		ImageResource icon = PropertyPanelResources.INSTANCE.defaultIcon();		
		
		try {
			//iconProvider.configureGmSession(this.gmSession);
			//iconProvider.configureUseCase(this.useCase);
			IconAndType iconAndType = this.iconProvider.apply(modelPath);
			if (iconAndType != null)
				icon = iconAndType.getIcon();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
					
		this.iconImage.setResource(icon);
		
		if (icon == PropertyPanelResources.INSTANCE.defaultIcon())
			this.iconImage.getElement().getStyle().setOpacity(0.1);
		else
			this.iconImage.getElement().getStyle().clearOpacity();	
		this.iconImage.getElement().getStyle().setMarginTop(39 - icon.getHeight(), Unit.PX);
		this.iconImage.getElement().getStyle().setMarginLeft(32 - icon.getWidth(), Unit.PX);			
	
		String entityString2 = "";
		StringBuilder declaredString = new StringBuilder();
		String selectiveInformation = SelectiveInformationResolver.resolve(entityType, this.parentEntity,
				this.gmSession.getModelAccessory().getMetaData(), this.useCase/*, null*/);
		
		if (selectiveInformation != null)
			entityString2 = selectiveInformation;
		else 
			entityString2 = entityType.getShortName();
		String entityString = typeSignature.replace(entityString2, "");
		//entityString = typeSignature.replace("/" + entityString2, "/");
		
		GmMetaModel ownerModel = null;
		GmCustomTypeInfo ownerEntity = null;
		String declaringString = "";
		
		if (this.parentEntity instanceof GmEntityType) {
			//RVE - commented out - should not been NULL
			//if (((GmEntityType) this.parentEntity).getDeclaringModel() != null) {
				declaringString = LocalizedText.INSTANCE.declaredAt();
				ownerModel = ((GmEntityType) this.parentEntity).getDeclaringModel();
			//}
		} else 	if (this.parentEntity instanceof GmEnumType) {
			//if (((GmEnumType) this.parentEntity).getDeclaringModel() != null) {
				declaringString = LocalizedText.INSTANCE.declaredAt();
				ownerModel = ((GmEnumType) this.parentEntity).getDeclaringModel();
			//}
		} else 	if (this.parentEntity instanceof GmProperty) {
			if (editEntity != null) {				
				ownerModel = editEntity.getDeclaringModel();
			} else {
				ownerEntity = ((GmProperty) this.parentEntity).getDeclaringType();					
				ownerModel = ownerEntity.getDeclaringModel();
			}
			//if (ownerModel != null) {
				declaringString = LocalizedText.INSTANCE.declaredInModel();
			//}
		} else 	if (this.parentEntity instanceof GmEnumConstant) {
			ownerEntity = ((GmEnumConstant) this.parentEntity).getDeclaringType();					
			ownerModel = ownerEntity.getDeclaringModel();
			//if (ownerModel != null) {
				declaringString = LocalizedText.INSTANCE.declaredInModel();
			//}
		}			
		
		if (ownerModel != null) {
			declaredString.append(" ").append(declaringString).append(" : ");
			//String selectiveInformation = SelectiveInformationResolver.resolve(ownerModel.entityType(), ownerModel,
			//		this.gmSession.getModelAccessory().getMetaData(), this.useCase/*, null*/);
			//if (selectiveInformation != null && !selectiveInformation.trim().equals(""))
			//	declaredString.append(selectiveInformation);
			//else 
			this.titleDeclaredLabel.setText(declaredString.toString());
			this.titleDeclaredLabel2.setText(" " + ownerModel.getName());
		}
		
		this.titleLabel.setText(entityString);
		this.titleLabel2.setText(entityString2);
		if (editEntity != null && ownerEntity != null && !editEntity.equals(ownerEntity))
			this.titleLabel3.setText(LocalizedText.INSTANCE.inherited());
		else 
			this.titleLabel3.setText("");
		
		
		if ((this.parentEntity instanceof GmMetaModel)) {
			this.panelRight.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().infoNoBorder());
			this.editingLabel.setText("");
			this.editedModelLabel.setText("");		
			setEastWidget(null);
		} else {
			//editingModelString.append(LocalizedText.INSTANCE.editingModel()).append(": ");
			//String selectiveInformation = SelectiveInformationResolver.resolve(editModel.entityType(), editModel,
			//		this.gmSession.getModelAccessory().getMetaData(), this.useCase/*, null*/);
			//if (selectiveInformation != null && !selectiveInformation.trim().equals(""))
			//	editingModelString.append(selectiveInformation);
			//else 
			//	editingModelString.append(editMode.getName());
			
			this.panelRight.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().infoBorder());			
			this.editingLabel.setText(LocalizedText.INSTANCE.youAreCurrentlyEditing());
			if (editModel != null)
				this.editedModelLabel.setText(editModel.getName());
			else if (ownerModel != null)
				this.editedModelLabel.setText(ownerModel.getName());
		}
		
		
		/*
		String description = "";
		EntityTypeDisplayInfo entityTypeDisplayInfo = gmSession.getModelAccessory().getCascadingMetaDataResolver().getMetaData().entity(parentEntity).
			.meta(EntityTypeDisplayInfo.T).useCase(useCase).exclusive();
		if (entityTypeDisplayInfo != null && entityTypeDisplayInfo.getDescription() != null)
			description = I18nTools.getLocalized(entityTypeDisplayInfo.getDescription());
		descriptionLabel.setText(description);
		*/
	}

	
}
