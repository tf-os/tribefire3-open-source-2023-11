# AutoCommit

These metadata properties allow you to configure whether a commit is automatically performed after manipulations are performed.

Metadata Property Name  | Type Signature  
------- | -----------
`AutoCommit` | `com.braintribe.model.meta.data.prompt.AutoCommit`
`ManualCommit` | `com.braintribe.model.meta.data.prompt.ManualCommit`

## General

If the ManualCommit (or no) metadata is configured, then the users need to manually hit the `Commit` button in order for saving their manipulations.
In the other way, if the AutoCommit is configured, then after creating the manipulations, the users won't need to manually hit the `Commit` button, as the manipulations will be committed automatically.

> For more information see [Predicate Metadata](../predicate.md).

You can attach this metadata to models.
