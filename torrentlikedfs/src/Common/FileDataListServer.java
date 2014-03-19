package Common;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class FileDataListServer implements Serializable {
	//private HashSet<FileData> fileDataList;
	private Hashtable<String, FileData> fileDataList;

	public FileDataListServer() {
		super();
		//fileDataList = (HashSet<FileData>) Collections.synchronizedSet(new HashSet<FileData>());	
		fileDataList = new Hashtable<String, FileData>();
	}
	
	public Hashtable<String, FileData> getFileDataList() {
		return fileDataList;
	}
	
	public void setFileDataList(Hashtable<String, FileData> fileDataList) {
		this.fileDataList = fileDataList;
	}
	
	public void addItem(FileData fd){
		fileDataList.put(fd.getCrc(), fd);
	}
	
	public void deleteItem(FileData fd){
		fileDataList.remove(fd.getCrc());
	}
	
	public boolean contains(FileData fd){
		if(fileDataList.containsKey(fd.getCrc())) return true;
		else return false;
	}
	
	public int getSize(){
		return fileDataList.size();
	}
	
	// Returns a list of files wich are not included in the caller's file list
	public FileDataListClient getNotIncludedFileList(FileDataListClient fdl){
		
		FileDataListClient newfdl = new FileDataListClient();		
		for (int i=0; i<fdl.getSize(); i++){
			FileData fd = fdl.getItem(i);
			if (!this.contains(fd)){
				newfdl.addItem(fd);
			}
		}			
		return newfdl;
	}
	
	public void toStringFileDataList(){				
		Enumeration<FileData> e = fileDataList.elements();
		while (e.hasMoreElements()){
			FileData fd = e.nextElement();
			System.out.println("Name: "+fd.getName()+", size: "+fd.getSize());
		}
			
	}
}
