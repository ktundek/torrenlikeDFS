package Client;

import java.io.Serializable;

public class Group implements Serializable{
	private PeerData peerdata;
	private FileDataList fileList;
	
	public Group(PeerData peerdata, FileDataList fileList) {
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

	public FileDataList getFileList() {
		return fileList;
	}

	public void setFileList(FileDataList fileList) {
		this.fileList = fileList;
	}		
}
