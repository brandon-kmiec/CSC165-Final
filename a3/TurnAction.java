package a3;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.physics.PhysicsObject;
import net.java.games.input.Event;
import org.joml.*;

/**
*	TurnAction is an input manager class for the pitch and yaw movements of the avatar.
*/
public class TurnAction extends AbstractInputAction {
	private MyGame game;
	private GameObject av;
	private Vector4f oldUp;
	private Matrix4f oldRotation, newRotation, rotAroundAvatarUp;
	private PhysicsObject po;
	
	/**	creates a TurnAction with MyGame as specified */
	public TurnAction(MyGame g) {
		game = g;
		
		po = game.getAvatar().getPhysicsObject();
	} // end TurnAction Constructor
	
	/**	pitch and yaw the avatar based on Event */
	@Override
	public void performAction(float time, Event e) {
		// allow movement if getStopDol is false		
		if(!game.getStopDol()) {
			time /= 1.5f;
		
			float keyValue = e.getValue();

			if(keyValue > -0.2 && keyValue < 0.2)
				return; // deadzone
		
			av = game.getAvatar();
			
			float y = 0;
		
			// detect which component is being activated and perform globalYaw or localPitch accordingly
			if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.A) {
				//av.globalYaw(new Matrix4f(), time, 0);
				y = time * 7.5f;
			} // end if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.D) {
				//av.globalYaw(new Matrix4f(), time, 1);
				y = time * -7.5f;
			} // end else if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.UP) {
				av.localPitch(new Matrix4f(), time, 0);
			} // end else if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.DOWN) {
				av.localPitch(new Matrix4f(), time, 1);
			} // end else if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.RY) {
				if(keyValue > 0) {
					av.localPitch(new Matrix4f(), time, 1);
				} // end if
				else if(keyValue < 0) {
					av.localPitch(new Matrix4f(), time, 0);
				} // end else if
			} // end if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.X) {
				if(keyValue > 0) {
					y = time * 5;
					//av.globalYaw(new Matrix4f(), time, 1);
				} // end if
				else if(keyValue < 0) {
					y = time * -5;
					//av.globalYaw(new Matrix4f(), time, 0);
				} // end else if
			} // end else if

			po.applyTorque(0, y, 0);
		} // end if
	} // end performAction
} // end TurnAction Class