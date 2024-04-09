package a3;

import tage.*;

import org.joml.*;

import java.util.UUID;
import java.util.Vector;
import java.util.Iterator;
import java.io.IOException;

public class GhostManager {
	private MyGame game;
	private Vector<GhostAvatar> ghostAvs = new Vector<GhostAvatar>();
	
	public GhostManager(VariableFrameRateGame vfrg) {
		game = (MyGame)vfrg;
	} // end GhostManager Constructor 
	
	public void createGhost(UUID id, Vector3f p) throws IOException {
		System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(id, s, t, p);
		Matrix4f initialScale = (new Matrix4f()).scaling(1.0f);
		newAvatar.setLocalScale(initialScale);
		ghostAvs.add(newAvatar);
	} // end createGhost
	
	public void removeGhostAvatar(UUID id) {
		GhostAvatar ghostAv = findAvatar(id);
		if(ghostAv != null) {
			game.getEngine().getSceneGraph().removeGameObject(ghostAv);
			ghostAvs.remove(ghostAv);
		} // end if
		else {
			System.out.println("tried to remove - unable to find ghost in list");
		} // end else
	} // end removeGhostAvatar

	private GhostAvatar findAvatar(UUID id) {
		GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ghostAvs.iterator();
		while(it.hasNext()) {
			ghostAvatar = it.next();
			if(ghostAvatar.getID().compareTo(id) == 0) {
				return ghostAvatar;
			} // end if
		} // end while
		return null;
	} // end findAvatar
	
	public void updateGhostAvatar(UUID id, Vector3f position) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if(ghostAvatar != null) {
			ghostAvatar.setPosition(position);
		} // end if
		else {
			System.out.println("tried to update - unable to find ghost in list");
		} // end else
	} // end updateGhostAvatar
} // end GhostManager class