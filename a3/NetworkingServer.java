package a3;

import java.io.IOException;

import tage.networking.IGameConnection.ProtocolType;

public class NetworkingServer {
	private GameServerUDP thisUDPServer;
	//private GameServerTCP thisTCPServer;
	
	public NetworkingServer(int serverPort, String protocol) {
		try {
			if(protocol.toUpperCase().compareTo("TCP") == 0) {
				//thisTCPServer = new GameServerTCP(serverPort);
			} // end if
			else {
				thisUDPServer = new GameServerUDP(serverPort);
			} // end else
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end NetworkingServer Constructor
	
	public static void main(String[] args) {
		if(args.length > 1) {
			NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		} // end if
	} // end main
} // end NetworkingServer