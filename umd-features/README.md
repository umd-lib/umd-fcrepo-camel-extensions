# UMD Features

Provides the features xml to install the UMD fcrepo camel extensions to Karaf.

Also, includes the umd-fcrepo-indexing-solr feature that uses the UMD fcrepo camel extensions instead of the fcrepo equivalents. The umd-fcrepo-indexing-solr is not a separate module, but just a feature defined in the [features.xml](src/main/resources/features.xml). The configuration file name for the umd-fcrepo-indexing-solr module is `org.fcrepo.camel.indexing.solr.cfg` unlike the other umd features whose confiration file name begins with `edu.umd.lib.fcrepo.camel`.
