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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.braintribe.logging.Logger;
import com.braintribe.model.ftp.FtpConnection;

/**
 * this class mainly abstracts org.apache.commons.net.ftp.FTPClient.
 * It provides a more high level (file driven) interface for accessing an FTP server
 */
public class FtpConnector {

	private static final int REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE = 550;

	protected static Logger logger = Logger.getLogger(FtpConnector.class);
	
	private FtpConnection connection;
	private FTPClient client;
	
	private boolean connected;
	
		/**
	 * Creates a new ftp connector initializing with a  {@link org.apache.commons.net.ftp.FTPClient FTPClient}
	 * 
	 * @param connection
	 *   - The {@link FtpConnection.i2z.connector.filetransfer.impl.transport.ftp.FTPConnection FtpConnection} to use
	 */
	public FtpConnector(FtpConnection connection) {
		this.connection = connection;
		this.client = new FTPClient();
	}

	/**
	 * 
	 * @param con
	 * @throws ConnectionException
	 * @throws NullPointerException
	 */
	public static void validate(FtpConnection con) throws ConnectionException, NullPointerException {
		if (con == null)
			throw new NullPointerException(String.format("the supplied connection is null"));
		
		if (con.getHost() == null)
			throw new NullPointerException(String.format("host for ftp connection with id '%s' is null", con.getId()));
		if ("".equals(con.getHost()))
			throw new ConnectionException(con,String.format("no host set for ftp connection with id '%s'", con.getId()));
	}
	
	
	public FtpConnection getConnection() {
		return this.connection;
	}

	public FTPClient getClient() {
		return this.client;
	}

	public boolean isConnected() {
		return this.client.isConnected();
	}


	/**
	 * 
	 * @return true if connection was successfully established
	 */
	public boolean connect() {	
		if (this.connected)
			return true;
		
		String connectionSpec = this.getConnectionInfo();
				
		if (logger.isTraceEnabled())
			logger.trace(String.format("connecting to %s", connectionSpec));
		
		try {
			/*
			 * connect to host
			 */
			this.client.connect(this.connection.getHost(), this.connection.getPort());
			
			/*
			 * check if we should switch to a specific client mode
			 */
			if (this.connection.getClientMode() != null) {
				
				if (logger.isTraceEnabled())
					logger.trace(String.format("entering client mode %s", this.connection.getClientMode()));

				switch (this.connection.getClientMode()) {
					case ACTIVE_LOCAL:
						this.client.enterLocalActiveMode();
						break;
					case PASSIVE_LOCAL:
						this.client.enterLocalPassiveMode();
						break;
					case ACTIVE_REMOTE:
						if (!this.client.enterRemoteActiveMode(InetAddress.getByName(this.connection.getRemoteActiveHost()), this.connection.getRemoteActivePort()))
							throw new Exception(
								String.format("unable to switch to mode ACTIVE_REMOTE_DATA_CONNECTION_MODE for %s", connectionSpec));
						break;
					case PASSIVE_REMOTE:
						if (!this.client.enterRemotePassiveMode())
							throw new Exception("unable to switch to mode PASSIVE_REMOTE_DATA_CONNECTION_MODE");
						break;
					default:
						throw new Exception("Unknown client mode "+this.connection.getClientMode());
				}
			}
				
			
			/*
			 * login
			 */
			if (logger.isTraceEnabled())
				logger.trace(String.format("logging in with user %s", this.connection.getUsername()));
						
			boolean success = false;
			if (this.connection.getAccount() != null)
				success = this.client.login(this.connection.getUsername(), this.connection.getPassword(), this.connection.getAccount());
			else
				success = this.client.login(this.connection.getUsername(), this.connection.getPassword());
			
			if (!success)
				throw new ConnectException(String.format("unable to login as user %s for reason %s", this.connection.getUsername(), this.getReply()));
			
			/*
			 * set file type
			 */
			if (this.connection.getFileType() != null) {
				int type = -1;
				switch (this.connection.getFileType()) {
					case ASCII:
						type = FTP.ASCII_FILE_TYPE;
						break;
					case EBCDIC:
						type = FTP.EBCDIC_FILE_TYPE;
						break;
					case BINARY:
						type = FTP.BINARY_FILE_TYPE;
						break;
					case LOCAL:
						type = FTP.LOCAL_FILE_TYPE;
						break;
				}				
				this.client.setFileType(type);
			}
			this.connected = true;
			return true;
		} catch (ConnectException e) {
			logger.warn(String.format("unable to connect %s: %s", connectionSpec, e.getMessage()));

			if (logger.isTraceEnabled())
				logger.trace(String.format("cause of connection problem for %s is %s", connectionSpec, e.getMessage()), e);

			return false;
		} catch (SocketTimeoutException e) {
			logger.warn(
				String.format("connection to %s timed out (connection timeout %d): %s"
					, connectionSpec
					, this.client.getConnectTimeout()
					, e.getMessage()));

			if (logger.isTraceEnabled())
				logger.trace(String.format("cause of connection problem for %s is %s", connectionSpec, e.getMessage()), e);

			return false;			
		} catch (Exception e) {
			logger.warn(String.format("error while connecting %s: %s", connectionSpec, e.getMessage()), e);
			return false;
		} 
	}
	
