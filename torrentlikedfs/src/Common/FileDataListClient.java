package Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class FileDataListClient implements Serializable{	
	private static final long serialVersionUID = 1L;
	private Vector<FileData> fileDataList;	

	public FileDataListClient() {
		super();
		fileDataList = new Vector<FileData>();
		//fileDataList =   Collections.synchronizedSet(new HashSet<FileData>());		
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
	
	public void toStringFileDataList(){
		/*Iterator itr = fileDataList.iterator();
		while (itr.hasNext()){
			FileData fd = (FileData)itr.next();
			System.out.println(fd.getName()+", "+fd.getSize());			
		}*/
		for (int i=0; i<fileDataList.size(); i++){
			System.out.println("FileDataListClient: "+fileDataList.get(i).getName()+", "+
					fileDataList.get(i).getSize());
		}
	}
}
