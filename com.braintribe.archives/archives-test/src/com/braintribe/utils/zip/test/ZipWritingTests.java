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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;

public class ZipWritingTests {
	private File source = new File( "res/writing/source/packed.zip");
	private File target = new File( "res/writing/target");
	
	
	private ZipContext readSource() {
		try {
			return Archives.zip().from(source);
		} catch (ArchivesException e) {
			Assert.fail( String.format("cannot prepare source zip as [%s]", e));
		}
		return null;
	}
	
	@Before
	public void before() {
		TestHelper.delete(target);
		
	}

	@Test
	public void testToFile() {
		ZipContext context = readSource();
		target.mkdirs();
		try {
			File targetFile = new File( target, "target.zip");
			context.to( targetFile);
			Assert.assertTrue( String.format( "contents of [%s] and [%s] have not the same hash", source.getAbsolutePath(), targetFile.getAbsolutePath()), TestHelper.compareZips(source, targetFile));
		} catch (ArchivesException e) {
			Assert.fail( String.format( "Cannot write zip context to [%s]", e));
		}
	}
	
	@Test
	public void testToOutputStream() {
		ZipContext context = readSource();
		target.mkdirs();
		try {
			File targetFile = new File( target, "target.zip");
			FileOutputStream stream = new FileOutputStream(targetFile);
			context.to( stream);
			stream.close();
			Assert.assertTrue( String.format( "contents of [%s] and [%s] have not the same hash", source.getAbsolutePath(), targetFile.getAbsolutePath()), TestHelper.compareZips(source, targetFile));
		} catch (Exception e) {
			Assert.fail( String.format( "Cannot write zip context to [%s]", e));
		} 
	}
	
	@Test
	public void testToZipInputStream() {
		ZipContext context = readSource();
		target.mkdirs();
		try {
			File targetFile = new File( target, "target.zip");
			FileOutputStream stream = new FileOutputStream( targetFile);
			ZipOutputStream zipStream = new ZipOutputStream( stream);
			context.to( zipStream);
			zipStream.close();
			Assert.assertTrue( String.format( "contents of [%s] and [%s] have not the same hash", source.getAbsolutePath(), targetFile.getAbsolutePath()), TestHelper.compareZips(source, targetFile));
		} catch (Exception e) {
			Assert.fail( String.format( "Cannot write zip context to [%s]", e));
		} 
	}

	@Test
	public void testFromZipInputStream() {
		try {
			FileInputStream inputStream = new FileInputStream( source);
			ZipInputStream zipInputStream = new ZipInputStream( inputStream);
			ZipContext zC = Archives.zip().from( zipInputStream);
			inputStream.close();
			for (String header : zC.getHeaders()) {
				System.out.println("-> " + header);
			}
		} catch (Exception e) {
			Assert.fail( String.format("cannot read per file from file [%s], exception [%s] thrown", source.getAbsolutePath(), e));
		}
	}

	
}
