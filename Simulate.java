import java.util.PriorityQueue;
import java.io.IOException;
import java.lang.Number;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Simulate {

  static double totalHandovers;
  static double totalCalls;
  static double totalBlockedCalls;
  static double totalDroppedCalls;
  static double percentageBlockedCalls;
  static double percentageDroppedCalls;
  static double simClock;
  static boolean[][] stationChannelIsFree = new boolean[20][10];

  // use a priority queue to schedule events
  static PriorityQueue<Event> pq = new PriorityQueue<Event>(10, new EventComparator());

  public static void main(String []args) {
    // to determine warmup period
    int replicationsBeta = 100;
    int updateIntervalBeta = 20;
    double simClockLimitBeta = 400010;
    recordInterval(replicationsBeta, updateIntervalBeta,
                   simClockLimitBeta, "stats_indi_no_res.txt", "no reservation");
    recordInterval(replicationsBeta, updateIntervalBeta,
                   simClockLimitBeta, "stats_indi_one_res.txt", "one reservation");

    // start collecting actual results
    int replications = 1000;
    int updateInterval = 100000;
    double warmUpLimit = 200000;
    double simClockLimit = 400010;
    loadSimulation(replications, updateInterval, warmUpLimit,
                   simClockLimit, "stats_no_res.txt", "no reservation");
    loadSimulation(replications, updateInterval, warmUpLimit,
                   simClockLimit, "stats_one_res.txt", "one reservation");
  }

  public static void recordInterval(int replications, int updateInterval,
                                    double simClockLimit, String filename, String fcaScheme) {
    for(int k = 0; k < replications; k++) {

      // reset all state variables
      totalHandovers = 0.00;
      totalCalls = 0.00;
      totalBlockedCalls = 0.00;
      totalDroppedCalls = 0.00;
      for(int i = 0; i < 20; i++) {
        for(int j = 0; j < 10; j++) stationChannelIsFree[i][j] = true;
      }

      double simClock = 0.00;
      double nextEventClock = 0.00;
      int i = 0;
      do {
        Event e = new Event(nextEventClock);
        pq.add(e);

        // update next event's arrival time using hypothesised interarrival time's distribution
        nextEventClock += Math.log(1- Math.random())/ (-1/1.36829041525);

        Event event = pq.remove();
        simClock = event.arrivalTime;
        eventHandler(event, fcaScheme);

        if((((Double) simClock).intValue() % updateInterval) <= 10) {
          if(i == 0) {
            percentageBlockedCalls = (totalBlockedCalls/totalCalls) * 100;
            percentageDroppedCalls = (totalDroppedCalls/totalCalls) * 100;
            String toSend = Integer.toString(k) + "\t" +
                            Double.toString(percentageBlockedCalls) + "\t" +
                            Double.toString(percentageDroppedCalls) + "\t" +
                            Double.toString(totalHandovers);
            appendFile(toSend, filename);
          }
          i++;
        }
        else i=0;
      } while(simClock < simClockLimit);

      System.out.println("Replication " + k + " done.");
      pq = new PriorityQueue<Event>(10, new EventComparator());
    }
  }

  public static void loadSimulation(int replications, int updateInterval, double warmUpLimit,
                                    double simClockLimit, String filename, String fcaScheme) {
    for(int k = 0; k < replications; k++) {

      // reset all state variables
      totalHandovers = 0.00;
      totalCalls = 0.00;
      totalBlockedCalls = 0.00;
      totalDroppedCalls = 0.00;
      for(int i = 0; i < 20; i++) {
        for(int j = 0; j < 10; j++) stationChannelIsFree[i][j] = true;
      }

      double simClock = 0.00;
      double nextEventClock = 0.00;
      int i = 0;
      do {
        Event e = new Event(nextEventClock);
        pq.add(e);

        // update next event's arrival time using hypothesised interarrival time's distribution
        nextEventClock += Math.log(1- Math.random())/ (-1/1.36829041525);

        Event event = pq.remove();
        simClock = event.arrivalTime;
        eventHandler(event, fcaScheme);
        if(simClock < warmUpLimit) {
          totalBlockedCalls = 0;
          totalDroppedCalls = 0;
          totalCalls = 0;
        }

        if((((Double) simClock).intValue() % updateInterval) <= 10) {
          if(i == 0) {
            percentageBlockedCalls = (totalBlockedCalls/totalCalls) * 100;
            percentageDroppedCalls = (totalDroppedCalls/totalCalls) * 100;

            System.out.println("Current simulation clock: " + simClock);
            System.out.println("FCA Scheme: " + fcaScheme + " -------------------------------------->");
            System.out.println("Percentage of calls blocked: " + Double.toString(percentageBlockedCalls));
            System.out.println("Percentage of calls dropped: " + Double.toString(percentageDroppedCalls));
            System.out.println("Number of handovers: " + Double.toString(totalHandovers));
          }
          i++;
        }
        else i=0;
      } while(simClock < simClockLimit);

      String toSend = Double.toString(percentageBlockedCalls) + "\t" +
                      Double.toString(percentageDroppedCalls) + "\t" +
                      Double.toString(totalHandovers);
      appendFile(toSend, filename);
      System.out.println("Replication " + k + " done.");

      pq = new PriorityQueue<Event>(10, new EventComparator());
    }
  }

  public static void appendFile(String aString, String filename) {
    BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			File file = new File(filename);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);

			bw.write(aString);
      bw.write(System.getProperty("line.separator"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        if (bw != null) bw.close();
        if (fw != null) fw.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public static void eventHandler(Event e, String fcaScheme) {
    if(e.eventType.equals("Initiation")) initiation(e, fcaScheme);
    else if(e.eventType.equals("Handover")) handOver(e);
    else if(e.eventType.equals("Termination")) terminate(e);
  }

  public static void initiation(Event e, String fcaScheme) {

    if((anyChannelsLeft(stationChannelIsFree[e.baseStation - 1], 0) && fcaScheme.equals("no reservation")) ||
       (anyChannelsLeft(stationChannelIsFree[e.baseStation - 1], 1) && fcaScheme.equals("one reservation"))) {
      // this block occurs when calls cannot be allocated
      Event updatedEvent = updateProblemEvent(e, "Termination", "Blocked");
      pq.add(updatedEvent);
      return;
    }
    else { // this block occurs when calls can be allocated
      // remember channel used so that it can be deallocated on this event's next call
      e.channelUsed = allocateChannel(stationChannelIsFree[e.baseStation - 1]);

      double durationLeftInStation = (2 - e.position) / (e.velocity / 3600);

      if(e.baseStation == 20 && e.callDuration > durationLeftInStation){
        // this block handles calls occuring at the last base station
        Event updatedEvent = updateEvent(e, 0, durationLeftInStation, e.callDuration, "Termination");
        pq.add(updatedEvent);
        return;
      }
      else if(e.callDuration <= durationLeftInStation) {
        // this block occurs when call finishes at current station
        Event updatedEvent = updateEvent(e, 0, e.callDuration, e.callDuration, "Termination");
        pq.add(updatedEvent);
        return;
      }
      else {
        // this block occurs when user crosses to the next station during a call
        Event updatedEvent = updateEvent(e, 1, durationLeftInStation, durationLeftInStation, "Handover");
        pq.add(updatedEvent);
        return;
      }
    }
  }

  public static void handOver(Event e) {
    totalHandovers++;

    //free previous station's used channel
    stationChannelIsFree[e.baseStation - 2][e.channelUsed] = true;

    if(anyChannelsLeft(stationChannelIsFree[e.baseStation - 1], 0)) {
      // this block occurs when calls cannot be allocated
      Event updatedEvent = updateProblemEvent(e, "Termination", "Dropped");
      pq.add(updatedEvent);
      return;
    }
    else { // this block occurs when calls can be allocated
      // remember channel used so that it can be deallocated on this event's next call
      e.channelUsed = allocateChannel(stationChannelIsFree[e.baseStation - 1]);

      double durationLeftInStation = 2 / (e.velocity / 3600);

      if(e.baseStation == 20 && e.callDuration > durationLeftInStation){
        // this block handles calls occuring at the last base station
        Event updatedEvent = updateEvent(e, 0, durationLeftInStation, e.callDuration, "Termination");
        pq.add(updatedEvent);
        return;
      }
      else if(e.callDuration <= durationLeftInStation) {
        // this block occurs when call finishes at current station
        Event updatedEvent = updateEvent(e, 0, e.callDuration, e.callDuration, "Termination");
        pq.add(updatedEvent);
        return;
      }
      else {
        // this block occurs when user crosses to the next station during a call
        Event updatedEvent = updateEvent(e, 1, durationLeftInStation, durationLeftInStation, "Handover");
        pq.add(updatedEvent);
        return;
      }
    }
  }

  public static Event updateProblemEvent(Event e, String changeEventType, String updateStatus) {
    e.eventType = changeEventType;
    e.status = updateStatus;
    return e;
  }

  public static Event updateEvent(Event e, int advStation, double advArrivalTime,
                                  double reduceCallDuration, String changeEventType) {
    e.baseStation += advStation;
    e.arrivalTime += advArrivalTime;
    e.callDuration -= reduceCallDuration;
    e.eventType = changeEventType;
    return e;
  }

  public static void terminate(Event e) {
    // only increase stat counters for finished events, prematurely changing data might misconstrue data
    totalCalls++;
    if(e.status.equals("Fine")) stationChannelIsFree[e.baseStation - 1][e.channelUsed] = true; //free channel
    else if(e.status.equals("Blocked")) totalBlockedCalls++;
    else if(e.status.equals("Dropped")) totalDroppedCalls++;
  }

  // second parameter describes number of channels that station should be left with
  public static boolean anyChannelsLeft(boolean[] station, int channelsLeft){
    int count = 0;
    for(int i = 0; i < station.length; i++) {
      if(station[i] == true) count++;
    }
    if(count <= channelsLeft) return true;
    return false;
  }

  public static int allocateChannel(boolean[] station) {
    for(int i = 0; i < station.length; i++) {
      if(station[i] == true) {
        station[i] = false;
        return i;
      }
    }
    return -1;
  }
}
