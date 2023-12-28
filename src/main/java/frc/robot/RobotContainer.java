// Copyright (c) FIRST and other WPILib contributors.

// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.commands.SwerveDriveCommand;
import frc.robot.subsystems.SwerveSubsystem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static frc.robot.Constants.*;


public class RobotContainer
{
    // The robot's subsystems and commands are defined here...
    private final SwerveSubsystem swerveSubsystem = new SwerveSubsystem();

    private final static XboxController controller_driveX = new XboxController(0);
    //    private final static Joystick controller_driveF = new Joystick(0);
    private final SendableChooser<Command> autoChooser;

    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    public RobotContainer()
    {
        swerveSubsystem.setDefaultCommand(new SwerveDriveCommand(
                swerveSubsystem,
                () -> -controller_driveX.getRawAxis(XboxController.Axis.kLeftY.value),
                () -> -controller_driveX.getRawAxis(XboxController.Axis.kLeftX.value),
                () -> -controller_driveX.getRawAxis(XboxController.Axis.kRightX.value),
                () -> controller_driveX.getPOV(),
                () -> controller_driveX.getBackButton()
        ));
        // Configure the trigger bindings

        NamedCommands.registerCommand("marker1", Commands.print("Passed marker 1"));
        NamedCommands.registerCommand("marker2", Commands.print("Passed marker 2"));
        NamedCommands.registerCommand("print hello", Commands.print("hello"));
    
        configureBindings();

        autoChooser = AutoBuilder.buildAutoChooser(); // Default auto will be `Commands.none()`
        SmartDashboard.putData("Auto Mode", autoChooser);
        // Shuffleboard.getTab("Auton Routine").add(autoChooser);
    

    }

    


    private void configureBindings()
    {
        new JoystickButton(controller_driveX,XboxController.Button.kRightBumper.value)
                .whenPressed(new InstantCommand(swerveSubsystem::zeroGyro));

                SmartDashboard.putData("On-the-fly path", Commands.runOnce(() -> {
                    Pose2d currentPose = swerveSubsystem.getPose();
                    
                    // The rotation component in these poses represents the direction of travel
                    Pose2d startPos = new Pose2d(currentPose.getTranslation(), new Rotation2d());
                    Pose2d endPos = new Pose2d(currentPose.getTranslation().plus(new Translation2d(2.0, 0.0)), new Rotation2d());
              
                    List<Translation2d> bezierPoints = PathPlannerPath.bezierFromPoses(startPos, endPos);
                    PathPlannerPath path = new PathPlannerPath(
                      bezierPoints, 
                      new PathConstraints(
                        4.0, 4.0, 
                        Units.degreesToRadians(360), Units.degreesToRadians(540)
                      ),  
                      new GoalEndState(0.0, currentPose.getRotation())
                    );
              
                    AutoBuilder.followPathWithEvents(path).schedule();
                  }));
    }

    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }
}
