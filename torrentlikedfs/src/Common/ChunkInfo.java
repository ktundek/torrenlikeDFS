package Common;

/**
 * Chunk structure for each file
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----
 * | 1st chunk | 2nd chunk | 3rd chunk | 4th chunk | 5th chunk | 6th chunk | ... 
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----
 * |   EMPTY   |   EMPTY   | COMPLETED | COMPLETED | DOWNLDING |   EMPTY   | ...
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----
 */

//Ez az osztaly a https://code.google.com/p/simpletorrentlikep2p/ cimen talalhato 
//Peer/ChunkManager.java file-ban talalhato ChunkInfo osztaly masolata
public class ChunkInfo
{
	private ChunkState[] chunksState;
	
	ChunkInfo(int chunkCount)
	{
		chunksState = new ChunkState[chunkCount];
		for (int i=0;i<chunkCount;i++)
			chunksState[i] = ChunkState.EMPTY;
	}
	
	public ChunkState getState(int index)
	{
		return chunksState[index];
	}
	
	public void setState(int index, ChunkState new_state)
	{
		chunksState[index] = new_state;
	}
	
	public int nrChunks()
	{
		return chunksState.length;
	}
}
