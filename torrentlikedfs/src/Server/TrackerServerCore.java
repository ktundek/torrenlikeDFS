package Server;

import Client.Group;
import Client.PeerData;
import Client.PeerItem;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
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

	public synchronized ServerRespMessages registerPeer(PeerItem peerItem){
		ServerRespMessages msg = ServerRespMessageItems.ACK;		
		
		//if (peerIList.contains(peerItem) || peerIList.containsPeerData(peerItem.getPeerData())){
		if (peerIList.contains(peerItem)){
			msg = ServerRespMessageItems.NACK_REG_PEER;
		}
		else{
			peerIList.addItem(peerItem);
		}	
		return msg;
	}
	
	public synchronized void unregisterPeer(PeerItem peerItem){		
		//PeerItem peerItem = unrpr.getPeerItem();
		peerIList.toStringList();
		System.out.println("PEER ITEM:" + peerItem.getPort()+", "+ peerItem.getPeerData().getInetAddress());
		if (peerIList.contains(peerItem)){
			peerIList.deleteItem(peerItem);
			// if user was part of a group
		}else System.out.println("User wasn't registerd!");
	}
	
	public synchronized ServerRespMessages registerGroup(RegisterGroupReq rgr){
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		Group group = rgr.getGroupitem();
		if (groupList.contains(group)){
			msg = ServerRespMessageItems.NACK_REG_GROUP;
		}		
		else{	
			groupList.addItem(group);
		}
		return msg;
	}
	
	public int getNrRegisteredPeer(){
		peerIList.toStringList();
		return peerIList.getSize();		
	}
}
