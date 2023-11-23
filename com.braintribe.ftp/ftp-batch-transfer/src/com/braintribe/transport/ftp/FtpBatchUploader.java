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
package com.braintribe.transport.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.net.ftp.FTPFile;

import com.braintribe.model.ftp.BatchFtpJob;
import com.braintribe.transport.ftp.enums.SourcePostProcessing;
import com.braintribe.transport.ftp.enums.TargetConflictHandling;
import com.braintribe.transport.ftp.enums.TargetTransferSuccessCheck;
import com.braintribe.transport.ftp.ConnectionException;

public class FtpBatchUploader extends FtpBatchTransfer<BatchFtpJob, FTPFile> {
	private boolean deleteLocal = false;
	private boolean strictTransferCheck = true;
	private TargetTransferSuccessCheck targetTransferSuccessCheck = TargetTransferSuccessCheck.single;
	
	@Override
	protected void validate(BatchFtpJob job) throws IllegalArgumentException, NullPointerException, Exception {
		//apply less strict generic validation rules  
		super.validate(job);
		
		try {
			if (SourcePostProcessing.move == job.getSourcePostProcessing())
				throw new IllegalArgumentException(String.format("invalid value 'sourcePostProcessing' = '%s' only '%s' and '%s' are currently supported", job.getSourcePostProcessing(), SourcePostProcessing.none, SourcePostProcessing.delete));
		} catch (IllegalArgumentException  e) {
			throw new IllegalArgumentException(String.format("invalid value 'sourcePostProcessing' = '%s' only '%s' and '%s' are currently supported", job.getSourcePostProcessing(), SourcePostProcessing.none, SourcePostProcessing.delete));
		}
	}
	
	@Override
	protected void checkRemote() throws IOException, Exception {
		if (!super.existsRemoteFolder(super.currentJob.getRemotePath()))
			if (!super.createRemoteFolder(super.currentJob.getRemotePath()))
				throw new Exception(String.format("remote folder '%s' does not exist for job '%s'", super.currentJob.getRemotePath(), super.currentJob.getName()));
	}
	
	@Override
	protected void checkLocal() throws Exception {
		boolean needWriteAccess = true;
		if (SourcePostProcessing.none == super.currentJob.getSourcePostProcessing())
			needWriteAccess = false;
		super.checkLocal(super.currentJob.getLocalPath(), needWriteAccess);
	}
	
	/**
	 * upload all files and if SourcePostProcessing.delete is set, delete local copies
	 * @return a flat list of all remote files and folders that have been uploaded
	 * @throws ConnectionException
	 * @throws Exception
	 */
	@Override
	protected List<FTPFile> doTransfer() throws ConnectionException, Exception {
		this.deleteLocal = false;
		if (SourcePostProcessing.delete == super.currentJob.getSourcePostProcessing())
			this.deleteLocal = true;
		
		File source = new File(super.currentJob.getLocalPath());
		return upload(source,super.currentJob.getRemotePath());
	}
	
	/**
	 * recursively uploads all files and folders from localPath to remotePath
	 * @param source
	 * @param curAbsPath
	 * @param remoteDir 
	 * @return a flat list of FTPFiles which reference all uploaded files
	 * @throws IOException
	 * @throws FileExistsException
	 * @throws Exception
	 */
	private List<FTPFile> upload(File source, String curAbsPath) throws IOException, FileExistsException, Exception {
		//build remote file list for name clash analysis
		if (logger.isTraceEnabled())
			logger.trace(String.format("starting upload for '%s' to '%s' for job '%s'", source.getAbsolutePath(), curAbsPath, super.currentJob.getName()));
		try {
			List<FTPFile> ret;
			if (source.isFile()) {
				ret = uploadFile(source,curAbsPath);
			} else {
				ret = uploadDirectory(source, curAbsPath);
				//TODO: check for successful transfer of all files in this directory				
			}
			if (ret == null)
				ret = new ArrayList<FTPFile>(0);
			return ret;
		} catch (Exception e) {
			String msg = String.format("upload of %s to %s failed.", source.getAbsolutePath(), curAbsPath);
			if (super.continueOnError) {
				logger.error(msg, e);
				return new ArrayList<FTPFile>(0);
			} else {
				throw new Exception (msg, e);
			}
		}
	}
	
