package Messages;

import java.io.Serializable;

public class RegisterGroupResp extends ServerResp implements Serializable{
	private ServerRespMessages msg;

	public RegisterGroupResp(ServerRespMessages msg) {
		super(msg);
		this.msg = msg;
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}
}
