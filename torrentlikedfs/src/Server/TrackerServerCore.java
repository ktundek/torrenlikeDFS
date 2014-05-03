package Server;

import Common.FileData;
import Common.FileDataListClient;
import Common.FileDataListServer;
import Common.Group;
import Common.GroupList;
import Client.PeerData;
import Client.PeerItem;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.ServerListRespMessages;
import Messages.ServerRespMessageItems;
import Messages.ServerRespMessages;
import Messages.UnRegisterPeerReq;

public class TrackerServerCore {
	private PeerItemList peerIList = null;
	private GroupList groupList = null;
	private FileDataListServer fileList = new FileDataListServer();  // list of files on the server;
	
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
	
	public synchronized ServerListRespMessages registerGroup(RegisterGroupReq rgr){
		System.out.println("-----------------REGISTER GROUP------------------");
		//ServerRespMessages msg = ServerRespMessageItems.ACK;	
		ServerListRespMessages msg= new ServerListRespMessages("", null);
		msg.setMsg("OK");		
		Group group = rgr.getGroup();
		FileDataListClient clientFileList = new FileDataListClient();		
		
		if (groupList.containsGroup(group)){   
			msg.setMsg("NACK"); // the group already exists
		}		
		else{	
			groupList.addItem(group);
			groupList.toStringGroup();			
			// if the servers file list does not contain a file from clients file list, it will be added
			clientFileList = fileList.getNotIncludedFileList(group.getFileList());
			//for (int i=0; i<clientFileList.getSize(); i++)
				//fileList.addItem(clientFileList.getItem(i));
			//System.out.println("----------CLIENT FILE LIST-------------");
			clientFileList.toStringFileDataList();
			
			msg.setObj(clientFileList);
			//ServerListRespMessages resp = new ServerListRespMessages(msg, clientFileList);	
				
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
	
	public synchronized void addFile(FileData filedata){
		fileList.addItem(filedata);
		//fileList.toStringFileDataList();
	}
	
	public synchronized FileDataListClient getNonExistetnFiles(Group group){
		FileDataListClient fdl = null;		
		return fdl;
	}
}
