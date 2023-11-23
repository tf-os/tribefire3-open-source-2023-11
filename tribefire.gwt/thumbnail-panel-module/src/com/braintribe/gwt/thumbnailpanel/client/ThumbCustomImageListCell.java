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

import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.model.resource.Resource;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.sencha.gxt.core.client.XTemplates;

/**
 * Cell implementation for the {@link ThumbnailListView}.
 *
 */
public class ThumbCustomImageListCell extends AbstractCell<ImageResourceModelData> {
	
	public interface ThumbnailXTemplate extends XTemplates {
		//title=\"{imageInfo}\"
		@XTemplate(""
				+ "<div uniqueid='{uniqueId}' onmouseover=\"changePreview(this,'activeSrc',true)\" onmouseout=\"changePreview(this,'inactiveSrc',false)\" id=\"thumbnailPanelSelectionContainer-{uniqueId}\" class=\"thumbnailPanelSelectionContainer {className}\" style=\"{thumbnailPanelSelectionContainerStyle}\">"
			    +   "<tpl if=\"textIconSrc != null\">"
				+   	"<div id=\"thumbnailPanelImageContainer-{uniqueId}\" class=\"{baseClassName}ImageContainer {className}\">"
				+			"<img onload=\"handleOnload(this)\" id=\"thumbnailPreview-{uniqueId}\" class=\"{baseClassName}Image {className}\" activeSrc=\"{activeSrc}\" inactiveSrc=\"{inactiveSrc}\" src=\"{src}\" style=\"{thumbnailImageStyle}\"/>	"	
				//+           "<video autoplay loop muted class=\"thumbnailPanelPreviewVideo\" style=\"display: none\" poster=\"{inactiveSrc}\" id=\"thumbnailPreviewVideo-{uniqueId}\"></video>"
				+		"</div>"				
				+		"<div id=\"thumbnailPanelInfoWrapper-{uniqueId}\" class=\"{baseClassName}InfoWrapper {className}\">"
			    + 		  	"<tpl if=\"beforeSelectiveInfo != null\">"
				+				"<div id=\"thumbnailPanelIconInfoWrapper-{uniqueId}\" class=\"{baseClassName}IconInfoWrapper {className}\">"			    
				+					"<img id=\"thumbnailImage-{uniqueId}\" class=\"{baseClassName}Icon {className}\" src=\"{textIconSrc}\" style=\"{iconSize}\" />	"	
				+					"<span class=\"{baseClassName}BeforeInfoWithIcon {className}\" style=\"{maxTextWidth}\">{beforeSelectiveInfo}</span>"
				+				"</div>"				
				+				"<span class=\"{baseClassName}MainInfoWithIcon {className}\">{selectiveInfo}</span>"
				+ 	 		"</tpl>"													
			    +   		"<tpl if=\"beforeSelectiveInfo == null\">"
				+				"<div id=\"thumbnailPanelIconInfoWrapper-{uniqueId}\" class=\"{baseClassName}IconInfoWrapper {className}\">"			    
				+					"<img id=\"thumbnailImage-{uniqueId}\" class=\"{baseClassName}Icon {className}\" src=\"{textIconSrc}\" style=\"{iconSize}\" />	"	
				+					"<span class=\"{baseClassName}InfoWithIcon {className}\" style=\"{maxTextWidth}\">{selectiveInfo}</span>"
				+				"</div>"				
				+ 	 		"</tpl>"																	
				+		"</div>"
				+   "</tpl>"					
			    +   "<tpl if=\"textIconSrc == null\">"
				+   	"<div id=\"thumbnailPanelImageContainer-{uniqueId}\" class=\"{baseClassName}ImageContainer {className}\">"
				+			"<img onload=\"handleOnload(this)\" id=\"thumbnailImage-{uniqueId}\" class=\"{baseClassName}Image {className}\" activeSrc=\"{activeSrc}\" inactiveSrc=\"{inactiveSrc}\" src=\"{src}\"  style=\"{thumbnailImageStyle}\"/>	"	
				+		"</div>"				
				+		"<div id=\"thumbnailPanelInfoWrapper-{uniqueId}\" class=\"{baseClassName}InfoWrapper {className}\">"				
			    +   		"<tpl if=\"beforeSelectiveInfo != null\">"
				+				"<span class=\"{baseClassName}BeforeInfo {className}\" style=\"{maxTextWidth}\">{beforeSelectiveInfo}</span>"
				+				"<span class=\"{baseClassName}MainInfo {className}\" style=\"{maxTextWidth}\">{selectiveInfo}</span>"
				+  			"</tpl>"													
			    +   		"<tpl if=\"beforeSelectiveInfo == null\">"
				+				"<span class=\"{baseClassName}Info {className}\" style=\"{maxTextWidth}\">{selectiveInfo}</span>"
				+  			"</tpl>"													
				+		"</div>"				
				+   "</tpl>"									
				+ "</div>"
				)	
			  												  
