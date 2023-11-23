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
package com.braintribe.gwt.gme.constellation.client.action;

public class ShowPackagingInfoAsTextAction /*extends Action*/ {
	/*
	private Packaging packaging;
	private Window packagingWindow;
	private TextArea packagingInfo;
	private String packagingInfoString;
	private String versionString; 
	
	public ShowPackagingInfoAsTextAction() {
		setHidden(true);
		setHoverIcon(ConstellationResources.INSTANCE.infoBig());
		setTooltip(LocalizedText.INSTANCE.packagingInfoDescription());
		setName(LocalizedText.INSTANCE.showAsText());
	}
	
	public void configurePackaging(Packaging packaging) {
		this.packaging = packaging;
		setHidden(packaging == null);
	}
	
	public void configureVersionString(String versionString) {
		this.versionString = versionString;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		getPackagingWindow().show();
	}
	
	private Window getPackagingWindow() {
		if (packagingWindow == null) {
			packagingWindow = new Window();
			packagingWindow.setBorders(false);
			packagingWindow.setBodyBorder(false);
			packagingWindow.setHeading(LocalizedText.INSTANCE.packagingInfo(versionString));
			packagingWindow.setSize("640px", "480px");
			packagingWindow.setModal(true);
			
			packagingInfo = new TextArea();
			packagingInfo.setReadOnly(true);
			
			FormPanel formPanel = new FormPanel();
			formPanel.setBorders(false);
			formPanel.add(packagingInfo);
			packagingWindow.add(formPanel);
		}
		
		packagingInfo.setValue(getPackagingInfoString());
		return packagingWindow;
	}
	
	private String getPackagingInfoString() {
		if (packagingInfoString != null)
			return packagingInfoString;
		
		StringBuilder builder = new StringBuilder();
		
		if (!versionString.isEmpty())
			builder.append(versionString).append("\n\n");
		
		Artifact terminalArtifact = packaging.getTerminalArtifact();
		
		builder.append(LocalizedText.INSTANCE.packagingInfoFor(terminalArtifact.getGroupId() + ":" + terminalArtifact.getArtifactId() + "-" + terminalArtifact.getVersion()));
		
		if (packaging.getDependencies() != null) {
			builder.append("\n\n").append(LocalizedText.INSTANCE.dependencies()).append("\n");
			for (Artifact artifact : packaging.getDependencies()) {
				builder.append("\n");
				builder.append(artifact.getGroupId() + ":" + artifact.getArtifactId() + "-" + artifact.getVersion());
			}
		}
		
		packagingInfoString = builder.toString();
		return packagingInfoString;
	}*/

}
