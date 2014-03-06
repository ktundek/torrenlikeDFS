package Messages;

import java.io.Serializable;

public class RegisterPeerResp extends ServerResp implements Serializable{
	private ServerRespMessages msg;
	//private int port;
	
	public RegisterPeerResp(ServerRespMessages msg) {
		super(msg);
		this.msg = msg;
		//this.port = port;
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}
	
	/*public int getPort(){
		return this.port;
	}*/
}
