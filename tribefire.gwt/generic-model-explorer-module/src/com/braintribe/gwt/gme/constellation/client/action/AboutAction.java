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

import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.action.AboutResources.AboutCssResource;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gxt.gxtresources.whitewindow.client.WhiteWindowAppearance;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.packaging.Packaging;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class AboutAction extends Action {
	
	static {
		AboutResources.INSTANCE.aboutStyles().ensureInjected();
	}
	
	private PersistenceGmSession session;
	private Supplier<Future<Packaging>> packagingProvider;
	private Supplier<String> userNameProvider;
	private Loader<Folder> folderLoader;
	private Folder folder;
	private ShowPackagingInfoAction showPackagingInfoAction;
	private boolean usePackagingInfoAction = false;
	
	private static String ICON_NAME = "$icon";
	
	public AboutAction() {
		setName(LocalizedText.INSTANCE.about());
		setIcon(ConstellationResources.INSTANCE.info());
		setHoverIcon(ConstellationResources.INSTANCE.infoBig());
		setTooltip(LocalizedText.INSTANCE.about());
	}	
	
	@Configurable
	public void setShowPackagingInfoAction(ShowPackagingInfoAction showPackagingInfoAction) {
		this.showPackagingInfoAction = showPackagingInfoAction;
	}
	
	@Required
	public void setPackagingProvider(Supplier<Future<Packaging>> packagingProvider) {
		this.packagingProvider = packagingProvider;
	}
	
	/**
	 * Defaults to false.
	 */
	@Configurable
	public void setUsePackagingInfoAction(boolean usePackagingInfoAction) {
		this.usePackagingInfoAction = usePackagingInfoAction;
	}
	
	@Required
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}
	
	public void setFolderLoader(Loader<Folder> folderLoader) {
		this.folderLoader = folderLoader;
	}
	
	@Required
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (folderLoader == null ) {
			folder = null;
			show();
			return;
		}
		
		folderLoader.load(AsyncCallbacks.of(result -> {
			folder = result;
			show();
		}, e -> {
			folder = null;
			show();
		}));
	}

	private void show() {
		packagingProvider.get().//
				andThen(result -> showAboutWindow(result)).//
				onError(e -> showAboutWindow(null));
	}
	
	private void showAboutWindow(Packaging result) {
		Window w = new Window(new WhiteWindowAppearance());
		w.setModal(true);
		w.setClosable(false);
//		w.setHeaderVisible(false);
		
		AboutContext context = new AboutContext();				
		
		boolean useIcon = false;
		if (folder != null) {
			for (Folder sub : folder.getSubFolders()) {
				if (sub.getName().equals(ICON_NAME)) {
					Resource r = GMEIconUtil.getLargestImageFromIcon(sub.getIcon());
					context.setFaviconUrl(UriUtils.fromString(session.resources().url(r).asString()));
					useIcon = true;
				}
			}
		}
		
		if (!useIcon)
			context.setFaviconUrl(UriUtils.fromString(faviconUrl()));				
		
		context.setUrl(getUrl());
		if (result != null) {
			context.setArtifactId(result.getTerminalArtifact().getArtifactId());
			context.setDate(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(result.getTimestamp()));
			context.setArtifactVersion(result.getTerminalArtifact().getVersion());
			context.setVersion(result.getVersion());
		} else {
			context.setArtifactId(LocalizedText.INSTANCE.packagingInfoNotAvailable(""));
			context.setDate("");
			context.setArtifactVersion("");
			context.setVersion("");					
		}
		context.setUserName(userNameProvider.get());
		
		HTML html = new HTML(template.about(context, AboutResources.INSTANCE.aboutStyles()));
		w.add(html);
		
		TextButton btnClose = new TextButton(LocalizedText.INSTANCE.close());
		btnClose.setIconAlign(IconAlign.TOP);
		btnClose.setScale(ButtonScale.LARGE);
		btnClose.setIcon(ConstellationResources.INSTANCE.finish());
		btnClose.addSelectHandler(event -> w.hide());
		btnClose.addStyleName("gmeGimaMainButton");
		
		w.setHeaderVisible(false);
		w.setSize("750px", "300px");

		if (usePackagingInfoAction && result != null) {
			TextButton btnPackaging = new TextButton(LocalizedText.INSTANCE.packaging());
			btnPackaging.setIconAlign(IconAlign.TOP);
			btnPackaging.setScale(ButtonScale.LARGE);
			btnPackaging.setIcon(ConstellationResources.INSTANCE.more32());
			btnPackaging.addSelectHandler(event -> {
				w.hide();
				if (showPackagingInfoAction != null)
					showPackagingInfoAction.perform(null);
			});
			w.addButton(btnPackaging);
		}
		
		w.addButton(btnClose);
		w.show();
		w.center();
	}
	
	private AboutTemplate template = GWT.create(AboutTemplate.class);
	
	interface AboutTemplate extends XTemplates {
		@XTemplate(source="about.html")
		SafeHtml about(AboutContext context, AboutCssResource style);
	}
	
	class AboutContext {
		private SafeUri faviconUrl;
		private String url;
		private String date;
		private String version;
		private String artifactId;
		private String artifactVersion;
		private String userName;
		
		public SafeUri getFaviconUrl() {
			return faviconUrl;
		}
		public void setFaviconUrl(SafeUri faviconUrl) {
			this.faviconUrl = faviconUrl;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public void setArtifactVersion(String artifactVersion) {
			this.artifactVersion = artifactVersion;
		}
		public String getArtifactVersion() {
			return artifactVersion;
		}

		public void setArtifactId(String artifactId) {
			this.artifactId = artifactId;
		}
		public String getArtifactId() {
			return artifactId;
		}
		
		public void setVersion(String version) {
			this.version = version;
		}
		public String getVersion() {
			return version;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}		
		public String getUserName() {
			return userName;
		}
	}
	
	private native String faviconUrl() /*-{
		return $doc.getElementById("faviIcon").href;
	}-*/;
	
	private native String getUrl() /*-{
		return $wnd.location.href;
	}-*/;
}
