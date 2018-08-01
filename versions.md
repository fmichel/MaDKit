MadkitLanEdition
================
1.7.6 Stable (Build: 134) (from 22/05/2015 to 01/08/2018)

# Creator(s):
Jason MAHDJOUB

# Developer(s):
Jason MAHDJOUB (Entred in the team at 22/05/2015)
Fabien MICHEL (Entred in the team at 01/02/1997)
Olivier GUTKNECHT (Entred in the team at 01/02/1997)
Jacques FERBER (Entred in the team at 01/02/1997)

# Modifications:


### 1.7.6 Stable (01/08/2018)
* Update OOD to 2.0.0 Beta 86.
* Update Utils to 3.19.0.
* Add save functions into MadKit Properties.
* Fiw network messages serialization problem.


### 1.7.5 Stable (27/07/2018)
* Update OOD to 2.0.0 Beta 85.
* Update Utils to 3.18.0.
* Save MKLE configuration that are different from a reference configuration. Other properties are not saved.


### 1.7.3 Stable (20/07/2018)
* Update OOD to 2.0.0 Beta 84.
* Update Utils to 3.17.0.
* Correct version's control of distant peer.


### 1.7.1 Stable (13/07/2018)
* Update OOD to 2.0.0 Beta 83.
* Update Utils to 3.16.1.
* Improve version's control of distant peer.
* Clean code.


### 1.7.0 Stable (20/05/2018)
* Update OOD to 2.0.0 Beta 82.
* Update Utils to 3.15.0.
* Add P2P connection protocol that support parametrisation of key aggreement.
* Support several key agreement (including Post Quantum Cryptography key agreement (New Hope).
* Fix security issue : when data is sent without being writed (default memory state), fill it with zeros.
* Fix security issue : sign symmetric encryption key into client/server connnection protocol.
* Fix security issue : with P2P key agreements, generate signature and encryptions keys with two steps (instead of one), in order to sign the exchanged symmetric encryption key.
* Fix security issue : class serialization are now filtered with white list and black list. Classes that are not into white list must implement the interfance 'SerializableAndSizable'. Messages sent to the network must implement the interface NetworkMessage.
* Optimization : use externalization process instead of desialization process during lan transfer.
* Fix security issue : classes exterlization processes control now the allocated memory during de-externalization phase.
* Security enhancement : initialisation vectors used with encryption has now a secret part composed of counter that is increased at each data exchange.
* Security enhancement : signature and encryption process use now a secret message that is increased at each data exchange.
* Security enhancement : P2P login agreement use now JPAKE and a signature authentication if secret key for signature is available (PassworKey.getSecretKeyForSignature()).
* Fix issue with dead lock into indirect connection process.
* Fix issue with dual connection between two same kernels.
* Externalising Java rewrited classes into JDKRewriteUtils project.
* Support of authenticated encryption algorithms. When use these algorithms, MKLE do not add a signature with independant MAC.
* Add some benchmarks.
* Support of YAML file properties.


### 1.6.5 Stable (27/02/2018)
* Debug UPNP connexion with macOS.
* Fix issue with multiple identical router's messages : do not remove the router to recreate it.


### 1.6.5 Stable (26/02/2018)
* Fiw a problem with UPNP connexion under macOS.


### 1.6.4 Stable (15/02/2018)
* Fix problem of port unbind with Windows.
* Fix problem of simulatenous connections with Mac OS
* Fix problem with interface address filtering


### 1.6.3 Stable (10/02/2018)
* Update OOD to 2.0.0 Beta 66.
* Update Utils to 3.10.5
* Change minimum public key size from 1024 to 2048


### 1.6.2 Stable (10/02/2018)
* Update OOD to 2.0.0 Beta 65.
* Update Utils to 3.10.4
* Change minimum public key size from 1024 to 2048


### 1.6.1 Stable (04/02/2018)
* Overlookers were not aware from new roles adding. Fix this issue.
* Add MadKit demos


### 1.6.0 Stable (31/01/2018)
* Updating OOD to 2.0.0 Beta 59
* Updating Utils to 3.9.0
* Messages can now be atomically non encrypted


### 1.5.2 Stable (13/12/2017)
* Updating OOD to 2.0.0 Beta 57
* Updating Utils to 3.7.1
* Debugging JavaDoc


### 1.5.0 Stable (13/11/2017)
* Updating OOD to 2.0.0 Beta 55
* Packets can now have sizes greater than Short.MAX_VALUE


### 1.4.5 Stable (02/11/2017)
* Updating OOD to 2.0.0 Beta 54


### 1.4.0 Stable (13/10/2017)
* Updating OOD to 2.0.0 Beta 48
* Several modifications into connection and access protocols
* Adding approved randoms parameters into MadkitProperties
* Adding point to point transfert connection signature and verification
* Saving automaticaly random's seed to be reload with the next application loading


### 1.2.1 Stable (31/08/2017)
* Including resources in jar files


### 1.2.0 Stable (05/08/2017)
* Correction a problem with database
* Adding P2PSecuredConnectionProtocolWithECDHAlgorithm connection protocol (speedest)
* Adding Client/ServerSecuredConnectionProtocolWithKnwonPublicKeyWithECDHAlgorithm connection protocol (speedest)
* Now all connection protocols use different keys for encryption and for signature
* Adding AccessProtocolWithJPake (speedest)
* Debugging desktop Jframe closing (however the JMV still become opened when all windows are closed)
* Several minimal bug fix
* Correction of JavaDoc
* Updating OOD to 2.0.0 Beta 20 version


### 1.1.3 Stable (05/08/2017)
* Updating OOD to 2.0.0 Beta 15


### 1.1.2 Stable (05/08/2017)
* Updating OOD to 2.0.0 Beta 14
* Optimizing some memory leak tests


### 1.1.0 Stable (04/08/2017)
* Convert project to Gradle project


### 1.0.0 Stable (04/06/2017)
* Correction of a bug with database deconnection
* Debugging indirect connections
* Solving a memory leak problem with ConversationID
* Solving a memory leak problem with TransferAgent (not killed)
* Solbing problem when deny BigDataProposition and kill agent just after
* Indirect connection send now ping message
* Adding white list for inet addresses in network properties
* Correcting problems of internal group/role references/dereferences


### 1.0.0 Beta 4 (27/05/2017)
* Agents are now identified by a long (and not int)
* Adding the function AbstractAgent.getAgentID()
* Removing static elements in Conversation ID


### 1.0.0 Beta 3 (23/05/2017)
* Update Utils to 2.7.1
* Update OOD to 2.0.0 Beta 1
* JDK 7 compatible


### 1.0.0 Beta 2 (07/03/2017)
* Renforce secret identifier/password exchange
* Add agent to launch into MKDesktop windows


### 1.0.0 Beta 0 (04/03/2017)
* First MadkitLanEdition release, based on Madkit

