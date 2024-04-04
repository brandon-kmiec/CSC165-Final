package a3;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

/**
*	FwdAction is an input manager class for the forward movements of the avatar.
*/
public class FwdAction extends AbstractInputAction {
	private MyGame game;
	private GameObject av;
	private Vector3f oldPosition, newPosition;
	private Vector3f fwdDirection;
	private Camera rvpCam;
	private float xinc, zinc;
	
	/**	creates a FwdAction with MyGame as specified */
	public FwdAction(MyGame g) {
		game = g;
		rvpCam = game.getRightVpCam();
		xinc = 0;
		zinc = 0;
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
			oldPosition = av.getWorldLocation();
			fwdDirection = av.getWorldForwardVector();
			
			// detect which component is being activated and modify newPosition accordingly
			if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.W)
				newPosition = oldPosition.add(fwdDirection.mul(time));
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.S)
				newPosition = oldPosition.add(fwdDirection.mul(-time));
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.Y) {
				if(keyValue > 0)
					newPosition = oldPosition.add(fwdDirection.mul(-time));
				else if(keyValue < 0)
					newPosition = oldPosition.add(fwdDirection.mul(time));
			} // end else if 
		
			av.setLocalLocation(newPosition);
		} // end if
		
	} // end performAction
} // end FwdAction Class