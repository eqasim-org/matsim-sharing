package org.matsim.contrib.sharing.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

import org.matsim.contrib.sharing.service.RentalInfo;
import org.matsim.contrib.sharing.service.SharingTeleportedRentalsHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.utils.io.IOUtils;

import com.google.inject.Inject;

public class SharingTeleportedControlerListener implements StartupListener, IterationEndsListener {

	private final SharingTeleportedRentalsHandler sharingHandler;
	private final EventsManager eventsManager;

	@Inject
	public SharingTeleportedControlerListener(SharingTeleportedRentalsHandler sharingHandler,
			EventsManager eventsManager) {
		this.sharingHandler = sharingHandler;
		this.eventsManager = eventsManager;

	}

	@Override
	public void notifyStartup(StartupEvent event) {

		this.eventsManager.addHandler(this.sharingHandler);

	}

	public void notifyIterationEnds(IterationEndsEvent event) {

		if (event.getIteration() > 0) {
			// write all data gathered in csv files
			String path = event.getServices().getControlerIO().getIterationPath(event.getIteration())
					+ "/sharingrentals.csv";

			Set<RentalInfo> rentals = this.sharingHandler.getRentals();
			try {
				// Because we want to write to CSV we would use the generic bufferred writer and
				// specify the csv file name
				BufferedWriter writer = IOUtils.getBufferedWriter(path);
				writer.write(
						"personid;starttime;endtime;distancedriven;pickupstationid;pickupstationlinkid;dropoffstationid;dropoffstationlinkid;"
								+ "vehicleid;serviceid\n");
				for (RentalInfo rental : rentals) {

					writer.write(String.join(";", rental.personId.toString(), Double.toString(rental.startTime),
							Double.toString(rental.endTime), Double.toString(rental.distance), rental.pickupStationId,
							rental.pickupLinkId.toString(), rental.dropoffStationId, rental.dropoffLinkId.toString(),
							rental.vehicleId, rental.serviceId.toString()));
					writer.newLine();
				}
				// flush() tells the Writer to output the stream
				writer.flush();

				// it is good practice to close the stream when no more output is expected
				writer.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

}
