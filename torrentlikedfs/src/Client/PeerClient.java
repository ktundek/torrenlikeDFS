
package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import Common.ChunkInfo;
import Common.ChunkManager;
import Common.ChunkPeerList;
import Common.ChunkState;
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
	
	public void run(){			
		FileData fd = chunkList.getFileData();		
		//Map<String, PeerList> chunks = orderChunkList2(chunkList);
		ArrayList<ChunkPeerList> chunks = orderChunkList2(chunkList);
		//Map<String, PeerList> chunks = chunkList.getChunkList();
		//Iterator<Entry<String, PeerList>> it = chunks.entrySet().iterator();
		int chunkNr = -1;

		boolean newFile = false;
		ChunkInfo ci = chunkm.getFileChunkInfo(fd);
		if (ci==null) newFile = true; // true = the peer doesn't have parts of the file
		// false = the peer has parts of the file, we have to download only missing chunks

		/*while (it.hasNext()) {			
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			PeerList peerList = (PeerList) pairs.getValue();*/
		for (int i=0; i<chunks.size();i++){
			ChunkPeerList cplist = chunks.get(i);
			String key = cplist.getChunkName();
			PeerList peerList = cplist.getPeerList();
			//chunkNr++;
			chunkNr = getChunkNumber(key);

			if ((newFile) || (!newFile && isEmpty2(ci, chunkNr))){ // if the peer doesn't have the file or the chunk is missing

				// create ChunkRequest
				ChunkReq req = new ChunkReq(peer);
				req.setFd(fd);
				req.setChunkNr(chunkNr);
				System.out.println("The requested chunk is : "+fd.getName()+", chunk no: "+chunkNr);
				//chunkNr++;
				boolean getNextPeer = true;

				while(getNextPeer){ // while we cannot find an online peer
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
							getNextPeer = false;
						} catch (IOException e) {
							System.out.println("The selected peer was offline. We delete it and select an another one.");
							peerList.removeItem(pd); // delete offline peer
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}				
					}
					else{// if none of the peers have the chunk, the peer will ask from the server
						System.out.println("The peer will get the chunk from the server");	
						getNextPeer = false;
						phandler.sendMessage(req);
					}
				}
			}
		}		
	}

	public synchronized ArrayList<ChunkPeerList> orderChunkList2(ChunkListResp resp){
		Map<String, PeerList> chunks = resp.getChunkList(); // <chunkName, PeerList>		
		ArrayList<ChunkPeerList> cplist = new ArrayList<ChunkPeerList>();
		ArrayList<ChunkPeerList> new_cplist = new ArrayList<ChunkPeerList>();
		int [] occurence = new int [chunks.size()];
		int nr = 0;
		int nr2 = 0;
		
		Iterator<Entry<String, PeerList>> it = chunks.entrySet().iterator();			
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			PeerList pl = (PeerList) pairs.getValue();			
			ChunkPeerList cpl = new ChunkPeerList(key, pl);
			cplist.add(cpl);
			occurence[nr] = pl.size();
			++nr;
		}
				
		if (occurence.length>0){
			java.util.Arrays.sort(occurence);
			for (int i=0; i<occurence.length; i++){
				for (int j=0; j<cplist.size(); j++){
					nr2 = 0;
					if (occurence[i]==cplist.get(j).getPeerList().size()){
						ChunkPeerList list = new ChunkPeerList(cplist.get(j).getChunkName(), cplist.get(j).getPeerList());
						
						for (int k=0; k<new_cplist.size(); k++){
							if (new_cplist.get(k).getChunkName().equals(list.getChunkName())){
								System.out.println("---ADD--");
								nr2++;					
							}
						}
						
						if (nr2==0){new_cplist.add(list);}						
					}
				}
			}
		}
		return new_cplist;
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
		 //System.out.println("---Rendezes elott---");
		 //writeOutChunkList(chunks);
		 //System.out.println("---Rendezes elott vege---");
		 
		 
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
	
	public boolean isEmpty(ChunkInfo ci, String chunkName){		
		boolean empty = true;
		//System.out.println("PeerCLient: isEmpty: Chunkname: "+ chunkName);
		//String[] part1 = chunkName.split("\\."); // chunk name ex.: asdf.txt1234afc50_1.chnk
		//for (int k=0; k<part1.length;k++) System.out.println("part1["+k+"]: "+part1[k]);
		//String[] part2 = part1[1].split("_"); // in part2 will be: part2[0]=txt1234afc50	part2[1]=1
		//for (int k=0; k<part2.length;k++) System.out.println("part2["+k+"]: "+part2[k]);
		//int chunkNr = Integer.parseInt(part2[1]);
		int chunkNr = getChunkNumber(chunkName);
		if (!ci.getState(chunkNr).equals(ChunkState.EMPTY)) {empty=false; System.out.println("chunkNr: "+ chunkNr+"chunkState: "+ci.getState(chunkNr).toString());}
		else System.out.println("chunkNr: "+ chunkNr+" chunkState: "+ci.getState(chunkNr).toString());		
		return empty;
	}
	
	public int getChunkNumber(String chunkName){
		String[] part1 = chunkName.split("\\."); // chunk name ex.: asdf.txt1234afc50_1.chnk
		String[] part2 = part1[1].split("_"); // in part2 will be: part2[0]=txt1234afc50	part2[1]=1
		return Integer.parseInt(part2[1]);
	}
	
	public boolean isEmpty2(ChunkInfo ci, int chunkNr){		
		boolean empty = true;		
		if (!ci.getState(chunkNr).equals(ChunkState.EMPTY)) {empty=false; System.out.println("chunkNr: "+ chunkNr+"chunkState: "+ci.getState(chunkNr).toString());}
		else System.out.println("chunkNr: "+ chunkNr+" chunkState: "+ci.getState(chunkNr).toString());		
		return empty;
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
