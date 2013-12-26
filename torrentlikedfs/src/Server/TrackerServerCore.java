package Server;

import Client.Group;
import Client.PeerData;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.ServerRespMessageItems;
import Messages.ServerRespMessages;
import Messages.UnRegisterPeerReq;

public class TrackerServerCore {
	private PeerDataList peerdList = null;
	private GroupList groupList = null;
	
	public TrackerServerCore() {
		peerdList = new PeerDataList();
		groupList = new GroupList();
	}

	public synchronized ServerRespMessages registerPeer(RegisterPeerReq rpr){
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		PeerData peerData = rpr.getPeerData();
		
		if (peerdList.contains(peerData)){
			msg = ServerRespMessageItems.NACK_REG_PEER;
		}
		else{
			peerdList.addItem(peerData);
		}	
		return msg;
	}
	
	public synchronized void unregisterPeer(UnRegisterPeerReq unrpr){		
		PeerData peerData = unrpr.getPeerdata();
		if (peerdList.contains(peerData)){
			peerdList.deleteItem(peerData);
			// if user was part of a group
		}else System.out.println("User wasn't registerd!");
	}
	
	public synchronized ServerRespMessages registerGroup(RegisterGroupReq rgr){
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		Group group = rgr.getGroupitem();
		if (groupList.contains(group)){
			
		}
		else{
			groupList.addItem(group);
		}
		return msg;
	}
}
