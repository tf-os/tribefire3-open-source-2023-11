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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;

public class TestHelper {
	
	
	public static byte [] getMd5(InputStream stream) {
		try {
			final MessageDigest digest = MessageDigest.getInstance( "MD5");
			final byte md[] = new byte[8192];
			for (int n = 0; (n = stream.read(md)) > -1;) {
				digest.update(md, 0, n);
			}
			return digest.digest();
		} catch (Exception e) {
			System.err.println( String.format( "Cannot read digest from stream as [%s]", e));
			return null;
		}
	}
	
	public static byte [] getMd5( File file) {
		try {
			FileInputStream stream = new FileInputStream( file);
			byte [] bytes = getMd5( stream);
			stream.close();
			return bytes;
		} catch (Exception e) {
			System.err.println( String.format( "Cannot read digest from file [%s] as [%s]", file.getAbsolutePath(), e));
			return null;
		} 
	}
	/**
	 * recursively delete the directory plus its content
	 * @param file - the directory {@link File} to delete 
	 */
	public static void delete( File file) {
		 if (!file.isDirectory()) {
			 file.delete();
		 } 
		 else {
			 File [] files = file.listFiles();
			 for (File suspect : files) {
				 delete( suspect);
			 }
			 file.delete();
		 }
	}
	
	/**
	 * compare a file, name and bytes 
	 * @param baseOne - the {@link URI} of the first parent directory 
	 * @param baseTwo - the {@link URI} of the second parent directory 
	 * @param one - the first {@link File}
	 * @param two - the second {@link File}
	 * @return - true if name and contents match, false otherwise 
	 */
	public static boolean compareFile( URI baseOne, URI baseTwo, File one, File two) {
		
		String nameOne = baseOne.relativize( one.toURI()).getPath();
		String nameTwo = baseTwo.relativize( two.toURI()).getPath();
		System.out.println( String.format("comparing [%s] to [%s]", nameOne, nameTwo));		
		if (!nameOne.equalsIgnoreCase(nameTwo)) {
			System.out.println( String.format("Name [%s] doesn't match [%s]", nameOne, nameTwo));
			return false;
		}
		// content test 
		byte[] bytesOne;
		try {
			bytesOne = Files.readAllBytes( Paths.get( one.toURI()));
		} catch (IOException e) {
			System.out.println( String.format("Cannot read bytes from [%s]", nameOne));
			return false;
		}
		byte[] bytesTwo;
		try {
			bytesTwo = Files.readAllBytes(Paths.get( two.toURI()));			
		} catch (IOException e) {
			System.out.println( String.format("Cannot read bytes from [%s]", nameTwo));
			return false;
		}
		
		boolean retval  = compareBytes( bytesOne, bytesTwo);
		if (!retval) {
			System.out.println( String.format("size or contents of [%s] doesn't match size of [%s]", nameOne, nameTwo));
		}
		return retval;
	}

