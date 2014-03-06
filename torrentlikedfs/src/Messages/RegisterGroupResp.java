package Messages;

import java.io.Serializable;

import Client.FileDataList;

public class RegisterGroupResp extends ServerResp implements Serializable{
	private ServerRespMessages msg;
	private FileDataList fileList;

	//public RegisterGroupResp(ServerRespMessages msg, FileDataList fileList) {
	public RegisterGroupResp(ServerRespMessages msg) {
		super(msg);
		this.msg = msg;
		//this.fileList = fileList;
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}

	public FileDataList getFileList() {
		return fileList;
	}

	public void setFileList(FileDataList fileList) {
		this.fileList = fileList;
	}
		
}
