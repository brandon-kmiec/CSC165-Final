package a3;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.physics.PhysicsObject;
import net.java.games.input.Event;
import org.joml.*;

/**
*	FwdAction is an input manager class for the forward movements of the avatar.
*/
public class FwdAction extends AbstractInputAction {
	private MyGame game;
	private GameObject av, fw, bw;
	private Vector3f oldPosition, newPosition;
	private Vector3f fwdDirection;
	private Vector3f bkwDirection;
	private Camera rvpCam;
	private float xinc, zinc;
	private ProtocolClient protClient;
	private PhysicsObject po;
	
	/**	creates a FwdAction with MyGame as specified */
	public FwdAction(MyGame g, ProtocolClient p) {
		game = g;
		protClient = p;
		rvpCam = game.getRightVpCam();
		xinc = 0;
		zinc = 0;
		
		po = game.getAvatar().getPhysicsObject();
	} // end FwdAction Constructor
	
	/**	move the avatar based on Event */
	@Override
	public void performAction(float time, Event e) {		
		// allow movement if getStopDol is false
		if(!game.getStopDol()) {
			time /= 1.5f;
			
			float keyValue = e.getValue();
			
			if(keyValue > -0.2 && keyValue < 0.2)
				return; // deadzone

			av = game.getAvatar();
			fw = game.getFrontWheel();
			bw = game.getBackWheel();
			oldPosition = av.getWorldLocation();
			fwdDirection = av.getWorldForwardVector();
			
			bkwDirection = av.getWorldForwardVector();
			bkwDirection.mul(-1);
			System.out.println(fwdDirection.toString() + "\n" + bkwDirection.toString());
			
			float x = 0, y = 0, z = 0;
			
			// detect which component is being activated and modify newPosition accordingly
			if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.W) {
				//game.applyForce(fwdDirection.x() + 10, fwdDirection.y() + 10, fwdDirection.z() + 10);
				newPosition = oldPosition.add(fwdDirection.mul(time));
				
				x = (fwdDirection.x()) * time * 1000;
				y = (fwdDirection.y()) * time * 1000;
				z = (fwdDirection.z()) * time * 1000;
				System.out.println(x + " " + y + " " + z);
			} // end if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.S) {
				//fwdDirection = av.getWorldForwardVector() * -1;
				
				newPosition = oldPosition.add(fwdDirection.mul(-time));
				
				x = (bkwDirection.x()) * time * 10;
				y = (bkwDirection.y()) * time * 10;
				z = (bkwDirection.z()) * time * 10;

				System.out.println(x + " " + y + " " + z);
			} // end else ify
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.Y) {
				if(keyValue > 0) {
					newPosition = oldPosition.add(fwdDirection.mul(-time));
					
					x = (bkwDirection.x()) * time * 10;
					y = (bkwDirection.y()) * time * 10;
					z = (bkwDirection.z()) * time * 10;
					System.out.println(x + " " + y + " " + z);
				} // end if
				else if(keyValue < 0) {
					newPosition = oldPosition.add(fwdDirection.mul(time));
					
					x = (fwdDirection.x()) * time * 1000;
					y = (fwdDirection.y()) * time * 1000;
					z = (fwdDirection.z()) * time * 1000;
					System.out.println(x + " " + y + " " + z);
				} // end else if
			} // end else if 
			
			if(x > 0 || y > 0 || z > 0) {
				//fw.setLocalRotation(fw.getLocalRotation().rotation(-(float)time, fw.getWorldRightVector()));
				fw.setLocalRotation((new Matrix4f()).rotation((float)time, 1, 0, 0));
				//fw.localPitch(new Matrix4f(), time, 1);
				bw.setLocalRotation((new Matrix4f()).rotation((float)time, 1, 0, 0));
				//bw.localPitch(new Matrix4f(), time, 1);
				//bw.setLocalRotation(bw.getLocalRotation().rotation(-(float)time, bw.getWorldRightVector()));
			} // end if
			
			System.out.println(x + " " + y + " " + z);
			//game.applyForce(x, y, z);
			po.applyForce(x, y, z, 0, 0, 0);
			
			//av.setLocalLocation(newPosition);
			protClient.sendMoveMessage(av.getWorldLocation());
		} // end if
		
	} // end performAction
} // end FwdAction Class