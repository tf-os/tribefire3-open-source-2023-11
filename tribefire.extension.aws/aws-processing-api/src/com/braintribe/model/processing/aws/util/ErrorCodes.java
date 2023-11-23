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
package com.braintribe.model.processing.aws.util;

import java.util.HashMap;
import java.util.Map;

public class ErrorCodes {

	public final static String AccessDenied = "AccessDenied";
	public final static String AccessPointAlreadyOwnedByYou = "AccessPointAlreadyOwnedByYou";
	public final static String AccountProblem = "AccountProblem";
	public final static String AllAccessDisabled = "AllAccessDisabled";
	public final static String AmbiguousGrantByEmailAddress = "AmbiguousGrantByEmailAddress";
	public final static String AuthorizationHeaderMalformed = "AuthorizationHeaderMalformed";
	public final static String BadDigest = "BadDigest";
	public final static String BucketAlreadyExists = "BucketAlreadyExists";
	public final static String BucketAlreadyOwnedByYou = "BucketAlreadyOwnedByYou";
	public final static String BucketNotEmpty = "BucketNotEmpty";
	public final static String CredentialsNotSupported = "CredentialsNotSupported";
	public final static String CrossLocationLoggingProhibited = "CrossLocationLoggingProhibited";
	public final static String EntityTooSmall = "EntityTooSmall";
	public final static String EntityTooLarge = "EntityTooLarge";
	public final static String ExpiredToken = "ExpiredToken";
	public final static String IllegalLocationConstraintException = "IllegalLocationConstraintException";
	public final static String IllegalVersioningConfigurationException = "IllegalVersioningConfigurationException";
	public final static String IncompleteBody = "IncompleteBody";
	public final static String IncorrectNumberOfFilesInPostRequest = "IncorrectNumberOfFilesInPostRequest";
	public final static String InlineDataTooLarge = "InlineDataTooLarge";
	public final static String InternalError = "InternalError";
	public final static String InvalidAccessKeyId = "InvalidAccessKeyId";
	public final static String InvalidAccessPoint = "InvalidAccessPoint";
	public final static String InvalidAccessPointAliasError = "InvalidAccessPointAliasError";
	public final static String InvalidAddressingHeader = "InvalidAddressingHeader";
	public final static String InvalidArgument = "InvalidArgument";
	public final static String InvalidBucketName = "InvalidBucketName";
	public final static String InvalidBucketState = "InvalidBucketState";
	public final static String InvalidDigest = "InvalidDigest";
	public final static String InvalidEncryptionAlgorithmError = "InvalidEncryptionAlgorithmError";
	public final static String InvalidLocationConstraint = "InvalidLocationConstraint";
	public final static String InvalidObjectState = "InvalidObjectState";
	public final static String InvalidPart = "InvalidPart";
	public final static String InvalidPartOrder = "InvalidPartOrder";
	public final static String InvalidPayer = "InvalidPayer";
	public final static String InvalidPolicyDocument = "InvalidPolicyDocument";
	public final static String InvalidRange = "InvalidRange";
	public final static String InvalidRequest = "InvalidRequest";
	public final static String InvalidSecurity = "InvalidSecurity";
	public final static String InvalidSOAPRequest = "InvalidSOAPRequest";
	public final static String InvalidStorageClass = "InvalidStorageClass";
	public final static String InvalidTargetBucketForLogging = "InvalidTargetBucketForLogging";
	public final static String InvalidToken = "InvalidToken";
	public final static String InvalidURI = "InvalidURI";
	public final static String KeyTooLongError = "KeyTooLongError";
	public final static String MalformedACLError = "MalformedACLError";
	public final static String MalformedPOSTRequest = "MalformedPOSTRequest";
	public final static String MalformedXML = "MalformedXML";
	public final static String MaxMessageLengthExceeded = "MaxMessageLengthExceeded";
	public final static String MaxPostPreDataLengthExceededError = "MaxPostPreDataLengthExceededError";
	public final static String MetadataTooLarge = "MetadataTooLarge";
	public final static String MethodNotAllowed = "MethodNotAllowed";
	public final static String MissingAttachment = "MissingAttachment";
	public final static String MissingContentLength = "MissingContentLength";
	public final static String MissingRequestBodyError = "MissingRequestBodyError";
	public final static String MissingSecurityElement = "MissingSecurityElement";
	public final static String MissingSecurityHeader = "MissingSecurityHeader";
	public final static String NoLoggingStatusForKey = "NoLoggingStatusForKey";
	public final static String NoSuchBucket = "NoSuchBucket";
	public final static String NoSuchBucketPolicy = "NoSuchBucketPolicy";
	public final static String NoSuchKey = "NoSuchKey";
	public final static String NoSuchLifecycleConfiguration = "NoSuchLifecycleConfiguration";
	public final static String NoSuchTagSet = "NoSuchTagSet";
	public final static String NoSuchUpload = "NoSuchUpload";
	public final static String NoSuchVersion = "NoSuchVersion";
	public final static String NotImplemented = "NotImplemented";
	public final static String NotModified = "NotModified";
	public final static String NotSignedUp = "NotSignedUp";
	public final static String OperationAborted = "OperationAborted";
	public final static String PermanentRedirect = "PermanentRedirect";
	public final static String PreconditionFailed = "PreconditionFailed";
	public final static String Redirect = "Redirect";
	public final static String RequestHeaderSectionTooLarge = "RequestHeaderSectionTooLarge";
	public final static String RequestIsNotMultiPartContent = "RequestIsNotMultiPartContent";
	public final static String RequestTimeout = "RequestTimeout";
	public final static String RequestTimeTooSkewed = "RequestTimeTooSkewed";
	public final static String RequestTorrentOfBucketError = "RequestTorrentOfBucketError";
	public final static String RestoreAlreadyInProgress = "RestoreAlreadyInProgress";
	public final static String ServerSideEncryptionConfigurationNotFoundError = "ServerSideEncryptionConfigurationNotFoundError";
	public final static String ServiceUnavailable = "ServiceUnavailable";
	public final static String SignatureDoesNotMatch = "SignatureDoesNotMatch";
	public final static String SlowDown = "SlowDown";
	public final static String TemporaryRedirect = "TemporaryRedirect";
	public final static String TokenRefreshRequired = "TokenRefreshRequired";
	public final static String TooManyAccessPoints = "TooManyAccessPoints";
	public final static String TooManyBuckets = "TooManyBuckets";
	public final static String UnexpectedContent = "UnexpectedContent";
	public final static String UnresolvableGrantByEmailAddress = "UnresolvableGrantByEmailAddress";
	public final static String UserKeyMustBeSpecified = "UserKeyMustBeSpecified";
	public final static String NoSuchAccessPoint = "NoSuchAccessPoint";
	public final static String InvalidTag = "InvalidTag";
	public final static String MalformedPolicy = "MalformedPolicy";

