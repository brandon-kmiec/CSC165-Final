package a3;

import tage.*;

import org.joml.*;

import java.util.UUID;

public class GhostAvatar extends GameObject {
	private UUID id;
	//private Vector3f pos;
	
	public GhostAvatar(UUID id, ObjShape s, TextureImage t, Vector3f p) {
		super(GameObject.root(), s, t);
		this.id = id;
		//this.pos = p;
		//setPosition(pos);
		setPosition(p);
	} // end GhostAvatar Constructor
	
	public UUID getID() {
		return id;
	} // end getId
	
	public Vector3f getPosition() {
		return getWorldLocation();
	} // end getPosition
	
	public void setId(UUID id) {
		this.id = id;
	} // end setId
	
	public void setPosition(Vector3f pos) {
		setLocalLocation(pos);
	} // end setPosition
} // end GhostAvatar class