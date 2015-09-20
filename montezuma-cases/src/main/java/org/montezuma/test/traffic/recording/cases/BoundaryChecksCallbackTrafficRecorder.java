package org.montezuma.test.traffic.recording.cases;

import analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.B;
import analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.C;
import dontanalysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.D;
import dontanalysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.A;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.sql.SQLException;

public class BoundaryChecksCallbackTrafficRecorder {
	public static final String	BOUNDARY_CHECKS_RECORDING_SUBDIR	= "boundarychecks_callback";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new A());
				System.out.println("Loading CUT class to avoid static init processing: " + new B());
				System.out.println("Loading CUT class to avoid static init processing: " + new C());
				System.out.println("Loading CUT class to avoid static init processing: " + new D());
				RecordingAspect.turnOn();

				A cut = new A();

				final String result = cut.a();
				RecordingAspect.turnOff();
				System.out.println(result);
			}
		}, BOUNDARY_CHECKS_RECORDING_SUBDIR);
	}
}
