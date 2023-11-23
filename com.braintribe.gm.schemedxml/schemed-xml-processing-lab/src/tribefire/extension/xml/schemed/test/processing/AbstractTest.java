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
package tribefire.extension.xml.schemed.test.processing;

import java.io.File;

import org.junit.experimental.categories.Category;

import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PlainSysoutConsole;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.stream.ReferencingFileInputStream;

import tribefire.extension.xml.schemed.processing.XsdAnalyzingProcessor;
import tribefire.extension.xml.schemed.service.AnalyzeXsd;
import tribefire.extension.xml.schemed.service.AnalyzedXsd;
import tribefire.extension.xml.schemed.test.commons.xsd.test.util.TestUtil;

@Abstract
@Category(KnownIssue.class)
public class AbstractTest {
	protected static File contents = new File( "res");

	
	protected static void before(File output) {
		TestUtil.ensure(output);
		ConsoleConfiguration.install( PlainSysoutConsole.INSTANCE);
	}
	
	protected AnalyzedXsd runTest( File input, File output, String xsdName, String packageName, String modelName) {
		AnalyzeXsd request = AnalyzeXsd.T.create();
		
		File inputFile = new File( input, xsdName);
		request.setSchema( Resource.createTransient(() -> new ReferencingFileInputStream( inputFile)));
		request.setOutputDir( output.getAbsolutePath());
		
		request.setTopPackageName(packageName);
		request.setSkeletonModelName( modelName);
		
		request.setExchangePackageOutput(true);
		//request.setJarOutput(true);
		
		XsdAnalyzingProcessor processor = new XsdAnalyzingProcessor();
	
		AnalyzedXsd response = processor.analyzeXsdFile((ServiceRequestContext) null, request);
		
		return response;

	}
}
