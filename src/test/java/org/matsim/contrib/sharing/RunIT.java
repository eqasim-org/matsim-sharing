package org.matsim.contrib.sharing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.contrib.sharing.run.SharingConfigGroup;
import org.matsim.contrib.sharing.run.SharingModule;
import org.matsim.contrib.sharing.run.SharingServiceConfigGroup;
import org.matsim.contrib.sharing.run.SharingServiceConfigGroup.ServiceScheme;
import org.matsim.contrib.sharing.service.SharingUtils;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.UncheckedIOException;

public class RunIT {

	@Test
	public final void test() throws UncheckedIOException, ConfigurationException {
		URL fixtureUrl = getClass().getClassLoader().getResource("siouxfalls");
		CommandLine cmd = new CommandLine.Builder(new String[] {
				"--config-path", fixtureUrl.getFile() + "/config.xml",//
				"--config:controler.lastIteration", "2"}) //
				.requireOptions("config-path") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"));
		cmd.applyConfiguration(config);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		// We need to add the sharing config group
		SharingConfigGroup sharingConfig = new SharingConfigGroup();
		config.addModule(sharingConfig);

		// We need to define a service ...
		SharingServiceConfigGroup serviceConfig = new SharingServiceConfigGroup();
		sharingConfig.addService(serviceConfig);

		// ... with a service id. The respective mode will be "sharing:velib".
		serviceConfig.setId("mobility");

		// ... with freefloating characteristics
		serviceConfig.setMaximumAccessEgressDistance(100000);
		serviceConfig.setServiceScheme(ServiceScheme.StationBased);
		serviceConfig.setServiceAreaShapeFile(null);

		// ... with a number of available vehicles and their initial locations
		serviceConfig.setServiceInputFile("shared_vehicles.xml");

		// ... and, we need to define the underlying mode, here "car".
		serviceConfig.setMode("car");

		// Finally, we need to make sure that the service mode (sharing:velib) is
		// considered in mode choice.
		List<String> modes = new ArrayList<>(Arrays.asList(config.subtourModeChoice().getModes()));
		modes.add(SharingUtils.getServiceMode(serviceConfig));
		config.subtourModeChoice().setModes(modes.toArray(new String[modes.size()]));
		
		SharingServiceConfigGroup serviceConfigBike = new SharingServiceConfigGroup();
		sharingConfig.addService(serviceConfigBike);

		// ... with a service id. The respective mode will be "sharing:velib".
		serviceConfigBike.setId("velib");

		// ... with freefloating characteristics
		serviceConfigBike.setMaximumAccessEgressDistance(100000);
		serviceConfigBike.setServiceScheme(ServiceScheme.StationBased);
		serviceConfigBike.setServiceAreaShapeFile(null);

		// ... with a number of available vehicles and their initial locations
		serviceConfigBike.setServiceInputFile("shared_vehicles.xml");

		// ... and, we need to define the underlying mode, here "car".
		serviceConfigBike.setMode("bike");

		// Finally, we need to make sure that the service mode (sharing:velib) is
		// considered in mode choice.
		modes = new ArrayList<>(Arrays.asList(config.subtourModeChoice().getModes()));
		modes.add(SharingUtils.getServiceMode(serviceConfigBike));
		config.subtourModeChoice().setModes(modes.toArray(new String[modes.size()]));
		
		
		SharingServiceConfigGroup serviceConfigBikeFF = new SharingServiceConfigGroup();
		sharingConfig.addService(serviceConfigBikeFF);

		// ... with a service id. The respective mode will be "sharing:velib".
		serviceConfigBikeFF.setId("wheels");

		// ... with freefloating characteristics
		serviceConfigBikeFF.setMaximumAccessEgressDistance(100000);
		serviceConfigBikeFF.setServiceScheme(ServiceScheme.Freefloating);
		serviceConfigBikeFF.setServiceAreaShapeFile(null);

		// ... with a number of available vehicles and their initial locations
		serviceConfigBikeFF.setServiceInputFile("shared_vehicles.xml");

		// ... and, we need to define the underlying mode, here "car".
		serviceConfigBikeFF.setMode("bike");

		// Finally, we need to make sure that the service mode (sharing:velib) is
		// considered in mode choice.
		modes = new ArrayList<>(Arrays.asList(config.subtourModeChoice().getModes()));
		modes.add(SharingUtils.getServiceMode(serviceConfigBikeFF));
		config.subtourModeChoice().setModes(modes.toArray(new String[modes.size()]));
		
		
		

		// We need to add interaction activity types to scoring
		ActivityParams pickupParams = new ActivityParams(SharingUtils.PICKUP_ACTIVITY);
		pickupParams.setScoringThisActivityAtAll(false);
		config.planCalcScore().addActivityParams(pickupParams);

		ActivityParams dropoffParams = new ActivityParams(SharingUtils.DROPOFF_ACTIVITY);
		dropoffParams.setScoringThisActivityAtAll(false);
		config.planCalcScore().addActivityParams(dropoffParams);
		
		ActivityParams bookingParams = new ActivityParams(SharingUtils.BOOKING_ACTIVITY);
		bookingParams.setScoringThisActivityAtAll(false);
		config.planCalcScore().addActivityParams(bookingParams);

		// We need to score car
		ModeParams carScoringParams = new ModeParams("car");
		config.planCalcScore().addModeParams(carScoringParams);
		
		// We need to score bike
		ModeParams bikeScoringParams = new ModeParams("bike");
		config.planCalcScore().addModeParams(bikeScoringParams);

		// Set up controller (no specific settings needed for scenario)
		Controler controller = new Controler(config);

		// Does not really "override" anything
		controller.addOverridingModule(new SharingModule());

		// Enable QSim components
		controller.configureQSimComponents(SharingUtils.configureQSim(sharingConfig));

		controller.run();
		
		Map<String, Long> counts = countLegs(controller.getControlerIO().getOutputPath() + "/output_events.xml.gz");
		Assert.assertEquals(29370, (long) counts.get("car"));
		Assert.assertEquals(26837, (long) counts.get("walk"));
		Assert.assertEquals(90, (long) counts.get("bike"));
		Assert.assertEquals(10150, (long) counts.get("pt"));
	}
	
	static Map<String, Long> countLegs(String eventsPath) {
		EventsManager manager = EventsUtils.createEventsManager();

		Map<String, Long> counts = new HashMap<>();
		manager.addHandler((PersonDepartureEventHandler) event -> {
			counts.compute(event.getLegMode(), (k, v) -> v == null ? 1 : v + 1);
		});

		new MatsimEventsReader(manager).readFile(eventsPath);

		System.out.println("Counts:");
		for (Map.Entry<String, Long> entry : counts.entrySet()) {
			System.out.println("  " + entry.getKey() + " " + entry.getValue());
		}

		return counts;
	}

}
