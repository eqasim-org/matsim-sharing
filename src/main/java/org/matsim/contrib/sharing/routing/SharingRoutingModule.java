package org.matsim.contrib.sharing.routing;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.sharing.service.SharingService;
import org.matsim.contrib.sharing.service.SharingUtils;
import org.matsim.core.config.Config;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.Facility;

public class SharingRoutingModule implements RoutingModule {
	private final RoutingModule accessEgressRoutingModule;
	private final RoutingModule mainModeRoutingModule;

	private final InteractionFinder interactionFinder;
	private final Config config;
	private final Network network;
	private final PopulationFactory populationFactory;

	private final Id<SharingService> serviceId;

	public SharingRoutingModule(Scenario scenario, RoutingModule accessEgressRoutingModule,
			RoutingModule mainModeRoutingModule, InteractionFinder interactionFinder, Id<SharingService> serviceId) {
		this.interactionFinder = interactionFinder;
		this.accessEgressRoutingModule = accessEgressRoutingModule;
		this.mainModeRoutingModule = mainModeRoutingModule;
		this.config = scenario.getConfig();
		this.network = scenario.getNetwork();
		this.serviceId = serviceId;
		this.populationFactory = scenario.getPopulation().getFactory();
	}

	@Override
	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
			Person person) {
		List<PlanElement> allElements = new LinkedList<>();

		Optional<InteractionPoint> pickupInteraction = interactionFinder.findPickup(fromFacility);
		Optional<InteractionPoint> dropoffInteraction = interactionFinder.findDropoff(toFacility);

		if (pickupInteraction.isEmpty() || dropoffInteraction.isEmpty()) {
			return null;
		}

		if (pickupInteraction.get().equals(dropoffInteraction.get())) {
			return null;
		}

		// Create walk-out-of-building stage
		List<? extends PlanElement> exitElements = routeAccessEgressStage(fromFacility.getLinkId(),
				fromFacility.getLinkId(), departureTime, person);
		allElements.addAll(exitElements);
		// Create activity where the vehicle is searched for
		Activity bookActivity = createBookingActivity(departureTime, fromFacility.getLinkId());
		bookActivity.setStartTime(departureTime);
		allElements.add(bookActivity);

		// Route pickup stage

		List<? extends PlanElement> pickupElements = routeAccessEgressStage(fromFacility.getLinkId(),
				pickupInteraction.get().getLinkId(), departureTime, person);
		allElements.addAll(pickupElements);

		for (PlanElement planElement : pickupElements) {
			departureTime = TripRouter.calcEndOfPlanElement(departureTime, planElement, config);
		}

		// Pickup activity
		Activity pickupActivity = createPickupActivity(departureTime, pickupInteraction.get());
		pickupActivity.setStartTime(departureTime);
		allElements.add(pickupActivity);

		departureTime = TripRouter.calcEndOfPlanElement(departureTime, pickupActivity, config);

		// Route main stage

		List<? extends PlanElement> mainElements = routeMainStage(pickupActivity.getLinkId(),
				dropoffInteraction.get().getLinkId(), departureTime, person);
		allElements.addAll(mainElements);

		for (PlanElement planElement : pickupElements) {
			departureTime = TripRouter.calcEndOfPlanElement(departureTime, planElement, config);
		}

		// Dropoff activity
		Activity dropoffActivity = createDropoffActivity(departureTime, dropoffInteraction.get());
		dropoffActivity.setStartTime(departureTime);
		allElements.add(dropoffActivity);

		departureTime = TripRouter.calcEndOfPlanElement(departureTime, dropoffActivity, config);

		// Route dropoff stage

		List<? extends PlanElement> dropoffElements = routeAccessEgressStage(dropoffActivity.getLinkId(),
				toFacility.getLinkId(), departureTime, person);
		allElements.addAll(dropoffElements);

		return allElements;
	}

	// TODO: The following two functions are almost an exact replicate of the
	// functions in UserLogic. Try to conslidate.

	private List<? extends PlanElement> routeAccessEgressStage(Id<Link> originId, Id<Link> destinationId,
			double departureTime, Person person) {
		Facility originFacility = new LinkWrapperFacility(network.getLinks().get(originId));
		Facility destinationFacility = new LinkWrapperFacility(network.getLinks().get(destinationId));

		return accessEgressRoutingModule.calcRoute(originFacility, destinationFacility, departureTime, person);
	}

	private List<? extends PlanElement> routeMainStage(Id<Link> originId, Id<Link> destinationId, double departureTime,
			Person person) {
		Facility originFacility = new LinkWrapperFacility(network.getLinks().get(originId));
		Facility destinationFacility = new LinkWrapperFacility(network.getLinks().get(destinationId));

		return mainModeRoutingModule.calcRoute(originFacility, destinationFacility, departureTime, person);
	}

	private Activity createBookingActivity(double now, Id<Link> linkId) {
		Activity activity = populationFactory.createActivityFromLinkId(SharingUtils.BOOKING_ACTIVITY, linkId);
		activity.setStartTime(now);
		activity.setMaximumDuration(SharingUtils.INTERACTION_DURATION);
		SharingUtils.setServiceId(activity, serviceId);
		return activity;
	}

	private Activity createPickupActivity(double now, InteractionPoint interaction) {
		Activity activity = populationFactory.createActivityFromLinkId(SharingUtils.PICKUP_ACTIVITY,
				interaction.getLinkId());
		activity.setStartTime(now);
		activity.setMaximumDuration(SharingUtils.INTERACTION_DURATION);
		SharingUtils.setServiceId(activity, serviceId);

		if (interaction.isStation()) {
			SharingUtils.setStationId(activity, interaction.getStationId().get());
		}

		return activity;
	}

	private Activity createDropoffActivity(double now, InteractionPoint interaction) {
		Activity activity = populationFactory.createActivityFromLinkId(SharingUtils.DROPOFF_ACTIVITY,
				interaction.getLinkId());
		activity.setStartTime(now);
		activity.setMaximumDuration(SharingUtils.INTERACTION_DURATION);
		SharingUtils.setServiceId(activity, serviceId);

		if (interaction.isStation()) {
			SharingUtils.setStationId(activity, interaction.getStationId().get());
		}

		return activity;
	}
}
