package Common;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import Client.Peer;
import Client.PeerData;
import Messages.ChunkListResp;

public class Test {
	private ChunkListResp chunkListresp = null;
	
	public Test() {
		super();
	}

	public ChunkListResp getChunkList() {
		return chunkListresp;
	}

	public void setChunkList(ChunkListResp chunkListresp) {
		this.chunkListresp = chunkListresp;
	}
	
	public ChunkListResp createChunkList(PeerData peerData){
		FileData fileData = createFileData();
		Map<String, PeerList> chunkList = createChunkMap();
		ChunkListResp list = new ChunkListResp(peerData, chunkList, fileData);
		//chunkListresp = list;
		return list;
	}
	
	public FileData createFileData(){
		File file = new File("C:/Peer/life.pdf");
		FileData fd = new FileData(file);
		return fd;
	}
	
	public Map<String, PeerList> createChunkMap(){
		Map<String, PeerList> chunkList = new HashMap<String, PeerList>();
		String chnk0 = "life.pdf213020dfd35_0.chnk";
		String chnk1 = "life.pdf213020dfd35_1.chnk";
		String chnk2 = "life.pdf213020dfd35_2.chnk";
		chunkList.put(chnk0, createPeerList0());
		chunkList.put(chnk1, new PeerList());
		chunkList.put(chnk2, createPeerList1());
		return chunkList;
	}
	
	public PeerList createPeerList0(){
		PeerList peerList = new PeerList();
		PeerData pd1 = null;
		PeerData pd2 = null;
		PeerData pd3 = null;
		try {
			pd1 = new PeerData(1234, InetAddress.getByName("10.14.233.56"));
			pd2 = new PeerData(1234, InetAddress.getByName("10.14.233.55"));
			pd3 = new PeerData(1234, InetAddress.getByName("10.14.233.54"));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		peerList.addItem(pd1);
		peerList.addItem(pd2);
		peerList.addItem(pd3);
		return peerList;
	}
	
	public PeerList createPeerList1(){
		PeerList peerList = new PeerList();
		PeerData pd1 = null;		
		try {
			pd1 = new PeerData(1234, InetAddress.getByName("10.14.233.56"));			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		peerList.addItem(pd1);		
		return peerList;
	}
}
