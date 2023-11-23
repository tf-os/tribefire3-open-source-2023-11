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
package com.braintribe.model.processing.wopi.app;

/**
 * 
 * Get Web App Header information - Helper enum; check for details in
 * <a href= "http://wopi.readthedocs.io/projects/wopirest/en/latest">WOPI REST documentation</a>
 * 
 */
public enum WopiHeader {

	// The following HTTP headers MAY be included with all WOPI requests.

	/**
	 * A <b>string</b> specifying the version of the WOPI client. There is no standard for how this <b>string</b> is to
	 * be formatted. This <b>string</b> MUST NOT be used for anything other than logging.
	 */
	ClientVersion("X-WOPI-ClientVersion"),

	/**
	 * A <b>string</b> indicating the name of the machine making the call, which MUST NOT be used for anything other
	 * than logging.
	 */
	ClientMachineName("X-WOPI-MachineName"),

	/**
	 * A <b>Boolean</b> value that indicates that the WOPI client has requested the WOPI server to return a value for
	 * X-WOPI-PerfTrace.
	 */
	PerfTraceRequested("X-WOPI-PerfTraceRequested"),

	/**
	 * A <b>string</b> that the WOPI server MAY use when logging server activity to correlate that activity with WOPI
	 * client activity.
	 */
	CorrelationID("X-WOPI-CorrelationID"),

	/**
	 * A restricted scenario is a case where a user is able to operate on a file in a limited way. For example, a user
	 * might be allowed to change a file in the course of filling out a form while not having permission to freely edit
	 * the file. The value of this header varies depending on the scenario. The value of this header is determined
	 * through convention understood by the client and server implementer.<br>
	 * The header MUST be present and the value must be correct in cases where the WOPI action represents a restricted
	 * scenario.
	 */
	UsingRestrictedScenario("X-WOPI-UsingRestrictedScenario"),

	/**
	 * A set of data signed using a SHA256 (A 256 bit SHA-2-encoded [FIPS180-2]) encryption algorithm. The value of
	 * X-WOPI-Proof is decrypted using the value of <b>oldvalue</b> in <b>ct_wopi-proof-key</b> in Discovery as the
	 * public key. <br>
	 * The value of X-WOPI-Proof MUST match the following pattern.<br>
	 * 4 bytes in network byte order representing the length of the <b>&lt;token&gt;</b> as an <b>integer</b> + the
	 * <b>&lt;token&gt;</b> represented in UTF-8 [UNICODE] +<br>
	 * 4 bytes in network byte order representing the length of the URL of the WOPI request as an <b>integer</b> + the
	 * absolute URL of the WOPI request in uppercase + <br>
	 * 4 bytes in network byte order representing the length of X-WOPI-TimeStamp (see this section) + the value of
	 * X-WOPI-TimeStamp.<br>
	 * The intent of passing this header is to allow the WOPI server to validate that the WOPI request originated from
	 * the WOPI client that provided the public key in Discovery via ct_wopi-proof-key.
	 */
	Proof("X-WOPI-Proof"),

	/**
	 * @see #Proof
	 */
	ProofOld("X-WOPI-ProofOld"),

	/**
	 * A 64-bit <b>integer</b> that represents the number of 100-nanosecond intervals that have elapsed between 12:00:00
	 * midnight, January 1, 0001 and the time of the request. The WOPI client MUST include this HTTP header if it
	 * includes X-WOPI-Proof or X-WOPI-ProofOld.
	 */
	TimeStamp("X-WOPI-TimeStamp"),

	// The following HTTP headers can be included with all WOPI responses.

	/**
	 * A <b>string</b> specifying the version of the WOPI server and MUST be included with all WOPI responses. There is
	 * no standard for how this <b>string</b> is to be formatted. This <b>string</b> MUST NOT be used for anything other
	 * than logging.
	 */
	ServerVersion("X-WOPI-ServerVersion"),

	/**
	 * A string specifying the name of the WOPI server and MUST be included with all WOPI responses, which MUST NOT be
	 * used for anything other than logging.
	 */
	ServerMachineName("X-WOPI-MachineName"),

