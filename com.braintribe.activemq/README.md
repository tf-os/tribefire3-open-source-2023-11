# com.braintribe.activemq

Example configuration.json for connecting to an ActiveMQ service:

```
{
  "_type" : "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
  "bindId" : "tribefire-mq",
  "denotation" : {
    "_type" : "com.braintribe.model.messaging.jms.JmsActiveMqConnection",
    "name" : "ActiveMq Denotation",
    "hostAddress": "tcp://localhost:61616"
  }
}
```

Information on failover URLs: https://activemq.apache.org/failover-transport-reference
