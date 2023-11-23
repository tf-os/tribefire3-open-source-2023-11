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

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.gmview.util.client.GmPreviewUtil;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resourceapi.request.PreviewType;
import com.braintribe.model.uicommand.RefreshPreview;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

public class PreviewPanel extends FlowPanel implements GmEntityView, InitializableBean, DisposableBean, PreviewRefreshHandler {
	
	public static interface PreviewListener {
		public void onPreviewLoaded();
	}
	
	private GenericEntity parentEntity;
	private GmPreviewUtil previewUtil;
	
	private Image img;
	private Image activeImg;
	private static int maxHeight = 200;
	
	private List<PreviewListener> previewListeners;
	private String useCase;
	private String standardPreviewUrl;
	private String activePreviewUrl;
	private PreviewType currentPreviewType = null;
	private Timer mouseOverTimer;
	private boolean mouseEventsDisabled = false;
	
	public PreviewPanel() {
//		add(getImg());
//		add(getAudioPanel());
//		add(getVideoPanel());
		
		Style s = getElement().getStyle();
		s.setOverflow(Overflow.HIDDEN);
		s.setProperty("maxHeight", maxHeight + "px");
		s.setProperty("textAlign", "center");
		s.setProperty("overflow", "hidden");
		
		addDomHandler(event -> {
			if (mouseEventsDisabled)
				return;
			
			mouseEventsDisabled = true;
			getMouseOverTimer().schedule(100);
			fetchPreviewUrl(PreviewType.ACTIVE);
		}, MouseOverEvent.getType());
		
		addDomHandler(event -> {
			if (mouseEventsDisabled)
				return;
			
			mouseEventsDisabled = true;
			getMouseOverTimer().schedule(100);
			fetchPreviewUrl(PreviewType.STANDARD);
		}, MouseOutEvent.getType());
	}
	
	@Required
	public void setPreviewUtil(GmPreviewUtil previewUtil) {
		this.previewUtil = previewUtil;
	}
	
	public void setParentEntity(GenericEntity parentEntity) {
		this.parentEntity = parentEntity;
		standardPreviewUrl = null;
		activePreviewUrl = null;
		currentPreviewType = null;
		clear();
		
		/*
		if(parentEntity instanceof HasName) {
			HasName hasName = (HasName) parentEntity;			
			if(hasName.getName().equalsIgnoreCase("audio"))
				add(getAudioPanel());
			else if(hasName.getName().equalsIgnoreCase("video"))
				add(getVideoPanel());
			else
				add(getImg());
			
		}else {
			add(getImg());	
		}		
		
		ModelPath mp = new ModelPath();
		mp.add(new RootPathElement(parentEntity));
		getAudioPanel().setContent(mp);
		getVideoPanel().setContent(mp);
		*/
		
		handleOther(PreviewType.STANDARD);
	}
	
	private void handleOther(PreviewType type) {
		Image image = getImg();
		add(image);
		image.setUrl("");
		image.getElement().removeAttribute("style");
		image.getElement().getStyle().clearDisplay();
		
		Image activeImage = getActiveImg();
		add(activeImage);
		activeImage.setUrl("");
		activeImage.getElement().removeAttribute("style");
		activeImage.getElement().getStyle().setDisplay(Display.NONE);
		
		fetchPreviewUrl(type);
	}
	
	@Override
	public void onPreviewRefresh(RefreshPreview refreshPreview) {
		if (parentEntity == null)
			return;
		
		//RVE - actually remove check for TypeSignature, as Custom types returns Parent type
		//if (parentEntity.type().getTypeSignature().equals(refreshPreview.getTypeSignature()) && parentEntity.getId().equals(refreshPreview.getId())) 
		if (parentEntity.getId().equals(refreshPreview.getId())) 
			setParentEntity(parentEntity);
		
		//RVE - what does this code do when always this.getGmSession() = NULL ???
		/*
		try {
			this.getGmSession().queryCache().entity(refreshPreview.getTypeSignature(), refreshPreview.getEntityId()).find(new com.braintribe.processing.async.api.AsyncCallback<GenericEntity>() {
				
				@Override
				public void onSuccess(GenericEntity future) {
					if(parentEntity == future) {
						setParentEntity(parentEntity);
					}	
				}
				
				@Override
				public void onFailure(Throwable t) {
					t.printStackTrace();
				}
			});
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		*/
	}
	