	/**
	 * A <b>string</b> that the WOPI client MAY use to track performance data. It is included in a WOPI response if the
	 * header X-WOPI-PerfTraceRequest in the request is present and equal to "true".
	 */
	PerfTrace("X-WOPI-PerfTrace"),

	/**
	 * A <b>string</b> indicating that an error occurred while processing the WOPI request, which is included in a WOPI
	 * response if the status code is 500. This <b>string</b> MAY include details about the error, and MUST NOT be used
	 * for anything other than logging.
	 */
	ServerError("X-WOPI-ServerError"),

	// The following HTTP headers are included with special WOPI requests.

	/**
	 * The value of the <sc> URI parameter.
	 */
	SessionContext("X-WOPI-SessionContext"),

	/**
	 * A <b>string</b> specifying the requested operation from the WOPI server.
	 */
	Override("X-WOPI-Override"),

	/**
	 * A <b>string</b> specifying either a file extension or a full file name. <br>
	 * If only the extension is provided, the name of the initial file without extension SHOULD be combined with the
	 * extension to create the proposed name.<br>
	 * The WOPI server MUST modify the proposed name as needed to create a new file that is both legally named and does
	 * not overwrite any existing file, while preserving the file extension.<br>
	 * This header MUST be present if X-WOPI-RelativeTarget is not present.
	 */
	SuggestedTarget("X-WOPI-SuggestedTarget"),

	/**
	 * A <b>string</b> that specifies a file name. The WOPI server MUST NOT modify the name to fulfill the request.
	 */
	RelativeTarget("X-WOPI-RelativeTarget"),

	/**
	 * A <b>boolean</b> value that specifies whether the host MUST overwrite the file name if it exists.
	 */
	OverwriteRelativeTarget("X-WOPI-OverwriteRelativeTarget"),

	/**
	 * An <b>integer</b> specifying the size of the request body.
	 */
	Size("X-WOPI-Size"),

	/**
	 * A <b>string</b> provided by the WOPI client that the WOPI server MUST use to identify the lock on the file.
	 */
	Lock("X-WOPI-Lock"),

	/* TBD */
	LockFailureReason("X-WOPI-LockFailureReason"),

	/**
	 * A <b>string</b> previously provided by the WOPI client that the WOPI server MUST have used to identify the lock
	 * on the file.
	 */
	OldLock("X-WOPI-OldLock"),

	/**
	 * [MS-SSWPS] describes how a WOPI client obtains the application ID required to access a specific secure store.
	 * This value is equal to the <b>appId</b> element of the <b>application</b> element nested under the
	 * <b>CreateApplication</b> element.
	 */
	ApplicationId("X-WOPI-ApplicationId"),

	/**
	 * A <b>string</b> specifying the type of restricted link being requested by the WOPI client. The valid values of
	 * this <b>string</b> are determined through convention. The only valid value currently is "FORMS".
	 */
	RestrictedLink("X-WOPI-RestrictedLink"),

	/**
	 * A <b>string</b> specifying the URL that the WOPI client requested. For example, the URL that allows the user to
	 * fill out a form.
	 */
	RestrictedUseLink("X-WOPI-RestrictedUseLink"),

	/**
	 * An <b>integer</b> specifying the upper bound of the expected size of the file being requested. Optional. The WOPI
	 * server will use the maximum value of a 4-byte <b>integer</b> if this value is not set in the request.
	 */
	MaxExpectedSize("X-WOPI-MaxExpectedSize"),

	/**
	 * An optional <b>string</b> value indicating the version of the file. Its value should be the same as Version value
	 * in CheckFileInfo
	 */
	ItemVersion("X-WOPI-ItemVersion");

	// stores the key value of header
	private final String key;

	// constructor with key value of header
	private WopiHeader(final String key) {
		this.key = key;
	}

	/**
	 * @return the key value of the header
	 */
	public String key() {
		return key;
	}

	@Override
	public String toString() {
		return name() + "=" + key();
	}

}