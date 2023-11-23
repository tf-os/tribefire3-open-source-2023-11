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
package com.braintribe.gwt.thumbnailpanel.client;

import com.braintribe.gwt.thumbnailpanel.client.ThumbnailPanel.EntityPropertyBean;
import com.braintribe.gwt.thumbnailpanel.client.expert.ImageLoader;
import com.braintribe.gwt.thumbnailpanel.client.expert.ImageLoaderListener;
import com.braintribe.gwt.thumbnailpanel.client.resources.ThumbnailPanelClientBundle;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.resource.Resource;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Visibility;

public class ImageResourceModelData implements ImageLoaderListener {
	private static final double thumbnailFactor = 0.75;
	
	/**
	 * 
	 */
	//public static String LOADING_SRC = ThumbnailPanelClientBundle.INSTANCE.loading().getSafeUri().asString();
	//public static String FAILURE_SRC = ThumbnailPanelClientBundle.INSTANCE.failure().getSafeUri().asString();
	public static String WATERMARK_SRC = ThumbnailPanelClientBundle.INSTANCE.watermark().getSafeUri().asString();
	public static String MODEL_ICON_SRC = ThumbnailPanelClientBundle.INSTANCE.modelIcon().getSafeUri().asString();
	public static String GROUP_ICON_SRC = ThumbnailPanelClientBundle.INSTANCE.dataGrid().getSafeUri().asString();
	
	private static final String feedbackImageIdPrefix = "loadingImage";
	
	private Resource rasterImageResource;
	private GenericEntity entity;
	private ImageLoader imageLoader;
	private String uniqueId;
	private String className;
	private boolean visible;
	private boolean hovered;
	//private String src;
	private int imageHeight;
	private long height;
	private int imageWidth;
	private long width;
	private String info;
	private int thumbnailSize;
	private double priority;
	private Icon typeIcon;
	
	private String beforeSelectiveInfo;
	private String selectiveInfo;
	//private String fileName;
	//private String currentSrc;
	//private Long fileSize;
	private EntityPropertyBean entityPropertyBean;
	private Object ownerEntity;
	private boolean condensed;
	private String previewUrl;
	private String activePreviewUrl;
	private int previewWidth;
	private int previewHeight;
	private boolean coverImage;
	private String groupName;
	private boolean groupModel;
	private boolean breakModel;
	
	public void setEntity(GenericEntity entity) {
		this.entity = entity;
	}
	
	public GenericEntity getEntity() {
		return entity;
	}
	

	public void setImageLoader(ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
	}
	
	public ImageLoader getImageLoader() {
		return imageLoader;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		imageLoader.fireOnVisibilityChanged(this);
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setHovered(boolean hovered) {
		this.hovered = hovered;
	}
	
	public boolean isHovered() {
		return hovered;
	}
	
	public void setUniqueId(String uniqueId){
		this.uniqueId = uniqueId;
	}
	
	public ImageResourceModelData() {
		this.visible = true;
		//this.currentSrc = "";
	}
	
	/*public String getCurrentSrc() {
		return currentSrc;
	}*/
	
	/*public void setSrc(String src){
		this.src = src;
	}
	
	public String getSrc(){
		return src;
	}*/
	
	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}
	
	public String getPreviewUrl() {
		return this.previewUrl;
	}
	
	public void setActivePreviewUrl(String activePreviewUrl) {
		this.activePreviewUrl = activePreviewUrl;
	}
	
	public String getActivePreviewUrl() {
		return activePreviewUrl;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}
	
	public void setImageHeight(int height){
		this.imageHeight = height;
	}
	
	public int getImageHeight(){
		return imageHeight;
	}
	
	public void setImageWidth(int width){
		this.imageWidth = width;
	}
	
	public int getImageWidth(){
		return imageWidth;
	}
	
	public void setPreviewHeight(int height){
		this.previewHeight = height;
	}
	
	public int getPreviewHeight(){
		return previewHeight;
	}