	public String getConnectionInfo() {
		return String.format(" %s/%s@%s:%d"
				, (this.connection.getUsername() != null ? this.connection.getUsername() : "<unspecified>")
				, (this.connection.getPassword() != null ? this.connection.getPassword().replaceAll(".", "*") : "<unspecified>")
				, this.connection.getHost()
				, this.connection.getPort());
	}
	
	public boolean disconnect() {
		String connectionSpec = this.getConnectionInfo();
		
		try {
			if (!this.client.isConnected())
				return true;
	
			this.client.logout();
			
			this.client.disconnect();
			
			return true;
		} catch (SocketException e) {
			logger.warn(
					String.format("unable to disconnect %s: %s", connectionSpec, e.getMessage()));
				return false;
		} catch (Exception e) {
			logger.warn(
				String.format("error while disconnecting %s: %s", connectionSpec, e.getMessage()), e);
			return false;
		} finally {
			this.connected = false;
		}
		
	}

	/**
	 * Gets back the reply code of the last command processes formatted as string
	 * 
	 * @return
	 *   - The reply code of the last command processes formatted as string
	 *   
   * @throws IOException 
   *   - thrown if any I/O error occurred.
	 */
	public int getReplyCode() throws IOException {
		return this.client.getReply();
	}
	
	/**
	 * Gets back the reply code of the last command processes formatted as string
	 * 
	 * @return
	 *   - The reply code of the last command processes formatted as string
	 */
	public String getReply() {
		try {
			int replyCode = this.client.getReplyCode();
			
			String data = this.client.getReplyString();
			
			return String.format("reply code %d: %s", replyCode, data);
		} catch (Exception e) {
			return String.format("unable to get reply from server %s :: %s", e.getMessage(), ExceptionUtils.getFullStackTrace(e));
		}
	}
	
	
	/**
   * Checks if the directory <code>directoryPath</code> exists by changing into the directory
   * 
   * <p>
   * <b>Note</b> that this command tries to change into the given working directory
   * </p>
   * 
   * @param directoryPath
   *   - The path to the directiory
   * @return 
   *   - <code>true</code> if exists, <code>false</code> otherwise
   *   
   * @throws IOException 
   *   - thrown if any I/O error occurred.
   */
	public boolean checkDirectoryExists(String directoryPath) throws IOException {
		
		int returnCode = this.client.cwd(directoryPath);
		
		if (returnCode == REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE)
			return false;
		
		return true;
	}
  
	/**
   * Checks whether a file exists or not by trying to open a file stream to it
   * 
   * @param filePath
   *   - The path to the file to check
   * @return 
   *   - <code>true</code> if exists, <code>false</code> otherwise
   *   
   * @throws IOException 
   *   - thrown if any I/O error occurred.
   */
	public boolean checkFileExists(String filePath) throws IOException {
		try {
			Pattern pattern = Pattern.compile(filePath);
			List<FTPFile> files = this.listFiles(".", pattern);
			boolean fileExists = ((files != null) && (files.size() > 0));
			logger.debug("Target file "+filePath+" exists: "+fileExists);
			return fileExists;
		} catch (Exception e) {
			throw new IOException("Could not verify the existence of "+filePath, e);
		}
	}

