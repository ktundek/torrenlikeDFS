package Common;

import java.net.InetAddress;

public interface Constants {

    public final static String TRACKER_HOST = "10.14.233.203";//"193.226.40.25";//"10.14.233.251";//"192.168.1.2";;
	//public final static InetAddress TRACKER_HOST = InetAddress.getByName("10.14.233.58");//"193.226.40.25";//"10.14.233.251";//"192.168.1.2";;
    public final static int TRACKER_PORT = 50503; //9000;
    public final static int PEERSERVER_PORT = 50508;
    public final static int TRACKER_ALIVE_TIME = 15000; // if the tracker doesn't get an alive request from the client in every 15 sec, 
    													//then it is considered dead;
    
    public final static int CHUNK_SIZE = 100*1024;	// 100KB
}
