package Messages;

import java.io.Serializable;

import Common.FileDataListClient;

public class RegisterGroupResp extends ServerResp implements Serializable{
	private ServerRespMessages msg;		
	
	//public RegisterGroupResp(ServerRespMessages msg, FileDataListClient fileList) {
	public RegisterGroupResp(ServerRespMessages msg) {
		super(msg);
		this.msg = msg;
		//this.fileList = fileList;
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}			
}