	public void setPreviewWidth(int width){
		this.previewWidth = width;
	}
	
	public int getPreviewWidth(){
		return previewWidth;
	}
	
	public void setHeight(long  height){
		this.height =  height;
	}

	public long getHeight(){
		return height;
	}
	
	public void setWidth(long width){
		this.width = width;
	}
	
	public long getWidth(){
		return width;
	}

	public void setCoverImage(Boolean coverImage){
		this.coverImage = coverImage;
	}
	
	public Boolean getCoverImage(){
		return coverImage;
	}
	
	public void setInfo(String info){
		this.info = info;
	}
	
	public String getInfo(){
		return info;
	}

	public void setContainerSize(int thumbnailSize) {
		double factor = 1;
		
		boolean adaptHeight = getImageHeight() >= thumbnailSize;
		boolean adaptWidth = getImageWidth() >= thumbnailSize;
		
		double height = adaptHeight ? thumbnailSize*thumbnailFactor : getImageHeight();
		double width = adaptWidth ? thumbnailSize*thumbnailFactor : getImageWidth();
		
		if(getImageHeight() > getImageWidth()){
			factor = (double)getImageWidth()/getImageHeight();
			width = height * factor;
		}else if(getImageWidth() > getImageHeight()){
			factor = (double)getImageHeight()/getImageWidth();
			height = width * factor;
		}
		
		setWidth(Math.round(width));		
		setHeight(Math.round(height));
		
		this.thumbnailSize = thumbnailSize;
	}
	
	public int getContainerSize() {
		return thumbnailSize;
	}

	public void setBeforeSelectiveInfo(String beforeSelectiveInfo) {
		this.beforeSelectiveInfo = beforeSelectiveInfo;
	}
	
	public String getBeforeSelectiveInfo() {
	    return beforeSelectiveInfo;
	}
	
	/**
	public void setSelectiveInfo(String selectiveInformation, boolean detailed) {
		this.selectiveInfo = selectiveInformation;
		String infoString = "";
		if(detailed){
			infoString = "&lt;b&gt;Type: &lt;/b&gt;" + " image/?" + "&lt;br /&gt;" +
					"&lt;b&gt;Dimension: &lt;/b&gt;" + getImageHeight() + " x " + getImageWidth() + "&lt;br /&gt;" +
		            "&lt;b&gt;Title: &lt;/b&gt;" + selectiveInformation + "&lt;br /&gt;" + 
		            "&lt;b&gt;Size: &lt;/b&gt;" + "Unknown";
		}
		else{
			infoString = "&lt;b&gt;Title: &lt;/b&gt;" + selectiveInformation + "&lt;br /&gt;";
		}
		setInfo(infoString);
	}
	*/
	
	public void setSelectiveInfo(String selectiveInformation, boolean detailed) {
		this.selectiveInfo = selectiveInformation;
		String infoString = "";
		if(detailed){
			infoString = "<b>Type: </b>" + " image/?" + "<br />;" +
					"<b>Dimension: </b>" + getImageHeight() + " x " + getImageWidth() + "<br />" +
		            "<b>Title: </b>" + selectiveInformation + "<br />" + 
		            "<b>Size: </b>" + "Unknown";
		}
		else{
			infoString = "<b>Title: </b>" + selectiveInformation + "<br />";
		}
		setInfo(infoString);
	}
	
	public String getSelectiveInfo() {
	    return selectiveInfo;
	}

    /**
	public void setFileName(String name) {
		this.fileName = name;
		String infoString = "&lt;b&gt;Type: &lt;/b&gt;" + " image/?" + "&lt;br /&gt;" +
				"&lt;b&gt;Dimension: &lt;/b&gt;" + getImageHeight() + " x " + getImageWidth() + "&lt;br /&gt;" +
	            "&lt;b&gt;Title: &lt;/b&gt;" + name + "&lt;br /&gt;" + 
	            "&lt;b&gt;Size: &lt;/b&gt;" + "Unknown";		
		setInfo(infoString);
	}
	*/
	public void setFileName(String name) {
		//this.fileName = name;
		String infoString = "<b>Type: </b>" + " image/?" + "<br />" +
				"<b>Dimension: </b>" + getImageHeight() + " x " + getImageWidth() + "<br />" +
	            "<b>Title: </b>" + name + "<br />" + 
	            "<b>Size: </b>" + "Unknown";		
		setInfo(infoString);
	}

