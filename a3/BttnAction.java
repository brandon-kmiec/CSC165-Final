package a3;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

/**
*	BttnAction is an input manager class for the anything that might be considered a button.
*	@author Brandon Kmiec
*/
public class BttnAction extends AbstractInputAction {
	private MyGame game;
	private GameObject av;
	private boolean rideDolphin = false;
	
	/**	creates a BttnAction with MyGame as specified */
	public BttnAction(MyGame g) {
		game = g;
	} // end BttnAction Constructor
	
	/**	toggle avatar wireframe and xyz axis based on Event */
	@Override
	public void performAction(float time, Event e) {
		av = game.getAvatar();
		
		// toggle dolphin wireframe
		if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Button._2 ||
			e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key._2)
			game.toggleAvWf();
			
		// toggle xyz axis
		if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Button._0 ||
			e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.O)
			game.toggleXYZAxis();

	} // end performAction
} // end BttnAction Class