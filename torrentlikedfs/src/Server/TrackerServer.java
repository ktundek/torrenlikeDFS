package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class TrackerServer implements Constants{
	//private ServerSocket serverSocket;
	//private TrackerServerCore tcs;

	/*public TrackerServer(int port) {
		try {
			serverSocket = new ServerSocket(TRACKER_PORT);
		} catch (IOException e) {			
			System.out.println("Could not listen on the port " + TRACKER_PORT);
			System.exit(-1);
			e.printStackTrace();
		}
	}*/
	
	/*public void run(){		
		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		TrackerItem ti = null;
		
		System.out.println("Waiting for clients...");
		while (true){									
			try {
				socket = serverSocket.accept();
				System.out.println("New client registered on port:"+ socket.getPort());
				ti = new TrackerItem(socket, tcs);
				ti.start();
				//in = new ObjectInputStream(socket.getInputStream());
				//out = new ObjectOutputStream(socket.getOutputStream());
				//out.flush();
				//in.readObject();
				out.writeObject("You are connected to the server on port: "+ socket.getPort());				
			} 
			catch (IOException e) {
				System.out.println("Connection is not accepted");
				e.printStackTrace();
				break;
			}
		}
	}*/
	
	public static void main(String args[]){
		ServerSocket serverSocket = null;
		TrackerServerCore tsc = new TrackerServerCore();		
		int port = 9000;
		Socket socket = null;			
		int serverItemNr = 0;
		
		try {
			serverSocket = new ServerSocket(TRACKER_PORT);
		} catch (IOException e) {			
			System.out.println("Could not listen on the port " + TRACKER_PORT);
			System.exit(-1);
			e.printStackTrace();
		}
		
		System.out.println("Tracker is waiting for clients...");
		while (true){									
			try {
				socket = serverSocket.accept();				
				serverItemNr++;
				System.out.println("New client registered on port:"+ socket.getPort());
				ClientObserver observer = new ClientObserver(tsc, System.currentTimeMillis(), socket.getPort(), serverItemNr);
				TrackerItem ti = new TrackerItem(socket, tsc, observer, serverItemNr);					
			} 
			catch (IOException e) {
				System.out.println("Connection is not accepted");
				e.printStackTrace();
				break;
			}
		}
	}
}
