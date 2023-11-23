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
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.ftp.BatchFtpJob;
import com.braintribe.model.ftp.FtpConnection;
import com.braintribe.transport.ftp.enums.SourcePostProcessing;
import com.braintribe.transport.ftp.ConnectionException;

/**
 * This is an abstract implementation of batch FTP transfer operations.  
 * This Poller receives a List of {@link BatchFtpJob} and executes them. After transger optionally source files are deleted or moved to on the remote server to an archive directory.
 * configuration parameters:
 * <ul>
 *  <li>targetProcessingHook</li>
 *  <li>poolConnections </li>
 *  <li>maxRename: sets the maximum number of renaming attempts per target file</li>
 * </ul>
 * @see {@link FtpPollerTest} for for a usage example
 */
public abstract class FtpBatchTransfer<JobT extends BatchFtpJob,TargetFile> {
/*
 * abstract methods
 */

	protected boolean poolConnections = true; 
	
	/**
	 * verify local directory
	 */
	protected abstract void checkLocal() throws IOException, Exception;
	
	/**
	 * this method makes sure, that all remote folders exist, are readable and if necessary also writable
	 */
	protected abstract void checkRemote() throws IOException, Exception;

	/**
	 * this function assumes that the remote connection is already in the correct directory and then starts the transfer from there
	 */
	protected abstract List<TargetFile> doTransfer() throws IOException, Exception;
	
	
/*
 * implementation
 */
	public static String DEFAULT_SOURCE_ARCHIVE_DIRECTORY = "archive";
	public static String FILE_EXTENSION_SEPARATOR = ".";

	protected static Logger logger = Logger.getLogger(FtpBatchTransfer.class);

	/**
	 * if set to <code>false</code> every exception stops the complete process.<br>
	 * if <code>false</code> failed jobs will be added to the failedJobs List and can be retrieved via getFailedJobs.
	 */
	protected boolean continueOnError = false;
	
	/** the default timeout for all concerns (connection, controlSocketTimeout, dataSocketTimeout) can be overriden*/
	protected int defaultTimeout = 300000;
	/** if set overrides this.defaultTimeout as the socketTimeout for the control connection*/
	protected int controlSocketTimeout = 300000;
	/** if set overrides this.defaultTimeout as the socketTimeout for the data connections*/
	protected int dataSocketTimeout = 300000;
	
	protected TargetProcessingHook<TargetFile> tpp;
	protected long maxRename = Long.MAX_VALUE; //16 bit

	private Map<FtpConnection, List<JobT>> jobMap = new LinkedHashMap<FtpConnection, List<JobT>>();

	private Iterator<FtpConnection> connectionIterator;

	protected FtpConnection currentConnection;
	protected Iterator<JobT> jobIterator;
	protected JobT currentJob;
	protected FtpConnector currentFtp;
	
	List<JobT> failedJobs;
	List<JobT> failedConnections;

	/**
	 * deletes all jobs
	 */
	public void init() {
		this.jobMap = new LinkedHashMap<FtpConnection, List<JobT>>();
		this.failedJobs = new LinkedList<JobT>();
		this.resetIteration();
	}
	
	/**
	 * @note only protected for expandability
	 * @param job JOB_TYPE
	 * @throws Exception if validation fails
	 * @throws NullPointerException if FtpPoller or FtpConnection are null
	 */
	protected void validate(JobT job) throws NullPointerException, Exception {
		if (job == null)
			throw new NullPointerException("a supplied job is null");
		logger.debug("validating job '" + job.getName() +"'");
		
		FtpConnection con = job.getConnection();
		if (con == null)
			throw new NullPointerException(String.format("the connection supplied for job.id '%s' is null", job.getId()));
		
		FtpConnector.validate(con);

		if (job.getLocalPath() == null)
			throw new NullPointerException(String.format("no local directory set for job '%s'", job.getName()));
		
		if (job.getRemotePath() == null)
			throw new Exception(String.format("no remote directoy set for job '%s'", job.getId()));
		if (job.getRemotePath().equals(""))
			throw new Exception(String.format("remote directoy for job '%s' is empty", job.getId()));
		
		if (job.getSourcePostProcessing() == null)
			throw new NullPointerException(String.format("sourcePostProcessing not set for job '%s'", job.getName()));
		
		SourcePostProcessing pp = job.getSourcePostProcessing();
		if (SourcePostProcessing.move == pp)
			if (job.getSourceArchivePath() == null || "".equals(job.getSourceArchivePath()))
				throw new Exception(String.format("no remote archive directory set to move remote files after download"));
		
		if (job.getTargetConflictHandling() == null)
			throw new NullPointerException(String.format("targetConflictHandling not set for job '%s'", job.getName()));
	}
	
