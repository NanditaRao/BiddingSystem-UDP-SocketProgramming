package bidding;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class UDPClient extends Thread{

	
	/****/
	
	/*** @param args */
	
	public static boolean ACK;
	
	public static ArrayList<client> clientList = new ArrayList<client>();
	public static DatagramSocket clientSocket;
	public static boolean OFFLINE_BUY=false;
	public static boolean REGISTER = false;
	public static boolean DEREGISTER = false;
	public static boolean ACTIVE = false;
	public static String direct_buy;
	public static String sellerName;
	public static String[] split_command1 = new String[20];
	public static int port;
	public static InetAddress host;
	public static String name;
	
	public static void sendACK(InetAddress IP,int port)
	{
		System.out.println("Sending ack for register");
		byte[] data = new byte[4096];
		data = "acknowledgement".getBytes();
		DatagramPacket sendPacket = new DatagramPacket(data,data.length, IP, port);
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
		
	}
	
	
	public static void sendUDPPacket(String info , InetAddress IP , int port)
	{
		
		byte[] sendData = new byte[4096];
		sendData=info.getBytes();
		//The code for waiting for time out and 5 attempts is commented because it makes the
		 //code go into an infinite loop
		
				/*int attempt_no = 0;
				ACK = false;
				while ((!ACK && attempt_no < 5) ) 
				{		*/
					DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IP, port);

					try {
						clientSocket.send(sendPacket);
						/*try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}*/
						
					} catch (IOException e) {
						
					}
					
					
			/*}
					if(!ACK)
						System.out.println("[ Host unavailable ]");*/
	}
					
					
	//Used to broadcast information about all clients to all the clients
	public static void updateTables(String[] InfoList) throws UnknownHostException
	{
		clientList.clear();
		String[] split_client_info = new String[3];
		
		for(int i=0;i<InfoList.length; i++)
		{
			
			split_client_info = InfoList[i].trim().split(" ");
			String username= split_client_info[0].trim();
			InetAddress IP= InetAddress.getByName(split_client_info[1].substring(1));
			int port= Integer.parseInt(split_client_info[2].trim());
			client c = new client(username,IP,port);
			clientList.add(c);
		}
	
		System.out.println("sobs>[Client table updated]");
		System.out.print("sobs>");
	}
	
	
	//Receiver thread
	public void startReceiving()
	{
		Thread tReceiverthread = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				try 
				{
					UDPReceiver();

				} 
				catch (Exception e) 
				{
					// e.printStackTrace();
				}
			}
	

			@SuppressWarnings("unchecked")
			public void UDPReceiver() throws IOException
			{
				while(true)
				{
			
					byte[] receiveData = new byte[8192];
					DatagramPacket receivePacket =new DatagramPacket(receiveData,receiveData.length);
					try 
					{
						clientSocket.receive(receivePacket);
					}
					
					catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(!ACTIVE)
						continue;
					InetAddress receiverIP = receivePacket.getAddress();
					int receiver_port = receivePacket.getPort();
					String reply =new String(receivePacket.getData());
				
				//	Checking if the received packet is an ack
					if(reply.trim().equalsIgnoreCase("acknowledgement"))
					{
						
						ACK = true;
						if(OFFLINE_BUY)
						{
							System.out.println("sobs> [ "+sellerName+" is offline. Request has been forwarded to the server ]");
							System.out.print("sobs>");
								OFFLINE_BUY = false;
						}
						else if(REGISTER)
						{
							System.out.println("sobs> [Welcome "+name+", you have successfully signed in.] ");
							System.out.print("sobs>");
							REGISTER =false;
						}
						else if(DEREGISTER)
						{
							System.out.println("sobs> [You have successfully signed out. Bye!]");
							System.out.print("sobs>");
							DEREGISTER = false;
						}
					}
					
					//broadcasting to all clients
					
					else if(reply.trim().equals("broadcast"))
					{
						String[] clientInfoList = new String[1000];
												
						while(!reply.trim().equals("sent"))
						{
							byte[] receiveData1 = new byte[4096];
							DatagramPacket receivePacket1 =new DatagramPacket(receiveData1,receiveData1.length);
							clientSocket.receive(receivePacket1);
							String Reply = new String(receivePacket1.getData());
							if(Reply.trim().equals("sent"))
								break;
							Reply = Reply.replace("[", "");
							Reply = Reply.replace("]", "");
							clientInfoList = Reply.split(",");
														
						}
						updateTables(clientInfoList);
						
					}
					//buy and offline buy
					else if(reply.contains("buy"))
					{
						
						int i;
						String[] split_buy = new String[3];
						split_buy = reply.split(" ");
						String buyerName=null;
						for(i =0;i<clientList.size();i++)
						{
							client c = clientList.get(i);
							if(c.cli_IP.toString().equals(receiverIP.toString()) && c.cli_port == receiver_port)
							{
								buyerName = c.username;
								System.out.println("sobs>[ "+buyerName+" wants to buy your item "+split_buy[2].trim()+" ]");
								System.out.print("sobs>");
								direct_buy = "direct "+split_buy[2].trim()+" "+buyerName;
								sendUDPPacket(direct_buy,host,port);
								break;
							}
						}
						
												
					}
					
					else if(split_command1[0].equals("deregister"))
					{
						
							
					}
					else if(split_command1[0].equals("register"))
					{
						//Receiving offline messages					
						while(!reply.trim().equals("done"))
						{
							
							System.out.println("sobs> [ " +reply+" ]");
							byte[] receiveData2 = new byte[8192];
							DatagramPacket receivePacket2 =new DatagramPacket(receiveData2,receiveData2.length);
							try {
								clientSocket.receive(receivePacket2);
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							reply =new String(receivePacket2.getData());
						}//end of while for receiving offline messages
						System.out.print("sobs>");
					}//end of register
			
					else if(split_command1[0].trim().equals("info"))
					{
						
			
						if(split_command1.length == 1)
						{

							//Receiving info for all items
							while(!reply.trim().equals("done"))
							{
								
								System.out.println("sobs>" +reply);
								
								byte[] receiveData2 = new byte[8192];
								DatagramPacket receivePacket2 =new DatagramPacket(receiveData2,receiveData2.length);
								try {
									clientSocket.receive(receivePacket2);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								reply =new String(receivePacket2.getData());
						
							}
							System.out.print("sobs>");
						}
						else
						{
							
							System.out.println("sobs>" +reply);
							System.out.print("sobs>");
					
						}
			
					}//end of info
					else 
					{
						
						System.out.println("sobs>" +reply);
						System.out.print("sobs>");
					}
					
					
			
				}//end of while loop
				
			}//end of receiver function
	
		});//end of runnable
		
		tReceiverthread.start();
		//System.out.println("Thread started");
	}//end of startReceiver
		
	
	public static void main(String[] args) throws Exception {
		
		new UDPClient().startProgram();
		// TODO Auto-generated method stub
	}
	
	public void startProgram() throws Exception
	{
		
		String[] split_commands = new String[10];
		String command = "";
		
		
		Pattern pattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		BufferedReader inFromUser =	new BufferedReader(new InputStreamReader(System. in) );
		clientSocket = new DatagramSocket();
		boolean value = false;
		startReceiving();
		System.out.println("Please enter your command\n");
				
				//Validating the first input
				
		while(!value)
		{
		    command = inFromUser.readLine();
			split_commands = command.split(" ");
			if (split_commands[0].equals("sobs"))
			{
				if(split_commands.length == 4)
				{
					if((Integer.parseInt(split_commands[3]) < 1024) || (Integer.parseInt(split_commands[3]) > 65535))
					{
						System.out.println("[ Error : port number out of range ]");
							
					}
					else
					{
						if(split_commands[1].equals("-c"))
						{
							port = Integer.parseInt(split_commands[3]);
							Matcher match = pattern.matcher(split_commands[2]);
							if (match.matches())
							{
								host = InetAddress.getByName(split_commands[2]);
								value = true;
							}
							else
							{
								System.out.println("[Error : Invalid IP ]");
								
							}
						}
						else
						{
							System.out.println("[Error : No a client command ]");
						}
					}
				}
				else
			    {
				System.out.println("[ Error : arguments ]");
					
			    }
			}
			else
			{
				System.out.println("[ Error : Incorrect command ]");
						
			}
				
		}
				
				
				
				
		System.out.print("sobs>");
				
		while(true)
		{
					
			command = inFromUser.readLine();
			split_command1 = command.split(" ");
			if(split_command1[0].equals("buy"))
			{
				if(!ACTIVE)
				{
					System.out.println("sobs>[Error : Not registered]");
					System.out.print("sobs>");
				}
				else if(split_command1.length != 3)
				{
					System.out.println("sobs> [Error : incorrect number of arguments ]");
					System.out.print("sobs>");
				}
				else
				{
					sellerName = split_command1[1];
					InetAddress seller_IP = null;
					int seller_port = 0;
					int i;
					for(i=0;i<clientList.size();i++)
					{
						client c = clientList.get(i);
						if(sellerName.trim().equals(c.username))
						{
							seller_IP = c.cli_IP;
							seller_port = c.cli_port;
							break;
								
								
						}
					}
					if(i == clientList.size())
					{
						OFFLINE_BUY = true;
						direct_buy = "direct "+split_command1[2]+" "+name;
						sendUDPPacket(direct_buy,host,port);
					}
						
					else
					{
						sendUDPPacket(command,seller_IP,seller_port);
					}
						
				}
			}
			else if(split_command1[0].equals("register"))
			{
				if(split_command1.length != 2)
				{
					System.out.println("sobs> [Error : incorrect number of arguments ]");
					System.out.print("sobs>");
				}
				else
				{
					name = split_command1[1];
					REGISTER = true;
					ACTIVE = true;
					sendUDPPacket(command,host,port);
				}
					
			}
			else if(split_command1[0].equals("deregister"))
			{
				
				if(!ACTIVE)
				{
					System.out.println("sobs>[Error : Not registered]");
					System.out.print("sobs>");
				}
				else if(split_command1.length != 1)
				{
					System.out.println("sobs> [Error : incorrect number of arguments ]");
					System.out.print("sobs>");
				}
				else
				{
					DEREGISTER = true;
					ACTIVE = false;
					sendUDPPacket(command,host,port);
				}
					
			}
			else if(split_command1[0].equalsIgnoreCase("sell"))
			{
				if(!ACTIVE)
				{
					System.out.println("sobs> [Error : Not registered]");
					System.out.print("sobs>");
				}
				else if(split_command1.length != 6)
				{
					System.out.println("sobs> [Error : incorrect number of arguments");
					System.out.print("sobs>");
				}
						
				else
				{
					sendUDPPacket(command,host,port);
				}
			}
			else if(split_command1[0].equalsIgnoreCase("bid"))
			{
				if(!ACTIVE)
				{
					System.out.println("sobs>[Error : Not registered]");
					System.out.print("sobs>");
				}
				else if(split_command1.length != 3)
				{
					System.out.println("sobs> [Error : incorrect number of arguments ]");
					System.out.print("sobs>");
				}
						
						
				else
				{
					sendUDPPacket(command,host,port);
				}
			}
			else if(split_command1[0].equalsIgnoreCase("info"))
			{
				if(!ACTIVE)
				{
					System.out.println("sobs>[Error : Not registered]");
					System.out.print("sobs>");
				}
				else if(split_command1.length > 3)
				{
					System.out.println("sobs> [Error : incorrect number of arguments ]");
					System.out.print("sobs>");
				}
						
						
				else
				{
					sendUDPPacket(command,host,port);
				}
			}
			else
			{
				System.out.println("sobs>[Error : Unknown command ]");
				System.out.print("sobs>");
			}
				
		}
				
				
				//clientSocket.close();
	}


}


