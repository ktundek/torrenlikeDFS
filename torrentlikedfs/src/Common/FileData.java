package Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import Exceptions.UnableToReadFileException;

public class FileData implements Serializable{
	private String name;
	private long size;
	private String path;
	//private byte[] hash;
	private String crc;
	
	public FileData(File file) {
		try {
			if (!file.canRead())			
				throw new UnableToReadFileException(file.getCanonicalPath());			
			name = file.getName();			
			size = file.length();		
			path = file.getCanonicalPath();
			//hash = createChecksum(path);
			crc = createChecksum(path);
		} catch (UnableToReadFileException | IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Unable to read file: "+file.getName());
			e.printStackTrace();
		}
	}
	
	// Calculate checksum for a file
	//public byte[] createChecksum(String filePath){
	public String createChecksum(String filePath) throws UnsupportedEncodingException{
		FileInputStream fis = null;
		byte[] mdBytes = null;		
		
		try {
			fis = new FileInputStream(filePath);
			MessageDigest md = MessageDigest.getInstance("MD5");	    
		    byte[] dataBytes = new byte[1024]; // buffer			    
		    int read = 0; 
		 
		    //String toEnc = "abc";
		    //md.update(toEnc.getBytes(), 0, toEnc.length());
		    
		    while ((read = fis.read(dataBytes)) != -1) {		    	
		    	md.update(dataBytes, 0, read);
		    }
		    fis.close();		    
		    mdBytes = md.digest();	
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NO SUCH ALOGORITHM!");
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		String res="";
		for (int i = 0; i < mdBytes.length; i++) {			
			res+=Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1);
	    }			 
		//System.out.println("Digest(in hex format):: "+res);						
		
		/*System.out.println("BigInteger:");		
		BigInteger bi =new BigInteger(1, mdBytes);
		String crc = bi.toString();
		System.out.println("crc string:"+crc);*/
			
		return res;
	}
	
	public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers            
            append(name).
            append(size).
            append(crc).
            toHashCode();
    }
	
	public boolean equals(Object obj){
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof FileData))
            return false;

        FileData fd = (FileData) obj;
        return new EqualsBuilder().            
            append(name, fd.name).
            append(size, fd.size).
            append(crc, fd.crc).
            isEquals();
		/*boolean eq = false;
		if ((obj !=null) && (obj instanceof FileData)){
			FileData fd = (FileData) obj;
			if ((this.name.equals(fd.name)) && (this.size==fd.size)){
				eq = true;
			}
		}
		return eq;*/				
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCrc() {
		return crc;
	}

	public void setCrc(String crc) {
		this.crc = crc;
	}	

	/*public byte[] getHash() {
		return hash;
	}*/

	/*public void setHash(byte[] hash) {
		this.hash = hash;
	}*/	
		
}
