# UMD Fcrepo Camel Extensions
Extensions to the fcrepo-camel-toolbox for UMD's custom routes and configurations.

Uses fcrepo-camel-toolbox version 4.7.2.

## Installation
Add the umd-features repository to karaf.

```
repo-add mvn:edu.umd.lib.fcrepo/umd-features/<VERSION>/xml/features
```

Install the UMD Fcrepo Event Router module.

```
feature:install umd-fcrepo-event-router
```

Install the modules in the following order to setup the batch caching.

```
feature:install fcrepo-service-ldcache-file
feature:install umd-fcrepo-batch-ldcache-file
feature:install umd-fcrepo-ldpath
feature:install umd-fcrepo-indexing-solr
```

**Note:** The `fcrepo-ldpath` and `fcrepo-indexing-solr` modules should be uninstalled before installing the caching modules.

Install the UMD Fcrepo Broadcast module.

```
feature:install umd-fcrepo-broadcast
```

Install the UMD SPARQL Query module.

```
feature:install umd-fcrepo-sparql-query
```

## License

See the [LICENSE](LICENSE.md) file for license rights and limitations (Apache 2.0).
