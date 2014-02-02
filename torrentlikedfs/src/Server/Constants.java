package Server;

public interface Constants {

    public final static String TRACKER_HOST = "10.14.232.195";
    public final static int TRACKER_PORT = 9000;
    public final static int TRACKER_ALIVE_TIME = 15000; // if the tracker doesn't get an alive request from the client in every 15 sec, 
    													//then it is considered dead; 
}
