import java.net.*; // for InetAddress

public class IPFinder2 {
	public static void main(String[] args) {
		// Get name and IP address of the local host
		try {
			//Step 1:
			InetAddress address = InetAddress.getLocalHost(); // Gets the local host’s IP address in object format
			System.out.println("Local Host:");
			System.out.println("\t" + address.toString());// Gets the host name for this IP address.
			//Step 2:
		} catch (UnknownHostException e) {
			System.out.println("Unable to determine this host's address");
		}

		for (int i = 0; i < args.length; i++) {
			// Get name(s)/address(es) of hosts given on command-line
			try {
				//Step 3: Create an array of InetAddress instances for the specified host
				InetAddress[] addressList = InetAddress.getAllByName(args[i]);
				System.out.println(args[i] + ":");
				// Print the first name and all associated IP addresses. Assume array contains at least one entry./
				//Step 4:
				System.out.println("\t" + addressList[0].getHostName());
				for (int j = 0; j < addressList.length; j++)
					//Step 5:
					System.out.println("\t" + addressList[j].getHostAddress());
			} catch (UnknownHostException e) {
				System.out.println("Unable to find address for " + args[i]);
			}
		}
	}
}
