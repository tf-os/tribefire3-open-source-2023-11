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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class ModelProducer implements HasCommonFilesystemNode{	
	private File input;
	private File output;
	
	{
		Pair<File,File> pair = filesystemRoots("models");
		input = pair.first;
		output = pair.second;
	}
	
	private GenericModelTypeReflection reflection = GMF.getTypeReflection();
	private YamlMarshaller marshaller = new YamlMarshaller();
	
	
	private void produceModelYaml(String name, File file) {
		Model model = reflection.getModel(name);
		if (model == null) {
			Assert.fail("no model found named [" + name + "]");
			return;
		}
		GmMetaModel metaModel = model.getMetaModel();
		
		try ( OutputStream out = new FileOutputStream( file)) {
			marshaller.marshall(out, metaModel);
		}
		catch (IOException e) {
			Assert.fail("cannot marshall model [" + name + "] to file [" + file.getAbsolutePath() + "]");
		}		
	}
	
	@Test
	public void versionModel() {
		produceModelYaml("com.braintribe.gm:version-model", new File( output, "version-model.yaml"));
	}
	@Test
	public void compiledArtifactModel() {
		produceModelYaml("com.braintribe.devrock:compiled-artifact-model", new File( output, "compiled-artifact-model.yaml"));
	}
	
	

}
