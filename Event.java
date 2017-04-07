public class Event {

  double arrivalTime;
  double callDuration;
  double velocity;
  double position;
  int baseStation;
  int channelUsed = -1; // -1 indicates not used
  String eventType = "Initiation";
  String status = "Fine";

  public Event(double clock){
    this.arrivalTime = clock;
    this.callDuration = (Math.log(1- Math.random())/ (-1/99.8319274596)) + 10;
    double tmp = 0.0;
    for(int i = 0; i < 50; i++) tmp += Math.random();
    this.velocity = 120.0720949 + 9.0186046585430262 * (tmp - 25) / Math.sqrt(4.16666666667);
    this.position = Math.random() * 2;
    this.baseStation = 1 + (int) (Math.random() * 20);
  }
}
