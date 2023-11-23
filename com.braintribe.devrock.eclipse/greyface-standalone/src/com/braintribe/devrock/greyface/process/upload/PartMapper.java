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
package com.braintribe.devrock.greyface.process.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.eclipse.core.runtime.IStatus;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.codec.CodecException;
import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.hash.md5.MD5HashGenerator;
import com.braintribe.crypto.hash.sha1.Sha1HashGenerator;
import com.braintribe.devrock.greyface.GreyfaceException;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.devrock.greyface.process.retrieval.TempFileHelper;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.maven.metadata.MavenMetaDataMarshaller;
import com.braintribe.marshaller.maven.metadata.MavenMetaDataProcessor;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.processing.part.PartProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class PartMapper {

	private static final String SHA1_GF = ".*\\.sha1\\.gf\\..*";

	private static final String MD5_GF = ".*\\.md5\\.gf\\..*";

	private static Logger log = Logger.getLogger(PartMapper.class);
	
	private static PartTuple md5Tuple = PartTupleProcessor.create( PartType.MD5);
	private static PartTuple sha1Tuple = PartTupleProcessor.create( PartType.SHA1);
	private static PartMapper instance = new PartMapper();
	private static MavenMetaDataMarshaller marshaller = new MavenMetaDataMarshaller();
	
	private class PartTriple {
		public Part part;
		public Part md5Part;
		public Part sha1Part;
	}
	
	private enum HashType { md5, sha1, none, both}
	
	
	public static Map<File, String> prepareBatchUpload( Solution solution, String groupRoot, String artifactRoot, File solutionMetaData, boolean prunePom) throws GreyfaceException {
		Map<File, String> result = new HashMap<File, String>();
		Set<Part> parts = solution.getParts();
		
		Map<String, PartTriple> triples = splitToTriples(parts);
		
		// 
		if (prunePom) {
		
			String pom = solution.getArtifactId() + "-" + VersionProcessor.toString( solution.getVersion()) + ".pom";
			PartTriple pomTriple = triples.get( pom);
			if (pomTriple != null) {
				// prune
				boolean pruned = prunePom( pomTriple.part.getLocation());
				if (pruned) {
					pomTriple.md5Part = null;
					pomTriple.sha1Part = null;
					String msg =  "pruned explicit repository from solution [" + NameParser.buildName(solution) +"]'s pom";
					GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.INFO);
					GreyfacePlugin.getInstance().getLog().log(status);
				}
			}	
		}
		
		// check, and if required, create maven-metadata.xml for the solution
		PartTriple metaDataTriple = triples.get("maven-metadata.xml");
		if (metaDataTriple == null) {
			metaDataTriple = createMetaDataTriple(solution);
			triples.put("maven-metadata.xml", metaDataTriple);
			solution.getParts().add( metaDataTriple.part);
		}
			
						
		for (PartTriple triple : triples.values()) {
			HashType createdHashes = ensureHashes(triple);
			// add generated parts 
			switch (createdHashes) {
				case md5:
					solution.getParts().add( triple.md5Part);
					break;
				case sha1:
					solution.getParts().add( triple.sha1Part);
					break;
				case both:
					solution.getParts().add( triple.md5Part);
					solution.getParts().add( triple.sha1Part);
					break;
				default:
					break;
			}
			addPartTriple( triple, artifactRoot, result);		
		}	
				
		// add/generate meta data for the solution's parent
		PartTriple groupMetaDataTriple = createMetaDataTriple(solutionMetaData, solution);		
		addPartTriple( groupMetaDataTriple, groupRoot, result);
		solution.getParts().add( groupMetaDataTriple.part);
		solution.getParts().add( groupMetaDataTriple.md5Part);
		solution.getParts().add( groupMetaDataTriple.sha1Part);
		
		return makeSureHashesAreLast(result);
	}

	private static Map<File, String> makeSureHashesAreLast(Map<File, String> result) {
		// make sure all .md5 or .sha1 files come at the end - Artifactory needs it that way 
		Map<File, String> returnValue = new LinkedHashMap<File, String>();
		Map<File, String> hashes = new HashMap<>();
		// iterate and split  
		for (Entry<File, String> entry : result.entrySet()) {
			File file = entry.getKey();
			// hashes go into different map, standard directly to result 
			if (isHashFile( file.getName())) {
				hashes.put(file, entry.getValue());
			}
			else {
				returnValue.put(file, entry.getValue());
			}
		}
		// add all the hashes to the end
		returnValue.putAll(hashes);
		
		return returnValue;
	}
	
	protected static boolean isHashFile(String name1) {
		if (name1.endsWith(".md5") || name1.endsWith(".sha1")) 
			return true;
		if (name1.matches( MD5_GF) || name1.matches( SHA1_GF))
			return true;
		return false;
	}

	private static boolean prunePom( String location) {
		try {
			File file = new File( location);
			Document doc = DomParser.load().from( file);
			Element parentE = doc.getDocumentElement();
			Element repositoriesE = DomUtils.getElementByPath( parentE, "repositories", false);
			if (repositoriesE != null) {				
				String repositoryComment = System.lineSeparator() + DomParser.write().from(repositoriesE).to();
				Comment commentE = doc.createComment( repositoryComment);
				parentE.replaceChild(commentE, repositoriesE);
				DomParser.write().setOmitDeclaration().from(doc).to( file);
				return true;
			}									
		} catch (DomParserException e) {
			String msg = "cannot prune repositories from pom [" + location + "] as " + e.getMessage();
			GreyfaceStatus status = new GreyfaceStatus( msg, e);
			GreyfacePlugin.getInstance().getLog().log(status);
		}		
		return false;
	}
	
	
	
	private static void addPart( Part part, String root, Map<File, String> sourceToTargetMap) {		
		if (part == null)
			return;
		String sourceLocation = part.getLocation().replace('\\', '/');
		File sourceFile = new File( sourceLocation);
		if (sourceFile.exists() == false) {
			if (log.isDebugEnabled()) {
				log.debug( "ignoring file [" + sourceLocation + "] as cannot be found. Probably already uploaded");
			}
			return;
		}		
		int p = sourceLocation.lastIndexOf('/');			
		String name = TempFileHelper.extractFilenameFromTempFile(sourceLocation.substring(p+1));

		if (name.startsWith(".grp.")) {
			name = name.substring(5);
		}
		
		String targetLocation = root + "/" + name;						
		sourceToTargetMap.put( sourceFile, targetLocation);				
	}
	
	private static void addPartTriple( PartTriple partTriple, String root, Map<File, String> sourceToTargetMap) {
			addPart( partTriple.part, root, sourceToTargetMap);
			addPart( partTriple.md5Part, root, sourceToTargetMap);
			addPart( partTriple.sha1Part, root, sourceToTargetMap);
	}
		
	private static Map<String, PartTriple> splitToTriples( Set<Part> parts) {
		Map<String, PartTriple> triples = new HashMap<String, PartTriple>();
		for (Part part : parts) {
			PartTuple tuple = part.getType();
			HashType hashType = HashType.none;
			if (PartTupleProcessor.equals(tuple, md5Tuple)) {
				hashType = HashType.md5;
			}
			else if (PartTupleProcessor.equals(tuple, sha1Tuple)) {
				hashType = HashType.sha1;
			}
			String key = generateKey( part);
			PartTriple triple = triples.get( key);
			if (triple == null) {
				triple = instance.new PartTriple();
				triples.put( key, triple);
			}				
			switch ( hashType) {
				case md5:
					triple.md5Part = part;
					break;
				case sha1:
					triple.sha1Part = part;															
					break;
				default: 					
					triple.part = part;														
			}
		}
		return triples;
	}
	
	private static String generateKey( Part part) {
		String location = part.getLocation().replace('\\', '/');
		if (TempFileHelper.isATempFile(location)) {
			location = TempFileHelper.extractFilenameFromTempFile(location);
		}
		String name = location.substring( location.lastIndexOf('/') + 1);
		// double extension? 
		
		return name;
	}
	
	/**
	 * creates hashes if required 
	 * @param triple - the {@link PartTriple} to process 
	 * @return - the {@link HashType} signaling what was generated, any of the values 
	 * @throws GreyfaceException - arrgh
	 */
	private static HashType ensureHashes( PartTriple triple) throws GreyfaceException {
		if (triple.part == null) {
			return HashType.none;
		}
		String name = generateKey( triple.part);
			
		String location = triple.part.getLocation();
		byte [] bytes;
		InputStream in = null;		
		HashType retval = HashType.none;
		File file = new File(location);
		try {
			if (file.exists() == false) {
				if (log.isDebugEnabled()) {
					log.info("file [" + location +"] doesn't exist, probably has been uploaded already in this session");
				}
				return HashType.none;
			}
			in = new FileInputStream( file);			
			bytes = IOTools.slurpBytes( in);
		} catch (FileNotFoundException e2) {
			String msg = "cannot read bytes of [" + location + "] for hash creation as " + e2; 
			log.error( msg, e2);
			throw new GreyfaceException( msg, e2);
		} catch (IOException e2) {
			String msg = "cannot read bytes of [" + location + "] for hash creation as " + e2; 
			log.error( msg, e2);
			throw new GreyfaceException( msg, e2);
		}
		finally {
			if (in != null)
				IOTools.closeQuietly(in);
		}
		
		if (triple.md5Part == null) {
		
			// md5 hash
			File md5File = null;
			try {					
				// hash is "<hash value> <file name>" as it looks like 
				String hash = MD5HashGenerator.MD5(  bytes) + " " + file.getName();		
				md5File = TempFileHelper.createTempFileFromFilename(name + ".md5");
				IOTools.spit( md5File, hash, "UTF-8", false);
				Part part = PartProcessor.createPartFromPart(triple.part, md5Tuple);
				part.setLocation( md5File.getAbsolutePath());
				triple.md5Part = part;
				triple.part.setMd5Hash(hash);
				retval = HashType.md5;
		
			} catch (CryptoServiceException e1) {
				String msg = "cannot create md5 hash for [" + location + "] as " + e1; 
				log.error( msg, e1);
				throw new GreyfaceException( msg, e1);
			} catch (IOException e) {
				String msg = "cannot create md5 hash for [" + location + "] as " + e; 
				log.error( msg, e);
				throw new GreyfaceException( msg, e);
			}
			finally {
				if (md5File != null)
					md5File.deleteOnExit();
			}		
		}
		if (triple.sha1Part == null) {
			// sha1
			File sha1File = null;
			try {
				// hash is "<hash value> <file name>" as it looks like
				String hash = Sha1HashGenerator.SHA1( bytes) + " " + file.getName();
				sha1File = TempFileHelper.createTempFileFromFilename(name + ".sha1");
				IOTools.spit( sha1File, hash, "UTF-8", false);			
				Part part = PartProcessor.createPartFromPart(triple.part, sha1Tuple);
				part.setLocation( sha1File.getAbsolutePath());
				triple.sha1Part = part;
				triple.part.setSha1Hash(hash);
				// check what to return 
				switch (retval) {
					case md5:
						retval = HashType.both;
						break;
					default:
						retval = HashType.sha1;
				}
			} catch (CryptoServiceException e1) {
				String msg = "cannot create sha1 hash for [" + location + "] as " + e1; 
				log.error( msg, e1);
				throw new GreyfaceException( msg, e1);
			} catch (IOException e) {
				String msg = "cannot create sha1 hash for [" + location + "] as " + e; 
				log.error( msg, e);
				throw new GreyfaceException( msg, e);
			}
			finally {
				if (sha1File != null)
					sha1File.deleteOnExit();
			}
		}		
		return retval;
	}

	private static PartTriple createMetaDataTriple( Solution solution) throws GreyfaceException {
		PartTriple triple = instance.new PartTriple();
		File file = null;
		try {
			MavenMetaData metaData = MavenMetaDataProcessor.createMetaData(solution);
			file = TempFileHelper.createTempFileFromFilename( "maven-metadata.xml");
			marshaller.marshall( file, metaData);
			Part part = PartProcessor.createPartFromIdentification(solution, solution.getVersion(), PartTupleProcessor.create( PartType.META));
			part.setLocation( file.getAbsolutePath());
			triple.part = part;
		} catch (IOException e) {
			throw new GreyfaceException( "cannot create temporary file for metadata triple", e);
		} catch (CodecException e) {
			throw new GreyfaceException( "cannot create encode metadata triple", e);
		} catch (XMLStreamException e) {
			throw new GreyfaceException( "cannot save metadata triple", e);
		}
		finally {
			if (file != null)
				file.deleteOnExit();
		}		
		return triple;
	}
	private static PartTriple createMetaDataTriple( File location, Solution solution) throws GreyfaceException{
		PartTriple triple;
		try {
			triple = instance.new PartTriple();			
			MavenMetaData mavenMetaData = null;
			if (location != null) {				
				mavenMetaData = marshaller.unmarshall( location);			
			}
			mavenMetaData = MavenMetaDataProcessor.addSolution(mavenMetaData, solution);
			if (location == null) {
				location = TempFileHelper.createTempFileFromFilename( ".grp.maven-metadata.xml");
			}
			marshaller.marshall( location, mavenMetaData);		
			Part part = PartProcessor.createPartFromIdentification(solution, solution.getVersion(), PartTupleProcessor.create( PartType.GLOBAL_META));
			part.setLocation( location.getAbsolutePath());
			triple.part = part;
			
			ensureHashes(triple);
		} catch (Exception e) {
			throw new GreyfaceException( "cannot generate group maven meta data ", e);
		}
		
		return triple;
	}
	
	public static void main( String [] args) {
		Map<File,String> input = new HashMap<File, String>();
		for (String arg : args) {	
			input.put( new File( arg), arg);
		}
		Map<File, String> output = makeSureHashesAreLast(input);
		for (String out : output.values()) {
			System.out.println( out);
		}
	}
}