	/**
	 * 
	 * @param pathname
	 * @return
	 */
	public boolean changeWorkingDirectory(String pathname) {
		try {			
			boolean success = this.client.changeWorkingDirectory(pathname);
			
			if (!success) 
				logger.warn(String.format("unable to change working directory to '%s': %s", pathname, getReply()));								
			
			return success;
		} catch (IOException e) {			
			logger.warn(String.format("error while changing working directory to '%s': %s", pathname, e.getMessage()), e);
			return false;
		}
	}
	
	/**
	 * delegates the call to org.apache.commons.net.ftp.FTPClient
	 * @return The pathname of the current working directory. If it cannot be obtained, returns null
	 * @throws Exception if FtpClient throws a FTPConnectionClosedException or IOException
	 * @see org.apache.commons.net.ftp.FTPClient#printWorkingDirectory
	 */
	public String printWorkingDirectory() throws Exception {
		try {
			return this.client.printWorkingDirectory();
		} catch (Exception e) {
			throw new Exception(String.format("error while printing working directory: %s", e.getMessage()), e);
		}
	}
	
	public String getWorkingDirectory() {
		try {
			return this.client.printWorkingDirectory();
		} catch (Exception e) {
			logger.warn(String.format("error while printing working directory: %s", e.getMessage()), e);
			return null;
		}
	}
	

	public List<FTPFile> listFiles(String pathname) throws Exception {
		return this.listFiles(pathname, null);
	}
	
	public List<FTPFile> listFiles(String pathname, Pattern inclusionFilter) throws Exception {
		FTPFile [] arr = null;
		try {
			if (logger.isTraceEnabled())
				logger.trace(String.format("listing files of directory %s", pathname));
			
			arr = this.client.listFiles(pathname);
		} catch (Exception e) {
			String err = String.format("error listing files in current working directory %s: %s", pathname, e.getMessage());
			logger.warn(err, e);
			
			throw new Exception(err, e);
		}
		
		return FtpConnector.filter(FtpConnector.validate(arr), inclusionFilter);
	}
	
	/**
	 * @return an ArrayList
	 * @throws Exception
	 */
	public List<FTPFile> listFiles() throws Exception {
		String workingDirectory = this.printWorkingDirectory();
		
		FTPFile [] arr = null;
		try {
			
			if (logger.isTraceEnabled())
				logger.trace(String.format("listing files of directory %s", workingDirectory));
			
			arr = this.client.listFiles();
		} catch (Exception e) {
			String err = String.format("error listing files in current working directory %s: %s", workingDirectory, e.getMessage());
			logger.warn(err, e);
			
			throw new Exception(err, e);
		}
		
		return FtpConnector.validate(arr);
	}
	
	private static List<FTPFile> validate(FTPFile [] arr) throws Exception {
		List<FTPFile> fs = new ArrayList<FTPFile>();
		
		if (arr == null)
			return fs;
		
		for (int i=0; i < arr.length; i++) {
			if (arr[i] == null)
				throw new Exception(String.format("file listing contains null element at position %d", i));
				
			fs.add(arr[i]);
		}
		
		return fs;		
	}
	
	private static List<FTPFile> filter(List<FTPFile> files, Pattern inclusionFilter) {				
		if (files.isEmpty() || (inclusionFilter == null))
			return files;
		
		List<FTPFile> fs = new ArrayList<FTPFile>();
		
		Matcher m = inclusionFilter.matcher("");
		
		for (FTPFile file : files) {
			m.reset(file.getName());
			if (m.matches())
				fs.add(file);
		}
		
		return fs;
	}
	
	public boolean completePendingCommand() {
		try {
			return this.client.completePendingCommand();				
		} catch (Exception e) {
			logger.warn(String.format("error while completing pending command: %s", e.getMessage()), e);
			return false;
		}		
	}

