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
package com.braintribe.gwt.gme.constellation.client.resources;

import org.vectomatic.dom.svg.ui.SVGResource;
import org.vectomatic.dom.svg.ui.SVGResource.Validated;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ConstellationResources extends ClientBundle {
	
	public static final ConstellationResources INSTANCE = ((ConstellationResourcesFactory)GWT.create(ConstellationResourcesFactory.class)).getConstellationResources();
	
	@Source ("constellation.gss")
	public ConstellationCss css();
	
	@Source ("New_32x32.png")
	public ImageResource add();
	@Source ("New_16x16.png")
	public ImageResource addSmall();
	@Source ("addAndFinish_32x32.png")
	public ImageResource addFinish();
	public ImageResource arrowDownSmall();
	@Source ("Back_32x32.png")
	public ImageResource back();
	@Source ("Delete_32x32.png")
	public ImageResource cancel();
	@Source ("Delete_16x16.png")
	public ImageResource cancelSmall();
	@Source ("copy.orange.big.png")
	public ImageResource changes();
	@Source ("ChangesGray_16x16.png")
	public ImageResource changesTransient();
	@Source ("Remove_16x16.png")
	public ImageResource clear();
	@Source ("Remove_32x32.png")
	public ImageResource clearBig();
	@Source ("clipboard.orange.big.png")
	public ImageResource clipboard();
	@Source("Condensation_16x16.png")
	public ImageResource condensedGlobal();
	public ImageResource emptySmall();
	ImageResource dots();
	public ImageResource entity();
	public ImageResource export();
	public ImageResource exportBig();
	@Source ("Apply_32x32.png")
	public ImageResource finish();
	@Source ("Apply_16x16.png")
	public ImageResource finishSmall();
	@Source ("Front_32x32.png")
	public ImageResource front();
	@Source ("Hide Details_32x32.png")
	public ImageResource hideProperties();
	@Source ("Hide Details_16x16.png")
	public ImageResource hidePropertiesSmall();
	@Source ("home.orange.big.png")
	public ImageResource home();
	@Source ("Info_16x16.png")
	public ImageResource info();
	@Source ("Info_32x32.png")
	public ImageResource infoBig();
	@Source ("Maximize_16x16.png")
	public ImageResource maximize();
	@Source ("Maximize_32x32.png")
	public ImageResource maximizeBig();
	public ImageResource moveDown();
	public ImageResource moveUp();	
	@Source ("more-32.png")
	public ImageResource more32();
	@Source ("more-64.png")
	public ImageResource more64();	
	@Source ("letter.orange.big.png")
	public ImageResource notifications();
	@Source ("quickAccessOrange.png")
	public ImageResource quickAccess();
	@Source ("RedoOrange_32x32.png")
	public ImageResource redo();
	@Source ("RedoOrange_16x16.png")
	public ImageResource redoSmall();
	@Source ("Redo_32x32.png")
	public ImageResource redoBlack();
	@Source ("Redo_16x16.png")
	public ImageResource redoBlackSmall();
	@Source ("Remove_16x16.png")
	public ImageResource remove();
	@Source ("Remove_32x32.png")
	public ImageResource removeBig();
	@Source ("Restore_16x16.png")
	public ImageResource restore();
	@Source ("Restore_32x32.png")
	public ImageResource restoreBig();
	public ImageResource runTask();
	@Source ("SaveOrange_32x32.png")
	public ImageResource save();
	@Source ("SaveOrange_16x16.png")
	public ImageResource saveSmall();		
	public ImageResource separator();
	@Source ("Show Details_32x32.png")
	public ImageResource showProperties();
	@Source ("Show Details_16x16.png")
	public ImageResource showPropertiesSmall();
	@Source ("SwitchTo_16x16.png")
	public ImageResource switchToSmall();
	@Source ("SwitchTo_32x32.png")
	public ImageResource switchToBig();
	public ImageResource tasks();
	@Source ("Thumbnails_16x16.png")
	public ImageResource thumbs();
	@Source ("Thumbnails_32x32.png")
	public ImageResource thumbsBig();
	@Source ("ListView_16x16.png")
	public ImageResource uncondensed();
	@Source ("ListView_32x32.png")
	public ImageResource uncondensedBig();
	@Source ("UndoOrange_32x32.png")
	public ImageResource undo();
	@Source ("UndoOrange_16x16.png")
	public ImageResource undoSmall();
	@Source ("Undo_32x32.png")
	public ImageResource undoBlack();
	@Source ("Undo_16x16.png")
	public ImageResource undoBlackSmall();
	@Source ("Upload_16x16.png")
	ImageResource upload();
	@Source ("workbenchOrange.png")
	public ImageResource workbench();
	@Source ("validation-tick-small.png")
	public ImageResource tick();
	@Source ("View_16x16.png")
	public ImageResource view();
	@Source ("View_32x32.png")
	public ImageResource viewBig();
	@Source ("view-64.png")
	public ImageResource view64();
	@Source ("maximize-64.png")
	public ImageResource maximize64();
	public ImageResource maximizeSmall();
	@Source ("minimize-64.png")
	public ImageResource restore64();
	public ImageResource restoreSmall();
	@Source ("info-64.png")
	public ImageResource info64();
	@Source ("info-white.png")
	public ImageResource infoWhite();
	public ImageResource open();
	@Source ("grid-64.png")
	public ImageResource grid64();
	@Source ("list-64.png")
	public ImageResource list64();
	@Source ("metaeditor-64.png")
	public ImageResource metadataEditor64();
	@Source("modeler.svg") @Validated(validated = false)
	public SVGResource modellerSVG();	
	@Source ("process-64.png")
	public ImageResource processDesigner64();
	@Source ("webreader-64.png")
	public ImageResource webreader64();
	@Source ("Menu_16x16.png")
	public ImageResource menu();
	@Source ("menu-64.png")
	public ImageResource menu64();
	@Source ("Search_16x16.png")
	public ImageResource search();
	@Source ("settings-64.png")
	public ImageResource settings64();	
	@Source ("settings-32.png")
	public ImageResource settings32();	
	public ImageResource modelViewer();
	public ImageResource modelViewerBig();
	@Source("validation.svg") @Validated(validated = false)
	public SVGResource validationSVG();	

	
}
