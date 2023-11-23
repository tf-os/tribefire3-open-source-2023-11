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
package tribefire.extension.xml.schemed.test.marshaller.xsd;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.xmlunit.diff.Diff;

import com.braintribe.testing.category.KnownIssue;

import tribefire.extension.xml.schemed.marshaller.xsd.SchemedXmlXsdMarshaller;
import tribefire.extension.xml.schemed.model.xsd.Schema;


@Category(KnownIssue.class)
public class AbstractXsdMarshallerTest  {
	protected static File contents = new File("res");
	protected File parentDirectory;
	
	private SchemedXmlXsdMarshaller marshaller = new SchemedXmlXsdMarshaller();
	
	public static void delete( File file) {
		if (file == null || file.exists() == false)
			return;
		for (File child : file.listFiles()) {
			if (child.isDirectory()) {
				delete( child);
			} 
			child.delete();			
		}
	}

	public static void ensure(String checkdir) {
		File output = new File(checkdir);
		if (output.exists())
			delete( output);
		output.mkdirs();
	}
	public static void ensure(File output) {	
		if (output.exists())
			delete( output);
		output.mkdirs();
	}
	

	protected Schema readFile( File file) {
		try {
			return marshaller.unmarshall(file);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			Assert.fail("cannot unmarshall [" + file.getAbsolutePath() + "] as " + e);
		}
		return null;
	}
	
	protected void writeFile( File file, Schema schema) {
		try {
			marshaller.marshall(file, schema);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			Assert.fail("cannot unmarshall [" + file.getAbsolutePath() + "] as " + e);
		}
	}
	
	protected void compare( File source, File target) {
		XsdComparator comparator = new XsdComparator();
		Diff myDiff = comparator.compare(source, target);
		Assert.assertTrue("XML isn't matching " + myDiff.toString(), !myDiff.hasDifferences());
	}
}
