MultiAgent Development Kit - MaDKit - version 6
================================================

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fmichel/MaDKit.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.fmichel/MaDKit)

MaDKit is an open source multiagent-based software written in Java.

**MaDKit 6** is designed as a lightweight Java library for developing distributed applications and simulations using the multiagent paradigm.

### Features

* Artificial agents creation and life cycle management
* An organizational infrastructure for communication between agents, structuring the application
* High heterogeneity in agent architectures: No predefined agent model
* Multi-Agent based simulation and simulator authoring tools

### Approach

In contrast to agent-centered approaches, MaDKit follows an organization-centered approach ([OCMAS][1]): There is no predefnied agent model in MaDKit. 

Especially, MaDKit does not enforce any consideration about the internal structure of agents, thus allowing a developer to freely implements its own agent architectures. 

So, MaDKit is built upon the AGR (Agent/Group/Role) organizational model: Agents play roles in groups, and thus create artificial societies.
 
[1]: http://www.lirmm.fr/~fmichel/publi/pdfs/ferber04ocmas.pdf

## Programming with MaDKit

Programming with MaDKit can be done by either :

- Downloading the zip distribution and using the provided jar file.

- Declare MaDKit as a dependency using your favorite build tool : [https://mvnrepository.com/artifact/io.github.fmichel/MaDKit](https://mvnrepository.com/artifact/io.github.fmichel/MaDKit)

For instance, with Gradle:

```groovy
implementation "io.github.fmichel:MaDKit:6.0"
```

JDK 21+ is required. 

## More information

* [Official Homepage](http://www.madkit.net)
* [API Reference](https://madkit.net/javadoc/6.0/madkit.base/module-summary.html)
* [Tutorials](http://www.madkit.net/madkit/tutorials) V.5
* [Getting started](http://www.madkit.net/madkit/README.html) V.5
* [Documentation](http://www.madkit.net/madkit/documents.php)

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request
