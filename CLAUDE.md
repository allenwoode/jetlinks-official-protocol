# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **JetLinks Official Protocol** - a Java-based IoT device communication protocol implementation supporting
multiple transport layers (MQTT, TCP, UDP, HTTP, WebSocket, CoAP). The project provides reference implementations for
custom protocol development in the JetLinks IoT platform.

## Build and Development Commands

### Basic Maven Commands

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Package the project
mvn package

# Create an all-in-one executable JAR
mvn package -P all-in-one

# Install to local repository
mvn install

# Release build with signing and documentation
mvn clean package -P release
```

### Testing

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=JetLinksMqttDeviceMessageCodecTest

# Run tests with verbose output
mvn test -Dtest=BinaryMessageTypeTest -Dmaven.test.debug=true
```

## Code Architecture

### Transport Layer Support

The protocol supports multiple transport mechanisms:

- **MQTT**: Topic-based messaging with authentication via username/password
- **TCP**: Binary protocol with message length prefixing
- **UDP**: Binary protocol with embedded authentication
- **HTTP**: RESTful API with Bearer token authentication
- **WebSocket**: Real-time bidirectional communication
- **CoAP**: Constrained Application Protocol support

### Core Components

#### Protocol Provider (`JetLinksProtocolSupportProvider`)

- Main entry point that registers all transport codecs
- Configures authentication methods and metadata codecs
- Defines routes and documentation for each transport

#### Message Codecs

- `JetLinksMqttDeviceMessageCodec`: Handles MQTT message encoding/decoding
- `TcpDeviceMessageCodec`: Processes TCP binary messages with length prefixing
- `UDPDeviceMessageCodec`: Manages UDP datagram processing
- `JetLinksHttpDeviceMessageCodec`: HTTP/WebSocket message handling
- `JetLinksCoapDeviceMessageCodec`: CoAP message processing

#### Binary Protocol (`binary/` package)

- `BinaryMessageType`: Enum defining all supported binary message types
- `BinaryMessage` implementations for each message type
- `DataType`: Defines binary encoding rules for different data types
- Supports big-endian encoding with type-length-value format

#### Topic Processing (`TopicMessageCodec`)

- Handles MQTT topic routing and message transformation
- Supports both device and child device (gateway) scenarios
- Manages upstream/downstream message flows

### Key Configuration

- **MQTT Authentication**: `secureId|timestamp` username, MD5 password hash
- **TCP Authentication**: Secure key validation in first message
- **HTTP Authentication**: Bearer token in Authorization header
- **Binary Protocol**: 32-bit message length + message data + optional secure key

### Authentication Flow

1. **MQTT**: Connect with calculated username/password
2. **TCP**: Send online message with secure key as first packet
3. **UDP**: Include secure key in every packet
4. **HTTP**: Include Bearer token in Authorization header

### Dependencies

- **jetlinks-supports**: Core JetLinks platform integration
- **Spring Boot 2.2.8**: Framework foundation
- **Reactor/Netty**: Reactive networking
- **Jackson**: JSON processing
- **Lombok**: Code generation
- **Eclipse Californium**: CoAP protocol support
- **Vert.x**: Event-driven toolkit

### Project Structure

```
src/main/java/org/jetlinks/protocol/official/
├── JetLinksProtocolSupportProvider.java    # Main protocol provider
├── JetLinksMqttDeviceMessageCodec.java     # MQTT codec
├── TopicMessageCodec.java                  # Topic routing
├── binary/                                 # Binary protocol implementation
├── tcp/TcpDeviceMessageCodec.java         # TCP codec
├── udp/UDPDeviceMessageCodec.java         # UDP codec
├── http/JetLinksHttpDeviceMessageCodec.java # HTTP/WebSocket codec
└── cipher/                                # Encryption utilities
```

## Development Notes

### Message Flow

1. Device connects using transport-specific authentication
2. Platform validates credentials and sends ACK
3. Device sends property reports, events, or responses
4. Platform can send property reads, writes, or function invokes
5. Device responds with appropriate reply messages

### Binary Protocol Message Types

See `binary-protocol.md` for detailed encoding rules and examples.

- `0x00`: Keepalive
- `0x01`: Online (device connection) - includes secure key
- `0x02`: ACK (acknowledgment) - response codes: 0x00=OK, 0x01=Unauthenticated, 0x02=Unsupported
- `0x03`: Report property - OBJECT type data
- `0x04`: Read property - ARRAY type property list
- `0x05`: Read property reply - success: [0x01, OBJECT] or failure: [0x00, error_code, error_message]
- `0x06`: Write property - OBJECT type property data
- `0x07`: Write property reply - success: [0x01, OBJECT] or failure: [0x00, error_code, error_message]
- `0x08`: Function invoke - [function_id:STRING, parameters:OBJECT]
- `0x09`: Function invoke reply - success: [0x01, result:OBJECT] or failure: [0x00, error_code, error_message]

### Data Type Encoding

All data uses big-endian encoding with type-length-value format:

- STRING: type(0x0b) + length(2 bytes) + UTF-8 data
- OBJECT: type(0x0e) + field_count(2 bytes) + key-value pairs
- ARRAY: type(0x0d) + element_count(2 bytes) + typed elements

### Testing

The project includes comprehensive test coverage for all transport protocols and message types. Tests use JUnit 4 with
Reactor Test support.

## Development Workflow

### Recent Changes (Current Git Status)

- Modified `pom.xml` - project dependencies and build configuration
- Added/Modified `BinaryDeviceKeepaliveMessage.java` - new keepalive message implementation
- Modified `BinaryKeepaliveMessage.java`, `BinaryMessageType.java`, `KeepaliveMessage.java` - keepalive protocol updates
- Added/Modified `DeviceKeepalive.java` - TCP device keepalive support
- Modified `TcpDevice.java` - TCP device implementation updates
- Updated `BinaryMessageTypeTest.java` - test coverage for binary message types

### Key Files for Protocol Extension

- `BinaryMessageType.java` - Central enum for all binary message types, modify here to add new message types
- `JetLinksProtocolSupportProvider.java` - Main protocol registration, update for new transport support
- Transport-specific codecs in respective packages (tcp/, udp/, http/, etc.) for implementing new transports