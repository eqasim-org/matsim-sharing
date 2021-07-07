package org.matsim.contrib.sharing.fare;

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

public class SharingTimeFareHandler implements SharingPickupEventHandler, SharingDropoffEventHandler {

	private EventsManager eventsManager;
	private SharingServiceConfigGroup serviceParams;
	public static final String PERSON_MONEY_EVENT_PURPOSE_SHARING_FARE = "sharingFareTime";
	private Map<Id<Person>, SharingPickupEvent> pickups = new HashMap<>();
	
	public SharingTimeFareHandler(EventsManager eventsManager, SharingServiceConfigGroup serviceParams) {
		this.eventsManager = eventsManager;
		this.serviceParams = serviceParams;
	}
	
	@Override
	public void handleEvent(SharingDropoffEvent event) {
		if (event.getServiceId().toString().equals(serviceParams.getId())) {
			
			double pickuptime = this.pickups.get(event.getPersonId()).getTime();			
			double rentalTime = event.getTime() - pickuptime;
			double sharedFare = rentalTime * serviceParams.getTimeFare();
			eventsManager.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -sharedFare,  PERSON_MONEY_EVENT_PURPOSE_SHARING_FARE, event.getServiceId().toString()));
		}
	}

	@Override
	public void handleEvent(SharingPickupEvent event) {
		if (event.getServiceId().toString().equals(serviceParams.getId())) {
			this.pickups.put(event.getPersonId(), event);
		}
		
	}
	
	@Override
	public void reset(int iteration) {
		pickups.clear();
	}

}
