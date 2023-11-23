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
package com.braintribe.gwt.gme.notification.client.expert;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.uicommand.GotoUrl;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.shared.FastMap;

/**
 * Command which contains an URL to be opened either in GME, or in a new browser window.
 * 
 */
public class GotoUrlCommandExpert implements CommandExpert<GotoUrl> {

	private static final String BLANK_TARGET = "_blank";
	private static final String INLINE = "inline";
	private static final String regex = "\\$[\\\\{]([^}]*)[\\\\}]";
	private RegExp regexp = RegExp.compile(regex); 

	private ExplorerConstellation explorerConstellation;
	private Supplier<MasterDetailConstellation> masterDetailConstellationProvider;
	private Map<String, String> envProps;
	private Map<String, Object> windows = new FastMap<>();

	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}

	@Required
	public void setMasterDetailConstellationProvider(Supplier<MasterDetailConstellation> masterDetailConstellationProvider) {
		this.masterDetailConstellationProvider = masterDetailConstellationProvider;
	}
	
	@Configurable
	public void setEnvironmentProperties(Map<String, String> envProps) {
		this.envProps = envProps;
	}

	@Override
	public void handleCommand(GotoUrl gotoUrl) {
		String target = gotoUrl.getTarget();
		String url = gotoUrl.getUrl();
		
		boolean matches = regexp.test(url);
		if (matches) {
			if (envProps != null && !envProps.isEmpty()) {
				for (Entry<String, String> entry : envProps.entrySet()) {
					url = url.replaceAll("\\$[\\\\{]"+entry.getKey()+"[\\\\}]", entry.getValue());
				}			
			}
		}		
		
		url = url == null ? "#" : url;
		
		String name = gotoUrl.getName();
		if (target == null || !target.equals(INLINE)) {
			if (target == null)
				Window.open(url, BLANK_TARGET, "");
			else
				openBrowserTab(url, target);
			
			return;
		}
		
		MasterDetailConstellation masterDetailConstellation = masterDetailConstellationProvider.get();
		masterDetailConstellation.configureGmSession(explorerConstellation.getGmSession());
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(GotoUrl.T, gotoUrl));
		masterDetailConstellation.setContent(modelPath);
		explorerConstellation.maybeCreateVerticalTabElement(null, name, url, () -> masterDetailConstellation, null, null, false);
		masterDetailConstellation.collapseOrExpandDetailView(true);
	}
	
	/*
	 * This method prepares a hidden anchor element, and simulates a click on it, for opening the new browser tab.
	 * It is added to the DOM, and removed afterwards.
	 */
	private void openBrowserTab(String url, String targetName) {
        AnchorElement anchorElement = Document.get().createAnchorElement();
        anchorElement.setHref(url);
        anchorElement.setTarget(targetName);
        anchorElement.setInnerText("Text"); //Some text is required to make it clickable
        anchorElement.getStyle().setProperty("display", "none");
        
        Document.get().getBody().appendChild(anchorElement);
        Anchor anchor = Anchor.wrap(anchorElement);
        anchor.addClickHandler(event -> {
        	anchorElement.removeFromParent();
        	RootPanel.detachNow(anchor);
		});
        
        simulateClick(anchorElement);
    }
	
	private native void simulateClick(AnchorElement element) /*-{
	    var event = new MouseEvent('click', {
	        'view': window,
	        'bubbles': true,
	        'cancelable': true
	    });
	    
	    element.dispatchEvent(event);
	}-*/;
}
