package Messages;

public class ServerRespMessageItems {
	public final static ServerRespMessages ACK = new ServerRespMessages("OK");
	
	public final static ServerRespMessages NACK_REG_PEER = new ServerRespMessages("Peer already registered!");
	public final static ServerRespMessages ACK_UNREG_PEER = new ServerRespMessages("Peer has signed out!");
	
	public final static ServerRespMessages NACK_REG_GROUP = new ServerRespMessages("Group already registered!");
}