	/**
	 * resets the iteration order <br/>

	 * for each {@link FtpConnection}
	 * <ul>
	 * 	<li>connect to the remote FTP server
	 * 	<li>then for each {@link JobT}
	 *  	<ul>
	 * 		<li>traverse to client directory and look for new files<l/i>
	 * 		<li>download all files to local directory (directories are ignored)</li>
	 * 		<li>optionally: delete or move remote files to a sub folder after download is completed</li>
	 * 		</ul>
	 *  </li>
	 * </ul>
	 * @return a List of pointers to all transferred filed in their target format (most probably java.util.File or org.apache.commons.net.ftp.FTPFile)
	 * @see {@link #setContinueOnError(boolean)}
	 * @throws Exception
	 * @throws ConnectionException if a connection to a server cannot be established
	 * @throws IOException if the local download directory is missing and cannot be created
	 */
	public List<TargetFile> executeAll() throws Exception {
		this.resetIteration();
		logger.debug(String.format("starting execution"));
		long start = new Date().getTime();

		List<TargetFile> ret = new LinkedList<TargetFile>();
		while (this.hasNext()) {
			try {
				this.next();
				List<TargetFile> tmp = this.executeCurrentJob();
				if (tmp != null)
					ret.addAll( tmp );
			} catch (Exception e) {
				if (this.continueOnError)
					logger.error(e.getMessage());
				else
					throw e;
			}
		}
		
		//disconnect after last job
		this.disconnectCurrentConnection();
		logger.debug(String.format("ftp polling execution finished. total runtime %d seconds", (new Date().getTime() - start) / 1000) );
		return ret;
	}

	/**
	 * @return null if an error occurred and continueOnError is set <code>true</code>
	 * @throws Exception
	 */
	public List<TargetFile> executeCurrentJob() throws Exception {
		logger.debug(String.format("starting execution of job '%s'", this.currentJob.getId()));
		try {
			if (logger.isTraceEnabled())
				logger.trace(String.format("switching to root directory '/'"));
			this.currentFtp.changeWorkingDirectory("/");

			//check if local and remote folders exist and, if necessary, are writable  
			if (logger.isTraceEnabled())
				logger.trace(String.format("checking local and remote folders"));
			this.checkLocal();  //abstract function
			this.checkRemote(); //abstract function
			
			//switch back to root (in case of relative paths
			this.currentFtp.changeWorkingDirectory("/");
			
			if (logger.isTraceEnabled())
				logger.trace(String.format("switching to remote directory '%s'", this.currentJob.getRemotePath()));
			if (!this.currentFtp.changeWorkingDirectory(this.currentJob.getRemotePath()))
				throw new Exception(String.format("could not change to directory '%s'", this.currentJob.getRemotePath()));
			
			//execute the ftp transfer for this job
			logger.debug("starting transfer");
			List<TargetFile> transfered = this.doTransfer(); //abstract function
			
			//if i am the last job on this connection, try to disconnect from the FTP
			if (!this.hasNextJobOnConnection())
				this.disconnectCurrentConnection();
			return transfered;
		} catch (Exception e) {
			this.addFailedJob(this.currentJob);
			if (this.continueOnError) {
				logger.error(String.format("job with id '%s':'%s' failed with %s: %s", this.currentJob.getId(), this.currentJob.getName(), e.getClass().toString(),e.getMessage()),e);
				return null;
			} else {
				throw e;
			}
		}
	}

	private void connectCurrentConnection() throws ConnectionException {
		this.currentFtp = this.getConnector();
	}
	
	/**
	 * establish connection to server
	 * @throws ConnectionException if the connection to the server fails
	 */
	protected FtpConnector getConnector() throws ConnectionException {
		FtpConnector newFtp = null;
		int i=0;
		do {
			newFtp = new FtpConnector(this.currentConnection);
			if (this.defaultTimeout > 0) {
				//set connection timeout
				newFtp.setConnectTimeout(this.defaultTimeout);
				newFtp.setControlSocketTimeout(this.defaultTimeout);
				newFtp.setDataSocketTimeout(this.defaultTimeout);
				
				//set control socket timeout
				if (this.controlSocketTimeout > 0)
					newFtp.setControlSocketTimeout(this.controlSocketTimeout);
				
				//set data socket timeout
				if (this.dataSocketTimeout > 0)
					newFtp.setDataSocketTimeout(this.dataSocketTimeout);
			}
			
			logger.debug(String.format("connecting to ftp server %s@%s:%s", this.currentConnection.getUsername(), this.currentConnection.getHost(), this.currentConnection.getPort()));
			boolean connected = newFtp.connect();
			
			// trace log connection status
			if (logger.isTraceEnabled()) {
				try {
					logger.trace("Connected: "+connected);
					logger.trace(String.format("ftp server status message: %s", newFtp.getClient().getStatus()));
				} catch (Throwable e) {/* ignore */}
			}
			
			if (connected)
				return newFtp;
		} while (++i <= this.currentConnection.getMaxRetries());

		throw new ConnectionException(this.currentConnection,String.format("could not establish connection.id '%s' to '%s', after %d tries the last reply was '%s'.", this.currentConnection.getId(), this.currentConnection.getHost(), i, newFtp.getReply()));
	}
	
