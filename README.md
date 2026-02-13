# ShieldGuard-DDoS-Protection

A lightweight, Java-based defensive utility designed to mitigate Layer 7 (Application Layer) DDoS attacks using rate-limiting.

## Purpose
This tool helps developers protect their applications by identifying and throttling IP addresses that send too many requests in a short period.

## Features
- **Rate Limiting:** Protects against volumetric floods.
- **Auto-Cleaning Cache:** Frees up memory automatically after 10 seconds of inactivity.
- **Thread-Safe:** Designed for high-concurrency environments.

## Integration
Add the following to your `pom.xml` if you are using Maven:
```xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.2-jre</version>
</dependency>