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
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPFile;

import com.braintribe.logging.Logger;
import com.braintribe.model.ftp.BatchFtpJob;
import com.braintribe.transport.ftp.FtpBatchTransfer;
import com.braintribe.transport.ftp.enums.SourcePostProcessing;
import com.braintribe.transport.ftp.enums.TargetConflictHandling;
import com.braintribe.transport.ftp.ConnectionException;

/**
 * Directories on the remote server are ignored.
 * @see {@link FtpBatchTransfer} for for a usage example
 */
public class FtpBatchDownloader extends FtpBatchTransfer<BatchFtpJob, File> {

	private static Logger logger = Logger.getLogger(FtpBatchDownloader.class);
	
	@Override
	protected void checkRemote() throws IOException, Exception {
		if (!super.existsRemoteFolder(super.currentJob.getRemotePath()))
			if (!super.createRemoteFolder(super.currentJob.getRemotePath()))
				throw new Exception(String.format("remote folder '%s' does not exist and could not be created for job '%s'", super.currentJob.getRemotePath(), super.currentJob.getName()));

		String pathname = super.currentJob.getSourceArchivePath();

		//if move after transfer is true, check if MoveRemoteAfterTransferTarget exists and try to create
		if (SourcePostProcessing.move == super.currentJob.getSourcePostProcessing() )
			if (!super.existsRemoteFolder(pathname))
				if (!super.createRemoteFolder(pathname))
					throw new Exception(String.format("could not create remote folder '%s'", pathname));
	}
	
	@Override
	protected void checkLocal() throws Exception {
		super.checkLocal(super.currentJob.getLocalPath(), true);
	}
	
	/**
	 * download all files and if set move them to the 'MoveSourceAfterTransferTarget' directory
	 * @return
	 * @throws ConnectionException
	 * @throws Exception
	 */
	@Override
	protected List<File> doTransfer() throws ConnectionException, Exception {
		// prepare remote folder
		List<FTPFile> remoteFiles = generateFileList();

		//download
		List<File> localFiles = new ArrayList<File>();
		for (FTPFile remoteFile : remoteFiles) {
			try {
				//redundant but just to make sure
				if (remoteFile.isDirectory())
					continue;
				String remoteFilename = remoteFile.getName();
				File localFile = null;
				try {
					localFile = getTargetFile(remoteFilename);
					
					if (logger.isDebugEnabled()) {
						logger.debug("Downloading remote file "+remoteFilename+" to "+localFile.getAbsolutePath());
					}
					
					super.currentFtp.download(remoteFilename, localFile);
				} catch (IOException e) {
					String msg = String.format("download of the remote file '%s' for job '%s' has failed because of '%s'", remoteFilename, super.currentJob.getName(), e.getClass());
					//will be caught by outer try block
					throw new Exception(msg,e);
				}

				if (localFile != null)
					localFiles.add(localFile);
				
				//if local post processing was not successful, skip remote post processing
				if (super.tpp != null && !super.tpp.process(localFile, this.currentJob)) {
					logger.debug("Local post processing returned false, remote post processing will be skipped");
					continue;
				}
				
				//remote post processing
				if (SourcePostProcessing.delete == super.currentJob.getSourcePostProcessing()) {
					if (!super.currentFtp.deleteFile(remoteFilename))
						throw new Exception(String.format("remote file '%s' could not delete remotely after download", remoteFilename));
				} else if (SourcePostProcessing.move == super.currentJob.getSourcePostProcessing()) {
					boolean b = super.currentFtp.getClient().rename(remoteFilename, super.currentJob.getSourceArchivePath() + "/" + remoteFilename);
					if (b == false)
						throw new Exception(String.format("remote file '%s' could not be moved to directory '%s' after download", remoteFilename,  super.currentJob.getSourceArchivePath()));
				}
			} catch (Exception e) {
				if (super.currentJob.getContinueOnError())
					logger.error(e.getMessage(), e);
				else
					throw e;
			}
		}//end for
		
		logger.debug(String.format("downloaded %d files from for job '%s' on server '%s'", localFiles.size(), this.currentJob.getId(), this.currentJob.getConnection().getId()));
		return localFiles;
	}

