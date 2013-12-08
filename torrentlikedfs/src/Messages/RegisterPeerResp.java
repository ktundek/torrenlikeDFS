package Messages;

import java.io.Serializable;

public class RegisterPeerResp extends ServerResp implements Serializable{
	private ServerRespMessages msg;
	
	public RegisterPeerResp(ServerRespMessages msg) {
		super(msg);
		this.msg = msg;
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}
}
