package Messages;

import java.io.Serializable;

public class ChunkResp extends ServerResp implements Serializable{
	private ServerRespMessages msg;
	
	public ChunkResp(ServerRespMessages msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}
}