	private final static Map<String, String> errorCodeDetails;

	static {
		errorCodeDetails = new HashMap<>();
		errorCodeDetails.put(AccessDenied, "Access Denied");
		errorCodeDetails.put(AccessPointAlreadyOwnedByYou, "An access point with an identical name already exists in your account.");
		errorCodeDetails.put(AccountProblem,
				"There is a problem with your Amazon Web Services account that prevents the operation from completing successfully. For further assistance, Contact Us");
		errorCodeDetails.put(AllAccessDisabled, "All access to this Amazon S3 resource has been disabled. For further assistance, Contact Us");
		errorCodeDetails.put(AmbiguousGrantByEmailAddress, "The email address you provided is associated with more than one account.");
		errorCodeDetails.put(AuthorizationHeaderMalformed, "The authorization header you provided is not valid.");
		errorCodeDetails.put(BadDigest, "The Content-MD5 you specified did not match what we received.");
		errorCodeDetails.put(BucketAlreadyExists,
				"The requested bucket name is not available. The bucket namespace is shared by all users of the system. Select a different name and try again.");
		errorCodeDetails.put(BucketAlreadyOwnedByYou,
				"The bucket you tried to create already exists, and you own it. Amazon S3 returns this error in all Amazon Web Services Regions except us-east-1 (N. Virginia). For legacy compatibility, if you re-create an existing bucket that you already own in us-east-1, Amazon S3 returns 200 OK and resets the bucket access control lists (ACLs). For Amazon S3 on Outposts, the bucket you tried to create already exists in your Outpost and you own it.");
		errorCodeDetails.put(BucketNotEmpty, "The bucket you tried to delete is not empty.");
		errorCodeDetails.put(CredentialsNotSupported, "This request does not support credentials.");
		errorCodeDetails.put(CrossLocationLoggingProhibited,
				"Cross-location logging not allowed. Buckets in one geographic location cannot log information to a bucket in another location.");
		errorCodeDetails.put(EntityTooSmall, "Your proposed upload is smaller than the minimum allowed object size.");
		errorCodeDetails.put(EntityTooLarge, "Your proposed upload exceeds the maximum allowed object size.");
		errorCodeDetails.put(ExpiredToken, "The provided token has expired.");
		errorCodeDetails.put(IllegalLocationConstraintException,
				"Indicates that you are trying to access a bucket from a different Region than where the bucket exists. To avoid this error, use the --region option. For example: aws s3 cp awsexample.txt s3://testbucket/ --region ap-east-1.");
		errorCodeDetails.put(IllegalVersioningConfigurationException,
				"Indicates that the versioning configuration specified in the request is invalid.");
		errorCodeDetails.put(IncompleteBody, "You did not provide the number of bytes specified by the Content-Length HTTP header.");
		errorCodeDetails.put(IncorrectNumberOfFilesInPostRequest, "POST requires exactly one file upload per request.");
		errorCodeDetails.put(InlineDataTooLarge, "Inline data exceeds the maximum allowed size.");
		errorCodeDetails.put(InternalError, "We encountered an internal error. Please try again.");
		errorCodeDetails.put(InvalidAccessKeyId, "The Amazon access key ID you provided does not exist in our records.");
		errorCodeDetails.put(InvalidAccessPoint, "The specified access point name or account is not valid.");
		errorCodeDetails.put(InvalidAccessPointAliasError, "The specified access point alias name is not valid.");
		errorCodeDetails.put(InvalidAddressingHeader, "You must specify the Anonymous role.");
		errorCodeDetails.put(InvalidArgument,
				"This error might occur for the following reasons:\nThe specified argument was invalid.\nThe request was missing a required header\nThe specified argument was incomplete or in the wrong format.\nMust have length greater than or equal to 3.");
		errorCodeDetails.put(InvalidBucketName, "The specified bucket is not valid.");
		errorCodeDetails.put(InvalidBucketState, "The request is not valid with the current state of the bucket.");
		errorCodeDetails.put(InvalidDigest, "The Content-MD5 you specified is not valid.");
		errorCodeDetails.put(InvalidEncryptionAlgorithmError, "The encryption request that you specified is not valid. The valid value is AES256.");
		errorCodeDetails.put(InvalidLocationConstraint,
				"The specified location constraint is not valid. For more information about Regions, see How to Select a Region for Your Buckets.");
		errorCodeDetails.put(InvalidObjectState, "The operation is not valid for the current state of the object.");
		errorCodeDetails.put(InvalidPart,
				"One or more of the specified parts could not be found. The part might not have been uploaded, or the specified entity tag might not have matched the part's entity tag.");
		errorCodeDetails.put(InvalidPartOrder, "The list of parts was not in ascending order. Parts list must be specified in order by part number.");
		errorCodeDetails.put(InvalidPayer, "All access to this object has been disabled. For further assistance, Contact Us");
		errorCodeDetails.put(InvalidPolicyDocument, "The content of the form does not meet the conditions specified in the policy document.");
		errorCodeDetails.put(InvalidRange, "The requested range cannot be satisfied.");
		errorCodeDetails.put(InvalidRequest,
				"This error might occur for the following reasons:\nUse AWS4-HMAC-SHA256. \nThe access point can only be created for existing bucket.\nThe access point is not in a state where it can be deleted.\nThe access point can only be listed for an existing bucket.\nThe next token is invalid.\nAt least one action needs to be specified in a lifecycle rule. \nAt least one lifecycle rule should be specified.\nNumber of lifecycle rules should not exceed allowed limit of 1000 rules. \nInvalid range for parameter MaxResults.\nSOAP requests must be made over an HTTPS connection. \nAmazon S3 Transfer Acceleration is not supported for buckets with non-DNS compliant names. \nAmazon S3 Transfer Acceleration is not supported for buckets with periods (.) in their names. \nAmazon S3 Transfer Accelerate endpoint only supports virtual style requests. \nAmazon S3 Transfer Accelerate is not configured on this bucket. \nAmazon S3 Transfer Accelerate is disabled on this bucket. \nAmazon S3 Transfer Acceleration is not supported on this bucket. Contact Amazon Web Services Support for more information. \nAmazon S3 Transfer Acceleration cannot be enabled on this bucket. Contact Amazon Web Services Support for more information.");
		errorCodeDetails.put(InvalidSecurity, "The provided security credentials are not valid.");
		errorCodeDetails.put(InvalidSOAPRequest, "The SOAP request body is invalid.");
		errorCodeDetails.put(InvalidStorageClass, "The storage class you specified is not valid.");
		errorCodeDetails.put(InvalidTargetBucketForLogging,
				"The target bucket for logging does not exist, is not owned by you, or does not have the appropriate grants for the log-delivery group.");
		errorCodeDetails.put(InvalidToken, "The provided token is malformed or otherwise invalid.");
		errorCodeDetails.put(InvalidURI, "Couldn't parse the specified URI.");
		errorCodeDetails.put(KeyTooLongError, "Your key is too long.");
		errorCodeDetails.put(MalformedACLError, "The XML you provided was not well formed or did not validate against our published schema.");
		errorCodeDetails.put(MalformedPOSTRequest, "The body of your POST request is not well-formed multipart/form-data.");
		errorCodeDetails.put(MalformedXML,
				"This happens when the user sends malformed XML (XML that doesn't conform to the published XSD) for the configuration. The error message is, \"The XML you provided was not well formed or did not validate against our published schema.\"");
		errorCodeDetails.put(MaxMessageLengthExceeded, "Your request was too big.");
		errorCodeDetails.put(MaxPostPreDataLengthExceededError, "Your POST request fields preceding the upload file were too large.");
		errorCodeDetails.put(MetadataTooLarge, "Your metadata headers exceed the maximum allowed metadata size.");
		errorCodeDetails.put(MethodNotAllowed, "The specified method is not allowed against this resource.");
		errorCodeDetails.put(MissingAttachment, "A SOAP attachment was expected, but none were found.");
		errorCodeDetails.put(MissingContentLength, "You must provide the Content-Length HTTP header.");
		errorCodeDetails.put(MissingRequestBodyError,
				"This happens when the user sends an empty XML document as a request. The error message is, \"Request body is empty.\"");
		errorCodeDetails.put(MissingSecurityElement, "The SOAP 1.1 request is missing a security element.");
		errorCodeDetails.put(MissingSecurityHeader, "Your request is missing a required header.");
		errorCodeDetails.put(NoLoggingStatusForKey, "There is no such thing as a logging status subresource for a key.");
		errorCodeDetails.put(NoSuchBucket, "The specified bucket does not exist.");
		errorCodeDetails.put(NoSuchBucketPolicy, "The specified bucket does not have a bucket policy.");
		errorCodeDetails.put(NoSuchKey, "The specified key does not exist.");
		errorCodeDetails.put(NoSuchLifecycleConfiguration, "The lifecycle configuration does not exist.");
		errorCodeDetails.put(NoSuchTagSet, "This error occurs when the tag specified does not exist.");
		errorCodeDetails.put(NoSuchUpload,
				"The specified multipart upload does not exist. The upload ID might be invalid, or the multipart upload might have been aborted or completed.");
		errorCodeDetails.put(NoSuchVersion, "Indicates that the version ID specified in the request does not match an existing version.");
		errorCodeDetails.put(NotImplemented, "A header you provided implies functionality that is not implemented.");
		errorCodeDetails.put(NotModified, "The resource was not changed.");
		errorCodeDetails.put(NotSignedUp,
				"Your account is not signed up for the Amazon S3 service. You must sign up before you can use Amazon S3. You can sign up at the following URL: http://www.amazonaws.cn/s3");
		errorCodeDetails.put(OperationAborted, "A conflicting conditional operation is currently in progress against this resource. Try again.");
		errorCodeDetails.put(PermanentRedirect,
				"The bucket you are attempting to access must be addressed using the specified endpoint. Send all future requests to this endpoint.");
		errorCodeDetails.put(PreconditionFailed, "At least one of the preconditions you specified did not hold.");
		errorCodeDetails.put(Redirect, "Temporary redirect.");
		errorCodeDetails.put(RequestHeaderSectionTooLarge,
				"The request header and query parameters used to make the request exceeded the maximum allowed size.");
		errorCodeDetails.put(RequestIsNotMultiPartContent, "Bucket POST must be of the enclosure-type multipart/form-data.");
		errorCodeDetails.put(RequestTimeout, "Your socket connection to the server was not read from or written to within the timeout period.");
		errorCodeDetails.put(RequestTimeTooSkewed, "The difference between the request time and the server's time is too large.");
		errorCodeDetails.put(RequestTorrentOfBucketError, "Requesting the torrent file of a bucket is not permitted.");
		errorCodeDetails.put(RestoreAlreadyInProgress, "Object restore is already in progress.");
		errorCodeDetails.put(ServerSideEncryptionConfigurationNotFoundError, "The server-side encryption configuration was not found.");
		errorCodeDetails.put(ServiceUnavailable, "Reduce your request rate.");
		errorCodeDetails.put(SignatureDoesNotMatch,
				"The request signature that we calculated does not match the signature you provided. Check your Amazon secret access key and signing method. For more information, see REST Authentication and SOAP Authentication.");
		errorCodeDetails.put(SlowDown, "Reduce your request rate.");
		errorCodeDetails.put(TemporaryRedirect, "You are being redirected to the bucket while DNS updates.");
		errorCodeDetails.put(TokenRefreshRequired, "The provided token must be refreshed.");
		errorCodeDetails.put(TooManyAccessPoints, "You have attempted to create more access point than allowed.");
		errorCodeDetails.put(TooManyBuckets, "You have attempted to create more buckets than allowed.");
		errorCodeDetails.put(UnexpectedContent, "This request does not support content.");
		errorCodeDetails.put(UnresolvableGrantByEmailAddress, "The email address you provided does not match any account on record.");
		errorCodeDetails.put(UserKeyMustBeSpecified,
				"The bucket POST must contain the specified field name. If it is specified, check the order of the fields.");
		errorCodeDetails.put(NoSuchAccessPoint, "The specified access point does not exist");
		errorCodeDetails.put(InvalidTag, "You have passed bad tag input - duplicate keys, key/values are too long, system tags were sent.");
		errorCodeDetails.put(MalformedPolicy, "You have an invalid principal in policy.");

	}

	public static boolean isClientConfigurationError(String errorCode) {
		if (errorCode == null) {
			throw new IllegalArgumentException("The error code \"null\" cannot be analyzed.");
		}
		switch (errorCode) {
			case AccountProblem:
			case AmbiguousGrantByEmailAddress:
			case IllegalLocationConstraintException:
			case InvalidAccessKeyId:
			case InvalidBucketName:
			case InvalidPayer:
			case InvalidSecurity:
			case InvalidToken:
			case KeyTooLongError:
			case NoSuchBucket:
			case NoSuchKey:
			case NotSignedUp:
			case SignatureDoesNotMatch:
				return true;
			default:
				return false;
		}
	}

	public static boolean isAccessDeniedByAcl(String errorCode) {
		if (errorCode == null) {
			throw new IllegalArgumentException("The error code \"null\" cannot be analyzed.");
		}
		switch (errorCode) {
			case AccessDenied:
			case AllAccessDisabled:
				return true;
			default:
				return false;
		}
	}

	public static String getErrorCodeDetails(String errorCode) {
		if (errorCode == null) {
			return null;
		}
		return errorCodeDetails.get(errorCode);
	}
}
