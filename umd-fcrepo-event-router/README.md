# UMD Fcrepo Event Router

* Routes the events from the fcrepo queue to batch and live queue based on the event's user.
* Sets a header to identify batch events.
* Provides option to suppress pcdm container node indexing in batch mode.
* Provides option to set JMSPriority for batch events.

See [configuration file](src/main/cfg/edu.umd.lib.fcrepo.camel.router.cfg).
