---
layout: page
title: Getting Started
permalink: /getting-started/
---

## Requirements

- JDK 25 or the version required by the current release.
- Gradle, or a Gradle-compatible build such as the included wrapper.
- A desktop environment if you want to run JavaFX-based examples.

## Add MaDKit to a Gradle project

```groovy
dependencies {
    implementation "io.github.fmichel:madkit:6.0.5"
}
```

Use the version published for the release you are targeting. Check [Maven Central](https://central.sonatype.com/artifact/io.github.fmichel/madkit) for the current artifact.

## Learn in this order

1. Read [Core Concepts]({{ '/concepts/' | relative_url }}) to understand the AGR organization model.
2. Browse the small examples in [`MDK-samples`](https://github.com/fmichel/MaDKit/tree/main/MDK-samples).
3. Start from [`MDK-simu-template`](https://github.com/fmichel/MaDKit/tree/main/MDK-simu-template) when building a simulation.
4. Use the [API documentation]({{ '/api/latest/' | relative_url }}) for precise type and method details.

## Build the repository examples

From a checkout of the repository:

```bash
./gradlew :MDK-samples:compileJava
./gradlew :MDK-samples:test --no-daemon --console=plain
```

The sample README contains the complete package matrix and notes which GUI examples require a graphical desktop.
