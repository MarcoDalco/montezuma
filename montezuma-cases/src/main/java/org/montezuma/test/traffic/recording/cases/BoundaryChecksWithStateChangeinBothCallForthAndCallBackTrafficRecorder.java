package org.montezuma.test.traffic.recording.cases;

import analysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue.B;
import analysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue.C;
import dontanalysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue.D;
import dontanalysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue.A;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.sql.SQLException;

public class BoundaryChecksWithStateChangeinBothCallForthAndCallBackTrafficRecorder {
	public static final String	BOUNDARY_CHECKS_RECORDING_SUBDIR	= "boundarychecks_callback_with_double_state_change";

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
