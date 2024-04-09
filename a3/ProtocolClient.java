package a3;

import tage.networking.client.GameConnectionClient;

import org.joml.*;

import java.util.UUID;
import java.net.InetAddress;
import java.io.IOException;

public class ProtocolClient extends GameConnectionClient {
	private MyGame game;
	private UUID id;
	private GhostManager ghostManager;
	
	public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType pType,
							MyGame game) throws IOException {
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
	} // end ProtocolClient Constructor
	
	@Override
	protected void processPacket(Object msg) {
		String strMessage = (String)msg;
		System.out.println("message received --> " + strMessage);
		String[] messageTokens = strMessage.split(",");
		if(messageTokens.length > 0) {
			if(messageTokens[0].compareTo("join") == 0) {	// receive "join"
				// format: join,success or join,failure
				if(messageTokens[1].compareTo("success") == 0) {
					System.out.println("join success confirmed");
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
				} // end if
				if(messageTokens[1].compareTo("failure") == 0) {
					System.out.println("join failure confirmed");
					game.setIsConnected(false);
				} // end if
			} // end if
			
			if(messageTokens[0].compareTo("bye") == 0) {	// receive "bye"
				// format: bye, remoteId
				UUID ghostID = UUID.fromString(messageTokens[1]);
				ghostManager.removeGhostAvatar(ghostID);
			} // end if
			
			if((messageTokens[0].compareTo("dsfr") == 0) ||	// receive "dsfr"
				(messageTokens[0].compareTo("create") == 0)) {
				// format: create, remoteId, x,y,z or dsfr, remoteId, x,y,z
				UUID ghostID = UUID.fromString(messageTokens[1]);
				Vector3f ghostPosition = new Vector3f(Float.parseFloat(messageTokens[2]),
													  Float.parseFloat(messageTokens[3]),
													  Float.parseFloat(messageTokens[4]));
				try {
					ghostManager.createGhost(ghostID, ghostPosition);
				} catch(IOException e) {
					System.out.println("error creating ghost avatar");
				} // end try catch
			} // end if
			
			if(messageTokens[0].compareTo("wsds") == 0)	{	// rec. "wants..."
				UUID ghostID = UUID.fromString(messageTokens[1]);
				sendDetailsForMessage(ghostID, game.getPlayerPosition());
			} // end if
			
			if(messageTokens[0].compareTo("move") == 0)	{	// rec. "move..."
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				Vector3f ghostPosition = new Vector3f(Float.parseFloat(messageTokens[2]),
													  Float.parseFloat(messageTokens[3]),
													  Float.parseFloat(messageTokens[4]));
				
				ghostManager.updateGhostAvatar(ghostID, ghostPosition);
			} // end if
		} // end if
	} // end processPacket
	
	public void sendJoinMessage() {		// format: join, localId
		try {
			sendPacket(new String("join," + id.toString()));
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendJoinMessage
	
	public void sendCreateMessage(Vector3f pos) {	
		// format: (create, localId, x,y,z)
		try {
			String message = new String("create," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			System.out.println(message);
			sendPacket(message);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendCreateMessage
	
	public void sendByeMessage() {
		try {
			sendPacket(new String("bye," + id.toString()));
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendByeMessage
	
	public void sendDetailsForMessage(UUID remId, Vector3f pos) {
		// format: (dsfr, localId, x,y,z)
		try {
			String message = new String("dsfr," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendDetailsForMessage
	
	public void sendMoveMessage(Vector3f pos) {
		// format: (move, localId, x,y,z)
		try {
			String message = new String("move," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendMoveMessage
} // end ProtocolClient class