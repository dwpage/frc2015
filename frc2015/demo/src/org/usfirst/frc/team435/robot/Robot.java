
package org.usfirst.frc.team435.robot;


import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.internal.HardwareTimer;

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
    Joystick driver;
    Joystick smo; 
    Compressor compressor;
    DoubleSolenoid platformLifter1;
    DoubleSolenoid platformLifter2;
    SpeedController funnelConveyor; 
    Accelerometer internalAcceleromoter ;
    Button halfSpeed;
    Button compressorOn;
    Button compressorOff;
    Button conveyorTrigger;
    Button platformUp;
    
    DigitalInput funnelSensor;
    DigitalInput platformBottomLimit;
    DigitalInput platformTopLimit;

    public Robot() {
        //Motors
    	myRobot = new RobotDrive(0,1,2,3);
        funnelConveyor = new Jaguar(4);
        
        //Pneumatics
        compressor = new Compressor();
        compressor.start();
        platformLifter1 = new DoubleSolenoid(1,2);
        platformLifter2 = new DoubleSolenoid(3,4);
        
        myRobot.setExpiration(0.1);
        
        //DS inputs
        driver = new Joystick(0);
        smo = new Joystick(1);
      
        // Probably an enum for this
        halfSpeed = new JoystickButton(driver, 1);
        compressorOn = new JoystickButton(smo,5);
        compressorOff = new JoystickButton(smo, 6);
        platformUp = new JoystickButton(smo, 1);
        
        //sensors
        internalAcceleromoter = new BuiltInAccelerometer();
        funnelSensor = new DigitalInput(2);
        platformBottomLimit = new DigitalInput(3);
        platformTopLimit = new DigitalInput(4);
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
        Timer.Interface liftTimer = new HardwareTimer().newTimer();
        
        while (isOperatorControl() && isEnabled()) {
        	
        	if(halfSpeed.get())
        	{
        		myRobot.mecanumDrive_Cartesian(driver.getX()/2, driver.getY()/2.0, driver.getZ()/2.0, 0);		
        	}
        	else
        	{
        		myRobot.mecanumDrive_Cartesian(driver.getX(), driver.getY(), driver.getZ(), 0);	
        	}
            
        	if (compressorOn.get())
        	{
        		compressor.start();
        	}
        	if (compressorOff.get())
        	{
        		compressor.stop();
        	}
        	//trigger pulled to lift
        	if (platformUp.get())
        	{
        		platformLifter1.set(DoubleSolenoid.Value.kForward);
        		platformLifter2.set(DoubleSolenoid.Value.kForward);
        		liftTimer.reset();
        		liftTimer.start();
        	}
        	//Once they let go, make sure it has a full second to extend, then retract. Excepting a catch to hold tote now.
        	else if (liftTimer.hasPeriodPassed(1000))
        	{
        		liftTimer.stop();
        		platformLifter1.set(DoubleSolenoid.Value.kReverse);
        		platformLifter2.set(DoubleSolenoid.Value.kReverse);
        	}
        	
            
            Timer.delay(0.005);		// wait for a motor update time
        }
    }

    /**
     * Runs during test mode
     */
    public void test() {
    }
}
