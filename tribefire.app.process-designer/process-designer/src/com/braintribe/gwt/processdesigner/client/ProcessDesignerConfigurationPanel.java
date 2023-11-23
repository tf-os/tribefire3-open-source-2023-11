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
package com.braintribe.gwt.processdesigner.client;

import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.processdesigner.client.resources.LocalizedText;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class ProcessDesignerConfigurationPanel extends ContentPanel {
	
	private PersistenceGmSession session;
	//private ProcessDesignerConfiguration processDesignerConfiguration;
	private ProcessDesignerRenderer processDesignerRenderer;
	private PropertyPanel propertyPanel;
	private TextButton renderButton;
	
	public ProcessDesignerConfigurationPanel() {
		setHeaderVisible(false);
		setBorders(false);
		setBodyBorder(false);
		setBodyStyle("background: white");
		addButton(getRenderButton());
	}
	
	public void setProcessDesignerRenderer(ProcessDesignerRenderer processDesignerRenderer) {
		this.processDesignerRenderer = processDesignerRenderer;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
		propertyPanel.configureGmSession(session);
	}
	
	public void setProcessDesignerConfiguration(ProcessDesignerConfiguration processDesignerConfiguration) {
		//this.processDesignerConfiguration = processDesignerConfiguration;
		
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(ProcessDesignerConfiguration.T, processDesignerConfiguration));
		
		propertyPanel.setContent(modelPath);
		session.listeners().entity(processDesignerConfiguration).add(propertyPanel);
	}
	
	public void setPropertyPanel(PropertyPanel propertyPanel) {
		this.propertyPanel = propertyPanel;
		add(this.propertyPanel);
	}
	
	public TextButton getRenderButton() {
		if(renderButton ==  null){
			renderButton = new TextButton(LocalizedText.INSTANCE.render());
			renderButton.addSelectHandler(event -> processDesignerRenderer.render());
		}
		return renderButton;
	}

}