	private List<FTPFile> uploadDirectory(File source, String curAbsPath) throws IOException, FileExistsException, Exception {
		List<FTPFile> ret = new LinkedList<FTPFile>();
		//get all local files
		File[] localListFiles = source.listFiles();
		if (localListFiles == null)
			return ret;
		
		for (File cur : localListFiles) {
			if (cur.isFile()) {
				//call upload because of error handling / continue on error
				ret.addAll( upload(cur, curAbsPath) );
				continue;
			}
			
			//create target dir's absolute path
			String nextAbsPath = curAbsPath + "/" + cur.getName();
			if (super.currentFtp.checkDirectoryExists( nextAbsPath )) {
				/* FTP is now in target directory
				 * (@see currentFtp.checkDirectoryExists() javadoc:
				 * Checks if the directory directoryPath exists by changing into the directory 
				 */
			} else {
				//create target folder on ftp and switch to it
				if (!super.currentFtp.getClient().makeDirectory(nextAbsPath) )
					//directory creation failed
					throw new IOException(String.format("creation of directory '%s' on connection '%s' failed", nextAbsPath, super.currentConnection.getName()));
				super.currentFtp.changeWorkingDirectory(nextAbsPath);
			}
			
			//add folder to return list
			Pattern pattern = Pattern.compile( cur.getName() );
			ret.addAll( super.currentFtp.listFiles(curAbsPath, pattern) );
			
			//recursively call sub folder
			ret.addAll( this.upload(cur, nextAbsPath) );
		}//end for
		postProcessSource(source);
		return ret;
	}
	
	private List<FTPFile> uploadFile(File source, String curAbsPath) throws IOException, FileExistsException, Exception {
		String uploadName = source.getName();
		
		TargetConflictHandling tch = super.currentJob.getTargetConflictHandling();
		if (tch != TargetConflictHandling.overwrite) {
			
			if (logger.isTraceEnabled())
				logger.trace(String.format("check if remote file '%s' exists", source.getName()));

			
			if (super.currentFtp.checkFileExists(source.getName()) )
				if (tch == TargetConflictHandling.rename)
					uploadName = findFilename(source.getName());
				else
					throw new FileExistsException(String.format("file '%s' already exists in target folder '%s'",source.getName(), curAbsPath));
		} else {
			if (logger.isTraceEnabled())
				logger.trace(String.format("not checking if remote file '%s' exists as we would overwrite it anyway.", source.getName()));
		}

		if (logger.isTraceEnabled())
			logger.trace(String.format("uploading file '%s'", source.getName()));
		FileInputStream is = new FileInputStream(source);
		try {
			super.currentFtp.upload(uploadName, source.getName(), is);
		} catch(Exception e) {
			throw new Exception("Error while uploading file "+source.getAbsolutePath()+", exists: "+source.exists()+", isFile: "+source.isFile()+", size: "+source.length(), e);
		}
		
		//get reference of uploaded file
		if (logger.isTraceEnabled())
			logger.trace(String.format("getting FTPFile reference for '%s'", source.getName()));
		
		//should each uploaded file be checked individually?
		if (this.targetTransferSuccessCheck == TargetTransferSuccessCheck.none)
			return null;
		
		//try to list the file from the ftp server 
		Pattern pattern = Pattern.compile( uploadName );
		List<FTPFile> retlist = super.currentFtp.listFiles(".", pattern);
		if (retlist == null || retlist.isEmpty()) {
			if (this.strictTransferCheck)
				throw new Exception(String.format("upload of file %s failed", source.getAbsolutePath()));
			//else
			logger.warn(String.format("could not find file %s after upload on remote server", source.getAbsolutePath()));
			return null;
		}
		
		FTPFile file = retlist.get(0);
		//only post process locally if remote processing was successful
		if ((super.tpp != null && super.tpp.process(file, this.currentJob)) || (super.tpp == null)) {
			postProcessSource(source);
		}
		
		return retlist;
	}
	
