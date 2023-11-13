package org.matsim.contrib.sharing.service;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

public class RentalInfo {

	public double startTime;
	public double endTime;
	public double distance;
	public Id<SharingService> serviceId;
	public Id<Person> personId;
	public Id<Link> pickupLinkId;
	public Id<Link> dropoffLinkId;

	public String vehicleId;
	public String pickupStationId;
	public String dropoffStationId;

	public RentalInfo(double starttime, double endtime, Id<SharingService> serviceId, Id<Person> personId, Id<Link> pickupLinkId,
			Id<Link> dropoffLinkId, String vehicleId, String pickupstationId,
			String dropoffstationId, double distance) {
		this.startTime = starttime;
		this.endTime = endtime;
		this.distance = distance;
		this.serviceId = serviceId;
		this.personId = personId;
		this.pickupLinkId = pickupLinkId;
		this.dropoffLinkId = dropoffLinkId;
		this.vehicleId = vehicleId;
		this.pickupStationId = pickupstationId;
		this.dropoffStationId = dropoffstationId;
	}
}
