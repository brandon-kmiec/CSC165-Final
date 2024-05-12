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
					sendCreateMessage(game.getPlayerPosition(), game.getAvatarToUse());
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
				// format: create, remoteId, x,y,z, avatarToUse or dsfr, remoteId, x,y,z, avatarToUse
				UUID ghostID = UUID.fromString(messageTokens[1]);
				Vector3f ghostPosition = new Vector3f(Float.parseFloat(messageTokens[2]),
													  Float.parseFloat(messageTokens[3]),
													  Float.parseFloat(messageTokens[4]));
				String avatarToUse = messageTokens[5];
				try {
					ghostManager.createGhost(ghostID, ghostPosition, avatarToUse);
				} catch(IOException e) {
					System.out.println("error creating ghost avatar");
				} // end try catch
			} // end if
			
			if(messageTokens[0].compareTo("wsds") == 0)	{	// rec. "wants..."
				UUID ghostID = UUID.fromString(messageTokens[1]);
				sendDetailsForMessage(ghostID, game.getPlayerPosition(), game.getAvatarToUse());
			} // end if
			
			if(messageTokens[0].compareTo("move") == 0)	{	// rec. "move..."
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				Vector3f ghostPosition = new Vector3f(Float.parseFloat(messageTokens[2]),
													  Float.parseFloat(messageTokens[3]),
													  Float.parseFloat(messageTokens[4]));
				
				ghostManager.updateGhostAvatarPos(ghostID, ghostPosition);
			} // end if
			
			if(messageTokens[0].compareTo("turn") == 0) {	// rec. "turn..."
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				Matrix4f ghostRotation = new Matrix4f(Float.parseFloat(messageTokens[2]), Float.parseFloat(messageTokens[3]), Float.parseFloat(messageTokens[4]), 
													  Float.parseFloat(messageTokens[5]),
													  Float.parseFloat(messageTokens[6]), Float.parseFloat(messageTokens[7]), Float.parseFloat(messageTokens[8]), 
													  Float.parseFloat(messageTokens[9]),
													  Float.parseFloat(messageTokens[10]), Float.parseFloat(messageTokens[11]), Float.parseFloat(messageTokens[12]), 
													  Float.parseFloat(messageTokens[13]),
													  Float.parseFloat(messageTokens[14]), Float.parseFloat(messageTokens[15]), Float.parseFloat(messageTokens[16]), 
													  Float.parseFloat(messageTokens[17]));
													 
				ghostManager.updateGhostAvatarRot(ghostID, ghostRotation);
			} // end if
		} // end if
	} // end processPacket
	
	public void sendJoinMessage(String avatarToUse) {		// format: join, localId
		try {
			sendPacket(new String("join," + id.toString() + "," + avatarToUse));
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendJoinMessage
	
	public void sendCreateMessage(Vector3f pos, String avatarToUse) {	
		// format: (create, localId, x,y,z)
		try {
			String message = new String("create," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			message += "," + avatarToUse;
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
	
	public void sendDetailsForMessage(UUID remId, Vector3f pos, String avatarToUse) {
		// format: (dsfr, localId, x,y,z, avatarToUse)
		try {
			String message = new String("dsfr," + remId.toString() + "," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			message += "," + avatarToUse;
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
	
	public void sendTurnMessage(Matrix4f rot) {
		//format: (turn, localId, rot00, rot01, rot02, rot03, rot10, rot11, rot12, rot13, rot20, rot21, rot22, rot23, rot30, rot31, rot32, rot33)
		try {
			String message = new String("turn," + id.toString());
			message += "," + rot.m00() + "," + rot.m01() + "," + rot.m02() + "," + rot.m03();
			message += "," + rot.m10() + "," + rot.m11() + "," + rot.m12() + "," + rot.m13();
			message += "," + rot.m20() + "," + rot.m21() + "," + rot.m22() + "," + rot.m23();
			message += "," + rot.m30() + "," + rot.m31() + "," + rot.m32() + "," + rot.m33();
			sendPacket(message);
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
	} // end sendTurnMessage
} // end ProtocolClient class