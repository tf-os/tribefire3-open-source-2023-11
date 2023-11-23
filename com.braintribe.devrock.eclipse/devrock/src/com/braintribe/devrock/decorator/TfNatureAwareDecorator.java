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
package com.braintribe.devrock.decorator;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.nature.CommonNatureIds;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.utils.FileTools;

public class TfNatureAwareDecorator implements ILightweightLabelDecorator {

	static enum GmNature {
		model,
		module,
		api,
		tomcat,
	}

	private final Map<GmNature, Pair<ImageDescriptor, Color>> map = newMap();

	private static final long TENTH_OF_SECOND_IN_NANO = 100_000_000;

	public TfNatureAwareDecorator() {
		map(GmNature.model, "model.ico", 224, 249, 255);
		map(GmNature.module, "module.ico", 255, 224, 249);
		map(GmNature.api, "api.ico", 243, 255, 233);
		map(GmNature.tomcat, "tomcat.gif", null);
	}

	private volatile boolean decorateIcon = false;
	private volatile boolean decorateBackground = false;
	private volatile long flagsUpdated = 0;

	private void map(GmNature nature, String imageName, int r, int g, int b) {
		map(nature, imageName, new Color(Display.getCurrent(), r, g, b));
	}

	private void map(GmNature nature, String imageName, Color color) {
		map.put(nature, Pair.of(ImageDescriptor.createFromFile(getClass(), imageName), color));
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {
		// not supported
	}

	@Override
	public void dispose() {
		for (Pair<ImageDescriptor, Color> pair : map.values()) {
			if (pair.second != null)
				pair.second.dispose();
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {
		// not supported
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (!(element instanceof IProject))
			return;

		if (decorationTurnedOff())
			return;

		try {
			IProject project = (IProject) element;
			if (!project.isOpen())
				return;

			GmNature nature = resolveNature(project);
			if (nature == null)
				return;

			Pair<ImageDescriptor, Color> pair = map.get(nature);
			if (pair == null)
				return;

			if (decorateIcon)
				setImage(decoration, pair.first);

			if (decorateBackground && pair.second != null)
				decoration.setBackgroundColor(pair.second);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean decorationTurnedOff() {
		refreshFlagsIfNeeded();

		return !decorateIcon && !decorateBackground;
	}

	private void refreshFlagsIfNeeded() {
		long now = System.nanoTime();
		if (now - flagsUpdated < TENTH_OF_SECOND_IN_NANO)
			return;

		decorateIcon = readFlag(StorageLockerSlots.SLOT_TF_NATURE_PROJECT_ICONS);
		decorateBackground = readFlag(StorageLockerSlots.SLOT_TF_NATURE_PROJECT_BACKGROUND);
		flagsUpdated = now;
	}

	private boolean readFlag(String slot) {
		return DevrockPlugin.instance().storageLocker().getValue(slot, false);
	}

	private GmNature resolveNature(IProject project) {
		if (hasModelNature(project))
			return GmNature.model;

		if (isModule(project))
			return GmNature.module;

		if (isApi(project))
			return GmNature.api;

		if (isTomcat(project))
			return GmNature.tomcat;

		return null;
	}

	private void setImage(IDecoration decoration, ImageDescriptor image) {
		decoration.addOverlay(image, IDecoration.REPLACE);

		// I'm not kidding, this is how it's done, I copied this from somewhere
		IDecorationContext context = decoration.getDecorationContext();
		if (context instanceof DecorationContext)
			((DecorationContext) context).putProperty(IDecoration.ENABLE_REPLACE, Boolean.TRUE);
	}

	private boolean hasModelNature(IProject project) {
		try {
			return Arrays.asList(project.getDescription().getNatureIds()).contains(CommonNatureIds.NATURE_MODEL);

		} catch (CoreException e) {
			return false;
		}
	}

	private boolean isModule(IProject project) {
		File file = getProjectFile(project, "asset.man");
		if (!file.exists())
			return false;

		String text = FileTools.read(file).asString();
		if (text.contains("com.braintribe.model.asset.natures.TribefireModule") || //
				text.contains("com.braintribe.model.asset.natures.PrimingModule"))
			return true;

		return false;
	}

	private boolean isApi(IProject project) {
		File file = getProjectFile(project, "build.xml");
		if (!file.exists())
			return false;

		String text = FileTools.read(file).asString();
		if (text.contains("gm-api-ant-script"))
			return true;

		return false;
	}

	private boolean isTomcat(IProject project) {
		File file = getProjectFile(project, ".tomcatplugin");
		return file.exists();
	}

	private File getProjectFile(IProject project, String fileName) {
		return new File(project.getFile(fileName).getLocationURI());
	}

}