	public static boolean compareBytes( byte[] bytesOne, byte[] bytesTwo) {
		if (bytesOne.length != bytesTwo.length) {
			return false;
		}
		
		for (int i = 0; i < bytesOne.length; i++) {
			if (bytesOne[i] != bytesTwo[i]) {
				return false;
			}
		}
		return true;
	}
	/**
	 * compares two directories, name and contents 
	 * @param baseOne - the {@link URI} of the first parent directory 
	 * @param baseTwo - the {@link URI} of the second parent directory 
	 * @param one - the first directory {@link File}
	 * @param two - the second directory {@link File}
	 * @return - true if name and contents match, false otherwise 
	 */
	public static boolean compareDirectory( URI baseOne, URI baseTwo, File one, File two) {		
		String nameOne = baseOne.relativize( one.toURI()).getPath();
		String nameTwo = baseTwo.relativize( two.toURI()).getPath();
		System.out.println( String.format("comparing [%s] to [%s]", nameOne, nameTwo));
		
		if (!nameOne.equalsIgnoreCase(nameTwo)) {
			System.out.println( String.format("Name [%s] doesn't match [%s]", nameOne, nameTwo));
			return false;
		}
		File [] filesOne = one.listFiles();
		File [] filesTwo = two.listFiles();
		
		if (filesOne.length != filesTwo.length) {
			System.out.println( String.format("size of [%s] doesn't match size of [%s]", nameOne, nameTwo));
		}
		for (int i = 0; i < filesOne.length; i++) {
			File fileOne = filesOne[i];
			File fileTwo = filesTwo[i];
			
			if (fileOne.isDirectory()) {
				if (fileTwo.isDirectory()) {
					if (!compareDirectory(baseOne, baseTwo, fileOne, fileTwo)) {
						return false;
					}
				} else {
					System.out.println( String.format( "[%s] is a directory, [%s] isn't", fileOne.getName(), fileTwo.getName()));
					return false;
				}
			} 
			else if (fileTwo.isDirectory()) {
				System.out.println( String.format( "[%s] isn't a directory, [%s] is", fileOne.getName(), fileTwo.getName()));
				return false;
			} 
			else {
				if (!compareFile(baseOne, baseTwo, fileOne, fileTwo)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean compareMerged( File one, File... sources) {
		ZipContext suspectZC;
		try {
			suspectZC = Archives.zip().from(one);
		} catch (ArchivesException e) {
			System.out.println( String.format( "cannot build ZipContext from [%s]", one.getAbsolutePath()));
			return false;
		}
		List<ZipContext> sourceZCs = new ArrayList<ZipContext>();
		for (File file : sources) {
			try {
				ZipContext zc = Archives.zip().from(file);
				sourceZCs.add( zc);
			} catch (ArchivesException e) {
				System.out.println( String.format( "cannot build ZipContext from [%s]", file.getAbsolutePath()));
				return false;
			}
		}
		try {
			Map<ZipEntry, InputStream> entries = suspectZC.dump();
			for (Entry<ZipEntry, InputStream> entry : entries.entrySet()) {
				byte[] suspectPayload;
				String name = entry.getKey().getName();
				try {
					suspectPayload = getPayload( entry.getValue());
				} catch (IOException e) {
					String msg = String.format( "cannot retrieve payload of [%s] of merged [%s] ", name, one.getAbsolutePath());
					System.out.println( msg);
					return false;
				}
				
				boolean found = false;
				for (ZipContext zc : sourceZCs) {
					InputStream stream = zc.get( name);
					if (stream != null) {
						found = true;
						// 
						byte[] correspondingPayload;
						try {
							correspondingPayload = getPayload(stream);
						} catch (IOException e) {
							String msg = String.format( "cannot retrieve payload of [%s] of orignal file", name);
							System.out.println( msg);
							return false;
						}
						if (!compareBytes(suspectPayload, correspondingPayload)) {
							return false;
						}
					}
					if (found)
						break;
				}
				if (!found) {
					return false;
				}
			}
		}
		finally {
			suspectZC.close();
			for (ZipContext zc : sourceZCs) {
				zc.close();
			}
		}
		return true;
	}
	
	private static byte [] getPayload( InputStream stream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte [] buffer = new byte[2048];
		int count  = 0;
		while ((count = stream.read( buffer)) != -1) {
			byteArrayOutputStream.write( buffer, 0, count);
		}
		return byteArrayOutputStream.toByteArray();			 
	}
	public static boolean compareZips( File one, File two) {
		   
	    ZipFile file1 = null;
	    ZipFile file2 = null;
	    
	    try {
			try {
			  file1 = new ZipFile( one);
			} catch (IOException e) {
			  System.err.println("Could not open zip file " + one.getAbsolutePath() + ": " + e);
			  return false;
			}

			try {
			  file2 = new ZipFile( two);
			} catch (IOException e) {
				  System.err.println("Could not open zip file " + two.getAbsolutePath() + ": " + e);
			      return false;
			}

			Set<String> set1 = new LinkedHashSet<String>();
			for (Enumeration<? extends ZipEntry> e = file1.entries(); e.hasMoreElements();)
			  set1.add(e.nextElement().getName());

			Set<String> set2 = new LinkedHashSet<String>();
			for (Enumeration<? extends ZipEntry> e = file2.entries(); e.hasMoreElements();)
			  set2.add( e.nextElement().getName());

			int errcount = 0;
			int filecount = 0;
			for (Iterator<String> i = set1.iterator(); i.hasNext();) {
			  String name = i.next();
			  if (!set2.contains(name)) {
			    System.err.println(name + " not found in " + one.getAbsolutePath());
			    errcount += 1;
			    continue;
			  }
			  try {
			    set2.remove(name);
			    if (!streamsEqual(file1.getInputStream(file1.getEntry(name)), file2.getInputStream(file2
			        .getEntry(name)))) {
			      System.err.println(name + " does not match");
			      errcount += 1;
			      continue;
			    }
			  } catch (Exception e) {
			    System.err.println(name + ": IO Error " + e);
			    e.printStackTrace();
			    errcount += 1;
			    continue;
			  }
			  filecount += 1;
			}
			for (Iterator<String> i = set2.iterator(); i.hasNext();) {
			  String name = i.next();
			  System.err.println(name + " not found in " + two.getAbsolutePath());
			  errcount += 1;
			}
			System.out.println(filecount + " entries matched");
			if (errcount > 0) {
			  System.err.println(errcount + " entries did not match");
			  return false;
			}
			return true;
		} 
	    finally  {
	    	try {
				if (file1 !=null)
					file1.close();
				if (file2 != null)
					file2.close();
			} catch (IOException e) {
				//Nothing
			}
	    }
	  }

	  private static boolean streamsEqual(InputStream stream1, InputStream stream2) throws IOException {
	    byte[] buf1 = new byte[4096];
	    byte[] buf2 = new byte[4096];
	    boolean done1 = false;
	    boolean done2 = false;

	    try {
	      while (!done1) {
	        int off1 = 0;
	        int off2 = 0;

	        while (off1 < buf1.length) {
	          int count = stream1.read(buf1, off1, buf1.length - off1);
	          if (count < 0) {
	            done1 = true;
	            break;
	          }
	          off1 += count;
	        }
	        while (off2 < buf2.length) {
	          int count = stream2.read(buf2, off2, buf2.length - off2);
	          if (count < 0) {
	            done2 = true;
	            break;
	          }
	          off2 += count;
	        }
	        if (off1 != off2 || done1 != done2)
	          return false;
	        for (int i = 0; i < off1; i++) {
	          if (buf1[i] != buf2[i])
	            return false;
	        }
	      }
	      return true;
	    } finally {
	      stream1.close();
	      stream2.close();
	    }
	  }

}
