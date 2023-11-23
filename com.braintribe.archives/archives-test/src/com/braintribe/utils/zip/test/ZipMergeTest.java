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
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;

public class ZipMergeTest {
	private File source = new File( "res/merge/source");
	private File target = new File( "res/merge/target");
	private File packedOne = new File( source, "packedOne.zip");
	private File packedTwo = new File( source, "packedTwo.zip");
	private File merged = new File( target, "merged.zip");

	
	@Before
	public void prepare() {
		if (target.exists())
			TestHelper.delete( target);
		target.mkdirs();
		if (packedOne.exists())
			packedOne.delete();
		if (packedTwo.exists())
			packedTwo.delete();
		
		try {
			Archives.zip().pack( new File( source, "one")).to(packedOne).close();
		} catch (ArchivesException e) {
			Assert.fail( String.format("cannot prepare zip file [%s] for test", packedOne.getAbsolutePath()));
		}
		try {
			Archives.zip().pack( new File( source, "two")).to(packedTwo).close();
		} catch (ArchivesException e) {
			Assert.fail( String.format("cannot prepare zip file [%s] for test", packedOne.getAbsolutePath()));
		}
	}

	@Test
	public void mergeFiles() {
		try {
			Archives.zip().from(packedOne).merge( packedTwo).to( merged).close();
			if (!TestHelper.compareMerged(merged, packedOne, packedTwo)) {
				Assert.fail("Merged result is not identical with the sources");
			}
		} catch (ArchivesException e) {
			Assert.fail( String.format("exception [%s] thrown", e));
		}
	}
	
	@Test
	public void mergeStreams() {
		try {
			FileInputStream stream = new FileInputStream(packedTwo);
			ZipContext zcontext = Archives.zip().from(packedOne).merge( stream);
			stream.close();
			zcontext.to( merged).close();
			if (!TestHelper.compareMerged(merged, packedOne, packedTwo)) {
				Assert.fail("Merged result is not identical with the sources");
			}
		} catch (Exception e) {
			Assert.fail( String.format("exception [%s] thrown", e));
		}
	}
	@Test
	public void mergeZipStreams() {
		try {
			FileInputStream stream = new FileInputStream( packedTwo);
			ZipInputStream zStream = new ZipInputStream(stream);
			ZipContext zcontext = Archives.zip().from( packedOne).merge( zStream);
			stream.close();
			zcontext.to( merged).close();
			if (!TestHelper.compareMerged(merged, packedOne, packedTwo)) {
				Assert.fail("Merged result is not identical with the sources");
			}
		} catch (Exception e) {
			Assert.fail( String.format("exception [%s] thrown", e));
		}
	}
	
	
}
