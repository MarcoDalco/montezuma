package org.montezuma.test.traffic.recording.cases;

import analysethis.captureboundarychecks.EntryClassToAnalyse;
import analysethis.captureboundarychecks.SecondClassToAnalyse;
import dontanalysethis.captureboundarychecks.ClassAfterBoundaryExit;
import dontanalysethis.captureboundarychecks.ClassEnteringBoundary;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspectControl;

import java.sql.SQLException;

public class BoundaryChecksTrafficRecorder {
	public static final String	BOUNDARY_CHECKS_RECORDING_SUBDIR	= "boundarychecks";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspectControl.getInstance().turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new ClassEnteringBoundary());
				System.out.println("Loading CUT class to avoid static init processing: " + new EntryClassToAnalyse());
				System.out.println("Loading CUT class to avoid static init processing: " + new SecondClassToAnalyse());
				System.out.println("Loading CUT class to avoid static init processing: " + new ClassAfterBoundaryExit());
				RecordingAspectControl.getInstance().turnOn();

				ClassEnteringBoundary cut = new ClassEnteringBoundary();

				final String result = cut.enterBoundaryForFullTraversal();
				RecordingAspectControl.getInstance().turnOff();
				System.out.println(result);
			}
		}, BOUNDARY_CHECKS_RECORDING_SUBDIR);
	}
}
