// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/crypto;
import ballerina/java;

handle JAVA_NULL = java:createNull();

# Types of message content that can be published.
public type MessageContent int | float | byte | boolean | string | map<any> | xml | json | byte[];

# Types of exchanges supported by the Ballerina RabbitMQ Connector.
public type ExchangeType "direct"|"fanout"|"topic"|"headers";

# Constant for the RabbitMQ Direct Exchange type.
public const DIRECT_EXCHANGE = "direct";

# Constant for the RabbitMQ Fan-out Exchange type.
public const FANOUT_EXCHANGE = "fanout";

# Constant for the RabbitMQ Topic Exchange type.
public const TOPIC_EXCHANGE = "topic";

# Types of acknowledgement modes supported by the Ballerina RabbitMQ Connector.
public type AcknowledgementMode AUTO_ACK | CLIENT_ACK;

# Constant for the RabbitMQ auto acknowledgement mode.
public const AUTO_ACK = "auto";

# Constant for the RabbitMQ client acknowledgement mode.
public const CLIENT_ACK = "client";

# Basic properties of the message - routing headers etc.
#
# + replyTo - The queue name to which the other apps should send the response
# + contentType - Content type of the message
# + contentEncoding - Content encoding of the message
# + correlationId - Client-specific ID that can be used to mark or identify messages between clients
public type BasicProperties record {|
    string? replyTo;
    string? contentType = ();
    string? contentEncoding = ();
    string? correlationId = ();
|};

# Configurations used to declare a queue.
#
# + queueName - The name of the queue, which will be autogenerated if not specified
# + durable - True if declaring a durable queue (the queue will survive in a server restart)
# + exclusive - True if declaring an exclusive queue (restricted to this connection)
# + autoDelete - True if declaring an auto-delete queue (the server will delete it when it is no longer in use)
# + arguments - Other properties (construction arguments) for the queue
public type QueueConfiguration record {|
    string queueName;
    boolean durable = false;
    boolean exclusive = false;
    boolean autoDelete = true;
    map<any>? arguments = ();
|};

# Configurations used to declare an exchange.
#
# + exchangeName - The name of the exchange
# + exchangeType - The type of exchange
# + durable - True if declaring a durable exchange (the exchange will survive a server restart)
# + autoDelete - True if we are declaring an autodelete exchange (server will delete it when it is no longer in use)
# + arguments - Other properties (construction arguments) for the queue
public type ExchangeConfiguration record {|
    string exchangeName;
    ExchangeType exchangeType = DIRECT_EXCHANGE;
    boolean durable = false;
    boolean autoDelete = false;
    map<any>? arguments = ();
|};

# Configurations used to create a `rabbitmq:Connection`.
#
# + host - The host used for establishing the connection
# + port - The port used for establishing the connection
# + username - The username used for establishing the connection
# + password - The password used for establishing the connection
# + connectionTimeoutInMillis - Connection TCP establishment timeout in milliseconds and zero for infinite
# + handshakeTimeoutMillis -  The AMQP 0-9-1 protocol handshake timeout in milliseconds
# + shutdownTimeoutInMillis - Shutdown timeout in milliseconds, zero for infinite, and default is 10000. If consumers exceed
#                     this timeout, then any remaining queued deliveries (and other Consumer callbacks) will be lost
# + heartbeatInSeconds - The initially-requested heartbeat timeout in seconds and zero for none
# + secureSocket - Configurations for facilitating secure connections
public type ConnectionConfiguration record {|
    string host;
    int port = 5672;
    string? username = ();
    string? password = ();
    int? connectionTimeoutInMillis = ();
    int? handshakeTimeoutMillis = ();
    int? shutdownTimeoutInMillis = ();
    int? heartbeatInSeconds = ();
    SecureSocket? secureSocket = ();
|};

# Configurations for facilitating secure connections.
#
# + trustStore - Configurations associated with the TrustStore
# + keyStore - Configurations associated with the KeyStore
# + tlsVersion - TLS version
# + verifyHostname - True if hostname verification should be enabled
public type SecureSocket record {|
    crypto:TrustStore? trustStore = ();
    crypto:KeyStore? keyStore = ();
    string tlsVersion = "TLS";
    boolean verifyHostname = true;
|};
