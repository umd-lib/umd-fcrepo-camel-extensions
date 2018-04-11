# umd-osgi-extensions

This project provides useful classes to other projects in this
repository.

To include these classes, add the following to the "pom.xml" file
of the project:

```
<dependency>
    <groupId>edu.umd.lib</groupId>
    <artifactId>umd-osgi-extensions</artifactId>
    <version>${project.version}</version>
    <type>jar</type>
</dependency>
```

If using with a Karaf feature, include this bundle as a dependency in
the "features.xml" file (such as umd-features/src/main/resources/features.xml):

```
  <feature name="..." version="${project.version}">
    ...
    <bundle>mvn:edu.umd.lib.fcrepo/umd-osgi-extensions/${project.version}</bundle>
  </feature>
```

## AbstractManagedServiceFactory/AbstractManagedServiceInstance

These abstract classes can be subclassed to create OSGI services that
allow multiple instances to be created by adding an appropriate
configuration file to the Karaf etc/ directory.

See the following projects for example usage:

* umd-fcrepo-broadcast
* umd-fcrepo-sparql-query
* umd-fcrep-triplestore
* umd-fcrepo-notification

### Instance Route Ids

When creating multiple instances, it is _critical_ that the "route id"
of the instance be unique. If two instances have the same route id, one
of the instances will overwrite the other (exactly which one "wins"
is arbitrary).

In the above services, it is assumed that the the input stream for an
instance will always be unique, so the route id is created from a
combination of the service name, and the input stream. 



