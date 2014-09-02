package Common;

import java.io.Serializable;
import java.util.Hashtable;

public class ChunkList implements Serializable{
	private Hashtable<String, ChunkData> chunkList;

	public ChunkList() {
		super();
		chunkList = new Hashtable<String, ChunkData>();
	}			
	
}
