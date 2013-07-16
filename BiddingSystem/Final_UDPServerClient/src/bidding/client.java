package bidding;

import java.io.*;
import java.net.*;

public class client implements Serializable {
	
	client()
	{
		
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -8899019341801024756L;
	String username;
	InetAddress cli_IP;
	int cli_port;
    
	client(String user, InetAddress IP , int port )
	{
		username = user;
		cli_IP = IP;
		cli_port = port;
	}
}
