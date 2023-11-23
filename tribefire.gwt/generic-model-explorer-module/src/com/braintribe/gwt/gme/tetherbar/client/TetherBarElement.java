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
package com.braintribe.gwt.gme.tetherbar.client;

import java.util.function.Supplier;

import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;


/**
 * Model containing info on each element in the TetherBar.
 * @author michel.docouto
 *
 */
public class TetherBarElement {
	
	private String name;
	private String description;
	private ModelPath modelPath;
	private Supplier<? extends GmContentView> contentViewProvider;
	private GmContentView contentView;
	
	public TetherBarElement(ModelPath modelPath, String name, String description, Supplier<? extends GmContentView> contentViewProvider) {
		setName(name);
		setDescription(description);
		setModelPath(modelPath);
		setContentViewProvider(contentViewProvider);
	}
	
	public TetherBarElement(ModelPath modelPath, String name, String description, GmContentView contentView) {
		setName(name);
		setDescription(description);
		setModelPath(modelPath);
		this.contentView = contentView;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public ModelPathElement getModelPathElement() {
		return modelPath == null ? null : modelPath.last();
	}
	
	public ModelPath getModelPath() {
		return modelPath;
	}
	
	public void setModelPath(ModelPath modelPath) {
		this.modelPath = modelPath;
	}
	
	public GmContentView getContentView() throws RuntimeException {
		if (contentView == null) {
			contentView = contentViewProvider.get();
		}
		return contentView;
	}
	
	public GmContentView getContentViewIfProvided() {
		return contentView;
	}
	
	public void setContentViewProvider(Supplier<? extends GmContentView> contentViewProvider) {
		this.contentViewProvider = contentViewProvider;
	}

}
