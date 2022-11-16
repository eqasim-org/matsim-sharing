# MATSim sharing extension

This repository contains an extension for MATSim which makes it possible to simulate shared vehicle services. The extension is written in a modular way such that common features like reserving vehicles at a dock, picking up vehicles, returning vehicles, etc. are managed. However, the underlying mode of transport can be anything that can be simulated in MATSim. This means you can use construct a sharing services with network-simulated cars, network-simulated bikes or any teleportation-based modes (e.g. simple representations of shared bikes or scooters). Furthermore, we provide tools to automatically convert common data sources such as GBFS feeds.

More documentation is underway, on:
- How to set up a teleportation-based service (freefloating and station-based)
- How to set up a network-based service
- How the format of the service definition XML looks like (with stations and vehicles)

For the time being, there are a couple of examples. For instance, `RunCarsharing` sets up a simple (network-based) car-sharing service for standard scenarios such as Sioux Falls. `RunTeleportationBikesharing` is a similar example for a teleportation-based bikesharing service.

Main developers:
- Milos Balac (ETH Zurich)
- Sebastian Hörl (IRT SystemX)

Main reference:
- Balac, M. and S. Hörl (2021) [Simulation of intermodal shared mobility in the San Francisco Bay using MATSim](https://www.researchgate.net/publication/351069421_Simulation_of_intermodal_shared_mobility_in_the_San_Francisco_Bay_using_MATSim), to be presented at the 2021 Intelligent Transportation Systems Conference (ITSC).

## Versioning and Packging


The current version of `matsim-sharing` is `1.0.1` and is based on MATSim `13.0`. You can access it through the `v1.0.1` tag. The
`master` branch is kept at version `1.0.1` until the next release is prepared,
but may include additional developments since the last release.

The code is available as a Maven package. To use it, add the following repository
to your `pom.xml`:

```xml
<repository>
    <id>eth-ivt-sharing</id>
    <url>https://packagecloud.io/eth-ivt/sharing/maven2</url>
</repository>
```

Afterwards, you can add the following dependency to your project:

```xml
<dependency>
  <groupId>org.matsim.contrib</groupId>
  <artifactId>sharing</artifactId>
  <version>1.0.1</version>
</dependency>
```
