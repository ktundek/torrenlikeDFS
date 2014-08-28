package Common;

import java.io.Serializable;

import Client.PeerData;



public class Group implements Serializable{
	private PeerData peerdata;
	private FileDataListClient fileList;
	
	public Group(PeerData peerdata, FileDataListClient fileList) {
		super();
		this.peerdata = peerdata;
		this.fileList = fileList;
	}

	public PeerData getPeerData() {
		return peerdata;
	}

	public void setPeer(PeerData peerdata) {
		this.peerdata = peerdata;
	}

	public FileDataListClient getFileList() {
		return fileList;
	}

	public void setFileList(FileDataListClient fileList) {
		this.fileList = fileList;
	}		
}
