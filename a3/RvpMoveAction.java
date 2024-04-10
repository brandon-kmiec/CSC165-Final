package a3;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

/**
*	RvpMoveAction is an input manager class for the camera movements of the Right Viewport.
*	@author Brandon Kmiec
*/
public class RvpMoveAction extends AbstractInputAction {
	private MyGame game;
	private Camera rvpCam;
	private float xinc, yinc, zinc;
	
	/**	creates a RvpMoveAction with MyGame as specified */
	public RvpMoveAction(MyGame g) {
		game = g;
		rvpCam = game.getRightVpCam();
		xinc = 0;
		yinc = 0;
		zinc = 0;
	} // end RvpMoveAction Constructor
	
	/**	move camera on xz plane and zoom in/out based on Event */
	@Override
	public void performAction(float time, Event e) {
		float keyValue = e.getValue();
		
		// Gamepad movements
		if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.POV) {	// d-pad xz movement
			if(keyValue == 0.0) {	// No movement
				xinc = 0f;
				zinc = 0f;
			} // end if
			else if(keyValue == 0.125) {	// Forward Left
				xinc = -0.1f;
				zinc = 0.1f;
			} // end else if
			else if(keyValue == 0.25) {		// Forward
				xinc = 0f;
				zinc = 0.1f;
			} // end else if
			else if(keyValue == 0.375) {	// Forward Right
				xinc = 0.1f;
				zinc = 0.1f;
			} // end else if
			else if(keyValue == 0.5) {		// Right
				xinc = 0.1f;
				zinc = 0f;
			} // end else if
			else if(keyValue == 0.625) {	// Backward Right
				xinc = 0.1f;
				zinc = -0.1f;
			} // end else if
			else if(keyValue == 0.75) {		// Backward
				xinc = 0f;
				zinc = -0.1f;
			} // end else if
			else if(keyValue == 0.875) {	// Backward Left
				xinc = -0.1f;
				zinc = -0.1f;
			} // end else if
			else if(keyValue == 1.0) {		// Left
				xinc = -0.1f;
				zinc = 0f;
			} // end else if
		} // end if
		
		// Keyboard movements
		else {
			if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.T) {	// move forward
				xinc = 0f;
				zinc = -0.1f;
			} // end if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.G) {	// move backward
				xinc = 0f;
				zinc = 0.1f;
			} // end else if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.F) {	// move left
				xinc = -0.1f;
				zinc = 0f;
			} // end else if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.H) {	// move right
				xinc = 0.1f;
				zinc = 0f;
			} // end else if
			else {
				xinc = 0f;
				zinc = 0f;
			} // end else
		} // end else	
		
		//Keyboard and Gamepad zoom in/out
		if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Button._4 ||
			e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.V) {	// zoom in
			if(rvpCam.getLocation().y() < 1.5f)
				yinc = 0.0f;
			else
				yinc = -0.1f;
		} // end if
		else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Button._5 ||
			e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.B) {	// zoom out
			yinc = 0.1f;
		} // end else if
		else
			yinc = 0f;
		
		rvpCam.setLocation(rvpCam.getLocation().add(new Vector3f(xinc, yinc, zinc)));
	} // end performAction
} // end RvpMoveAction Class