	/**
	 * removes all folders from the list of files to download, makes sure that no null entries exist and 
	 * if moveRemoteAfterDownload is true, ensures that the remote folder exists
	 * @param job
	 * @param ftp
	 * @return the list of files to download
	 * @throws Exception
	 */
	protected List<FTPFile> generateFileList() throws Exception {
		List<FTPFile> files = new LinkedList<FTPFile>();
		List<FTPFile> folders = new LinkedList<FTPFile>();

		String inclusionFilterString = super.currentJob.getFilenameInclusionFilter();
		logger.debug(String.format("filename inclusion filter is '%s'", inclusionFilterString));
		Pattern inclusionFilter = null;
		Matcher m = null;
		if (inclusionFilterString != null) {
			inclusionFilter = Pattern.compile( inclusionFilterString );
			m = inclusionFilter.matcher("");
		}
		
		//include only files matched by the inclusion filter and remove folders from file list
		logger.debug(String.format("querying file list from server"));
		List<FTPFile> fileList = super.currentFtp.listFiles();
		logger.debug(String.format("server returned a list of %d files", fileList.size()));
		for (FTPFile cur : fileList) {
			if (logger.isTraceEnabled())
				//log the complete file list to trace
				logger.trace(String.format("%s", cur.getName()));

			//exclude linux hidden files
			if (cur.getName().startsWith(".")) {
				if (logger.isTraceEnabled())
					logger.trace(String.format("%s starts with a '.' and is therefore ignored", cur.getName()));
				continue;
			}
			
			//match with inclusion filter
			if (m != null) {
				m.reset(cur.getName());
				if (!m.matches()) {
					if (logger.isTraceEnabled())
						logger.trace(String.format("%s does not match filname inclusion filter", cur.getName()));
					continue;
				}
			}
		
			//if cur passes the name filter, check if it is a file 
			if (cur.isFile() && cur.getName() != null) {
				if (logger.isTraceEnabled())
					logger.trace(String.format("%s is a file", cur.getName()));
				files.add(cur);
			} else {
				if (logger.isTraceEnabled())
					logger.trace(String.format("%s is a folder", cur.getName()));
				folders.add(cur);
			}
		

		}
		logger.debug(String.format("found %d files and %d folders in directory", files.size(), folders.size()));

		return files;
	}
	
	/**
	 * creates the local file object
	 * if TargetConflictHandling is set to rename, appends _1, _2, ... to the filename if the remote filename already exists in the local directory
	 * @param absolutePath
	 * @param name
	 * @return 
	 * @throws Exception 
	 */
	protected File getTargetFile(String filename) throws Exception {
		File localDir = new File( super.currentJob.getLocalPath() ); 
		if (!localDir.exists()) {
			localDir.mkdirs();
		}
		String directory = localDir.getAbsolutePath();
		String path = directory + File.separatorChar + filename;
		File file = new File(path);

		if (logger.isTraceEnabled())
			logger.trace(String.format("checking if local file '%s' exists", path));
		if (!file.exists())
			return file;
		else if (TargetConflictHandling.overwrite == super.currentJob.getTargetConflictHandling()) {
			if (logger.isTraceEnabled())
				logger.debug(String.format("deleting local file '%s'", path));
			file.delete();
			return file;
		} else if (TargetConflictHandling.error == super.currentJob.getTargetConflictHandling())
			//neither overwrite nor download ==> exception 
			throw new Exception(String.format("file '%s' already exists. If you want to enable overwrite, set removeConflictingLocal = true", file.getAbsolutePath()));

		/*
		 * find an unused filename
		 */
		//find file extension
		if (logger.isTraceEnabled())
			logger.trace(String.format("finding unused filename for remote file '%s'", filename));
		int index = filename.lastIndexOf(FILE_EXTENSION_SEPARATOR);
		String name = filename.substring(0,index);

		String ext = filename.substring(index);
		if (logger.isTraceEnabled())
			logger.trace(String.format("extension for '%s' is '%s'", filename, ext));
		
		/*
		 * iterate over possibilities until Long.MAX_VALUE is reached
		 */
		for (long i=1; i<Long.MAX_VALUE; i++) {
			String newPath = null;
			newPath = directory + File.separatorChar + name + "_" + i + ext;
			file = new File(newPath);
			if (!file.exists()) {
				logger.debug(String.format("new filename for '%s' is '%s'", filename, newPath));
				break;
			}
		}
		return file;
	}
	
/*
 * Getters and Setters
 */
}
