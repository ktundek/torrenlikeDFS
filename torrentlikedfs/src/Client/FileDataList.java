package Client;

import java.io.Serializable;
import java.util.Vector;

public class FileDataList implements Serializable{
	private Vector<FileData> fileDataList;

	public FileDataList() {
		super();
		fileDataList = new Vector<FileData>();
	}

	public Vector<FileData> getFileDataList() {
		return fileDataList;
	}

	public void setFileDataList(Vector<FileData> fileDataList) {
		this.fileDataList = fileDataList;
	}
	
	public void addItem(FileData fd){
		fileDataList.add(fd);
	}
	
	public void deleteItem(FileData fd){
		fileDataList.remove(fd);
	}
	
	public boolean contains(FileData fd){
		if(fileDataList.contains(fd)) return true;
		else return false;
	}
	
	public int getSize(){
		return fileDataList.size();
	}
	
	public FileData getItem(int i){
		return fileDataList.get(i);
	}
}
