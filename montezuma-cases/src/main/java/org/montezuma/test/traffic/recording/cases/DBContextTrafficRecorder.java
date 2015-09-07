package org.montezuma.test.traffic.recording.cases;

import analysethis.matrix.lookups.db.DBContext;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBContextTrafficRecorder {
	public static final String	DBCONTEXT_RECORDING_SUBDIR	= "dbcontext";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				// System.out.println("Loading CUT class to avoid static init processing: " + new BigDecimalUtils());
				PreparedStatement myPreparedStatement = new MyPreparedStatement();
				RecordingAspect.turnOn();

				final String result1 = DBContext.simpleFetch("AHA");// DBContext.fromStaticToInstance(myPreparedStatement);
				RecordingAspect.turnOff();
				System.out.println(result1);
			}
		}, DBCONTEXT_RECORDING_SUBDIR);
	}
}
