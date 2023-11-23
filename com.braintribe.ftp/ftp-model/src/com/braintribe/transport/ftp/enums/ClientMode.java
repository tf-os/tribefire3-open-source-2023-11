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
package com.braintribe.transport.ftp.enums;


/**
 *
 */
public enum ClientMode {
	/**
	 * A constant indicating the FTP session is expecting all transfers
	 * to occur between the client (local) and server and that the server
	 * should connect to the client's data port to initiate a data transfer.
	 * This is the default data connection mode when and FTPClient instance
	 * is created.
	 **/
	ACTIVE_LOCAL,
	
	/**
	 * A constant indicating the FTP session is expecting all transfers
	 * to occur between two remote servers and that the server
	 * the client is connected to should connect to the other server's
	 * data port to initiate a data transfer.
	 **/
	ACTIVE_REMOTE,
	
	/**
	 * A constant indicating the FTP session is expecting all transfers
	 * to occur between the client (local) and server and that the server
	 * is in passive mode, requiring the client to connect to the
	 * server's data port to initiate a transfer.
	 **/
	PASSIVE_LOCAL,

	/**
	 * A constant indicating the FTP session is expecting all transfers
	 * to occur between two remote servers and that the server
	 * the client is connected to is in passive mode, requiring the other
	 * server to connect to the first server's data port to initiate a data
	 * transfer.
	 **/
	PASSIVE_REMOTE,
}