
package org.usfirst.frc.team435.robot;


import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.internal.HardwareTimer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class.
 * The SampleRobot class is the base of a robot application that will automatically call your
 * Autonomous and OperatorControl methods at the right time as controlled by the switches on
 * the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're inexperienced,
 * don't. Unless you know what you are doing, complex code will be much more difficult under
 * this system. Use IterativeRobot or Command-Based instead if you're new.
 */
public class Robot extends SampleRobot {
    RobotDrive myRobot;
    Joystick driver,smo; 
    Compressor compressor;
//    DoubleSolenoid platformLifter1, platformLifter2;
    DoubleSolenoid gripperPistons;
    Solenoid ejectorPiston;
    SpeedController funnelConveyor,leadScrew; 
    Accelerometer internalAcceleromoter ;
    Button halfSpeed,compressorOn,compressorOff,gripperTrigger,ejectorTrigger,platformUp,platformDown;
    boolean lifterBypass,lifterLimitBypass,funnelBypass;
    
    DigitalInput funnelSensor,platformBottomLimit,platformTopLimit,hasToteLoaded;
    final double LIFTER_SPEED_UP=0.6;
    final double LIFTER_SPEED_DOWN=-LIFTER_SPEED_UP;
    final double DEADBAND_FUNNEL_AXIS = 0.1; //+/- 10% from center still does nothing.
    final double FUNNEL_SPEED = 0.5; // Run at half speed
    final String FUNNEL_BYPASS = "Funnel_Bypass";
    final String LIFTER_BYPASS = "Lifter_Bypass";
    final String LIFTER_LIMIT_BYPASS = "Lifter_Limit_Bypass";
    
    Timer.Interface ejectorTimer;
    //Use to debounce the joystick button
    boolean lastButtonState = false;
    public Robot() {
    
    }
    
    @Override
    protected void robotInit() {
    
    	super.robotInit();
    	// Preferences are stored in ROM, and remembered between boots. We'll seed from that, but allow changing
    	// the boolean value of the smart dashboard mid match if needed
    	Preferences prefs = Preferences.getInstance();
    	
    	SmartDashboard.putBoolean(FUNNEL_BYPASS, prefs.getBoolean(FUNNEL_BYPASS, true));
        SmartDashboard.putBoolean(LIFTER_BYPASS, prefs.getBoolean(LIFTER_BYPASS, true));
        SmartDashboard.putBoolean(LIFTER_LIMIT_BYPASS, prefs.getBoolean(LIFTER_LIMIT_BYPASS, true));
        
        
        //Motors
    	myRobot = new RobotDrive(0,1,2,3);
        funnelConveyor = new Jaguar(4);
        leadScrew = new Jaguar(5);
        
        //Pneumatics
        compressor = new Compressor();
        compressor.start();
//        platformLifter1 = new DoubleSolenoid(1,2);
//        platformLifter2 = new DoubleSolenoid(3,4);
        ejectorPiston = new Solenoid(5);
        gripperPistons = new DoubleSolenoid(1,2);
        
        myRobot.setExpiration(0.1);
        
        //DS inputs
        driver = new Joystick(0);
        smo = new Joystick(1);
        
        // Probably an enum for this - buttons based on logitech extreme 3d
        halfSpeed = new JoystickButton(driver, 1);
        gripperTrigger = new JoystickButton(smo, 1);
        ejectorTrigger = new JoystickButton(smo,2);
        platformUp = new JoystickButton(smo, 5);
        platformDown = new JoystickButton(smo, 3);
        compressorOn = new JoystickButton(smo,4);
        compressorOff = new JoystickButton(smo, 6);
        
        //sensors
        internalAcceleromoter = new BuiltInAccelerometer();
        hasToteLoaded = new DigitalInput(1);
        funnelSensor = new DigitalInput(2);
        platformBottomLimit = new DigitalInput(3);
        platformTopLimit = new DigitalInput(4);
        
        
        //other
        ejectorTimer = new HardwareTimer().newTimer();
    }
    @Override
    protected void disabled() {
    	// TODO Auto-generated method stub
    	super.disabled();
    	leadScrew.set(0);
    	myRobot.mecanumDrive_Cartesian(0, 0, 0, 0);
    }
    /**
     * Drive left & right motors for 2 seconds then stop
     */
    public void autonomous() {
       /* myRobot.setSafetyEnabled(false);
        myRobot.drive(-0.5, 0.0);	// drive forwards half speed
        Timer.delay(2.0);		//    for 2 seconds
        myRobot.drive(0.0, 0.0);	// stop robot*/
    }

