# remotes-configuring-module

## General

This module binds an implementation for a `DynamicInitializer` which configures a single `GmWebRpcRemoteServiceProcessor` with a single
`RemotifyingInterceptor` and multiple remote domains.

By remote domains we mean domains where all the request are handled by the mentioned service processor and interceptor.

The input for this initializer is expected to be a single file called `remotes.yml`, content of which describes a single `Remotes` instance (from `tribefire.cortex:remotes-configuring-module`).

## bindInitializers()

Binds a `Dynamic Initialization Factory` doing what's described above. See `DynamicInitializer` (from `com.braintribe.gm:collaborative-smood-configuration-model`)
