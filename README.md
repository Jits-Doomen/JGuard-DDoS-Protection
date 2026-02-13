# JGuard-DDoS-Protection

A lightweight, Java-based defensive utility designed to mitigate Layer 7 (Application Layer) DDoS attacks using high-performance rate-limiting and global traffic control.

## Purpose
JGuard protects backend applications by identifying and throttling malicious traffic. It acts as a "software shield" that can be integrated into any Java project to protect your server's resources (CPU/RAM/Database) from being overwhelmed.

## Features
- **Individual Rate Limiting:** Throttles IP addresses that exceed request limits in a 10-second window.
- **5-Minute Penalty Box:** Confirmed attackers are automatically banned for 5 minutes.
- **Global Lockdown:** If *total* server traffic exceeds a critical threshold, all traffic is dropped for 30 seconds to allow the server to recover.
- **Memory Protection:** Automatic eviction of old data to prevent `OutOfMemoryError` attacks.
- **Thread-Safe:** Designed for high-concurrency environments using `AtomicInteger`.
___

___
## Administrative Commands
| Action | Code |
| :--- | :--- |
| **Check Bans** | `TrafficAnalyzer.getBannedIPs();` |
| **Unblock IP** | `TrafficAnalyzer.manuallyUnblock("1.2.3.4");` |
| **Whitelist** | `TrafficAnalyzer.addToWhitelist("1.2.3.4");` |
___

## Integration
Add the following to your `pom.xml` if you are using Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>32.1.2-jre</version>
    </dependency>
</dependencies>