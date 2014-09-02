package Client;

import java.io.Serializable;
import java.net.*;
import java.util.Enumeration;

import Logger.Logging;


public class PeerData implements Serializable{	
	private static final long serialVersionUID = 5372923354124483879L;
	private int port;
	private InetAddress inetAddress;	

	public PeerData(int port, InetAddress inetAddress) {
		super();
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("windows")){
			this.port = port;
			this.inetAddress = inetAddress;
		}
		if (os.contains("linux")){
			Enumeration en= null;
			int nr = 0;
			try {
				en = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e) {
				Logging.write(this.getClass().getName(), "PeerData", "Error setting inet addres on linux. "+e.getMessage());
			}while(en.hasMoreElements()){
			    NetworkInterface ni=(NetworkInterface) en.nextElement();
			    Enumeration ee = ni.getInetAddresses();
			    while(ee.hasMoreElements()) {
			    	nr++;
			        InetAddress ia= (InetAddress) ee.nextElement();
			        if (nr==2){
			        	//System.out.println(ia.getHostAddress());		        	
			        	try {
			    			this.inetAddress = InetAddress.getByName(ia.getHostAddress());
			    		} catch (UnknownHostException e) {
			    			Logging.write(this.getClass().getName(), "PeerData", "Error setting inet addres on linux. "+e.getMessage());
			    		}
			    			
			        }
			    }
			}
		}
	}
		

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}	
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean equals(Object object){
		PeerData pd = (PeerData) object;
		
		if (this==object) return true;
		if (!(object instanceof PeerData))
			return false;
		if (this.getInetAddress().equals(pd) && this.port == pd.port)
			return true;
		else
			return false;
	}
}
