# Usage

This Cartridge starts an embedded ActiveMQ JMS Broker that can be used by other processes as a JMS messaging platform.

The URL for connecting to this broker is as follows:

```
tcp://127.0.0.1:61616
```

The actual hostname/IP address and the port may vary.

If there is a cluster of ActiveMQ brokers, a failover URL should be used:

```
failover:(tcp://remotehost1:61616,tcp://remotehost2:61616)?initialReconnectDelay=100
```

See [the online ApacheMQ documentation](http://activemq.apache.org/failover-transport-reference.html "Failover Transport Reference")  for a detailed description of the connection URL parameters.