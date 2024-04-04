package a3;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;
import tage.nodeControllers.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;

	private boolean paused=false;
	private int counter=0;
	private double lastFrameTime, currFrameTime, elapsTime, timeElapsed;
	
	private boolean stopDol = false;
	private boolean visitedSphere = false, visitedCube = false, visitedTorus = false, visitedPlane = false;
	private boolean powerUpActive = false;
	private boolean drawXYZAxis = true;
	private boolean drawWireframe = false;
	
	private Camera cam;
	private Camera leftCamera, rightCamera;
	
	private	Vector3f loc, fwd, up, right, newLocation; 
	private Vector3f localPitch, globalYaw;

	private GameObject avatar, sphere, cube, torus, plane; 
	private GameObject xLine, yLine, zLine; 
	private GameObject octBin, spherePc, cubePc, torusPc, planePc;
	private GameObject powerUp;
	private GameObject ground;
	
	private ObjShape dolS, sphereShape, cubeShape, torusShape, planeShape;
	private ObjShape xLineS, yLineS, zLineS;
	private ObjShape octBinS, spherePcS, cubePcS, torusPcS, planePcS;
	private ObjShape powerUpS;
	private ObjShape groundPlane;
	
	private TextureImage doltx, sphereTx, cubeTx, torusTx, planeTx;
	private TextureImage octBinTX, spherePcTx, cubePcTx, torusPcTx, planePcTx;
	private TextureImage powerUpTx;
	private TextureImage groundTx;
	
	private Light light1, light2;
	
	private float camDolDistance = 0.0f;
	
	private int score = 0;
	
	private InputManager im;
	
	private CameraOrbitController orbitController;
	
	private Viewport leftVp, rightVp;
	
	private NodeController rc;
	private NodeController tcSphere, tcCube, tcTorus, tcPlane;
	
	public MyGame() { super(); } // end MyGame

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	} // end main

	@Override
	public void loadShapes()
	{
		// load dolphin shape
		dolS = new ImportedModel("dolphinHighPoly.obj");
		
		// load visit objects shapes
		cubeShape = new Cube();
		sphereShape = new Sphere();
		torusShape = new Torus();
		planeShape = new Plane();

		// load xyz world axes shapes
		xLineS = new Line((new Vector3f(0, 0, 0)), (new Vector3f(1, 0, 0)));
		yLineS = new Line((new Vector3f(0, 0, 0)), (new Vector3f(0, 1, 0)));
		zLineS = new Line((new Vector3f(0, 0, 0)), (new Vector3f(0, 0, 1)));
		
		// load octBin manual object
		octBinS = new ManualOctBin();
		
		// load visit shapes postcards shapes
		spherePcS = new Plane();
		cubePcS = new Plane();
		torusPcS = new Plane();
		planePcS = new Plane();
		
		// load powerUp shape
		powerUpS = new Sphere();
		
		// load groundPlane shape
		groundPlane = new Plane();
	} // end loadShapes

	@Override
	public void loadTextures()
	{
		// dolphin texture
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
	
		// visit objects textures
		cubeTx = new TextureImage("medieval-brick-wall.jpg");
		sphereTx = new TextureImage("customTexture2.png");
		torusTx = new TextureImage("customTexture.png");
		planeTx = new TextureImage("blackboard-green-old.jpg");
		
		// octBin manual object texture
		octBinTX = new TextureImage("silver.png");
		
		// textures for the visit object postcards
		spherePcTx = new TextureImage("customSpherePostcard.png");
		cubePcTx = new TextureImage("brickCubePostcard.png");
		torusPcTx = new TextureImage("customTorusPostcard.png");
		planePcTx = new TextureImage("blackboardPostcard.png");
		
		// powerUp texture
		powerUpTx = new TextureImage("sun.png");
		
		// gound texture
		groundTx = new TextureImage("rippled_sand.jpg");
	} // end loadTextures

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale, initialRotation;

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0,0,0);
		initialScale = (new Matrix4f()).scaling(3.0f);
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);
		
		// build cube visit object
		cube = new GameObject(GameObject.root(), cubeShape, cubeTx);
		initialTranslation = (new Matrix4f()).translation(-7, 0, 2);
		initialScale = (new Matrix4f()).scaling(0.5f);
		cube.setLocalTranslation(initialTranslation);
		cube.setLocalScale(initialScale);
		
		// build sphere visit object
		sphere = new GameObject(GameObject.root(), sphereShape, sphereTx);
		initialTranslation = (new Matrix4f()).translation(-5, 0, -15);
		initialScale = (new Matrix4f()).scaling(2.0f);
		sphere.setLocalTranslation(initialTranslation);
		sphere.setLocalScale(initialScale);
		
		// build torus visit object
		torus = new GameObject(GameObject.root(), torusShape, torusTx);
		initialTranslation = (new Matrix4f()).translation(6, 0, 3);
		initialScale = (new Matrix4f()).scaling(1.5f);
		torus.setLocalTranslation(initialTranslation);
		torus.setLocalScale(initialScale);
		
		// build plane visit object
		plane = new GameObject(GameObject.root(), planeShape, planeTx);
		initialTranslation = (new Matrix4f()).translation(0, 0, 15);
		initialScale = (new Matrix4f()).scaling(1.0f);
		initialRotation = (new Matrix4f()).rotation((float)Math.toRadians(90), 1, 0, 0);
		plane.setLocalTranslation(initialTranslation);
		plane.setLocalScale(initialScale);
		plane.setLocalRotation(initialRotation);
		
		// build xyz world axes
		xLine = new GameObject(GameObject.root(), xLineS, null);
		xLine.getRenderStates().setColor(new Vector3f(2, 0, 0));
		yLine = new GameObject(GameObject.root(), yLineS, null);
		yLine.getRenderStates().setColor(new Vector3f(0, 2, 0));
		zLine = new GameObject(GameObject.root(), zLineS, null);
		zLine.getRenderStates().setColor(new Vector3f(0, 0, 2));
		
		// build octagon bin manual object
		octBin = new GameObject(GameObject.root(), octBinS, octBinTX);
		initialTranslation = (new Matrix4f()).translation(3, 0, -3);
		initialScale = (new Matrix4f()).scaling(1.0f);
		octBin.setLocalTranslation(initialTranslation);
		octBin.setLocalScale(initialScale);
		
		// build sphere postcard located inside octBin
		spherePc = new GameObject(GameObject.root(), spherePcS, spherePcTx);
		initialTranslation = (new Matrix4f()).translation(2.0f, 0, 0);
		initialScale = (new Matrix4f()).scaling(0.0f);
		initialRotation = (new Matrix4f()).rotation((float)Math.toRadians(-30), 0, 0, 1);
		spherePc.setLocalTranslation(initialTranslation);
		spherePc.setLocalScale(initialScale);
		spherePc.setLocalRotation(initialRotation);
		spherePc.setParent(octBin);
		spherePc.propagateTranslation(true);
		spherePc.propagateRotation(true);
		
		// build cube postcard located inside octBin
		cubePc = new GameObject(GameObject.root(), cubePcS, cubePcTx);
		initialTranslation = (new Matrix4f()).translation(-2.0f, 0, 0);
		initialScale = (new Matrix4f()).scaling(0.0f);
		initialRotation = (new Matrix4f()).rotation((float)Math.toRadians(30), 0, 0, 1);
		cubePc.setLocalTranslation(initialTranslation);
		cubePc.setLocalScale(initialScale);
		cubePc.setLocalRotation(initialRotation);
		cubePc.setParent(octBin);
		cubePc.propagateTranslation(true);
		cubePc.propagateRotation(true);
		
		// build torus postcard located inside octBin
		torusPc = new GameObject(GameObject.root(), torusPcS, torusPcTx);
		initialTranslation = (new Matrix4f()).translation(0, 0, 2.0f);
		initialScale = (new Matrix4f()).scaling(0.0f);
		initialRotation = (new Matrix4f()).rotation((float)Math.toRadians(30), 1, 0, 0);
		torusPc.setLocalTranslation(initialTranslation);
		torusPc.setLocalScale(initialScale);
		torusPc.setLocalRotation(initialRotation);
		torusPc.setParent(octBin);
		torusPc.propagateTranslation(true);
		torusPc.propagateRotation(true);
		
		// build plane postcard located inside octBin
		planePc = new GameObject(GameObject.root(), planePcS, planePcTx);
		initialTranslation = (new Matrix4f()).translation(0, 0, -2.0f);
		initialScale = (new Matrix4f()).scaling(0.0f);
		initialRotation = (new Matrix4f()).rotation((float)Math.toRadians(-30), 1, 0, 0);
		planePc.setLocalTranslation(initialTranslation);
		planePc.setLocalScale(initialScale);
		planePc.setLocalRotation(initialRotation);
		planePc.setParent(octBin);
		planePc.propagateTranslation(true);
		planePc.propagateRotation(true);
		
		// build powerup
		powerUp = new GameObject(GameObject.root(), powerUpS, powerUpTx);
		initialTranslation = (new Matrix4f()).translation(15, 0, -3);
		initialScale = (new Matrix4f()).scaling(0.5f);
		powerUp.setLocalTranslation(initialTranslation);
		powerUp.setLocalScale(initialScale);
		
		// build ground
		ground = new GameObject(GameObject.root(), groundPlane, groundTx);
		initialTranslation = (new Matrix4f()).translation(0, -1, 0);
		initialScale = (new Matrix4f()).scaling(50.0f);
		//initialRotation = (new Matrix4f()).rotation((float)Math.toRadians(90), 0, 0, 0);
		ground.setLocalTranslation(initialTranslation);
		ground.setLocalScale(initialScale);
		//ground.setLocalRotation(initialRotation);
		ground.getRenderStates().setTiling(1);
	} // end buildObjects

	@Override
	public void createViewports()
	{	(engine.getRenderSystem()).addViewport("LEFT", 0, 0, 1f, 1f);
		(engine.getRenderSystem()).addViewport("RIGHT", 0.75f, 0, 0.25f, 0.25f);
		
		leftVp = (engine.getRenderSystem()).getViewport("LEFT");
		rightVp = (engine.getRenderSystem()).getViewport("RIGHT");
		
		leftCamera = leftVp.getCamera();
		rightCamera = rightVp.getCamera();
		
		rightVp.setHasBorder(true);
		rightVp.setBorderWidth(4);
		rightVp.setBorderColor(0.0f, 1.0f, 0.0f);
		
		leftCamera.setLocation(new Vector3f(-2, 0, -2));
		leftCamera.setU(new Vector3f(1, 0, 0));
		leftCamera.setV(new Vector3f(0, 1, 0));
		leftCamera.setN(new Vector3f(0, 0, -1));
		
		rightCamera.setLocation(new Vector3f(0, 2, 0));
		rightCamera.setU(new Vector3f(1, 0, 0));
		rightCamera.setV(new Vector3f(0, 0, 1));
		rightCamera.setN(new Vector3f(0, -2, 0));
	} // end createViewports
		
	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(0.75f, 0.75f, 0.75f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
		
		// spotlight pointing down on octBin
		light2 = new Light();
		light2.setLocation(new Vector3f (0.0f, 0.0f, 0.0f));
		light2.setType(Light.LightType.SPOTLIGHT);
		//(engine.getSceneGraph()).addLight(light2);	// disabled for A2
	} // end initializeLights

	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		timeElapsed = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);
		
		// ------------- inputs -------------
		im = engine.getInputManager();
		
		// ------------- positioning the camera -------------
		//(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,5));
		String gpName = im.getFirstGamepadName();
		cam = engine.getRenderSystem().getViewport("LEFT").getCamera();
		orbitController = new CameraOrbitController(leftCamera, avatar, gpName, engine);
		
		// ---------------- input manager actions ----------------
		// IAction actions
		FwdAction fwdAction = new FwdAction(this);
		TurnAction turnAction = new TurnAction(this);
		BttnAction bttnAction = new BttnAction(this);
		RvpMoveAction rvpMoveAction = new RvpMoveAction(this);
		
		// gamepad movement actions
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//Pitch disabled for A2
		//im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RY, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		// gamepad/keyboard button actions
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._2, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key._2, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._0, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.O, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		// keyboard movement actions
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//Pitch disabled for A2
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		// right viewport camera movement (gamepad)
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.POV, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._4, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._5, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// right viewport camera movement (keyboard)
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.T, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.G, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.F, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.H, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.V, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.B, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		// ---------------- Node Controllers ----------------
		rc = new RotationController(engine, new Vector3f(0, 1, 0), 0.0004f);

		tcSphere = new TranslateController(engine, 2.0f, 1, 0.0003f, 1.0f, sphere.getWorldLocation());
		tcCube = new TranslateController(engine, 2.0f, 2, 0.0003f, 2.0f, cube.getWorldLocation());
		tcPlane = new TranslateController(engine, 2.0f, 0, 0.0003f, 2.0f, plane.getWorldLocation());
		tcTorus = new TranslateController(engine, 2.0f, 1, 0.0003f, 1.0f, torus.getWorldLocation());
		
		rc.addTarget(octBin);
		tcSphere.addTarget(sphere);
		tcCube.addTarget(cube);
		tcPlane.addTarget(plane);
		tcTorus.addTarget(torus);
		
		(engine.getSceneGraph()).addNodeController(rc);
		(engine.getSceneGraph()).addNodeController(tcSphere);
		(engine.getSceneGraph()).addNodeController(tcCube);
		(engine.getSceneGraph()).addNodeController(tcPlane);
		(engine.getSceneGraph()).addNodeController(tcTorus);
		
		rc.enable();
	} // end initializeGame

	@Override
	public void update()
	{	
		// change elapsTime based on if powerUp has been activated or not
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		if(powerUpActive)
			elapsTime = (currFrameTime - lastFrameTime) / 150.0;
		else
			elapsTime = (currFrameTime - lastFrameTime) / 500.0;
	
		// update the input manager
		im.update((float)elapsTime);
	
		// check the distance between the dolphin and other objects
		checkDolDistance();
		
		// toggle the XYZ Axis
		if(drawXYZAxis) {
			xLine.getRenderStates().enableRendering();
			yLine.getRenderStates().enableRendering();
			zLine.getRenderStates().enableRendering();
		} // end if
		else {
			xLine.getRenderStates().disableRendering();
			yLine.getRenderStates().disableRendering();
			zLine.getRenderStates().disableRendering();
		} // end else
			
		// toggle the avatar wireframe
		if(drawWireframe)
			avatar.getRenderStates().setWireframe(true);
		else
			avatar.getRenderStates().setWireframe(false);

		//if(visitedSphere && visitedCube && visitedPlane && visitedTorus)
		//	rc.enable();

		// build and set HUD
		buildSetHUD();
		
		// place the camera on the back of the dolphin
		loc = avatar.getWorldLocation();
		fwd = avatar.getWorldForwardVector();
		up = avatar.getWorldUpVector();
		right = avatar.getWorldRightVector();
		cam.setU(right);
		cam.setV(up);
		cam.setN(fwd);
		cam.setLocation(loc.add(up.mul(1.3f)).add(fwd.mul(-2.5f)));
		
		orbitController.updateCameraPosition();
	} // end update
	
	private void checkDolDistance() {
		// get the distance between the dolphin and the camera.  Stop the dolphin if the distance if >10.0f, allow to move if
		//	<10.0f.  Dolphin will resume moving if the player hops on the back of the dolphin, making the distance < 10.0f.
		/*
		camDolDistance = avatar.getWorldLocation().distance(cam.getLocation());
		if(camDolDistance > 10.0f)
			stopDol = true;
		else if(camDolDistance < 10.0f)
			stopDol = false;
		*/
		
		// check if the dolphin is close enough to each visit location, if the distance is less than the value specified for 
		//  each object, set the object scale to 0.0f, the postcard scale for that object to 0.25f, increment the score, and 
		//  the visited boolean to true.
		if(!visitedSphere && avatar.getWorldLocation().distance(sphere.getWorldLocation()) < 2.5f) {
			//sphere.setLocalScale((new Matrix4f()).scaling(0.0f));
			spherePc.setLocalScale((new Matrix4f()).scaling(0.25f));
			visitedSphere = true;
			score++;
			tcSphere.enable();
		} // end if
		if(!visitedCube && avatar.getWorldLocation().distance(cube.getWorldLocation()) < 1.25f) {
			//cube.setLocalScale((new Matrix4f()).scaling(0.0f));
			cubePc.setLocalScale((new Matrix4f()).scaling(0.25f));
			visitedCube = true;
			score++;
			tcCube.enable();
		} // end if
		if(!visitedTorus && avatar.getWorldLocation().distance(torus.getWorldLocation()) < 2.25f) {
			//torus.setLocalScale((new Matrix4f()).scaling(0.0f));
			torusPc.setLocalScale((new Matrix4f()).scaling(0.25f));
			visitedTorus = true;
			score++;
			tcTorus.enable();
		} // end if
		if(!visitedPlane && avatar.getWorldLocation().distance(plane.getWorldLocation()) < 1.5f) {
			//plane.setLocalScale((new Matrix4f()).scaling(0.0f));
			planePc.setLocalScale((new Matrix4f()).scaling(0.25f));
			visitedPlane = true;
			score++;
			tcPlane.enable();
		} // end if
		
		// check if the dolphin is close enough to the powerUp, if the distance is less than 1.25f, set the scale of the 
		//	powerUp to 0.0f and set the powerUpActive boolean to true.
		if(!powerUpActive && avatar.getWorldLocation().distance(powerUp.getWorldLocation()) < 1.25f) {
			powerUp.setLocalScale((new Matrix4f()).scaling(0.0f));
			powerUpActive = true;
		} // end if
	} // end checkDolDistance
	
	private void buildSetHUD() {
		// avatar's world position hud (right viewport)
		float avX = avatar.getWorldLocation().x();
		float avY = avatar.getWorldLocation().y();
		float avZ = avatar.getWorldLocation().z();
		String dispStrAvLoc = "X: " + avX + " Y: " + avY + " Z: " + avZ;
		Vector3f hudAvLocColor = new Vector3f(1,1,1);
		(engine.getHUDmanager()).setHUD1(dispStrAvLoc, hudAvLocColor, (int)(rightVp.getActualLeft() + 3), (int)(leftVp.getActualHeight() * 0.015));
				
		// score, time elapsed hud (left viewport)
		int elapsTimeSec = Math.round((float)timeElapsed);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String dispStrScore = "Score = " + score + ", Time = " + elapsTimeStr;
		Vector3f hudScoreColor = new Vector3f(1, 1, 1);
		if(score >= 4) {
			dispStrScore = "You Win! " + "Time = " + elapsTimeStr;
			(engine.getHUDmanager()).setHUD2(dispStrScore, hudScoreColor, (int)(leftVp.getActualWidth() * 0.45), (int)(leftVp.getActualHeight() * 0.515));
		} // end if
		else {
			timeElapsed += (currFrameTime - lastFrameTime) / 1000.0;
			(engine.getHUDmanager()).setHUD2(dispStrScore, hudScoreColor, (int)(leftVp.getActualWidth() * 0.0079), (int)(leftVp.getActualHeight() * 0.015));
		} // end else
	} // end buildSetHUD

	// replaced with input manager
	@Override
	public void keyPressed(KeyEvent e)
	{	
		switch(e.getKeyCode())
		{
			/*
			case KeyEvent.VK_C:
				counter++;
				break;
			case KeyEvent.VK_1:	// pause the game
				paused = !paused;
				break;
			*/
			/*
			case KeyEvent.VK_W:	// move dolphin forward
				if(!stopDol) {
					fwd = dol.getWorldForwardVector();
					loc = dol.getWorldLocation();
					newLocation = loc.add(fwd.mul((float)elapsTime));
					dol.setLocalLocation(newLocation);
				}// end if
				break;
			case KeyEvent.VK_S: // move dolphin backwards
				if(!stopDol) {
					fwd = dol.getWorldForwardVector();
					loc = dol.getWorldLocation();
					newLocation = loc.add(fwd.mul(-(float)elapsTime));
					dol.setLocalLocation(newLocation);
				} // end if
				break;
			case KeyEvent.VK_A:	// turn (yaw) left
				if(!stopDol)
					dol.globalYaw(new Matrix4f(), elapsTime, 0);
				break;
			case KeyEvent.VK_D:	// turn (yaw) right
				if(!stopDol)
					dol.globalYaw(new Matrix4f(), elapsTime, 1);
				break;
			case KeyEvent.VK_UP:	// turn (pitch) up
				if (!stopDol)
					dol.localPitch(new Matrix4f(), elapsTime, 0);
				break;
			case KeyEvent.VK_DOWN: // turn (pitch) down
				if(!stopDol)
					dol.localPitch(new Matrix4f(), elapsTime, 1);
				break;
			*/
			/*
			case KeyEvent.VK_SPACE:	// hop on/off dolphin
				setRideDolphin(!rideDolphin);
				break;
			*/
			/*
			case KeyEvent.VK_2:
				dol.getRenderStates().setWireframe(true);
				break;
			case KeyEvent.VK_3:
				dol.getRenderStates().setWireframe(false);
				break;
			case KeyEvent.VK_4:
				(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,0));
				break;
			*/
		} // end switch
		super.keyPressed(e);
	} // end keyPressed
	
	// disconnect the camera from the dolphin (not riding the dolphin)
	public void disconnectDolCam() {
		cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
		loc = avatar.getWorldLocation();
		fwd = avatar.getWorldForwardVector();
		up = avatar.getWorldUpVector();
		right = avatar.getWorldRightVector();
		cam.setU(right);
		cam.setV(up);
		cam.setN(fwd);
		cam.setLocation(loc.add(up.mul(0.5f)).add(fwd.mul(-0.5f)).add(right.mul(1.0f)));
	} // end disconnectDolCam
	
	// return the avatar (dolphin)
	public GameObject getAvatar() {
		return avatar;
	} // end getAvatar
	
	// return value of stopDol
	public boolean getStopDol() {
		return stopDol;
	} // end getStopDol
	
	// toggle (boolean) drawXYZAxis
	public void toggleXYZAxis() {
		drawXYZAxis = !drawXYZAxis;
	} // end toggleXYZAxis
	
	// toggle (boolean) wireframe of the avatar
	public void toggleAvWf() {
		drawWireframe = !drawWireframe;
	} // end toggleAvWf
	
	public Camera getRightVpCam() {
		return engine.getRenderSystem().getViewport("RIGHT").getCamera();
	} // end getRightVpCam
} // end MyGame