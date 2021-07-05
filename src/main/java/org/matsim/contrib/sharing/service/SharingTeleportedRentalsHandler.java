package org.matsim.contrib.sharing.service;

import java.util.HashMap;
import java.util.Map;

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

public class SharingTeleportedRentalsHandler implements SharingPickupEventHandler, SharingDropoffEventHandler, TeleportationArrivalEventHandler {
	private EventsManager eventsManager;
	private SharingServiceConfigGroup serviceParams;
	private Map<Id<Person>, SharingPickupEvent> pickups = new HashMap<>();
	private Map<Id<Person>, Double> distance = new HashMap<>();
	
	public static final String PERSON_MONEY_EVENT_PURPOSE_SHARING_FARE = "sharingFareDistance";
	public static final String PERSON_MONEY_EVENT_PURPOSE_SHARING_BASE_FARE = "sharingBaseFare";

	
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
			distance.compute(event.getPersonId(), (k,v) -> v == null ? event.getDistance() : v + event.getDistance());
		}
		
	}
	
	@Override
	public void reset(int iteration) {
		pickups.clear();
		distance.clear();		
	}

	@Override
	public void handleEvent(SharingDropoffEvent event) {

		if (event.getServiceId().toString().equals(serviceParams.getId())) {
			Verify.verify(this.distance.containsKey(event.getPersonId()));
			double sharedFare = this.distance.get(event.getPersonId()) * this.serviceParams.getDistanceFare();
			double sharedBaseFare = this.serviceParams.getBaseFare();
			eventsManager.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), sharedFare,  PERSON_MONEY_EVENT_PURPOSE_SHARING_FARE, event.getServiceId().toString()));
			eventsManager.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), sharedBaseFare,  PERSON_MONEY_EVENT_PURPOSE_SHARING_BASE_FARE, event.getServiceId().toString()));
			this.distance.remove(event.getPersonId());
			this.pickups.remove(event.getPersonId());
		}
	}

}
