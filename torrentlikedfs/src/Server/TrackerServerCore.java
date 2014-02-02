package Server;

import Client.FileData;
import Client.Group;
import Client.PeerData;
import Client.PeerItem;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.ServerRespMessageItems;
import Messages.ServerRespMessages;
import Messages.UnRegisterPeerReq;

public class TrackerServerCore {
	private PeerItemList peerIList = null;
	private GroupList groupList = null;
	
	public TrackerServerCore() {
		peerIList = new PeerItemList();
		groupList = new GroupList();
	}

	public synchronized ServerRespMessages registerPeer(RegisterPeerReq rpr, int port){
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		PeerData peerData = rpr.getPeerData();
		PeerItem peerItem = new PeerItem(peerData, port);
				
		System.out.println("--------------------REGISTER PEER-----------------");
		if (peerIList.contains(peerItem)){
			msg = ServerRespMessageItems.NACK_REG_PEER;
		}
		else{
			peerIList.addItem(peerItem);
			System.out.println("Nr. of peers: "+ getNrRegisteredPeer());
			peerIList.toStringList();
		}
		System.out.println("----------------END REGISTER PEER-----------------");
		return msg;
	}
	
	public synchronized void unregisterPeer(PeerItem peerItem){						
		peerIList.toStringList();		
		int index = peerIList.containsPeerItemIndex(peerItem);
		if (index!= -1) {
			peerIList.deleteItem(index);
			System.out.println("USER WAS DELETED!");
		}
		else System.out.println("UNREGISTER PEER: Can not find Peer in the list!");		
	}
	
	public synchronized ServerRespMessages registerGroup(RegisterGroupReq rgr){
		System.out.println("-----------------REGISTER GROUP------------------");
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		Group group = rgr.getGroup();
		if (groupList.contains(group)){   
			msg = ServerRespMessageItems.NACK_REG_GROUP;
		}		
		else{	
			groupList.addItem(group);
		}
		System.out.println("-------------END REGISTER GROUP------------------");
		return msg;
	}
	
	public synchronized FileData getFile(){
		FileData fd = null;
		//for (int i=0; i<groupList.)
		return fd;
	}
	
	public int getNrRegisteredPeer(){		
		return peerIList.getSize();		
	}
}