	private void disconnectCurrentConnection() {
		if (this.currentFtp == null)
			return; 
		this.currentFtp.disconnect();
	}
	
	public void close() {
		this.disconnectCurrentConnection();
		this.init();
	}

/*
 * Generalized Methods
 */
	
	protected void checkLocal(String path, boolean needWriteAccess) throws Exception {
		if (path == null)
			throw new NullPointerException(String.format("local directory is not set for job '%s'", this.currentJob.getName()));
		File localDir = new File( path );
		
		if (localDir.exists()) {
			if (needWriteAccess && !localDir.canWrite())
				throw new Exception(String.format("cannot write to local directory '%s' for job '%s'",localDir.getAbsolutePath(), this.currentJob.getName()));
		} else {
			if (localDir.getParentFile() == null)
				throw new FileNotFoundException(String.format("the parent directory of the local directory '%s' does not exist for job '%s'", localDir.getAbsolutePath(), this.currentJob.getName()));
			// local directory does not exist => create download directory
			if (!localDir.mkdirs())
				throw new Exception(String.format("local directory '%s' does not exist and cannot be created for job '%s'", localDir.getAbsolutePath(),  this.currentJob.getName()));
		}
	}
	
	protected boolean existsRemoteFolder(String pathname) throws IOException {
		return this.currentFtp.checkDirectoryExists(pathname);
	}
	
	protected boolean createRemoteFolder(String pathname) throws IOException, Exception {
		logger.debug(String.format("creating remote directory '%s'", pathname));
		return this.currentFtp.getClient().makeDirectory(pathname);
	}
	
	
/*
 * Iteration control
 */
	/**
	 * Moves to the next job and returns a reference. After this call the returned job can be executed via the @see {@link #executeCurrentJob()} function.<br />
	 * If continueOnError is true this function still throws an {@link ConnectionException} if the last connection in the iteration order fails   
	 * @return a reference to next job in the iteration order
	 * @see {@link #executeCurrentJob()}
	 * @see {@link #resetIteration()}
	 * @see {@link #hasNext()}
	 * @note acts as like an iterator implementation as well as control function for the FTP connection.<br />
	 * @throws NoSuchElementException if there are no more elements in the iteration order
	 * @throws ConnectionException if continueOnError is false and FTP connection fails and, or if the last FTP connection fails
	 */
	public JobT next() throws NoSuchElementException, ConnectionException {
		if (!hasNext())
			throw new NoSuchElementException("this " + FtpBatchTransfer.class + "  has no more jobs left");
		
		if (hasNextJobOnConnection()) {

			if (!this.poolConnections) {
				this.disconnectCurrentConnection();
				this.connectCurrentConnection();
			}
			
		} else {
			
			if (this.currentFtp != null && this.currentFtp.isConnected()) {
				disconnectCurrentConnection();
			}
			
			this.nextConnection();
			try {
				this.connectCurrentConnection();
			} catch (ConnectionException e) {
				List<JobT> jobsOnServer = this.jobMap.get(this.currentConnection);
				this.failedJobs.addAll( jobsOnServer );

				this.currentConnection = null;

				if (this.continueOnError) {
					if (!this.hasNextConnection())
						//throw ConnectionException if there are no more connections left
						throw e;
					//if there are still connections left, log and continue
					logger.error(String.format("could not establish connection.id '%s' to '%s', reply was '%s'. %d jobs failed.", this.currentConnection.getId(), this.currentConnection.getHost(), this.currentFtp.getReply(), jobsOnServer.size()));
					this.currentConnection = null;
					this.next();
				} else {
					throw e;
				}
			}
		}

		return this.nextJobOnConnection();
	}
	
	private boolean hasNextConnection() {
		if (this.connectionIterator == null)
			this.connectionIterator = this.jobMap.keySet().iterator();
		return this.connectionIterator.hasNext();
	}
	
	private FtpConnection nextConnection() {
		this.currentConnection = this.connectionIterator.next();
		this.jobIterator = this.jobMap.get(this.currentConnection).iterator();
		this.currentFtp = null;
		this.currentJob = null;
		return this.currentConnection;
	}

	public FtpConnection getCurrentConnection() {
		return this.currentConnection;
	}
	
	private boolean hasNextJobOnConnection() {
		if (this.currentConnection == null)
			return false;
		if (this.jobIterator == null)
			this.jobIterator = this.jobMap.get(this.currentConnection).iterator();
		return this.jobIterator.hasNext();
	}
	
