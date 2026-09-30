<h1 align="center">MaDKit: Multi-agent systems Development Kit</h1>
&emsp;

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fmichel/madkit.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.fmichel/madkit)
[![JavaDoc](https://img.shields.io/badge/JavaDoc-API-green)](https://madkit.net/javadoc)
[![Java Version](https://img.shields.io/badge/java-23+-green.svg)](https://www.oracle.com/java/technologies/javase-jdk23-downloads.html)
[![Gradle](https://img.shields.io/badge/gradle-8.12+-green.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-CeCILL--C-green)](http://www.cecill.info/index.en.html)

**Open source Java API for developing and simulating Multi-Agent Systems** ([MAS](https://en.wikipedia.org/wiki/Multi-agent_system))

MaDKit is designed as a lightweight Java library for developing distributed applications and simulations using the multiagent paradigm.

## Features

* Artificial agents creation and life cycle management
* An organizational infrastructure for communication between agents, structuring the application
* High heterogeneity in agent architectures: No predefined agent model
* Multi-Agent based simulation and simulator authoring tools

## Approach

In contrast to agent-centered approaches, MaDKit follows an organization-centered approach ([OCMAS][1]): There is no predefnied agent model in MaDKit. 

Especially, MaDKit does not enforce any consideration about the internal structure of agents, thus allowing a developer to freely implements its own agent architectures. 

So, MaDKit is built upon the AGR (Agent/Group/Role) organizational model: Agents play roles in groups, and thus create artificial societies.
 
[1]: http://www.lirmm.fr/~fmichel/publi/pdfs/ferber04ocmas.pdf

## Simulation authoring

MaDKit is designed to provide tools for easily create agent-based simulation engines from scratch, so that one can achieve particular requirements.

It also provides default simulation settings that can be used and extended to quickly build an agent-based simulation, only focusing on the agent modeling part.

Its conceptual approach to multi-agent based simulation mainly relies on this [research paper](http://www.lirmm.fr/~fmichel/publi/pdfs/michel09mas_and_ms.pdf).

## Programming with MaDKit
 JDK 25+ is required.

Using MaDKit can be done by [declaring it as a dependency using your favorite build tool](https://mvnrepository.com/artifact/io.github.fmichel/madkit).

For instance, with Gradle:

```groovy
implementation "io.github.fmichel:madkit:6.0.5"
```

## Getting Started

JDK 23 or newer and a graphical desktop are required to run the JavaFX-based sample
browser and GUI examples. The repository includes the Gradle wrapper, so no separate
Gradle installation is needed.

### Run and discover the samples

The easiest way to explore MaDKit is the `MDK-samples` browser. From this repository's
root directory, run:

```sh
./gradlew :MDK-samples:run --no-daemon --console=plain
```

The browser lists sample families such as **Launching**, **Agents**, **Organization**,
**Messaging**, **GUI**, **Logging and randomization**, and **ChartFX**. Select a family
to read its packaged Markdown documentation, then use the **Launch** buttons to start
the registered examples. The catalog is explicit: only examples listed by the launcher
are runnable from the browser, so helper classes are not started accidentally.

For the complete package/class catalog, manual smoke-test notes, and chart examples, see
[`MDK-samples/README.md`](MDK-samples/README.md). Each sample family also has a package-level
`README.md` under `MDK-samples/src/main/java`, which is the documentation displayed by
the browser.

The browser and GUI/ChartFX samples need an active graphical display. Samples launched
from the browser run in background threads; stop a running example using its own UI or
close the application when finished.

Useful commands, run from the repository root:

```sh
# Compile the samples
./gradlew :MDK-samples:compileJava

# Run the headless TestNG/AssertJ sample tests
./gradlew :MDK-samples:test --no-daemon --console=plain
```

The repository also contains larger application examples:

* `MDK-simu-template`: A simple simulation using default classes and settings
* `MDK-marketorg-app`: A classic bid/offer multi-agent application
* `MDK-bees-app`: A complete organization-based simulation


## More information
* [JavaDoc API Reference](https://madkit.net/javadoc)
* [Official Homepage](http://www.madkit.net) V.5
* [Tutorials](http://www.madkit.net/madkit/tutorials) V.5
* [Documentation](http://www.madkit.net/madkit/documents.php) V.5

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<div align="center">
<img src=MaDKit/src/main/resources/madkit/images/madkit_logo.png width=9% />
</div>