		SafeHtml createImage(String uniqueId, SafeStyles thumbnailPanelSelectionContainerStyle, String imageInfo, SafeUri src, String activeSrc, String inactiveSrc,
				SafeStyles thumbnailImageStyle, String beforeSelectiveInfo, String selectiveInfo, String baseClassName, String className,
				SafeUri textIconSrc, SafeStyles iconSize, SafeStyles maxTextWidth);
	}
	
	private final ThumbnailXTemplate imageTemplate = GWT.create(ThumbnailXTemplate.class);
	
	private ThumbnailPanel thumbnailPanel;
	
	public ThumbCustomImageListCell(ThumbnailPanel thumbnailPanel) {
		this.thumbnailPanel = thumbnailPanel;
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, ImageResourceModelData model, SafeHtmlBuilder sb) {
		if (model == null || !model.isVisible())
			return;

		String uniqueId = model.getUniqueId();
		int thumbnailSize = model.getContainerSize();
		String imageInfo = model.getInfo();
		// String currentSrc = model.getCurrentSrc();
		String beforeSelectiveInfo = model.getBeforeSelectiveInfo();
		String selectiveInfo = model.getSelectiveInfo();
		String className = "";
		String baseClassName = model.isBreakModel() ? "thumbnailPanelSeparator" : "thumbnailPanel";
		int iconSize = 0;

		long thumbWidth = thumbnailSize;
		long thumbHeight = thumbnailSize;

		String src = null, activeSrc = null;
		String textIconSrc = null;
		if (model.getTypeIcon() != null) {
			Resource resource = GMEIconUtil.getLargestImageFromIcon(model.getTypeIcon().getIcon());
			if (resource == null)
				resource = GMEIconUtil.getLargeImageFromIcon(model.getTypeIcon().getIcon());
			if (resource == null)
				resource = GMEIconUtil.getMediumImageFromIcon(model.getTypeIcon().getIcon());
			if (resource == null)
				resource = GMEIconUtil.getSmallImageFromIcon(model.getTypeIcon().getIcon());
			if (resource == null)
				resource = GMEIconUtil.getSmallestImageFromIcon(model.getTypeIcon().getIcon());
			if (resource != null) {
				textIconSrc = thumbnailPanel.gmSession.getModelAccessory().getModelSession().resources().url(resource).asString();
				iconSize = 16;
			}
		}
		
		// RVE test
		// model.setPreviewUrl("http://placehold.it/120x120&text=image1");
		
		long width;
		long height;

		if (model.getPreviewUrl() == null || model.getPreviewUrl().isEmpty()) {
			if (model.getImageLoader() != null) {
				src = model.getImageLoader().getSrc();
				if (src == null)
					src = model.getImageLoader().getDefaultSrc(); // if no image, use default (wartermark or modelIcon)
			} else
				src = ImageResourceModelData.GROUP_ICON_SRC;
			
			width = model.getWidth();
			height = model.getHeight();
			className = model.getClassName();
		} else {
			src = model.getPreviewUrl();
			activeSrc = model.getActivePreviewUrl();
			
			textIconSrc = model.getImageLoader().getSrc(); // Preview Not use default image!!!
			iconSize = 24;
			if (model.getPreviewWidth() == 0)
				width = thumbnailSize;
			else
				width = model.getPreviewWidth();
			if (model.getPreviewHeight() == 0)
				height = thumbnailSize;
			else
				height = model.getPreviewHeight();
			thumbWidth = width;
			thumbHeight = height;
		}

		if (model.getCoverImage())
			className += " thumbnailPanelImageCover";
		else if (width == height)
			className += " thumbnailPanelImageFitContain";

		// check max width and height
		int maxThumbWidth = thumbnailPanel.getMaxThumbnailWidth();
		int maxThumbHeight = thumbnailPanel.getMaxThumbnailHeight();

		width = (int) Math.min(width, maxThumbWidth);
		height = (int) Math.min(height, maxThumbHeight);

		thumbWidth = (int) Math.min(thumbWidth, maxThumbWidth);
		thumbHeight = (int) Math.min(thumbHeight, maxThumbHeight);

		// Double marginLeft = (double) (width/2);
		// Double marginTop = (double) (height/2);

		/* String visible; if (model.isVisible()) { visible = "visible"; } else { visible = "hidden"; } */

		SafeStylesBuilder thumbnailPanelSelectionContainerStyle = new SafeStylesBuilder();
		thumbnailPanelSelectionContainerStyle.width(thumbWidth, Unit.PX);
		thumbnailPanelSelectionContainerStyle.height(thumbHeight, Unit.PX);

		SafeStylesBuilder thumbnailImageStyle = new SafeStylesBuilder();
		thumbnailImageStyle.trustedNameAndValue("max-height", height, Unit.PX);
		// thumbnailImageStyle.marginLeft(-marginLeft, Unit.PX);
		// thumbnailImageStyle.marginTop(-marginTop, Unit.PX);

		SafeStylesBuilder iconSizeStyle = new SafeStylesBuilder();
		iconSizeStyle.width(iconSize, Unit.PX);
		iconSizeStyle.height(iconSize, Unit.PX);

		SafeStylesBuilder maxTextWidthStyle = new SafeStylesBuilder();
		maxTextWidthStyle.trustedNameAndValue("max-width", thumbWidth - iconSize - 5, Unit.PX);

		SafeUri safeUriSrc = null;
		SafeUri activeUriSrc = null;
		SafeUri inactiveUriSrc = null;
		
		SafeUri safeUriTextIconSrc = null;

		if (src != null)
			safeUriSrc = src.startsWith("data:image/") ? UriUtils.fromSafeConstant(src) : UriUtils.fromString(src);
		inactiveUriSrc = safeUriSrc;	
			
		if (activeSrc != null)
			activeUriSrc = src.startsWith("data:image/") ? UriUtils.fromSafeConstant(activeSrc) : UriUtils.fromString(activeSrc);
		else
			activeUriSrc = safeUriSrc;
			
		if (textIconSrc != null)
			safeUriTextIconSrc = UriUtils.fromString(textIconSrc);

		sb.append(imageTemplate.createImage(uniqueId, thumbnailPanelSelectionContainerStyle.toSafeStyles(), imageInfo, safeUriSrc,
				activeUriSrc != null ? activeUriSrc.asString() : "", inactiveUriSrc != null ? inactiveUriSrc.asString() : "", 
				thumbnailImageStyle.toSafeStyles(), beforeSelectiveInfo, selectiveInfo, baseClassName, className, safeUriTextIconSrc,
				iconSizeStyle.toSafeStyles(), maxTextWidthStyle.toSafeStyles()));

		if (thumbnailPanel.gmeDragAndDropSupport != null && !model.isGroupModel() && !model.isBreakModel()) {
			Scheduler.get().scheduleDeferred(() -> {
				Element element = Document.get().getElementById("thumbnailPanelSelectionContainer-" + uniqueId);
				if (element != null)
					thumbnailPanel.prepareDropTarget(element, thumbnailPanel.imagesListStore.indexOf(model));
			});
		}
	}
	
}