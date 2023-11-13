package org.matsim.contrib.sharing.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.sharing.run.SharingServiceConfigGroup;
import org.matsim.contrib.sharing.service.events.SharingDropoffEvent;
import org.matsim.contrib.sharing.service.events.SharingDropoffEventHandler;
import org.matsim.contrib.sharing.service.events.SharingPickupEvent;
import org.matsim.contrib.sharing.service.events.SharingPickupEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.TeleportationArrivalEvent;
import org.matsim.core.api.experimental.events.handler.TeleportationArrivalEventHandler;

import com.google.common.base.Verify;

public class SharingTeleportedRentalsHandler
		implements SharingPickupEventHandler, SharingDropoffEventHandler, TeleportationArrivalEventHandler {
	private EventsManager eventsManager;
	private SharingServiceConfigGroup serviceParams;
	private Map<Id<Person>, SharingPickupEvent> pickups = new HashMap<>();
	private Map<Id<Person>, Double> distance = new HashMap<>();
	private Set<RentalInfo> rentals = new HashSet<>();

	public static final String PERSON_MONEY_EVENT_PURPOSE_SHARING_FARE = "sharingFare";

	public SharingTeleportedRentalsHandler(EventsManager eventsManager, SharingServiceConfigGroup serviceParams) {
		this.eventsManager = eventsManager;
		this.serviceParams = serviceParams;
	}

	@Override
	public void handleEvent(SharingPickupEvent event) {

		if (event.getServiceId().toString().equals(serviceParams.getId())) {
			pickups.put(event.getPersonId(), event);
		}
	}

	@Override
	public void handleEvent(TeleportationArrivalEvent event) {
		if (pickups.containsKey(event.getPersonId())) {
			distance.compute(event.getPersonId(), (k, v) -> v == null ? event.getDistance() : v + event.getDistance());
		}

	}

	@Override
	public void handleEvent(SharingDropoffEvent event) {

		if (event.getServiceId().toString().equals(serviceParams.getId())) {
			Verify.verify(this.distance.containsKey(event.getPersonId()));
			// distance fare
			double sharedDistanceFare = this.distance.get(event.getPersonId()) * this.serviceParams.getDistanceFare();

			// base fare
			double sharedBaseFare = this.serviceParams.getBaseFare();

			// time fare
			double pickuptime = this.pickups.get(event.getPersonId()).getTime();
			double rentalTime = event.getTime() - pickuptime;
			double sharedTimeFare = rentalTime * serviceParams.getTimeFare();

			// minimum fare
			double minimumFare = serviceParams.getMinimumFare();

			double sharedFare = Math.max(minimumFare, sharedBaseFare + sharedDistanceFare + sharedTimeFare);
			eventsManager.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -sharedFare,
					PERSON_MONEY_EVENT_PURPOSE_SHARING_FARE, event.getServiceId().toString()));

			SharingPickupEvent pickupEvent = this.pickups.get(event.getPersonId());

			RentalInfo rentalInfo = new RentalInfo(pickupEvent.getTime(), event.getTime(), event.getServiceId(),
					event.getPersonId(), pickupEvent.getLinkId(), event.getLinkId(),
					pickupEvent.getAttributes().get("vehicle"), pickupEvent.getAttributes().get("station"),
					event.getAttributes().get("station"), this.distance.get(event.getPersonId()));
			this.rentals.add(rentalInfo);

			this.pickups.remove(event.getPersonId());
			this.distance.remove(event.getPersonId());

		}
	}
	
	public Set<RentalInfo> getRentals() {
		return this.rentals;
	}

	@Override
	public void reset(int iteration) {
		pickups.clear();
		distance.clear();
		rentals.clear();
	}

}
