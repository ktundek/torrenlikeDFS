package Server;

import Client.FileData;
import Client.FileDataList;
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
	private FileDataList fileList = null;  // list of files on the server;
	
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
		System.out.println("---------- UNREGISTER PEER------------");
		int index = peerIList.containsPeerItemIndex(peerItem);
		if (index!= -1) {
			peerIList.deleteItem(index);
			groupList.deleteItem(peerItem.getPeerData());
			peerIList.toStringList();
			groupList.toStringGroup();
			System.out.println("USER WAS DELETED!");
		}
		else System.out.println("UNREGISTER PEER: Can not find Peer in the list!");	
		System.out.println("----------END UNREGISTER PEER------------");
	}
	
	public synchronized ServerRespMessages registerGroup(RegisterGroupReq rgr){
		System.out.println("-----------------REGISTER GROUP------------------");
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		Group group = rgr.getGroup();
		FileDataList clientFileList = new FileDataList();
		
		if (groupList.containsGroup(group)){   
			msg = ServerRespMessageItems.NACK_REG_GROUP;
		}		
		else{	
			groupList.addItem(group);
			groupList.toStringGroup();			
			// if the servers file list does not contain a file from clients file list, it will be added
			//clientFileList = fileList.getNotIncludedFileList(group.getFileList());
			System.out.println("----------CLIENT FILE LIST-------------");
			clientFileList.toStringFileDataList();
				
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
	
	public  FileDataList getNonExistetnFiles(Group group){
		FileDataList fdl = null;
		
		return fdl;
	}
}
