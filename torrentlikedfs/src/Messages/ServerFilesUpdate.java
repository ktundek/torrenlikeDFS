package Messages;

import java.io.Serializable;
import java.util.Vector;

import Client.PeerData;



public class ServerFilesUpdate extends ChunkMessage implements Serializable {
	private Vector<Object> row = null;
	
	public ServerFilesUpdate(PeerData peer) {
		super(peer);
		// TODO Auto-generated constructor stub
	}

	public Vector<Object> getRow() {
		return row;
	}

	public void setRow(Vector<Object> row) {
		this.row = row;
	}	

}
