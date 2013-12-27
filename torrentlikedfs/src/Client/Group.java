package Client;

public class Group {
	private PeerData peer;
	private FileDataList fileList;
	
	public Group(PeerData peer, FileDataList fileList) {
		super();
		this.peer = peer;
		this.fileList = fileList;
	}

	public PeerData getPeer() {
		return peer;
	}

	public void setPeer(PeerData peer) {
		this.peer = peer;
	}

	public FileDataList getFileList() {
		return fileList;
	}

	public void setFileList(FileDataList fileList) {
		this.fileList = fileList;
	}
		
}