	private JobT nextJobOnConnection() {
		this.currentJob = this.jobIterator.next();
		return this.currentJob;
	}
	
	/**
	 * automatically resets the iteration after the last element has been reached (<code>false</code> has been returned)
	 * @return true if there is another Job in the execution order
	 * @see {@link #resetIteration()}
	 */
	public boolean hasNext() {
		//if no job left on current connection, return if another connection exists (it must have at least one job @see addJob())
		boolean ret = this.hasNextJobOnConnection() || hasNextConnection();
		if (!ret)
			resetIteration();
		return ret; 
	}
	
	/**
	 * resets the iteration so that it will start again on the next call of the next() function
	 * @see {@link #next()}
	 * @see {@link #hasNext()}
	 */
	public void resetIteration() {
		this.currentConnection = null;
		this.connectionIterator = null;
		this.jobIterator = null;
		this.currentJob = null;
	}
	
	
/*
 * Getters and Setters
 */
	
	public void addJob(JobT job) throws Exception {
		try {
			validate(job);
		} catch (Exception e) {
			logger.error(String.format("job '%s' could not be validated because of '%s': '%s'",job.getName(), e.getClass(), e.getMessage()));
			this.addFailedJob(job);
			if (!this.continueOnError)
				throw e;
		}
		
		FtpConnection con = job.getConnection();
		List<JobT> list;
		if (this.jobMap.containsKey(con) == false) {
			list = new LinkedList<JobT>();
			this.jobMap.put(con,list);
		} else
			list = this.jobMap.get(con);
		list.add(job);
	}

	/**
	 * @note also calls {@link #init()} function
	 * @param jobs
	 * @throws Exception if a job validation throws an error
	 */
	public void setJobs(Collection<? extends JobT> jobs) throws Exception {
		this.init();
		if (jobs != null) {
			for (JobT job : jobs) {
				this.addJob(job);
			}
		}
	}

	public Collection<? extends JobT> getJobs() {
		List<JobT> ret = new LinkedList<JobT>();
		for (FtpConnection key : this.jobMap.keySet())
			for (JobT job : this.jobMap.get(key))
				ret.add(job);
		return ret;
	}
	
	void addFailedJob(JobT arg) {
		if (this.failedJobs == null)
			this.failedJobs = new LinkedList<JobT>();
		this.failedJobs.add(arg);
	}

	/**
	 * @return a list of failed jobs in the order they occured
	 */
	public List<JobT> getFailedJobs() {
		return this.failedJobs;
	}

	public boolean isContinueOnError() {
		return this.continueOnError;
	}

	/**
	 * if set to true the poller will try to recover during errors.</br>
	 * if it can, exceptions will be logged and failed jobs will be added to the failed jobs list
	 * @param continueOnException
	 * @see {@link #getFailedJobs()}
	 */
	public void setContinueOnError(boolean arg) {
		this.continueOnError = arg;
	}

	public void setMaxRename(long arg) throws Exception {
		if (arg < 0)
			throw new Exception("cannot set maximum renaming iterations lower than zero");
		this.maxRename = arg;
	}
	
	public TargetProcessingHook<TargetFile> getTargetProcessingHook() {
		return this.tpp;
	}
	
	public void setTargetProcessingHook(TargetProcessingHook<TargetFile> arg) throws Exception {
		this.tpp = arg;
	}

	public boolean isPoolConnections() {
		return this.poolConnections;
	}
	@Configurable
	public void setPoolConnections(boolean poolConnections) {
		this.poolConnections = poolConnections;
	}

	public int getDefaultTimeout() {
		return this.defaultTimeout;
	}

	/**
	 * Sets the default timeout value in Milliseconds. This value will always be used as the timeout
	 * for the initial connect to a server 
	 * can be overridden by {@link #setControlSocketTimeout()} and {@link #setDataSocketTimeout()}
	 * @see org.apache.commons.net.SocketClient#setConnectTimeout(int)
	 */
	public void setDefaultTimeout(int arg) throws Exception {
		if (arg < 0)
			throw new Exception("Default timeout has to be > 0");
		this.defaultTimeout = arg;
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
	public void setControlSocketTimeout(int arg) throws Exception {
		if (arg < 0)
			throw new Exception("Control socket timeout has to be > 0");
		this.controlSocketTimeout = arg;
	}
	
	/**
	 * Is used as the default socketTimeout {@link java.net.Socket#setSoTimeout(int)} by the underlying FTPClient<br>
	 * known connections using this timeout as socketTimeout
	 * <ul>
	 *     <li>FTP Data connection</li>
	 * </ul>
	 * @see org.apache.commons.net.ftp.FTPClient#setDataTimeout(int)
	 */
	public void setDataSocketTimeout(int arg) throws Exception {
		if (arg < 0)
			throw new Exception("Data Socket timeout has to be > 0");
		this.dataSocketTimeout = arg;
	}
}
