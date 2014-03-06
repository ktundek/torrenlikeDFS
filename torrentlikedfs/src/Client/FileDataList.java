package Client;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class FileDataList implements Serializable{
	private Vector<FileData> fileDataList;
	//private HashSet<FileData> fileDataList;
	//private Set<FileData> fileDataList = new HashSet<FileData>();

	public FileDataList() {
		super();
		fileDataList = new Vector<FileData>();
		//fileDataList =   Collections.synchronizedSet(new HashSet<FileData>());		
	}

	/*public Set<FileData> getFileDataList() {
		return fileDataList;
	}*/
	public Vector<FileData> getFileDataList() {
		return fileDataList;
	}

	/*public void setFileDataList(HashSet<FileData> fileDataList) {
		this.fileDataList = fileDataList;
	}*/
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
	
	// Returns a list of files wich are not included in the caller's file list
	/*public FileDataList getNotIncludedFileList(FileDataList fdl){
		FileDataList newfdl = new FileDataList();
		Iterator itr = fdl.getFileDataList().iterator();
		while (itr.hasNext()){
			FileData fd = (FileData)itr.next();
			if (!this.contains(fd))
				newfdl.addItem(fd);
		}	
		return newfdl;
	}*/
	
	public FileData getItem(int i){
		return fileDataList.get(i);
	}
	
	public void toStringFileDataList(){
		/*Iterator itr = fileDataList.iterator();
		while (itr.hasNext()){
			FileData fd = (FileData)itr.next();
			System.out.println(fd.getName()+", "+fd.getSize());			
		}*/
		for (int i=0; i<fileDataList.size(); i++){
			System.out.println(fileDataList.get(i).getName()+", "+
					fileDataList.get(i).getSize());
		}
	}
}
