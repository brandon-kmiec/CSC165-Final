package a3;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;
import tage.nodeControllers.*;
import tage.networking.IGameConnection.ProtocolType;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.*;
import tage.audio.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.*;
import org.joml.*;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;

	private boolean paused=false;
	private int counter=0;
	private double lastFrameTime, currFrameTime, elapsTime, timeElapsed;
	
	// booleans
	private boolean stopDol = false;
	private boolean visitedSphere = false, visitedCube = false, visitedTorus = false, visitedPlane = false;
	private boolean powerUpActive = false;
	private boolean drawXYZAxis = true;
	private boolean drawWireframe = false;
	private boolean avatarLight = true;
	
	private static String avatarToUse;
	
	private	Vector3f loc, fwd, up, right, newLocation; 
	private Vector3f localPitch, globalYaw;

	// game objects
	private GameObject avatar, frontWheel, backWheel;
	private GameObject sphere, cube, torus, plane; 
	private GameObject cone;
	private GameObject xLine, yLine, zLine; 
	private GameObject octBin, spherePc, cubePc, torusPc, planePc;
	private GameObject powerUp;
	private GameObject ground;
	
	// shapes
	private ObjShape avatarS, frontWheelS, backWheelS;
	private ObjShape sphereShape, cubeShape, torusShape, planeShape;
	private ObjShape coneS;
	private ObjShape xLineS, yLineS, zLineS;
	private ObjShape octBinS, spherePcS, cubePcS, torusPcS, planePcS;
	private ObjShape powerUpS;
	private ObjShape groundPlane;
	
	// textures
	private TextureImage avatarTx, sphereTx, cubeTx, torusTx, planeTx;
	private TextureImage coneTx;
	private TextureImage octBinTX, spherePcTx, cubePcTx, torusPcTx, planePcTx;
	private TextureImage powerUpTx;
	private TextureImage groundTx, groundHM;
	
	// light
	private Light light1, light2, light3;
	
	private float camDolDistance = 0.0f;
	
	private int score = 0;
	
	private InputManager im;
	
	// orbit controller
	private CameraOrbit3D orbitController;
	
	// camera
	private Camera cam;
	private Camera leftCamera, rightCamera;
	
	// viewports
	private Viewport leftVp, rightVp;
	
	// nodeControllers
	private NodeController rc;
	private NodeController tcSphere, tcCube, tcTorus, tcPlane;
	
	// skybox
	private int fluffyClouds;
	
	// networking
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	
	// ghost
	private GhostManager gm;
	private ObjShape ghostS;
	private TextureImage ghostT;
	
	// physics
	private PhysicsEngine physicsEngine;
	private PhysicsObject rect1P, rect2P, planeP, cubeP, sphereP, cylinder1P, cylinder2P;
	private boolean running = true;
	private float vals[] = new float[16];
	
	// audio
	private IAudioManager audioMgr;
	private Sound outsideSound, carSound, crashSound;
	
	
	public MyGame(String serverAddress, int serverPort, String protocol) {
		super(); 
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if(protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	} // end MyGame

	public static void main(String[] args) {
		MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		
		avatarToUse = DisplaySettingsDialog.getAvatar();
		
		game.initializeSystem();
		game.game_loop();
	} // end main

	@Override
	public void loadShapes() {
		// load avatar shape
		avatarS = new ImportedModel("CarNoWheels.obj");
		frontWheelS = new ImportedModel("FrontWheels.obj");
		backWheelS = new ImportedModel("BackWheels.obj");

		// ghost avatar shape
		ghostS = new ImportedModel("CustomCar.obj");
		
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
		groundPlane = new TerrainPlane(1000);	// pixels per axis = 1000x1000
		
		// cone shape
		coneS = new ImportedModel("cone.obj");
	} // end loadShapes

	@Override
	public void loadTextures() {
		// avatar and ghost texture
		if(avatarToUse.equals("blue") || avatarToUse.equals("default")) {
			avatarTx = new TextureImage("CustomCarUV_wrap_blue.png");
			ghostT = new TextureImage("CustomCarUV_wrap_blue.png");
		} // end if
		else if(avatarToUse.equals("red")) {
			avatarTx = new TextureImage("CustomCarUV_wrap_red.png");
			ghostT = new TextureImage("CustomCarUV_wrap_red.png");
		} // end else if
	
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
		groundHM = new TextureImage("ground_height_map.png");
		
		// cone texture
		coneTx = new TextureImage("Cone.png");
	} // end loadTextures
	
	@Override
	public void loadSkyBoxes() {
		fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	} // end loadSkyBoxes
	
	@Override
	public void loadSounds() {
		AudioResource resource1, resource2, resource3;
		audioMgr = engine.getAudioManager();
		
		resource1 = audioMgr.createAudioResource("assets/sounds/car_engine.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("assets/sounds/outside.wav", AudioResourceType.AUDIO_SAMPLE);
		resource3 = audioMgr.createAudioResource("assets/sounds/CrashSound.wav", AudioResourceType.AUDIO_SAMPLE);
		
		carSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, false);
		outsideSound = new Sound(resource2, SoundType.SOUND_EFFECT, 300, true);
		crashSound = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
		
		carSound.initialize(audioMgr);
		outsideSound.initialize(audioMgr);
		crashSound.initialize(audioMgr);
		
		carSound.setMaxDistance(10.0f);
		carSound.setMinDistance(0.5f);
		carSound.setRollOff(5.0f);
		
		outsideSound.setMaxDistance(50.0f);
		outsideSound.setMinDistance(0.5f);
		outsideSound.setRollOff(5.0f);
		
		crashSound.setMaxDistance(20.0f);
		crashSound.setMinDistance(0.5f);
		crashSound.setRollOff(5.0f);
	} // end loadSounds

	@Override
	public void buildObjects() {	
		Matrix4f initialTranslation, initialScale, initialRotation;

		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatarTx);
		initialTranslation = (new Matrix4f()).translation(0, 0, 0);
		initialScale = (new Matrix4f()).scaling(1.0f);
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);
			// front wheels
		frontWheel = new GameObject(GameObject.root(), frontWheelS, avatarTx);
		initialTranslation = (new Matrix4f()).translation(0, 0, 0);
		initialScale = (new Matrix4f()).scaling(1.0f);
		frontWheel.setLocalTranslation(initialTranslation);
		frontWheel.setLocalScale(initialScale);
		frontWheel.setParent(avatar);
		frontWheel.propagateTranslation(true);
		frontWheel.propagateRotation(true);
			// back wheels
		backWheel = new GameObject(GameObject.root(), backWheelS, avatarTx);
		initialTranslation = (new Matrix4f()).translation(0, 0, 0);
		initialScale = (new Matrix4f()).scaling(1.0f);
		backWheel.setLocalTranslation(initialTranslation);
		backWheel.setLocalScale(initialScale);
		backWheel.setParent(avatar);
		backWheel.propagateTranslation(true);
		backWheel.propagateRotation(true);
		
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
		
		/*
		// build plane visit object
		plane = new GameObject(GameObject.root(), planeShape, planeTx);
		initialTranslation = (new Matrix4f()).translation(0, 0, 15);
		initialScale = (new Matrix4f()).scaling(1.0f);
		initialRotation = (new Matrix4f()).rotation((float)Math.toRadians(90), 1, 0, 0);
		plane.setLocalTranslation(initialTranslation);
		plane.setLocalScale(initialScale);
		plane.setLocalRotation(initialRotation);
		*/
		
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
		
		/*
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
		*/
		
		// build ground
		ground = new GameObject(GameObject.root(), groundPlane, groundTx);
		initialTranslation = (new Matrix4f()).translation(0, -1, 0);
		initialScale = (new Matrix4f()).scaling(50.0f, 3.0f, 50.0f);
		ground.setLocalTranslation(initialTranslation);
		ground.setLocalScale(initialScale);
		// enable ground heightMap
		ground.setHeightMap(groundHM);
		ground.getRenderStates().setTiling(1);
		//ground.getRenderStates().setTileFactor(10);
		
		// build cone
		cone = new GameObject(GameObject.root(), coneS, coneTx);
		initialTranslation = (new Matrix4f()).translation(-2, -0.9f, -2);
		cone.setLocalTranslation(initialTranslation);
	} // end buildObjects

	@Override
	public void createViewports() {	
		(engine.getRenderSystem()).addViewport("LEFT", 0, 0, 1f, 1f);
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
		
		rightCamera.setLocation(new Vector3f(0, 5, 0));
		rightCamera.setU(new Vector3f(1, 0, 0));
		rightCamera.setV(new Vector3f(0, 0, -1));
		rightCamera.setN(new Vector3f(0, -2, 0));
	} // end createViewports
		
	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.75f, 0.75f, 0.75f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
		
		// spotlight pointing down on octBin
		light2 = new Light();
		light2.setLocation(new Vector3f (0.0f, 0.0f, 0.0f));
		light2.setType(Light.LightType.SPOTLIGHT);
		(engine.getSceneGraph()).addLight(light2);	// disabled for A2
		
		light3 = new Light();
		light3.setLocation(new Vector3f (0.0f, 0.0f, 0.0f));
		light3.setType(Light.LightType.SPOTLIGHT);
		(engine.getSceneGraph()).addLight(light3);
	} // end initializeLights

	@Override
	public void initializeGame() {	
		// ------------------ Initialize Physics System ------------------
		float[] gravity = {0f, -5f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);
		
		
		// ------------------ Create Physics World ------------------
		float mass = 1.0f;
		float upVec[] = {0, 1, 0};
		double[] tempTransform;
		float size[] = {1.5f,1.25f,3.125f};
		
		// avatar
		Matrix4f transform = new Matrix4f(avatar.getLocalTranslation());
		transform.set(3, 2, 0.125f);
		tempTransform = toDoubleArray(transform.get(vals));
		rect1P = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
		avatar.setPhysicsObject(rect1P);
		
		// ground
		transform = new Matrix4f(ground.getLocalTranslation());
		tempTransform = toDoubleArray(transform.get(vals));
		planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, upVec, 0.0f);
		ground.setPhysicsObject(planeP);
		
		// cone
		transform = new Matrix4f(cone.getLocalTranslation());
		transform.set(3, 1, 0);
		tempTransform = toDoubleArray(transform.get(vals));
		size[0] = 1.875f; size[1] =  2f; size[2] = 1.875f;
		rect2P = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
		cone.setPhysicsObject(rect2P);
		
		// cube
		transform = new Matrix4f(cube.getLocalTranslation());
		tempTransform = toDoubleArray(transform.get(vals));
		size[0] = 1.0f; size[1] = 1.0f; size[2] = 1.0f;
		cubeP = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
		cube.setPhysicsObject(cubeP);
		//transform.set()
		
		// sphere
		transform = new Matrix4f(sphere.getLocalTranslation());
		tempTransform = toDoubleArray(transform.get(vals));
		sphereP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, 1.75f);
		sphere.setPhysicsObject(sphereP);
		
		// cylinder
		transform = new Matrix4f(torus.getLocalTranslation());
		tempTransform = toDoubleArray(transform.get(vals));
		cylinder1P = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, 1.25f, 0.5f);
		torus.setPhysicsObject(cylinder1P);
		
		// cylinder
		transform = new Matrix4f(octBin.getLocalTranslation());
		tempTransform = toDoubleArray(transform.get(vals));
		cylinder2P = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, 1.25f, 1.0f);
		octBin.setPhysicsObject(cylinder2P);
		
		// enable rendering
		engine.enableGraphicsWorldRender();
		//engine.enablePhysicsWorldRender();
				
		
		// ------------------ Networking ------------------
		setupNetworking();


		// ------------------ Elapsed Time & Window Size ------------------
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		timeElapsed = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);
		
		
		// ------------- Inputs -------------
		im = engine.getInputManager();
		
		
		// ------------- Positioning Orbit Camera -------------
		String gpName = im.getFirstGamepadName();
		cam = engine.getRenderSystem().getViewport("LEFT").getCamera();
		
		if(gpName == null)
			gpName = im.getKeyboardName();
		
		orbitController = new CameraOrbit3D(leftCamera, avatar, gpName, engine);
		
		
		// ---------------- Input Manager Actions ----------------
		// IAction actions
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);
		BttnAction bttnAction = new BttnAction(this);
		//RvpMoveAction rvpMoveAction = new RvpMoveAction(this);
		
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
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._3, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key._3, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		// keyboard movement actions
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//Pitch disabled for A2
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		// right viewport camera movement (gamepad)
		// xz camera movement disabled for final project
		//im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.POV, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._4, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._5, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// right viewport camera movement (keyboard)
		// xz camera movement disabled for final project
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.T, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.G, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.F, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.H, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.V, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.B, rvpMoveAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		/*
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
		*/
		
		// ---------------- Sound ----------------
		// set location
		carSound.setLocation(avatar.getWorldLocation());
		outsideSound.setLocation(new Vector3f(0, 0, 0));
		crashSound.setLocation(avatar.getWorldLocation());
		
		setEarParameters();
		
		// play sound
		carSound.play();
		outsideSound.play();
	} // end initializeGame
	
	public void setEarParameters() {
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(cam.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	} // end setEarParameters
	
	private void setupNetworking() {
		isClientConnected = false;
		try {
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort,
										   serverProtocol, this);
		} catch(UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} // end try catch
		
		if(protClient == null) {
			System.out.println("missing protocol host");
		} // end if
		else {
			// ask client protocol to send initial join message
			// to server, with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage(avatarToUse);
		} // end else
	} // end setupNetworking

	@Override
	public void update() {
		// change elapsTime based on if powerUp has been activated or not
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		elapsTime = (currFrameTime - lastFrameTime) / 150.0;

		// update physics
		if(running) {
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float)elapsTime * 150.0f);
			for(GameObject go : engine.getSceneGraph().getGameObjects()) {
				if(go.getPhysicsObject() != null) {					
					// set translation
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3, 0, mat.m30());
					mat2.set(3, 1, mat.m31());
					mat2.set(3, 2, mat.m32());
					go.setLocalTranslation(mat2);
					
					// set rotation
					mat.getRotation(aa);
					mat3.rotation(aa);
					go.setLocalRotation(mat3);
					
					// update altitude of avatar based on height map
					if(go != ground) {
						Vector3f tempLoc = go.getWorldLocation();

						float height = ground.getHeight(tempLoc.x(), tempLoc.z()) - 0.75f;
						go.setLocalLocation(new Vector3f(tempLoc.x(), height, tempLoc.z()));
					} // end if
				} // end if
			} // end for
		} // end if
		else {
			// update altitude of avatar based on height map
			Vector3f tempLoc = avatar.getWorldLocation();
			float height = ground.getHeight(tempLoc.x(), tempLoc.z()) - 0.75f;
			avatar.setLocalLocation(new Vector3f(tempLoc.x(), height, tempLoc.z()));
		} // end else
			
		// toggle the avatar light
		if(avatarLight) {
			light2.setLocation(new Vector3f(avatar.getWorldLocation().x, avatar.getWorldLocation().y + 5, avatar.getWorldLocation().z));
		}
		else {
			light2.setLocation(new Vector3f(5000, -500, 5000));
		}
		
		rightCamera.setLocation(new Vector3f(avatar.getWorldLocation().x, avatar.getWorldLocation().y + 5, avatar.getWorldLocation().z));
		rightCamera.setU(avatar.getWorldRightVector());
		rightCamera.setV(avatar.getWorldForwardVector());
	
		// update the input manager
		im.update((float)elapsTime);
	
		// check the distance between the dolphin and other objects
		//checkDolDistance();
		
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

		// build and set HUD
		buildSetHUD();
		
		// place the camera on the back of the avatar
		loc = avatar.getWorldLocation();
		fwd = avatar.getWorldForwardVector();
		up = avatar.getWorldUpVector();
		right = avatar.getWorldRightVector();
		cam.setU(right);
		cam.setV(up);
		cam.setN(fwd);
		cam.setLocation(loc.add(up.mul(1.3f)).add(fwd.mul(-2.5f)));
		
		orbitController.updateCameraPosition();
		
		processNetworking((float)elapsTime);
		
		// update sound
		carSound.setLocation(avatar.getWorldLocation());
		//outsideSound.setLocation(cone.getWorldLocation());
		crashSound.setLocation(avatar.getWorldLocation());
		setEarParameters();
	} // end update
	
	protected void processNetworking(float elapsTime) {
		// process packets received by the client from the server
		if(protClient != null)
			protClient.processPackets();
	} // end processNetworking
	/*
	private void checkDolDistance() {

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
	*/
	
	private void buildSetHUD() {
		// avatar's world position hud (right viewport)
		float avX = avatar.getWorldLocation().x();
		float avY = avatar.getWorldLocation().y();
		float avZ = avatar.getWorldLocation().z();
		String dispStrAvLoc = "X: " + avX + " Y: " + avY + " Z: " + avZ;
		Vector3f hudAvLocColor = new Vector3f(0,0,0);
		(engine.getHUDmanager()).setHUD1(dispStrAvLoc, hudAvLocColor, (int)(rightVp.getActualLeft() + 3), (int)(leftVp.getActualHeight() * 0.015));
				
		// score, time elapsed hud (left viewport)
		int elapsTimeSec = Math.round((float)timeElapsed);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String dispStrScore = "Time = " + elapsTimeStr;
		Vector3f hudScoreColor = new Vector3f(0, 0, 0);
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
			case KeyEvent.VK_SPACE:
				running = !running;
				System.out.println("Physics: " + running);
				break;
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
	
	// return the avatar
	public GameObject getAvatar() {
		return avatar;
	} // end getAvatar
	
	public String getAvatarToUse() {
		return avatarToUse;
	} // end getAvatarToUse
	
	public GameObject getFrontWheel() {
		return frontWheel;
	} // end getFrontWheel

	public GameObject getBackWheel() {
		return backWheel;
	} // end getBackWheel
	
	// return the ghost shape
	public ObjShape getGhostShape() {
		return ghostS;
	} // end getGhostShape
	
	// return the ghost texture
	public TextureImage getGhostTexture() {
		return ghostT;
	} // end getGhostTexture
	
	// return the ghost manager
	public GhostManager getGhostManager() {
		return gm;
	} // end getGhostManager
	
	// return the engine
	public Engine getEngine() {
		return engine;
	} // end getEngine
	
	public PhysicsEngine getPhysicsEngine() {
		return physicsEngine;
	} // end getPhysicsEngine
	
	public void setIsConnected(boolean isConnected) {
		this.isClientConnected = isConnected;
	} // end setIsConnected
	
	// return the player position
	public Vector3f getPlayerPosition() {
		return avatar.getWorldLocation();
	} // end getPlayerPosition
	
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
	
	// toggle (boolean) avatar light
	public void toggleAvatarLight() {
		avatarLight = !avatarLight;
	} // end toggleAvatarLight
	
	public Camera getRightVpCam() {
		return engine.getRenderSystem().getViewport("RIGHT").getCamera();
	} // end getRightVpCam
	
	// ------------- UTILITY FUNCTIONS used by physics -------------
	private float[] toFloatArray(double[] arr) {
		if(arr == null) {
			return null;
		} // end if
		
		int n = arr.length;
		float[] ret = new float[n];
		
		for(int i = 0; i < n; i++) {
			ret[i] = (float)arr[i];
		} // end for
		return ret;
	} // end toFloatArray
	
	private double[] toDoubleArray(float[] arr) {
		if(arr == null) {
			return null;
		} // end if
		
		int n = arr.length;
		double[] ret = new double[n];
		
		for(int i = 0; i < n; i++) {
			ret[i] = (double)arr[i];
		} // end for
		return ret;
	} // end toDoubleArray
	
	private void checkForCollisions() {
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
		
		dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		
		for(int i = 0; i < manifoldCount; i++) {
			manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			
			for(int j = 0; j < manifold.getNumContacts(); j++) {
				contactPoint = manifold.getContactPoint(j);
				if(contactPoint.getDistance() < 0.0f) {
					System.out.println("---- hit between " + obj1 + " and " + obj2 + " ----");
					
					if(obj1 != planeP || obj2 != planeP)
						crashSound.play();
					
					break;
				} // end if
			} // end for
		} // end for
	} // end checkForCollisions
	
	public void applyForce(float fx, float fy, float fz) {
		rect1P.applyForce(fx, fy, fz, 0, 0, 0);
		avatar.setPhysicsObject(rect1P);
	} // end applyForce

	public void setVelocity(float x, float y, float z) {
		float[] vals = {x, y, z};
		rect1P.setLinearVelocity(vals);
		avatar.setPhysicsObject(rect1P);
	} // end setVelocity

	private class SendCloseConnectionPacketAction extends AbstractInputAction {
		// for leaving the game... need to attach to an input device
		@Override
		public void performAction(float time, net.java.games.input.Event evt) {
			if(protClient != null && isClientConnected == true) {
				protClient.sendByeMessage();
			} // end if
		} // end performAction
	} // end SendCloseConnectionPacketAction class
} // end MyGame