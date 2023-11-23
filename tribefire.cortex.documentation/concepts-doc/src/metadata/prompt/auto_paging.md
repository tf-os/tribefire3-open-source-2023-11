# AutoPagingSize

This metadata allows you to configure the auto paging size when fetching queries from the services side and the auto paging feature is enabled.

Metadata Property Name  | Type Signature  
------- | -----------
`AutoPaging` | `com.braintribe.model.meta.data.prompt.AutoPagingSize`

## General

If the AutoPagingSize metadata is configured, then we use its **size** value for knowing how many ammount of data to fetch.

You can attach this metadata to entities or templates.
There are 3 ways which can influence the paging size of an auto paging:

We can configure the `Template` with this metadata, or the `Entity type` and finally the `WorkbenchConfiguration` can also have that size configured.
Returns the configured auto paging size. There are 3 possibilities for that. It comes from the {@link Template},
The size is checked in that order. The one existing first will be the one used.
