package bidding;
import java.io.*;
import java.net.*;
import java.util.ArrayList;



public class UDPServer extends Thread
{

	
	/**
	 * 
	 */
	//private static final long serialVersionUID = 8362159773100396897L;
	private static DatagramSocket serverSocket;
	public static boolean ACK = false;
	public static ArrayList<client> clientList = new ArrayList<client>();
	public static ArrayList<offline_user> offline_List = new ArrayList<offline_user>();
	public static ArrayList<Item> itemList = new ArrayList<Item>();
	public static String name;

	/**
	 * 
	 */
	public static void sendACK(InetAddress IP,int port)
	{
		System.out.println("Sent ack");
		byte[] data = new byte[4096];
		data = "acknowledgement".getBytes();
		DatagramPacket sendPacket = new DatagramPacket(data,data.length, IP, port);
		try {
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
	}
	//Used to broadcast client information
	public static ArrayList<String> clientInfo()
	{
		ArrayList<String> info = new ArrayList<String>();
		for(int i = 0; i < clientList.size() ; i++)
		{
			client c = clientList.get(i);
			String s = c.username+" "+c.cli_IP+" "+c.cli_port;
			info.add(s);
		}
		return info;
	}
	
	//send method
	public static void sendUDPPacket(String info , InetAddress IP , int port, String name) throws InterruptedException
	{
		System.out.println("Sending " + info);
		int i;
		for(i=0;i<clientList.size();i++)
		{
			client c =clientList.get(i);
			if(c.username.equalsIgnoreCase(name))
			{
				break;
			}
		}
		if (i == clientList.size())
		{
			info = "Offline message: "+info;
			offline_user o = new offline_user(name,info);
			offline_List.add(o);
			return;
		}
		byte[] sendData = new byte[4096];
		sendData = info.getBytes();
		int attempt_no = 0;
		//ACK =false;
		try 
		{

		/*while ((!ACK && attempt_no < 5)) 
			{*/
				System.out.println("Sending : "+info);
				System.out.println(ACK);
				DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IP, port);
				serverSocket.send(sendPacket);
				/*Thread.sleep(500);
				if(!ACK)
					sendAttempt++;
				System.out.println("sendAttemptCount: " + attempt_no);*/
		//	}
				
			/*if(!ACK)
				addOfflineMessage(name,info);
			for(int i=0;i<clientList.size();i++)
			{
				client c = clientList.get(i);
				if(c.username.equals(name))
				{
					clientList.remove(i);
					break;
				}
			}*/
				
				
				
				
		}
		catch (SocketException e) 
		{
			
			// TODO Auto-generated catch block
			 e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			

			 e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 */
	//FInds the name of the user from whom the packet has been received
	public static String getCurrentUser(InetAddress IP, int port)
	{
			client c = new client();
			for(int i=0;i<clientList.size();i++)
			{
				c = clientList.get(i);
				
			}
		for(int i=0; i < clientList.size();i++)
		{
			c = clientList.get(i);
			if((c.cli_IP.toString().equals(IP.toString())) && (c.cli_port == port))
			{
				return c.username;
			}
		}
		return "none";
	}
	
	//add messagesfor offline users
	public static void addOfflineMessage(String username, String message)
	{
		System.out.println("Inside offline messages");
		
		offline_user o = new offline_user(username,message);
		offline_List.add(o);
		
		for(int j = 0; j<offline_List.size();j++)
		{
			offline_user o1 = offline_List.get(j);
			System.out.println("Offline message " + username +" "+message);
		}
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
	

			public void UDPReceiver() throws Exception
			{
				
				int itemCount = 0;
				while(true)
				{
			
					boolean flag1 = false;
					String message;
					String reply;
					byte[] receiveData1 = new byte[8192];
					DatagramPacket receivePacket1 = new DatagramPacket(receiveData1, receiveData1.length);	
					try {
						serverSocket.receive(receivePacket1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					InetAddress IPAddress =receivePacket1.getAddress();
					int port = receivePacket1.getPort();
					String command = new String(receivePacket1.getData());
					String currentuser = getCurrentUser(IPAddress,port);
					System.out.println("The current user is"+currentuser);
					String[] split_command1 = new String[20];
					split_command1 = command.split(" ");
							
					System.out.println("Printing the list of users");
					for(int i=0; i<clientList.size();i++)
					{
						client c = clientList.get(i);
						System.out.println(c.username);
					}
					
					
					
					if(command.trim().equals("acknowledgement"))
					{
						System.out.println("Acknowledgement received");
						ACK = true;
					}
				
					else if(split_command1[0].trim().equals("register"))
					{
						name = split_command1[1].trim();
						System.out.println("Entered registered part");
						System.out.println(clientList.isEmpty());
						if(!clientList.isEmpty())
						{
							
							for (int i=0; i <clientList.size(); i++)
							{
								client c = clientList.get(i);
								if(c.username.equalsIgnoreCase(name))
								{
									clientList.remove(i);
								}
							}
						}
						
						client CL = new client(name,IPAddress,port);
						System.out.println("Adding user");
						clientList.add(CL);
						
						sendACK(IPAddress,port);
									
						
						
						if(!offline_List.isEmpty())
						{
							for(int i=0;i<offline_List.size();i++)
							{
								offline_user u = offline_List.get(i);
								System.out.println(u.name);
								System.out.println(u.message);
							}
							System.out.println("Entered offline messages list");
							for (int i=0; i <offline_List.size(); i++)
							{
								offline_user u = offline_List.get(i);
								System.out.println(u.name);
								if(u.name.equalsIgnoreCase(name))
								{
									sendUDPPacket(u.message,IPAddress,port,name);
									
								}
							}
							
						}
						sendUDPPacket("done",IPAddress,port,name);
											
						for(int i=0 ; i<clientList.size(); i++)
						{
							System.out.println("Entered broadcast");
							client c = clientList.get(i);
							sendUDPPacket("broadcast",c.cli_IP,c.cli_port,c.username);
							ArrayList<String> client_info = new ArrayList<String>();
							client_info = clientInfo();
							System.out.println(client_info.toString());
							sendUDPPacket(client_info.toString(),c.cli_IP, c.cli_port,c.username);
							sendUDPPacket("sent",c.cli_IP,c.cli_port,c.username);
							
						}
						
						
						//sendUDPPacket("updated",IPAddress,port,name);
					}
					
					else if(split_command1[0].trim().equals("sell"))
					{
						
						System.out.println("Entered the sell part");
						String itemname = split_command1[1];
					
						int trans_limit = Integer.parseInt(split_command1[2]);
						int start_bid = Integer.parseInt(split_command1[3]);
						System.out.println(start_bid);
						int buy_now = Integer.parseInt(split_command1[4]);
						String description = split_command1[5];
					
					
						if((trans_limit > 0) && (start_bid >= 0) && (start_bid >0) )
						{
							itemCount = itemCount + 1;
							Item item = new Item(itemCount,itemname,currentuser,trans_limit,start_bid,buy_now,description);
							itemList.add(item);
							reply = "["+itemname+" added with number "+item.item_code+"]";
													
						}
						else
						{
							reply = "[ Error : arguments ]";
						}
						sendUDPPacket(reply,IPAddress,port,name);
					
					}
				
					else if(split_command1[0].trim().equals("deregister"))
					{
						
						int index = -1;
						for(int i=0 ; i < clientList.size(); i++ )
						{
							client c = clientList.get(i);
							if(c.username.equals(currentuser))
							{
								System.out.println(i);
								index = i;
							}
						}
					
						clientList.remove(index);
						sendACK(IPAddress,port);
						for(int i=0 ; i<clientList.size(); i++)
						{
							System.out.println("Entered broadcast");
							client c = clientList.get(i);
							sendUDPPacket("broadcast",c.cli_IP,c.cli_port,c.username);
							ArrayList<String> client_info = new ArrayList<String>();
							client_info = clientInfo();
							System.out.println(client_info.toString());
							sendUDPPacket(client_info.toString(),c.cli_IP, c.cli_port,c.username);
							sendUDPPacket("sent",c.cli_IP,c.cli_port,c.username);
							
						}
						
					
					}
				
					else if(split_command1[0].trim().equals("info"))
					{
						System.out.println("Entered info");
						if(split_command1.length > 1)
						{
							int i;
							for(i = 0; i < itemList.size();i++)
							{
								Item it = itemList.get(i);
								if(Integer.parseInt(split_command1[1].trim()) == it.item_code)
								{
									String response = it.get_info();
									sendUDPPacket(response,IPAddress,port,currentuser);
									break;
								}
							}
							if ( i == itemList.size())
							{
								String response = "[Error: "+Integer.parseInt(split_command1[1].trim())+" item not found]";
								sendUDPPacket(response,IPAddress,port,currentuser);
													
							}
						}
						else 
						{
							if(itemList.isEmpty())
							{
								String response = "[Error : empty]";
								sendUDPPacket(response,IPAddress,port,currentuser);
																			
							}
							else
							{
								
								for(int i=0; i< itemList.size();i++)
								{
									Item I = itemList.get(i);
									String response = I.get_info();
									System.out.println(response);
									sendUDPPacket(response,IPAddress,port,currentuser);
			
								}
								
							}
							
							sendUDPPacket("done",IPAddress,port,currentuser);
												
						}
						
					}//else if of info ends
					
					
					else if(split_command1[0].trim().equals("bid"))
					{
						
						System.out.println("Entered bid");
						for(int i = 0; i < itemList.size(); i++)
						{
							Item it = itemList.get(i); 
							if(Integer.parseInt(split_command1[1].trim()) == it.item_code)
							{
								if(currentuser.equals(it.owner))
								{
									String response = "[ Error : owner ]";
									sendUDPPacket(response,IPAddress,port,currentuser);
									flag1 = true;
								}
								else if(currentuser.equals(it.buyername))
								{
									String response = "[ Error : duplicate bid ]";
									sendUDPPacket(response,IPAddress,port,currentuser);
									flag1 = true;
								}
								else if(Integer.parseInt(split_command1[2].trim()) < 0)
								{
									String response = "[ Error : negative bid ]";
									sendUDPPacket(response,IPAddress,port,currentuser);
									flag1 = true;
								}
								else
								{
									int amount = Integer.parseInt(split_command1[2].trim());
									amount = amount + it.current_bid;
									it.set_current_bid(amount);
									it.set_buyername(currentuser);
									it.transaction_count++;
									
							
									if((it.transaction_count == it.translimit) || (it.current_bid >= it.buynow))
									{
										int j;
										String response = "[ purchased :"+it.item_code+" "+it.name+" "+it.current_bid+" ]";
										sendUDPPacket(response,IPAddress,port,currentuser);
										
								
										for(j = 0; j< clientList.size();j++)
										{
											client c = clientList.get(j);
											if(it.owner.equals(c.username))
											{
												String response1 = "[ sold :"+it.item_code+" "+it.name+" "+it.current_bid+" ]";
												sendUDPPacket(response1,c.cli_IP,c.cli_port,c.username);
												
											}
										}
										
										
										
										itemList.remove(i);
										flag1 = true;
									}
									else
									{
										String response = it.item_code+" "+it.name+" "+it.current_bid;
										sendUDPPacket(response,IPAddress,port,currentuser);
										flag1 = true;
									}
									
								}
								
							}
						
						}
					
						if(flag1 == false)
						{
							String response = "Item not found";
							sendUDPPacket(response,IPAddress,port,currentuser);
							
						}
					}//else if for bid ends here
					
					else if(split_command1[0].trim().equals("direct"))
					{
						System.out.println("Entered direct");
						String buyerName = split_command1[2].trim();
						System.out.println(buyerName);
						InetAddress buyer_IP = null;
						int buyer_port = 0;
						int item_Code = Integer.parseInt(split_command1[1].trim());
						System.out.println(item_Code);
						int j;
						for(j=0;j<clientList.size();j++)
						{
							client c = clientList.get(j);
							if(buyerName.equals(c.username))
							{
								buyer_IP = c.cli_IP;
								buyer_port = c.cli_port;
							}
						}
						System.out.println("Reached till after the for loop");
						//When the seller is offline
						if((buyer_IP.toString().equals(IPAddress.toString())) && (buyer_port == port))
						{
							System.out.println("This is an offline buy");
							int i;
							int code =0;
							String itemName=null;
							int buyNowPrice=0;
							String itemOwner =null;
							for(i=0;i<itemList.size();i++)
							{
								Item I = itemList.get(i);
								if(I.item_code == item_Code)
								{
									code = I.item_code;
									itemName=I.name;
									buyNowPrice=I.buynow;
									itemOwner = I.owner;
									break;
									
								}
								
							}
							
							if(i == itemList.size())
							{
								message = "[Error: "+item_Code+" not found]";
								sendUDPPacket(message,IPAddress,port,currentuser);
								
							}
							else
							{
								itemList.remove(i);
								message ="[purchased "+code+" "+itemName+" "+buyNowPrice+" ]";
								System.out.println(message);
								sendUDPPacket(message,IPAddress,port,currentuser);
								System.out.println("Adding seller message to offline list");
								message ="Offline message :[sold "+code+" "+itemName+" ]";
								System.out.println("Adding the offline message");
								addOfflineMessage(itemOwner,message);
								sendACK(IPAddress,port);
							}
						}
						//Normal operation when the seller is online
						else
						{
						int i;
						int code =0;
						String itemName=null;
						int buyNowPrice=0;
						for(i=0;i<itemList.size();i++)
						{
							Item I = itemList.get(i);
							if(I.item_code == item_Code)
							{
								code = I.item_code;
								itemName=I.name;
								buyNowPrice=I.buynow;
								break;
								
							}
							
						}
						
						if(i == itemList.size())
						{
							message = "[Error: "+item_Code+" not found]";
							sendUDPPacket(message,IPAddress,port,currentuser);
							
						}
						else
						{
							itemList.remove(i);
							message ="purchased "+code+" "+itemName+" "+buyNowPrice;
							System.out.println(message);
							sendUDPPacket(message,buyer_IP,buyer_port,buyerName);
							System.out.println("Sending to seller");
							message ="sold "+code+" "+itemName;
							sendUDPPacket(message,IPAddress,port,currentuser);
						}
						
						}
						
						
						
					}
			
								
				}//end of while loop
				
			}//end of receiver function
	
		});//end of runnable
		
		tReceiverthread.start();
		System.out.println("Thread started");
	}//end of startReceiver
		
	
	

	
	public static void main(String[] args) throws Exception
	{
		// TODO Auto-generated method stub
		
		String[] split_commands = new String[3];
			
		clientList.clear();
		itemList.clear();
		offline_List.clear();
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System. in) );
		boolean value = false;
		boolean flag = true;
		
		
		System.out.println("Please enter the command\n");
		while(!value)
		{
			String command = inFromUser.readLine();
			split_commands = command.split(" ");
			if(split_commands.length == 3)
			{
				if((Integer.parseInt(split_commands[2]) < 1024) || (Integer.parseInt(split_commands[2]) > 65535))
				{
					System.out.println("[Error : port number out of range ]");
					
				}
				else
				{
					if(!split_commands[1].equals("-s"))
					{
						System.out.println("[Error : not a server command ]");
					}
					else
					{
					value = true;
					}
				}
			}
			else
			{
				System.out.println("[Error : 2 arguments required ]");
				
			}
		}
		
		
		serverSocket = new DatagramSocket(Integer.parseInt(split_commands[2]));
		System.out.println("Server has been started\n");
		byte[] sendData = new byte[8192];
		
		new UDPServer().startReceiving();
		
		
		
		
	}//main
}//class
			
		
		
		
		
		
		
		

				
		
		
	
	
 
	

	