	public void download(String remoteFilename, File localFile) throws FileNotFoundException, IOException, Exception {//throws Exception {
		this.download(remoteFilename, null, localFile);
	}
	
	public void download(String remoteFilename, Long size, File localFile) throws FileNotFoundException, IOException, Exception {// throws Exception {
		if (logger.isTraceEnabled())
			logger.trace(String.format("download(file %s -> %s): begin", remoteFilename, localFile.getAbsolutePath()));
		
		InputStream is = null;
		FileOutputStream os = null;
		try {
			is = getDownloadStream(remoteFilename);
			os = new FileOutputStream(localFile);
			//check if file still exists
			if (is == null)
				throw new FileNotFoundException(String.format("the remote file '%s' has vanished from the server since the time that the directory's contents were listed", remoteFilename));
			
			IOUtils.copy(is, os);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
						
		if (!this.completePendingCommand())
			logger.warn(String.format("unable to complete pending command after file download of %s: %s", remoteFilename, this.getReply()));
		
		if (size != null) {
			if (localFile.length() != size.longValue())
				throw new Exception(
					String.format("size of downloaded file %d does not match expected size %d for file %s"
						, localFile.length()
						, size.longValue()
						, localFile.getAbsolutePath()));
		}
		
		if (logger.isTraceEnabled())
			logger.trace(String.format("download(file %s -> %s): completed download", remoteFilename, localFile.getAbsolutePath()));
		
	}
	
	public InputStream getDownloadStream(String remoteFilename) throws IOException {
		// create an input stream to the file content and use file output stream to write it
		InputStream is;
		if ((is = this.client.retrieveFileStream(remoteFilename)) == null)
			throw new IOException(String.format("input file stream for '%s' is null", remoteFilename));
		
		return is;
	}
	
	public void upload(String remote, String local, InputStream is) throws Exception {
		String signature = String.format("upload(file %s -> %s)", local, remote);
		
		if (logger.isTraceEnabled())
			logger.trace(String.format("%s ... begin", signature));

		try {
			this.client.storeFile(remote, is);
		} catch (Exception e) {
			throw new Exception(
				String.format("%s: error uploading file: %s"
					, signature
					, e.getMessage()), e);
		} finally {			
			IOUtils.closeQuietly(is);			
		}
		
		if (logger.isTraceEnabled())
			logger.trace(String.format("%s: completed upload", signature));
		
	}

	public boolean deleteFile(String pathname) {
		try {			
			
			if (logger.isTraceEnabled())
				logger.trace(String.format("deleting file %s", pathname));
			
			boolean success = this.client.deleteFile(pathname);
			
			if (!success) 
				logger.warn(String.format("unable to delete file '%s': %s", pathname, getReply()));								
			
			return success;
		} catch (IOException e) {			
			logger.warn(String.format("error while deleting file '%s': %s", pathname, e.getMessage()), e);
			return false;
		}		
	}

	public boolean deleteFiles(String pathname, List<FTPFile> files) {
		String [] filenames = new String[files.size()];
				
		for (int i=0,size=files.size(); i < size; i++) 
			filenames[i] = files.get(i).getName();
		
		return deleteFiles(pathname, filenames);
	}
	
	public boolean deleteFiles(String pathname, FTPFile ... files) {
		String [] filenames = new String[files.length];
				
		for (int i=0; i < files.length; i++) 
			filenames[i] = files[i].getName();
		
		return deleteFiles(pathname, filenames);
	}
	
	public boolean deleteFiles(String pathname, String ... filenames) {
		try {			
			
			if (logger.isTraceEnabled())
				logger.trace(String.format("deleting files %s from directory %s", java.util.Arrays.toString(filenames), pathname));
			
			int errors = 0;
			for (String filename : filenames) {
				String path = pathname + "/" + filename; 
				if (!this.client.deleteFile(path)) {
					errors++;
					logger.warn(String.format("unable to delete file '%s': %s", path, getReply()));
				}
					
			}
			
			if (errors != 0) 
				logger.warn(String.format("unable to delete %d files from directory %s", errors, pathname));								
			
			return (errors == 0);
		} catch (IOException e) {			
			logger.warn(String.format("error while deleting file '%s': %s", pathname, e.getMessage()), e);
			return false;
		}		
	}
	
	
	public boolean deleteDirectoryRecursively(String parentDirectory)  {
		List<String> errors = new ArrayList<String>();
		
		try {
			if (!this.checkDirectoryExists(parentDirectory))
				return true;
		} catch (Exception e) {
			logger.warn(String.format("error checking if directory %s exists: %s", parentDirectory, e.getMessage()), e);
			return false;
		}
		
		try {
			this.deleteDirectoryRecursively(parentDirectory, null, errors);			
			return (errors.isEmpty());
		} catch (Exception e) {
			logger.warn(String.format("error deleting directory %s: %s", parentDirectory, e.getMessage()), e);
			return false;
		}
	}
	
	private void deleteDirectoryRecursively(String parentDirectory, String currentDirectory, List<String> errors) throws IOException {
		
		String workingDirectory = parentDirectory;
		if (currentDirectory != null)
			workingDirectory += "/" + StringUtils.trim(currentDirectory);
		
		
		FTPFile [] children = this.client.listFiles(workingDirectory);

		if (ArrayUtils.isEmpty(children))
			return;
		
		for (FTPFile file : children) {
			
			String filename = file.getName();
			
			// skip parent directory and the directory itself
			if (filename.equals(".") || filename.equals(".."))
				continue;
							
			if (file.isDirectory()) {
				deleteDirectoryRecursively(workingDirectory, filename, errors);
			} else {
				String filePath;
				if (currentDirectory != null)
					filePath = parentDirectory + "/" + currentDirectory + "/" + filename;				
				else
					filePath = parentDirectory + "/" + filename;

				
				// delete the file
				boolean deleted = this.client.deleteFile(filePath);
				if (deleted) {
					if (logger.isTraceEnabled())
						logger.trace(String.format("deleted file %s", filePath));
				} else {
					String err = String.format("error deleting file %s: %s", filePath, this.getReply());
					logger.warn(err);
					errors.add(err);
				}
			}
		}

		// finally, remove the directory itself
		boolean removed = this.client.removeDirectory(workingDirectory);
		if (removed) {
			if (logger.isTraceEnabled())
				logger.trace(String.format("deleted directory %s", workingDirectory));
		} else {
			String err = String.format("error deleting directory %s: %s", workingDirectory, this.getReply());
			logger.warn(err);
			errors.add(err);
		}
	}


/*
 * Getters and Setters
 */
	/**
	 * the timeout for the initial connect() command
	 * @see org.apache.commons.net.SocketClient#setConnectTimeout(int)
	 */
	public void setConnectTimeout(int connectTimeout) {
		client.setConnectTimeout(connectTimeout);
	}

	/**
	 * Is used as the default socketTimeout {@link java.net.Socket#setSoTimeout(int)} by the underlying FTPClient<br>
	 * known connections using this timeout as socketTimeout
	 * <ul>
	 *     <li>FTP control connection</li>
	 * </ul>
	 * 
	 * This is not the same as connectTimeout.<br>
	 * Also please note that the FTP data connections (used for LIST,GET,PUT,...) use {@link #setDataTimout(int)} as their socketTimeout
	 * @see org.apache.commons.net.SocketClient#setDefaultTimeout(int)
	 * @see java.net.Socket#setSoTimeout(int)
	 * @see #setConnectTimeout(int)
	 */
	public void setControlSocketTimeout(int timeout) {
		client.setDefaultTimeout(timeout);
	}

	/**
	 * Is used as the default socketTimeout {@link java.net.Socket#setSoTimeout(int)} by the underlying FTPClient<br>
	 * known connections using this timeout as socketTimeout
	 * <ul>
	 *     <li>FTP Data connection</li>
	 * </ul>
	 * @see org.apache.commons.net.ftp.FTPClient#setDataTimeout(int)
	 */
	public void setDataSocketTimeout(int timeout) {
		client.setDataTimeout(timeout);
	}
}
