<h1 align="center">MaDKit: MultiAgent Development Kit </h1>
&emsp;

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fmichel/madkit.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.fmichel/madkit)
[![JavaDoc](https://img.shields.io/badge/JavaDoc-API-green)](https://madkit.net/javadoc)
[![Java Version](https://img.shields.io/badge/java-23+-green.svg)](https://www.oracle.com/java/technologies/javase-jdk23-downloads.html)
[![Gradle](https://img.shields.io/badge/gradle-8.12+-green.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-CeCILL--C-green)](http://www.cecill.info/index.en.html)

**MaDKit is a Multiagent-based software written in Java.**

It is designed as a lightweight Java library for developing distributed applications and simulations using the multiagent paradigm.

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
JDK 23+ is required. 

Using MaDKit can be done by [declaring it as a dependency using your favorite build tool](https://mvnrepository.com/artifact/io.github.fmichel/madkit).

For instance, with Gradle:

```groovy
implementation "io.github.fmichel:madkit:6.0.1"
```

## Getting Started
This repo contains 3 sub projects that give an hint about what can be done with MaDKit:

* MDK-simu-template: A simple example of a simulation using default classes and settings
* MDK-marketorg-app: A classic bid/offer multi-agent application
* MDK-bees-app: A complete simulation example 


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
