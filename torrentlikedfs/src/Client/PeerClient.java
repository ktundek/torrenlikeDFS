
package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import Common.ChunkInfo;
import Common.ChunkManager;
import Common.FileData;
import Common.PeerList;
import Messages.ChunkListResp;
import Messages.ChunkReq;
import Messages.ChunkResp;

public class PeerClient extends Thread{
	// A PeerClient kapni fogk egy listat azokrol a Seedeerekrol, akiknek megvannak a kert file chunkjai
	// ebbol a listabol fogja kihamozni, h mikor melyik Seederhez kell kapcsolodjon, melyik porton, de addig is...
	private ChunkManager chunkm = null;
	private ChunkListResp chunkList = null;
	private InetAddress host;
	private int port;

	public PeerClient(ChunkManager chunkm, ChunkListResp chunkList){
		super();
		this.chunkm = chunkm;
		this.chunkList = chunkList;
		this.start();
	}
	/*public PeerClient(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		this.start();
	}*/
	
	public void run(){			
		FileData fd = chunkList.getFileData();
		Map<String, PeerList> chunks = chunkList.getChunkList();		
		Iterator<Entry<String, PeerList>> it = chunks.entrySet().iterator();
		int chunkNr = 0;
		while (it.hasNext()) {			
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			PeerList peerList = (PeerList) pairs.getValue(); 
			
			Random rnd = new Random();
			int nr = rnd.nextInt(peerList.size());
			PeerData pd = peerList.getItem(nr);
			System.out.println("The random number is: "+nr+" so the selected seeder is: "+pd.getInetAddress()+", port: "+pd.getPort());
			
			ChunkReq req = new ChunkReq(pd);
			req.setFd(fd);
			req.setChunkNr(chunkNr);
			System.out.println("The requested chunk is from: "+fd.getName()+", chunk no: "+chunkNr);
			chunkNr++;
			
			ObjectOutputStream outs = null; 
			ObjectInputStream ins = null;
			host = pd.getInetAddress();
			port = pd.getPort();
			try {
				Socket socket = new Socket(host, port);
				outs = new ObjectOutputStream(socket.getOutputStream());
				outs.flush();
				ins = new ObjectInputStream(socket.getInputStream());
								
				outs.writeObject(req);		
				Object resp = ins.readObject();
				System.out.println(resp);
				ChunkResp chunkResp = (ChunkResp)resp;
				chunkm.onChunkRespPeer(chunkResp);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
			
		}

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