    /**
     * Runs the motors with arcade steering.
     */
    public void operatorControl() {
        myRobot.setSafetyEnabled(true);
        
      
        
        while (isOperatorControl() && isEnabled()) {
        	manageDashboard();
        	manageDrive();
        	manageCompressor();
        	manageGripper();
        	manageLift();
            manageEjector();
            
            Timer.delay(0.005);		// wait for a motor update time
        }
    }
    public void manageGripper()
    {
    	//We have a transition to button being held
    	if (gripperTrigger.get() && !lastButtonState)
		{
    		if (gripperPistons.get().equals(DoubleSolenoid.Value.kForward))
			{
    			gripperPistons.set(DoubleSolenoid.Value.kReverse);
			}
    		else
    		{
    			gripperPistons.set(DoubleSolenoid.Value.kForward);
    		}
    		lastButtonState=true;
		}
    	// Trigger released, reset last state (and isolate pistons off from air system, retain current state)
    	else if (!gripperTrigger.get())
    	{
    		lastButtonState=false;
    		gripperPistons.set(DoubleSolenoid.Value.kOff);
    	}
    }
    public void manageEjector()
    {
    	if (ejectorTrigger.get() && (hasToteLoaded.get()||lifterBypass ))
    	{
    		ejectorPiston.set(true);
    		ejectorTimer.reset();
    		ejectorTimer.start();
    	}
    	if (ejectorTimer.get()>2000)
    	{
    		ejectorTimer.stop();
    		ejectorPiston.set(false);
    	}
    }
    public void manageFunnel()
    {
    	if (funnelBypass)
    	{
    		funnelConveyor.set(scaleDeadband(DEADBAND_FUNNEL_AXIS, smo.getThrottle()));	
    	}
    	else if(funnelSensor.get()) 
    	{
    		funnelConveyor.set(FUNNEL_SPEED);	
    	}
    	else
    	{
    		funnelConveyor.set(0);
    	}
    	
    }
    public void manageCompressor()
    {
    	if (compressorOn.get())
    	{
    		compressor.start();
    	}
    	if (compressorOff.get())
    	{
    		compressor.stop();
    	}
    }
    public void manageDrive()
    {
    	if(halfSpeed.get())
    	{
    		myRobot.mecanumDrive_Cartesian(driver.getX()/2.0d, driver.getY()/2.0d, driver.getZ()/2.0d, 0);
    	}
    	else
    	{
    		myRobot.mecanumDrive_Cartesian(driver.getX(), driver.getY(), driver.getZ(), 0);	
    	}
    }
    public void manageLift()
    {
    	//button pushed to lift
    	if (platformUp.get())
    	{
//    		platformLifter1.set(DoubleSolenoid.Value.kForward);
//    		platformLifter2.set(DoubleSolenoid.Value.kForward);
    		
    		leadScrew.set(safeMotorValue(LIFTER_SPEED_UP, platformBottomLimit, platformTopLimit,lifterLimitBypass));
    	}
    	else if (platformDown.get())
    	{
//    		platformLifter1.set(DoubleSolenoid.Value.kReverse);
//    		platformLifter2.set(DoubleSolenoid.Value.kReverse);
    		leadScrew.set(safeMotorValue(LIFTER_SPEED_DOWN, platformBottomLimit, platformTopLimit,lifterLimitBypass));
    	}

    }
    public static double safeMotorValue(double desired_speed, DigitalInput ls_reverse, DigitalInput ls_forward, boolean bypass)
    {
    	if (bypass)
    	{
    		return desired_speed;
    	}
    				
    	if( (desired_speed>0 && !ls_forward.get()) || (desired_speed <0 && !ls_reverse.get()))
    	{
    		return desired_speed;
    	}
    	else
    	{
    		return 0;
    	}
    }
    
    public double scaleDeadband(double deadband, double value)
    {
    	if (Math.abs(value)<deadband)
    	{
    		return 0;
    	}
    	else
    	{
    		return (value-(Math.abs(value)/value*deadband))/(1-deadband);
    	}
    }
    public void manageDashboard()
    {
    	funnelBypass = SmartDashboard.getBoolean(FUNNEL_BYPASS);
    	lifterBypass = SmartDashboard.getBoolean(LIFTER_BYPASS);
    	lifterLimitBypass = SmartDashboard.getBoolean(LIFTER_LIMIT_BYPASS);
    }
    /**
     * Runs during test mode
     */
    public void test() {
    	System.out.println("deadband 0.1, giving value 0.05, expected 0, got back "+ scaleDeadband(0.1, 0.05));
    	System.out.println("deadband 0.1, giving value -0.05, expected 0, got back "+ scaleDeadband(0.1, -0.05));
    	System.out.println("deadband 0.1, giving value 1.0, expected 1, got back "+ scaleDeadband(0.1, 1.0));
    	System.out.println("deadband 0.1, giving value -1.0, expected -1, got back "+ scaleDeadband(0.1, -1.0));
    	System.out.println("deadband 0.1, giving value 0.55, expected 0.5, got back "+ scaleDeadband(0.1, 0.55));
    	System.out.println("deadband 0.1, giving value -0.55, expected -0.5, got back "+ scaleDeadband(0.1, -0.55));
    	
    		
    }
}
