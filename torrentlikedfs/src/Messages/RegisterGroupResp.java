package Messages;

import java.io.Serializable;

public class RegisterGroupResp extends ServerResp implements Serializable{
	private ServerRespMessages msg;

	public RegisterGroupResp(ServerRespMessages msg, ServerRespMessages msg2) {
		super(msg);
		msg = msg2;
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}
}
