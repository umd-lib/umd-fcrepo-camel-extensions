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