	public void addPreviewListener(PreviewListener listener) {
		if (previewListeners == null)
			previewListeners = new ArrayList<>();
		
		previewListeners.add(listener);
	}
	
	private void fetchPreviewUrl(PreviewType type) {
		if (currentPreviewType == type)
			return;
		
		if (PreviewType.ACTIVE.equals(type)) {
			if (activePreviewUrl == null) {
				activePreviewUrl = previewUtil.previewUrl(parentEntity, getUseCase(), type);
				activeImg.setUrl(activePreviewUrl);
			}
			
			activeImg.getElement().getStyle().clearDisplay();
			img.getElement().getStyle().setDisplay(Display.NONE);
		} else {
			if (standardPreviewUrl == null) {
				standardPreviewUrl = previewUtil.previewUrl(parentEntity, getUseCase(), type);
				img.setUrl(standardPreviewUrl);
			}
			
			img.getElement().getStyle().clearDisplay();
			activeImg.getElement().getStyle().setDisplay(Display.NONE);
		}
		
		currentPreviewType = type;
		/*
		previewUtil.previewUrl(parentEntity, getUseCase(), type).get(new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}
			
			@Override
			public void onSuccess(String url) {
				getImg().setUrl(url);
			}
		});
		*/
	}
	
	public Image getImg() {
		if(img == null) {
			img = new Image();			
			String uid = "previewImage-" + DOM.createUniqueId();
			img.getElement().setId(uid);
			img.addAttachHandler(event -> {
				if (event.isAttached())
					onload(PreviewPanel.this, uid);
			});
		}
		return img;
	}
	
	public Image getActiveImg() {
		if (activeImg != null)
			return activeImg;
		
		activeImg = new Image();			
		String uid = "activePreviewImage-" + DOM.createUniqueId();
		activeImg.getElement().setId(uid);
		activeImg.getElement().getStyle().setDisplay(Display.NONE);
		activeImg.addAttachHandler(event -> {
			if (event.isAttached())
				onload(PreviewPanel.this, uid);
		});
		
		return activeImg;
	}

	public void handleLoad(Element img, int width, int height) {
		Style s = img.getStyle();
		s.setProperty("objectFit", "contain");
		if(equals1(width, height)) {
			s.setProperty("width", maxHeight + "px");
			s.setProperty("height", maxHeight + "px");
		}else {
			s.setProperty("width", "100%");		
		}		
		firePreviewLoaded();
	}
	
	private void firePreviewLoaded() {
		if (previewListeners == null)
			return;
		
		for (PreviewListener listener : previewListeners)
			listener.onPreviewLoaded();
	}
	
	private Timer getMouseOverTimer() {
		if (mouseOverTimer != null)
			return mouseOverTimer;
		
		mouseOverTimer = new Timer() {
			@Override
			public void run() {
				mouseEventsDisabled = false;
			}
		};
		
		return mouseOverTimer;
	}
	
	private static native boolean equals1(int a, int b) /*-{
		return (a/b) == 1;
	}-*/;
	
	private native void onload(PreviewPanel panel, String id) /*-{
		var img = $wnd.document.getElementById(id);
		img.onload = function() {
			panel.@com.braintribe.gwt.gmview.client.PreviewPanel::handleLoad(Lcom/google/gwt/dom/client/Element;II)(img, img.width, img.height);
		}
	}-*/;	

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		//NOP
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
		return null;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		previewUtil.setSession(gmSession);
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
		return this.useCase;
	}

	@Override
	public void intializeBean() throws Exception {
		//NOP
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (previewListeners != null) {
			previewListeners.clear();			
			previewListeners = null;
		}
	}

}
