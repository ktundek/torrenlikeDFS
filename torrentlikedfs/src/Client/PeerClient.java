
package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import Common.ChunkInfo;
import Common.ChunkManager;
import Common.Constants;
import Common.FileData;
import Common.PeerList;
import Messages.ChunkListResp;
import Messages.ChunkReq;
import Messages.ChunkResp;

public class PeerClient extends Thread{
	// A PeerClient kapni fogk egy listat azokrol a Seedeerekrol, akiknek megvannak a kert file chunkjai
	// ebbol a listabol fogja kihamozni, h mikor melyik Seederhez kell kapcsolodjon, melyik porton, de addig is...
	private ChunkManager chunkm = null;
	private PeerHandler phandler = null;
	private ChunkListResp chunkList = null;
	private PeerData peer;
	//private String host;
	private InetAddress host;
	private int port;

	public PeerClient(ChunkManager chunkm, PeerHandler phandler, ChunkListResp chunkList, PeerData peer){
		super();
		this.chunkm = chunkm;
		this.phandler = phandler;
		this.chunkList = chunkList;
		this.peer = peer;
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
		//Map<String, PeerList> list = chunkList.getChunkList();
		Map<String, PeerList> chunks = orderChunkList(chunkList);
		Iterator<Entry<String, PeerList>> it = chunks.entrySet().iterator();
		int chunkNr = 0;
		while (it.hasNext()) {			
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			PeerList peerList = (PeerList) pairs.getValue(); 
			
			ChunkReq req = new ChunkReq(peer);
			req.setFd(fd);
			req.setChunkNr(chunkNr);
			System.out.println("The requested chunk is from: "+fd.getName()+", chunk no: "+chunkNr);
			chunkNr++;

			if (peerList.size()>0){
				Random rnd = new Random();
				int nr = rnd.nextInt(peerList.size());
				PeerData pd = peerList.getItem(nr);
				System.out.println("The random number is: "+nr+" so the selected seeder is: "+pd.getInetAddress()+", port: "+pd.getPort());			

				host = pd.getInetAddress();
				port = pd.getPort();								

				ObjectOutputStream outs = null; 
				ObjectInputStream ins = null;
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
					socket.close();			
				} catch (IOException e) {				
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			else{// if none of the peers have the chunk, the peer will ask from the server
				System.out.println("The peer will get the chunk from the server");
				/*try {
					host = InetAddress.getByName(Constants.TRACKER_HOST);
				} catch (UnknownHostException e) {
					System.out.println("----------------Hat ez nem jott be!!!");
					e.printStackTrace();
				}
				port = Constants.TRACKER_PORT;*/
				phandler.sendMessage(req);
			}
		}		
	}
	
	//
	public synchronized Map<String, PeerList> orderChunkList(ChunkListResp resp){
		 Map<String, PeerList> chunks = resp.getChunkList(); // <chunkName, PeerList>
		 Map<String, PeerList> new_chunks =  Collections.synchronizedMap(new HashMap<String, PeerList>()); // <chunkName, PeerList>		 		 
		 HashMap<String,Integer> occurrence = new HashMap<String,Integer>(); //<chunkName, nr of peers>
		 //TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>();
		 SortedSet<Map.Entry<String,Integer>> sortedEntries = new TreeSet<Map.Entry<String,Integer>>();

		 Iterator<Entry<String, PeerList>> it = chunks.entrySet().iterator();			
		 while (it.hasNext()) {			
			 Map.Entry pairs = (Map.Entry)it.next();
			 String key = (String) pairs.getKey();
			 PeerList pl = (PeerList) pairs.getValue();
			 occurrence.put(key, pl.size());				
		 }
		 System.out.println("---Rendezes elott---");
		 writeOutChunkList(chunks);
		 System.out.println("---Rendezes elott vege---");
		 
		 
		 //sorted_map.putAll(occurrence);
		 sortedEntries = entriesSortedByValues(occurrence);
		 System.out.println("sortedEntries: "+sortedEntries);
		 		 

		 //Iterator<Entry<String, Integer>> it2 = sorted_map.entrySet().iterator();
		 Iterator<Entry<String, Integer>> it2 = sortedEntries.iterator();
		 while(it2.hasNext()){
			 Map.Entry pairs2 = (Map.Entry)it2.next(); 
			 String key2 = (String) pairs2.getKey(); // we get the key from the sorted map
			 PeerList pl = chunks.get(key2); // we get the PeerList from the unsorted chunk list 
			 
			 new_chunks.put(key2, pl); // in this list <chunkName, PeerList> is sorted by the number of peers
		 }
		 System.out.println("---Rendezes utan---");
		 writeOutChunkList(new_chunks);
		 System.out.println("---Rendezes utan vege---");		 
		 return new_chunks;
	}
	
	// this method was copied from here: http://stackoverflow.com/questions/2864840/treemap-sort-by-value
	static <String,Integer extends Comparable<? super Integer>> SortedSet<Map.Entry<String,Integer>> entriesSortedByValues(Map<String,Integer> map) {
        SortedSet<Map.Entry<String,Integer>> sortedEntries = new TreeSet<Map.Entry<String,Integer>>(
            new Comparator<Map.Entry<String,Integer>>() {
                @Override public int compare(Map.Entry<String,Integer> e1, Map.Entry<String,Integer> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());        
        return sortedEntries;
    }
	
	public synchronized void writeOutChunkList(Map<String, PeerList> list){							
		Iterator<Entry<String, PeerList>> it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			System.out.println("the key:" + key);

			PeerList value = list.get(key);
			System.out.println("ChunkList size: "+value.size());
			for (int k=0; k<value.size(); k++){
				System.out.println("        "+value.getPeerData(k)+" ");			
			}
		}
	}
	
}
