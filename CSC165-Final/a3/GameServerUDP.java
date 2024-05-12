package a3;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServerUDP extends GameConnectionServer<UUID> {
	public GameServerUDP(int localPort) throws IOException {
		super(localPort, ProtocolType.UDP);
	} // end GameServerUDP Constructor
	
	@Override
	public void processPacket(Object o, InetAddress senderIP, int senderPort) {
		String message = (String)o;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0) {
			// case where server receives a JOIN message
			// format: join,localid
			if(msgTokens[0].compareTo("join") == 0) {
				try {
					IClientInfo ci;
					ci = getServerSocket().createClientInfo(senderIP, senderPort);
					UUID clientID = UUID.fromString(msgTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					sendJoinMessage(clientID, true);
				} catch(IOException e) {
					e.printStackTrace();
				} // end try catch
			} // end if
			
			// case where server receives a CREATE message
			// format: create,localid,x,y,z
			if(msgTokens[0].compareTo("create") == 0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
				String avatarToUse = msgTokens[5];
				sendCreateMessages(clientID, pos, avatarToUse);
				sendWantsDetailsMessages(clientID);
			} // end if
			
			// case where server receives a BYE message
			// format: bye,localid
			if(msgTokens[0].compareTo("bye") == 0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				System.out.println("Exit request received from - " + clientID.toString());
				sendByeMessages(clientID);
				removeClient(clientID);
			} // end if
			
			// case where server receives a DETAILS-FOR message
			if(msgTokens[0].compareTo("dsfr") == 0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				UUID remoteId = UUID.fromString(msgTokens[2]);
				String[] pos = {msgTokens[3], msgTokens[4], msgTokens[5]};
				String avatarToUse = msgTokens[6];
				sendDetailsMessage(clientID, remoteId, pos, avatarToUse);
			} // end if
			
			// case where server receives a MOVE message
			if(msgTokens[0].compareTo("move") == 0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
				sendMoveMessages(clientID, pos);
			} // end if
			
			// case where server receives a TURN message
			if(msgTokens[0].compareTo("turn") == 0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] rot = {msgTokens[2], msgTokens[3], msgTokens[4], msgTokens[5],
								msgTokens[6], msgTokens[7], msgTokens[8], msgTokens[9],
								msgTokens[10], msgTokens[11], msgTokens[12], msgTokens[13],
								msgTokens[14], msgTokens[15], msgTokens[16], msgTokens[17]};
				sendTurnMessage(clientID, rot);
			} // end if
		} // end if
	} // end processPacket
	
	public void sendJoinMessage(UUID clientID, boolean success) {
		// format: join,success or join,failure
		try {
			System.out.println("trying to confirm join");
			String message = new String("join,");
			if(success) {
				message += "success";
			} // end if
			else {
				message += "failure";
			} // end else
			sendPacket(message, clientID);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendJoinMessage
	
	public void sendCreateMessages(UUID clientID, String[] position, String avatarToUse) {
		// format: create,remoteId,x,y,z
		try {
			String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + avatarToUse;
			forwardPacketToAll(message, clientID);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendCreateMessages
	
	public void sendDetailsMessage(UUID clientID, UUID remoteId, String[] position, String avatarToUse) {
		try {
			String message = new String("dsfr," + remoteId.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + avatarToUse;			
			sendPacket(message, clientID);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendDetailsMessage
	
	public void sendWantsDetailsMessages(UUID clientID) {
		try {
			String message = new String("wsds," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendWantsDetailsMessages
	
	public void sendMoveMessages(UUID clientID, String[] position) {
		try {
			String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendMoveMessages
	
	public void sendTurnMessage(UUID clientID, String[] rot) {
		try {
			String message = new String("turn," + clientID.toString());
			message += "," + rot[0] + "," + rot[1] + "," + rot[2] + "," + rot[3];
			message += "," + rot[4] + "," + rot[5] + "," + rot[6] + "," + rot[7];
			message += "," + rot[8] + "," + rot[9] + "," + rot[10] + "," + rot[11];
			message += "," + rot[12] + "," + rot[13] + "," + rot[14] + "," + rot[15];
			forwardPacketToAll(message, clientID);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendTurnMessage
	
	public void sendByeMessages(UUID clientID) {
		try {
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendByeMessages
} // end GameServerUDP class