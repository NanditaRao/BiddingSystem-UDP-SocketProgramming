package bidding;

public class Item {
	
	int item_code;
	String name;
	String owner;
	int translimit;
	int startbid;
	int buynow;
	String description;
	int current_bid;
	int transaction_count = 0;
	String buyername;
	String i;
	
	
	Item(int code,String name1,String username, int trans_limit, int start_bid , int buy_now , String desc)
	{
		item_code = code;
		name =name1;
		owner = username;
		translimit = trans_limit;
		startbid = start_bid;
		buynow = buy_now;
		description = desc;
		current_bid = start_bid;
	
	}
	
	void set_current_bid(int value)
	{
		current_bid = value;
	}
	
	void set_buyername(String name)
	{
		buyername = name;
	}
	String get_info()
	{
		i = item_code+" "+name+" "+owner+" "+current_bid+" "+buynow+" "+description;
		return i;
	}

}
