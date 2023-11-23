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
package com.braintribe.model.processing.deployment.hibernate;

import java.io.File;

import org.w3c.dom.Document;

import com.braintribe.codec.dom.genericmodel.GenericModelRootDomCodec;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGeneratingService;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * The main method generates hbm mapping files for a meta-model given by an xml. One also has to configure an output
 * folder and a prefix to append at the beginning of each table name.
 * <p>
 * Example input: "InputFolder\InternalPaymentModel-1.0.xml" OutputFolder pay_ true
 */
public class HbmGeneratorMain {

	/**
	 * Generator arguments:
	 * 
	 * <ul>
	 * <li><b>[0]</b> input file containing meta model xml
	 * <li><b>[1]</b> output folder
	 * <li><b>[2]</b> (optional) prefix to use when generating table name for given entity
	 * <li><b>[3]</b> (optional) if "true", all automatically generated table/column names will be uppercase
	 * <li><b>[4]</b> (optional) target db system/vendor
	 * <li><b>[5]</b> (optional) path a file containing type hints
	 * </ul>
	 * 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Generating Hbm files.");

		if (args.length < 2) {
			throw new RuntimeException("You need to provide 2 arguments - input file and output folder");
		}

		String gmMetaModelInputPath = args[0];
		String mappingFilesOutputPath = args[1];
		String tablePrefix = args.length >= 3 ? args[2] : "";
		boolean allUppercase = args.length >= 4 ? Boolean.parseBoolean(args[3]) : false;
		String targetDb = args.length >= 5 ? args[4] : "";
		String typeHintsFile = args.length >= 6 ? args[5] : null;

		System.out.println("Input file:" + gmMetaModelInputPath);
		System.out.println("Output folder:" + mappingFilesOutputPath);
		System.out.println("Table prefix:" + (tablePrefix.isEmpty() ? "<unspecified>" : tablePrefix));
		System.out.println("Uppercase option:" + allUppercase);
		System.out.println("Target db:" + (targetDb.isEmpty() ? "<unspecified>" : targetDb));
		System.out.println("Type hints file:" + (typeHintsFile == null ? "<unspecified>" : typeHintsFile));

		HbmXmlGeneratingService service = new HbmXmlGeneratingService();
		service.setGmMetaModel(parseModel(gmMetaModelInputPath));
		service.setOutputFolder(mappingFilesOutputPath);
		service.setTablePrefix(tablePrefix);
		service.setAllUppercase(allUppercase);
		service.setTargetDb(targetDb);
		if (typeHintsFile != null) {
			service.setTypeHintsFile(typeHintsFile);
		}
		service.renderMappings();

		System.out.println("Hbm generating Finished!");
	}

	private static GmMetaModel parseModel(String gmMetaModelInputPath) throws Exception {
		File xmlFile = new File(gmMetaModelInputPath);
		Document modelDocument = parseXml(xmlFile);
		return parseGmMetaModel(modelDocument);
	}

	private static Document parseXml(File sourceFile) throws DomParserException {
		return DomParser.load().fromFilename(sourceFile.getAbsolutePath());
	}

	private static GmMetaModel parseGmMetaModel(Document document) {
		return new GenericModelRootDomCodec<GmMetaModel>().decode(document);
	}

}
