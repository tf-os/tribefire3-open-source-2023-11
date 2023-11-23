# tribefire.extension.aws

## CloudFront Integration

It is possible to access Resources stored in S3 via a CloudFront URL. There are two ways how this can be done.
Either the resources are publicly available (no authentication is necessary) or a pre-signed URL has to be used to access the resource via CloudFront.
In either case, the `CreateCloudFrontUrlForResource` service request can be used to create a streaming URL for a specific Access and Resource.
The optional flag `preSignUrl` can be used to specify whether the URL should be pre-signed or just a plain URL. If this flag is kept `NULL`, the URL will be automatically pre-signed if the CloudFront configuration is correct.
The result of this service request is a `CloudFrontUrl` entity which contains the URL and the information whether the URL is pre-signed or not.
### Public Resources

When no authentication is necssary, the URL of the Resource accessed via CloudFront just contains a base URL and the key-part of the corresponding `S3Source` (i.e., the path to the actual file on S3).

### Blocked Resources

As with URLs to public resources, pre-signed URLs also contain the same base URL and the path to the file on S3. But they also contain an expiry date and a signature to verify the validity of the URL.
`CreateCloudFrontUrlForResource` will return a pre-signed URL if either the CloudFrontConfiguration is valid (i.e., the group key Id and the private key is set) and/or if the request has the property `preSignUrl` set to true.

### Configuration

Preparing the configuration on TF side can be done as follows:

* Provide a full set of key information and the base URL
* Provide just the group key ID and base URL. The server will then create a key pair automatically

In any case, the provided information has to be put into a `CloudFrontConfiguration` object attached to the `S3Connector` deployable. The minimal configuration provided is the base URL of the CloudFront service. If pre-signing is required, also the key group Id (created and managed by CloudFront) and the public/private key pair are necessary. The `CloudFrontConfiguration` object gets created when at least the base URL is provided in the context object to the AWS Wire Templates.

If the public/private key pair is not provided, the AWS Wire Templates will automatically create a new key pair. The public key is stored in two different formats: in a Base64 format and in PEM format. The PEM format can then be used to register the public key with CloudFront. Make sure that CloudFront and the `CloudFrontConfiguration` have registered the same key group ID.

If you're using the default initializer of the AWS module, the following runtime properties can be used to set up the CloudFront integration:

Name | Description | Encrypted
------------ | ------------- | -------------
`S3_CLOUDFRONT_BASE_URL` | The base URL of the CloudFront service | false
`S3_CLOUDFRONT_KEYGROUP_ID` | The ID of the key group as provided by CloudFront | false
`S3_CLOUDFRONT_PUBLIC_KEY` | The public key (X509, Base64 encoded) | true
`S3_CLOUDFRONT_PRIVATE_KEY` | The private key (PKCS8, Base64 encoded) | true

When using the AWS Wire Templates, the following snippet can be used to enable CloudFront integration:

```java
private S3BinaryProcessTemplateContext publicS3Context() {
    //@formatter:off
    S3BinaryProcessTemplateContext context = S3BinaryProcessTemplateContext.builder()
            ...
            .setCloudFrontBaseUrl(runtime.S3_PUBLIC_STREAMING_URL())
            .setCloudFrontKeyGroupId(runtime.S3_CLOUDFRONT_KEY_GROUP_ID())
            .setCloudFrontPublicKey(runtime.S3_CLOUDFRONT_PUBLIC_KEY())
            .setCloudFrontPrivateKey(runtime.S3_CLOUDFRONT_PRIVATE_KEY())
            ...
            .build();
    //@formatter:on

    return context;
}
```

## Pre-Signed S3 URLs

It is also possible to pre-sign URLs pointing directly to the S3 resource (without going through CloudFront).

The `CreatePresignedUrlForResource` can be used to create a pre-signed URL for a specific Access and Resource. This does not require any other configuration on the TF side.