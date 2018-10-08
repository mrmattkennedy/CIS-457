import std.algorithm : remove;
import std.conv : to;
import std.socket : InternetAddress, Socket, SocketException, SocketSet, TcpSocket;
import std.stdio : writeln, writefln;
import std.string;

class server
{
private: 
	ushort port; 
	byte[] data;
	string firstLn;
	string fromClient;
	string command;
	TcpSocket serverSocket;
	SocketSet socketSet;
	Socket[] reads;
	enum MAX_CONNECTIONS = 60;

public: 
	this (string[] args) {

		if (args.length >= 2)
			port = to!ushort(args[1]);
		else
			port = 4444;
		setupSocket();
		serverSocket = new TcpSocket();
		socketSet = new SocketSet(MAX_CONNECTIONS + 1);
	}
	
	void setupSocket() {	
		assert(serverSocket.isAlive);
		serverSocket.blocking();
		serverSocket.bind(new InternetAddress(port));
		serverSocket.listen(10);
		writefln("Listening on port %d\n", port);
		
	//	loop();
	}

	void loop() {
		while (true) {
			socketSet.add(serverSocket);
			foreach(socket; reads)
				socketSet.add(socket);

			Socket.select(socketSet, null, null);
/*
			for (size_t i = 0; i < reads.length; i++) 
			{
				if (socketSet.isSet(reads[i]))
				{
					
				}
			}
			*/
			socketSet.reset();

		}
	}

}



int main (string[] args) {
	auto socketTest = new server(args);

	return 1;

}
