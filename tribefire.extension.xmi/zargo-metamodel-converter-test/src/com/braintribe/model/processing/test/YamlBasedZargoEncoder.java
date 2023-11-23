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
package com.braintribe.model.processing.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.zargo.MetaModelToZargoConverterWorker;
import com.braintribe.model.processing.zargo.wire.ZargoConverterModule;
import com.braintribe.model.processing.zargo.wire.contract.WorkerContract;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.IOTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
@Category(KnownIssue.class)
public class YamlBasedZargoEncoder implements HasCommonFilesystemNode{
	
	private File input;
	private File output;
	
	{
		Pair<File,File> pair = filesystemRoots("ym2z");
		input = pair.first;
		output = pair.second;
	}
	
	private YamlMarshaller marshaller = new YamlMarshaller();
	
	@Before
	public void before() {
		TestUtils.ensure(output);
	}
	
	private GmMetaModel loadModel( File file) {
		try ( InputStream in = new FileInputStream( file)) {
			return (GmMetaModel) marshaller.unmarshall( in);
		}
		catch (Exception e) {
			Assert.fail("cannot load model from [" + file.getAbsolutePath() + "]" + e.getMessage());
		}
		return null;
	}
	
	private void test(File modelFile, File out, File in) {
		
		
		InputStream inputStream;
		try {
			inputStream = in != null && in.exists() ? new FileInputStream( in) : null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");
			return;
		}
		
		GmMetaModel metaModel = loadModel(modelFile); 
		try (				
				WireContext<WorkerContract> context = Wire.contextBuilder( ZargoConverterModule.INSTANCE).build();
				OutputStream outputStream = new FileOutputStream(out);				
			) {						
				MetaModelToZargoConverterWorker worker = context.contract().metaModelToZargoWorker();
				worker.execute(metaModel, outputStream, inputStream);				
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		finally {
			if (inputStream != null) {
				IOTools.closeQuietly(inputStream);
			}
		}
	}
	
	
	@Test
	public void testCleanVersionModel() {			
		File file = new File( output, "version-model.zargo");
		File modelFile = new File( input, "version-model.yaml");
		test( modelFile, file, null);
	}
	@Test
	public void testCleanCompiledArtifactModel() {			
		File file = new File( output, "compiled-artifact-model.zargo");
		File modelFile = new File( input, "compiled-artifact-model.yaml");
		test( modelFile, file, null);
	}
	
	@Test
	public void testPrimedVersionModel() {			
		File file = new File( output, "version-model.zargo");
		File primer = new File( input, "version-model.zargo");
		File modelFile = new File( input, "version-model.touched.yaml");
		test( modelFile, file, primer);
	}
	@Test
	public void testPrimedCompiledArtifactModel() {			
		File file = new File( output, "compiled-artifact-model.zargo");
		File primer = new File( input, "compiled-artifact-model.zargo");
		File modelFile = new File( input, "compiled-artifact-model.yaml");
		test( modelFile, file, primer);
	}

}
