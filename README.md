# HyparView
Kotlin implementation of HyParView: a membership protocol for reliable gossip-based broadcast 

This system uses HyParView protocol to get knowledge of all the nodes in the system.  

Usage:
1) Define IP's and Port's for the system nodes under /src/main/resources/application.conf
2) To insert a new node into the system you should run Node.kt. This can be done using the following program arguments: CONTACT_NODE MY_NODE. If you wish to write the globalview to a file just run using: CONTACT_NODE MY_NODE test 

Every node in the system has a console for the system administrator interact and monitorize the node and system status.
If you are only interested on the HyParView implementation check src/main/partialview directory.

All the remaining technical aspects and results can be read on the report.pdf (pt-pt).

HyParView:  http://homepages.gsd.inesc-id.pt/~ler/reports/dsn07-leitao.pdf
