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
package com.braintribe.utils.zip.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;

/**
 * tests the packing / unpacking of directories and files 
 *  
 * @author pit
 *
 */
public class ZipIoTest {

	private File parent = new File("res/pack-unpack");
	
	
	public void packTest() {
		ZipContext zContext = Archives.zip();
		try {
			File source = new File( parent, "source");
			File target = new File( parent, "packed.zip");
			if (target.exists()) {
				target.delete();
			}
			zContext.pack( source).to(target).close();
		} catch (ArchivesException e) {
			String msg = String.format("Exception [%s] thrown", e);
			Assert.fail( msg);
		}		
	}
	
	
	
	public void unpackTest() {
		ZipContext zContext = Archives.zip();
		try {
			
			File source = new File( parent, "packed.zip");
			File target = new File( parent, "target");
			if (target.exists()) {
				TestHelper.delete( target);
			}			
			target.mkdirs();
			zContext.from( source).unpack(target).close();
		} catch (ArchivesException e) {
			String msg = String.format("Exception [%s] thrown", e);
			Assert.fail( msg);
		}		
	}

	
	@Test
	public void combinedTest() {
		packTest();
		unpackTest();
		File source = new File( parent, "source");
		File target = new File( parent, "target");
		if (!TestHelper.compareDirectory( source.toURI(), target.toURI(), source, target)) {
			Assert.fail("Result is not identical");
		}
	}

}
