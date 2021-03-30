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
