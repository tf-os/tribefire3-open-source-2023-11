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
package com.braintribe.model.processing.wopi.model;

import com.braintribe.model.processing.wopi.misc.HttpResponseJSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Return information about the file and permissions that the current user has relative to that file.
 * 
 * @see <a href= "https://wopi.readthedocs.io/projects/wopirest/en/latest/files/CheckFileInfo.html">WOPI
 *      CheckFileInfo</a>
 * 
 * 
 */
@JsonInclude(Include.NON_DEFAULT)
public class CheckFileInfo extends HttpResponseJSON {

	/**
	 * A Boolean value that indicates the WOPI client MAY allow connections to external services referenced in the file
	 * (for example, a marketplace of embeddable JavaScript apps). If this value is false, then the WOPI client MUST NOT
	 * allow such connections.
	 */
	public Boolean AllowExternalMarketplace;

	/**
	 * The name of the file without the path. Used for display in user interface (UI), and determining the extension of
	 * the file.
	 */
	public String BaseFileName;

	/**
	 * A string that the WOPI client MAY display to the user that indicates the brand name of the WOPI server.
	 */
	public String BreadcrumbBrandName;

	/**
	 * A URI to a web page that the WOPI client MAY navigate to when the user clicks on UI that displays
	 * BreadcrumbBrandName.
	 */
	public String BreadcrumbBrandUrl;

	/**
	 * A string that the WOPI client MAY display to the user that indicates the name of the file.
	 */
	public String BreadcrumbDocName;

	/**
	 * A URI to a web page that the WOPI client MAY navigate to when the user clicks on UI that displays
	 * BreadcrumbDocName.
	 */
	public String BreadcrumbDocUrl;

	/**
	 * A string that the WOPI client MAY display to the user that indicates the name of the folder that contains the
	 * file.
	 */
	public String BreadcrumbFolderName;

	/**
	 * A URI to a web page that the WOPI client MAY navigate to when the user clicks on UI that displays
	 * BreadcrumbFolderName.
	 */
	public String BreadcrumbFolderUrl;

	/**
	 * A user-accessible URI directly to the file intended for opening the file through a client. Can be a DAV URL
	 * ([RFC5323]), but MAY be any URL that can be handled by a client that can open a file of the given type.
	 */
	public String ClientUrl;

	/**
	 * A Boolean value that indicates that the WOPI client SHOULD close the browser window containing the output of the
	 * WOPI client when the user calls the close UI.
	 */
	public Boolean CloseButtonClosesWindow;

	/**
	 * A URI to a web page that the implementer deems useful to a user in the event that the user closes the rendering
	 * or editing client currently using this file.
	 */
	public String CloseUrl;

	/**
	 * A Boolean value that indicates that the WOPI client MUST disable caching of file contents in the browser cache.
	 */
	public Boolean DisableBrowserCachingOfUserContent;

	/**
	 * A Boolean value that indicates that the WOPI client MUST disable any print functionality under its control.
	 */
	public Boolean DisablePrint;

	/**
	 * Boolean value that indicates that the WOPI client MUST not permit the use of machine translation functionality
	 * that is exposed by the WOPI client.
	 */
	public Boolean DisableTranslation;

	/**
	 * A user-accessible URI to the file intended to allow the user to download a copy of the file.
	 */
	public String DownloadUrl;

	/**
	 * A URI to a location that allows the user to create an embeddable URI to the file.
	 */
	public String FileEmbedCommandUrl;

	/**
	 * A string value representing the file extension for the file. This value must begin with a .. If provided, WOPI
	 * clients will use this value as the file extension. Otherwise the extension will be parsed from the BaseFileName.
	 */
	public String FileExtension;

	/**
	 * An integer value that indicates the maximum length for file names that the WOPI host supports, excluding the file
	 * extension. The default value is 250. Note that WOPI clients will use this default value if the property is
	 * omitted or if it is explicitly set to 0.
	 */
	public int FileNameMaxLength;

	/** A URI to a location that allows the user to share the file. */
	public String FileSharingUrl;

	/**
	 * A URI to the file location that the WOPI client uses to get the file. If this is provided, a WOPI client MUST use
	 * this URI to get the file instead of HTTP://server/<...>/wopi.../files/<id>/contents (see section 3.3.5.3).
	 */
	public String FileUrl;

	/**
	 * A URI to a location that allows the user to view the version history for the file.
	 */
	public String FileVersionUrl;

	/**
	 * A string that is used by the WOPI server to uniquely identify the user.
	 */
	public String HostAuthenticationId;

