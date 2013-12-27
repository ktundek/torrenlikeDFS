package Client;

import java.util.Vector;

public class FileDataList {
	private Vector<FileData> fileDataList;

	public FileDataList(Vector<FileData> fileDataList) {
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
}
