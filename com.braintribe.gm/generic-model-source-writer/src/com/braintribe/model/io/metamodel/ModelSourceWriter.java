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
package com.braintribe.model.io.metamodel;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.braintribe.codec.dom.genericmodel.GenericModelRootDomCodec;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;

/**
 * 
 */
public class ModelSourceWriter {

	private final String filePath;
	private final String outputFolder;
	private final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private ModelSourceWriter(String[] args) {
		this.filePath = args[0];
		this.outputFolder = args[1];
	}

	private void generate() {
		System.out.println("Loading meta-model from: " + filePath);
		GmMetaModel metaModel = loadModel();
		
		GmSourceWriter writerService = new GmSourceWriter();

		writerService.setOutputDirectory(new File(outputFolder));
		writerService.setGmMetaModel(metaModel);
		writerService.enableWritingSourcesForExistingClasses();

		System.out.println("Rendering model sources into '" + outputFolder + "'");
		writerService.writeMetaModelToDirectory();
		System.out.println("Rendering finished successfully!");
	}

	private GmMetaModel loadModel() {
		GmMetaModel rootObject = null;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(filePath);
			rootObject = getCodec().decode(document);

		} catch (Exception e) {
			throw new RuntimeException("error while loading the model", e);
		}

		return rootObject;
	}

	private GenericModelRootDomCodec<GmMetaModel> getCodec() {
		GenericModelRootDomCodec<GmMetaModel> codec = new GenericModelRootDomCodec<GmMetaModel>();
		codec.setType(getRootType());
		return codec;
	}

	private GenericModelType getRootType() {
		return typeReflection.getType("com.braintribe.model.meta.GmMetaModel");
	}

	/**
	 * As args expects:
	 * args[0] - path to model which should be written ((full) name of metamodel.xml file)
	 * args[1] - output folder (full) path  
	 */
	public static void main(String[] args) {
		new ModelSourceWriter(args).generate();
	}

}
