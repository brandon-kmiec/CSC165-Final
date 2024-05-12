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
	//private GhostAvatar newAvatar;
	
	public GhostManager(VariableFrameRateGame vfrg) {
		game = (MyGame)vfrg;
	} // end GhostManager Constructor 
	
	public void createGhost(UUID id, Vector3f p, String texture) throws IOException {
		System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		
		/*
		TextureImage t1 = new TextureImage("CustomCarUV_wrap_blue.png");
		TextureImage t2 = new TextureImage("CustomCarUV_wrap_red.png");
		System.out.println(texture);
		if(texture.equals("blue") || texture.equals("default")) {
			newAvatar = new GhostAvatar(id, s, t1, p);
		} // end if
		else if(texture.equals("red")) {
			newAvatar = new GhostAvatar(id, s, t2, p);
		} // end else if
		*/
		System.out.println("CustomCarUV_wrap_" + texture + ".png");
		TextureImage t = new TextureImage("CustomCarUV_wrap_" + texture + ".png");
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
	
	public void updateGhostAvatarPos(UUID id, Vector3f position) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if(ghostAvatar != null) {
			ghostAvatar.setPosition(position);
		} // end if
		else {
			System.out.println("tried to update - unable to find ghost in list");
		} // end else
	} // end updateGhostAvatarPos

	public void updateGhostAvatarRot(UUID id, Matrix4f rotation) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if(ghostAvatar != null) {
			ghostAvatar.setRotation(rotation);
		} // end if
		else {
			System.out.println("tried to update - unable to find ghost in list");
		} // end else
	} // end updateGhostAvatarRot
} // end GhostManager class