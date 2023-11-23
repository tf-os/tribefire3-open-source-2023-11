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
package com.braintribe.gwt.gme.simplecontentview.client;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.uicommand.GotoUrl;
import com.braintribe.model.workbench.HyperlinkAction;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;

public class HyperLinkContentViewPanel extends FlowPanel implements GmContentView {
	
	private ModelPathElement selectedPathElement;
	private List<GmSelectionListener> gmSelectionListeners;
	private String url;
	
	public HyperLinkContentViewPanel() {
//		TextButton openExternalTabButton = new TextButton();
//		openExternalTabButton.setIcon(GmViewActionResources.INSTANCE.openBig());
//		openExternalTabButton.setToolTip("Open External");
//		openExternalTabButton.addSelectHandler(event -> Window.open(url != null ? url : "#","_blank",""));
//		addButton(openExternalTabButton);
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//NOP
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return null;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		//NOP
	}
	
	@Override
	public String getUseCase() {
		return null;
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (sl != null) {
			if (gmSelectionListeners == null)
				gmSelectionListeners = new ArrayList<>();
			gmSelectionListeners.add(sl);
		}
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (gmSelectionListeners != null) {
			gmSelectionListeners.remove(sl);
			if (gmSelectionListeners.isEmpty())
				gmSelectionListeners = null;
		}
	}
	
	private void fireGmSelectionListeners() {
		if (gmSelectionListeners != null) {
			for (GmSelectionListener listener : gmSelectionListeners)
				listener.onSelectionChanged(this);
		}
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		ModelPath modelPath = new ModelPath();
		modelPath.add(selectedPathElement);
		return modelPath;
	}
	
	@Override
	public GmContentView getView() {
		return this;
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
	public ModelPath getContentPath() {
		return null;
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		if (modelPath == null)
			return;
		
		Object urlSource = modelPath.last().getValue();
		url = "";
		boolean useFrame = true;
		if (urlSource instanceof HyperlinkAction) {
			HyperlinkAction hyperlinkAction = (HyperlinkAction) urlSource;
			url = hyperlinkAction.getUrl();
			useFrame = true;
		} else if(urlSource instanceof GotoUrl) {
			GotoUrl gotoUrl = (GotoUrl) urlSource;
			url = gotoUrl.getUrl();
			Boolean useImage = gotoUrl.getUseImage();
			useFrame = useImage == null || !useImage;
		}
		
		if (useFrame) {
			FlowPanel main = new FlowPanel();
			main.getElement().setAttribute("style", "position: relative;height:100%;width:100%");
			
			Frame frame = setUrl(url);
			frame.getElement().setAttribute("style", "position: absolute; left:0;top:0");
			
			Button button = new Button();
			button.getElement().setAttribute("style", "position: absolute; right:24px;top:24px; width: 36px; height: 36px;" + 
					"border-radius: 50%;" + 
					"background-color: silver;" + 
					"display: flex;" + 
					"align-items: center;" + 
					"justify-content: center;border:none; cursor:pointer; text-align:center");
			
//			button.setText("Open in new tab");
			button.setHTML("<img src='" + GmViewActionResources.INSTANCE.openBig().getSafeUri().asString() + "' height='24px' width='24px' />");
			button.addClickHandler(event -> Window.open(url != null ? url : "#","_blank",""));
			main.add(frame);
			main.add(button);
			add(main);			
		} else {
			FlowPanel flowPanel = new FlowPanel();
			Image image = new Image();
			image.setUrl(url);
			flowPanel.add(image);
			add(flowPanel);
		}
		
		selectedPathElement = modelPath.first();
		fireGmSelectionListeners();
	}
	
	private Frame setUrl(String url) {
	    Frame f = new Frame(url);
	    f.getElement().setPropertyInt("frameBorder", 0);
	    f.getElement().setAttribute("width", "100%");
		f.getElement().setAttribute("height", "100%");	    
	    return f;
	}

}