	/**
	 * URI to a web page that provides an editing experience for the file, utilizing the WOPI client.
	 */
	public String HostEditUrl;

	/**
	 * A URI to a web page that provides access to an editing experience for the file that can be embedded in another
	 * HTML page. For example, a page that provides an HTML snippet that can be inserted into the HTML of a blog.
	 */
	public String HostEmbeddedEditUrl;

	/**
	 * A URI to a web page that provides access to a viewing experience for the file that can be embedded in another
	 * HTML page. For example, a page that provides an HTML snippet that can be inserted into the HTML of a blog.
	 */
	public String HostEmbeddedView;

	/**
	 * A string that is the name provided by the WOPI server used to identify it for logging and other informational
	 * purposes.
	 */
	public String HostName;

	/**
	 * A string that is used by the WOPI server to pass arbitrary information to the WOPI client. The WOPI client MAY
	 * ignore this string if it does not recognize the contents. A WOPI server MUST NOT require that a WOPI client
	 * understand the contents of this string to operate.
	 */
	public String HostNotes;

	/** A URI that is the base URI for REST operations for the file. */
	public String HostRestUrl;

	/**
	 * A URI to a web page that provides a viewing experience for the file utilizing the WOPI client.
	 */
	public String HostViewUrl;

	/**
	 * A string that the WOPI client SHOULD display to the user indicating the Information Rights Management (IRM)
	 * policy for the file. This value SHOULD be combined with IrmPolicyTitle.
	 */
	public String IrmPolicyDescription;

	/**
	 * A string that the WOPI client SHOULD display to the user indicating the IRM policy for the file. This value
	 * SHOULD be combined with IrmPolicyDescription.
	 */
	public String IrmPolicyTitle;

	/** A string that SHOULD uniquely identify the owner of the file. */
	public String OwnerId;

	/**
	 * A string value indicating the domain the host page will be sending/receiving PostMessages to/from. Office for the
	 * web will only send outgoing PostMessages to this domain, and will only listen to PostMessages from this domain.
	 * 
	 * This value will be used as the targetOrigin when Office for the web uses the HTML5 Web Messaging protocol.
	 * Therefore, it must include the scheme and host name. If you are serving your pages on a non-standard port, you
	 * must include the port as well. The literal string *, while supported in the PostMessage protocol, is not allowed
	 * by Office for the web.
	 */
	public String PostMessageOrigin;

	/**
	 * A string that identifies the provider of information that a WOPI client MAY use to discover information about the
	 * user's online status (for example, whether a user is available via instant messenger). A WOPI client requires
	 * knowledge of specific presence providers to be able to take advantage of this value.
	 */
	public String PresenceProvider;

	/**
	 * A string that identifies the user in the context of the PresenceProvider.
	 */
	public String PresenceUserId;

	/**
	 * A URI to a webpage that explains the privacy policy of the WOPI server.
	 */
	public String PrivacyUrl;

	/**
	 * A Boolean value that indicates that the WOPI client SHOULD take measures to prevent copying and printing of the
	 * file. This is intended to help enforce IRM in WOPI clients.
	 */
	public Boolean ProtectInClient;

	/** Indicates that, for this user, the file cannot be changed. */
	public Boolean ReadOnly;

	/**
	 * A Boolean value that indicates that the WOPI client MUST NOT allow the user to download the file or open the file
	 * in a separate application.
	 */
	public Boolean RestrictedWebViewOnly;

	/**
	 * If it is present and not empty, it is a 256 bit SHA-2-encoded [FIPS180-2] hash of the file contents.
	 */
	public String SHA256;

	/**
	 * A URI that will sign the current user out of the WOPI server supported authentication system.
	 */
	public String SignoutUrl;

	/** The size of the file expressed in bytes. */
	public int Size;

	/**
	 * A Boolean value that indicates that the WOPI server supports multiple users making changes to this file
	 * simultaneously. It MUST be false.
	 */
	public Boolean SupportsCoauth;

	/**
	 * A Boolean value that indicates that the WOPI server supports ExecuteCellStorageRequest (see section 3.3.5.1.7)
	 * and ExcecuteCellStorageRelativeRequest (see section 3.3.5.1.8) operations for this file.
	 */
	public Boolean SupportsCobalt;

	/**
	 * A Boolean value that indicates that the host supports lock IDs up to 1024 ASCII characters long. If not provided,
	 * WOPI clients will assume that lock IDs are limited to 256 ASCII characters.
	 */
	public Boolean SupportsExtendedLockLength;