	private String findFilename(String filename) throws Exception {
		/*
		 * build remote file list for name clash analysis, this is done for each clash separately because a renamed file could clash with the next upload
		 * e.g. foo.txt is renamed to foo_2.txt, but the next file waiting for upload is also named foo_2.txt (foo_2.txt --rename--> foo_2_2.txt)
		 */
		Set<String> remoteDir = new HashSet<String>();
		for (FTPFile cur : super.currentFtp.listFiles())
			remoteDir.add(cur.getName());
		
		//find file extension
		if (logger.isTraceEnabled())
			logger.trace(String.format("finding unused filename for remote file '%s'", filename));
		int index = filename.lastIndexOf(FILE_EXTENSION_SEPARATOR);
		String name = filename.substring(0,index);

		String ext = filename.substring(index);
		if (logger.isTraceEnabled())
			logger.trace(String.format("extension for '%s' is '%s'", filename, ext));
		
		for (long i=1; i<this.maxRename; i++) {
			String newName = name + "_" + i + ext;
			if ( !remoteDir.contains(newName) ) {
				logger.debug(String.format("new filename for '%s' is '%s'", filename, newName));
				return newName;
			}
		}
		throw new Exception(String.format("even after iterating through %d names, no availible renaiming could be found for '%s'", this.maxRename, filename));
	}
	
	private void postProcessSource(File f) throws IOException {
		if (this.deleteLocal) {
			if (f.exists()) {
				if (f.isFile()) {
					logger.debug("Deleting file "+f.getAbsolutePath());
					if (!f.delete()) {
						logger.error("Could not delete local file "+f.getAbsolutePath());
					} else {
						logger.debug("Delete local file "+f.getAbsolutePath());
					}
				} else {
					logger.debug("Deleting directory "+f.getAbsolutePath());
					this.deleteDirectoryRecursively(f);
				}
			}
		}
	}

	protected void deleteDirectoryRecursively(File file) throws IOException {
		if (file.isDirectory()) {
			for (File c : file.listFiles())
				deleteDirectoryRecursively(c);
		}
		if (!file.delete()) {
			if (file.isFile()) {
				logger.error("Could not delete local file "+file);
			} else {
				logger.error("Could not delete local directory "+file);
			}
		}
	}

/*
 * Getters and Setters
 */
	@Override
	public void setTargetProcessingHook(TargetProcessingHook<FTPFile> arg) throws Exception {
		if (this.targetTransferSuccessCheck != TargetTransferSuccessCheck.single)
			throw new Exception(String.format("cannot set TargetProcessing hook because TargetTransferSuccessCheck is set to '%s'. Please set targetTransferSuccessCheck to '%s' otherwise no ProcessingHook can be used", this.targetTransferSuccessCheck, TargetTransferSuccessCheck.single));
		super.setTargetProcessingHook(arg);
	}

	public TargetTransferSuccessCheck getTargetTransferSuccessCheck() {
		return targetTransferSuccessCheck;
	}

	public void setTargetTransferSuccessCheck(TargetTransferSuccessCheck arg) throws Exception {
		if (arg == TargetTransferSuccessCheck.batch)
			throw new Exception(TargetTransferSuccessCheck.class + " " + arg + "is not yet supported");
		this.targetTransferSuccessCheck = arg;
	}

	public boolean getStrictTransferCheck() {
		return strictTransferCheck;
	}
	
	/**
	 * If the strict transfer check is activated, an error is thrown if the uploaded file cannot be found after uploading.
	 * @default true
	 * @param arg
	 */
	public void setStrictTransferCheck(boolean arg) {
		this.strictTransferCheck = arg;
	}
}