
package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Messages.ChunkReq;

public class PeerClient extends Thread{
	// A PeerClint kapni fogk egy listat azokrol a Seedeerekrol, akiknek megvannak a kert file chunkjai
	// ebbol a listabol fogja kihamozni, h mikor melyik Seederhez kell kapcsolodjon, melyik porton, de addig is...
	private String host;
	private int port;

	public PeerClient(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		this.start();
	}
	
	public void run(){
		/*ObjectOutputStream outs = null; 
		ObjectInputStream ins = null;
		try {
			Socket socket = new Socket(host, port);
			outs = new ObjectOutputStream(socket.getOutputStream());
			outs.flush();
			ins = new ObjectInputStream(socket.getInputStream());
			
			ChunkReq req = new ChunkReq();
			outs.writeObject(req);		
			Object resp = ins.readObject();
			System.out.println(resp);			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
}