	/**
	 * A Boolean value that indicates that the WOPI server supports EnumerateChildren (see section 3.3.5.4.1) and
	 * DeleteFile (see section 3.3.5.1.9) operations for this file.
	 */
	public Boolean SupportsFolders;

	/**
	 * A Boolean value that indicates that the WOPI server supports Lock (see section 3.3.5.1.3), Unlock (see section
	 * 3.3.5.1.4), RefreshLock (see section 3.3.5.1.5), and UnlockAndRelock (see section 3.3.5.1.6) operations for this
	 * file.
	 */
	public Boolean SupportsLocks;

	/**
	 * A Boolean value that indicates that the WOPI server supports scenarios where users can operate on files in
	 * limited ways via restricted URLs.
	 */
	public Boolean SupportsScenarioLinks;

	/**
	 * A Boolean value that indicates that the WOPI server supports calls to a secure data store utilizing credentials
	 * stored in the file.
	 */
	public Boolean SupportsSecureStore;

	/**
	 * A Boolean value that indicates that the WOPI server supports PutFile (see section 3.3.5.3.2) and PutRelativeFile
	 * (see section 3.3.5.1.2) operations for this file.
	 */
	public Boolean SupportsUpdate;

	/**
	 * A string that is used by the WOPI server to uniquely identify the user.
	 */
	public String TenantId;

	/**
	 * A URI to a webpage that explains the terms of use policy of the WOPI server.
	 */
	public String TermsOfUseUrl;

	/**
	 * A string that is used to pass time zone information to a WOPI client in the format chosen by the WOPI server.
	 */
	public String TimeZone;

	/**
	 * A Boolean value that indicates that the user has permission to view a broadcast of this file. A broadcast is file
	 * activity that involves one or more presenters controlling the view of the file for a set of attendees. For
	 * example, a slideshow can be broadcast by a presenter to many attendees.
	 */
	public Boolean UserCanAttend;

	/**
	 * A Boolean value that indicates the user does not have sufficient permissions to create new files on the WOPI
	 * server.
	 */
	public Boolean UserCanNotWriteRelative;

	/**
	 * A Boolean value that indicates that the user has permission to broadcast this file to a set of users who have
	 * permission to broadcast or view a broadcast of this file. A broadcast is file activity that involves one or more
	 * presenters controlling the view of the file for a set of attendees. For example, a slideshow can be broadcast by
	 * a presenter to many attendees.
	 */
	public Boolean UserCanPresent;

	/**
	 * A Boolean value that indicates that the user has permissions to alter the file.
	 */
	public Boolean UserCanWrite;

	/**
	 * A string that is the name of the user. If blank, the WOPI client MAY be configured to use a placeholder string in
	 * some scenarios, or to show no name at all.
	 */
	public String UserFriendlyName;

	/**
	 * A string that is used by the WOPI server to uniquely identify the user.
	 */
	public String UserId;

	/**
	 * The current version of the file based on the server's file versioning schema. This value MUST change when the
	 * file changes, and version values MUST never repeat for a given file.
	 */
	public String Version;

	/**
	 * A Boolean value that indicates that the WOPI client MUST NOT allow the user to use the WOPI client�s editing
	 * functionality to operate on the file. This does not mean that the user doesn't have rights to edit the file.
	 */
	public Boolean WebEditingDisabled;

	// -------------------
	/**
	 * A Boolean value that indicates that the host supports the GetLock operation.
	 */
	public Boolean SupportsGetLock;

	/**
	 * A Boolean value that indicates that the host supports the PutUserInfo operation.
	 */
	public Boolean SupportsUserInfo;

	/**
	 * A string that represents the last time that the file was modified. This time must always be a must be a UTC time,
	 * and must be formatted in ISO 8601 round-trip format. For example, "2009-06-15T13:45:30.0000000Z".
	 */
	public String LastModifiedTime;

	/**
	 * A Boolean value that indicates that the host supports the DeleteFile operation.
	 */
	public Boolean SupportsDeleteFile;

	/**
	 * A Boolean value that indicates that the host supports the following WOPI operations: CheckContainerInfo,
	 * CreateChildContainer, CreateChildFile, DeleteContainer, DeleteFile, EnumerateAncestors (containers),
	 * EnumerateAncestors (files), EnumerateChildren (containers), GetEcosystem (containers), RenameContainer
	 */
	public Boolean SupportsContainers;

	/**
	 * An array of strings containing the Share URL types supported by the host.
	 * 
	 * These types can be passed in the X-WOPI-UrlType request header to signify which Share URL type to return for the
	 * GetShareUrl (files) operation.
	 */
	public String[] SupportedShareUrlTypes;

}
