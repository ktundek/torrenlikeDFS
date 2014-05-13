package Common;

import java.io.Serializable;

public class ChunkData implements Serializable{
	private FileData fileData;
	private int nr;
	private String crc;
	
	public ChunkData(FileData fileData, int nr) {
		super();
		this.fileData = fileData;
		this.nr = nr;
	}

	public FileData getFileData() {
		return fileData;
	}

	public void setFileData(FileData fileData) {
		this.fileData = fileData;
	}

	public int getNr() {
		return nr;
	}

	public void setNr(int nr) {
		this.nr = nr;
	}
		
}