	@Override
	public void onLoadingStatusChanged(ImageLoader imageLoader) {
		ImageElement imageElement = ((ImageElement)Document.get().getElementById(feedbackImageIdPrefix + "-" + uniqueId).cast());
		if(imageElement != null){
			//System.err.println(feedbackImageIdPrefix + "-" + uniqueId + " loading status " + imageLoader.getImageLoadingStatus().name());
			Style style = imageElement.getStyle();
			switch(imageLoader.getImageLoadingStatus()){
			case FAILURE:
				//imageElement.setSrc(FAILURE_SRC);
				//this.currentSrc = FAILURE_SRC;
				break;
			case LOADED:
				//setSrc(imageLoader.getSrc());
				style.setVisibility(Visibility.HIDDEN);
				this.visible = false;
				break;
			case LOADING:
				//imageElement.setSrc(LOADING_SRC);
				//this.currentSrc = LOADING_SRC;
				break;
			case UNLOADED:
				style.setVisibility(Visibility.VISIBLE);
				this.visible = true;
				break;
			case WAITING:
				break;
			default:
				break;
			}
		}
	}
 
 	@Override
	public void onVisibiltyChanged(ImageResourceModelData imageResourceModelData) {
		//NOP
	}
 	
 	// TODO this whole method is equivalent to:  {return entity == object;} 
 	public boolean refersTo(Object object) {
		if (entity == object)
			return true;
		
		return false;
		
		/*if (object instanceof EnhancedCollection) {
			if (object instanceof EnhancedList) {
				if (entity == ((EnhancedList<?>) object).getDelegate()) {
					//modelObject = object; //TODO: why were these assignments needed?
					return true;
				}
			} else if (object instanceof EnhancedSet) {
				if (entity == ((EnhancedSet<?>) object).getDelegate()) {
					//modelObject = object;
					return true;
				}
			} else if (object instanceof EnhancedMap) {
				if (entity == ((EnhancedMap<?,?>) object).getDelegate()) {
					//modelObject = object;
					return true;
				}
			}
		}
		
		return false;*/
	}

	public void setRasterImageResource(Resource rasterImageResource) {
		this.rasterImageResource = rasterImageResource;
	}
	
	public Resource getRasterImageResource() {
		return rasterImageResource;
	}
	
	public String getUniqueId() {
		return uniqueId;
	}
	
	public EntityPropertyBean getEntityPropertyBean() {
		return entityPropertyBean;
	}
	
	public void setEntityPropertyBean(EntityPropertyBean entityPropertyBean) {
		this.entityPropertyBean = entityPropertyBean;
	}
	
	public void setCondensed(boolean condensed) {
		this.condensed = condensed;
	}
	
	public boolean isCondensed() {
		return condensed;
	}

	public double getPriority() {
		return priority;
	}

	public void setPriority(double priority) {
		this.priority = priority;
	}

	public Icon getTypeIcon() {
		return typeIcon;
	}

	public void setTypeIcon(Icon icon) {
		this.typeIcon = icon;
	}

	public Object getOwnerEntity() {
		return ownerEntity;
	}

	public void setOwnerEntity(Object ownerEntity) {
		this.ownerEntity = ownerEntity;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public boolean isGroupModel() {
		return groupModel;
	}

	public void setGroupModel(boolean groupModel) {
		this.groupModel = groupModel;
	}

	public boolean isBreakModel() {
		return breakModel;
	}

	public void setBreakModel(boolean breakModel) {
		this.breakModel = breakModel;
	}
	
}
