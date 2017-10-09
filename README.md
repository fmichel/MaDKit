MaDKit: Multiagent Development Kit
================================================

[![Build Status](https://travis-ci.org/fmichel/MaDKit.svg?branch=testingTravis)](https://travis-ci.org/fmichel/MaDKit)

MaDKit is an open source multiagent-based software written in Java.

MaDKit-5 is designed as a lightweight software library for developing distributed applications and simulations using the multiagent paradigm.

### Features

* Artificial agents creation and life cycle management
* An organizational infrastructure for communication between agents, structuring the application
* High heterogeneity in agent architectures: No predefined agent model
* Multi-Agent based simulation and simulator authoring tools
* Multi-agent based distributed application authoring facilities


### Approach

In contrast to conventional approaches, which are mostly agent-centered, MaDKit follows an organization-centered approach ([OCMAS][1]): There is no predefnied agent model in MaDKit. 

Especially, MaDKit does not enforce any consideration about the internal structure of agents, thus allowing a developer to freely implements its own agent architectures. 

So, MaDKit is built upon the AGR (Agent/Group/Role) organizational model: Agents play roles in groups and thus create artificial societies.
 
[1]: http://www.lirmm.fr/~fmichel/publi/pdfs/ferber04ocmas.pdf

## More information

* [Official Homepage](http://www.madkit.net)
* [API Reference](http://www.madkit.net/madkit/docs/api)
* [Tutorials](http://www.madkit.net/madkit/tutorials)
* [Getting started](http://www.madkit.net/madkit/README.html)
* [Documentation](http://www.madkit.net/madkit/documents.php)
* [Forum](http://www.madkit.net/madkit/forum)

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request
