package org.montezuma.test.traffic.recording.cases;

import analysethis.com.somecompany.dao.CompiledStatementStoringPreparedStatementCreator;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspectControl;

import java.sql.SQLException;

public class CompiledStatementStoringPreparedStatementCreatorTrafficRecorder {
	public static final String	COMPILED_STATEMENT_RECORDING_SUBDIR	= "compiledstatement";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspectControl.getInstance().turnOff();
				final MyConnection connection = new MyConnection();
				RecordingAspectControl.getInstance().turnOn();

				CompiledStatementStoringPreparedStatementCreator cut5 = new CompiledStatementStoringPreparedStatementCreator("insert into delete_log (resource_id, params) values( ? )", 1234L);
				try {
					cut5.createPreparedStatement(connection);
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
				System.out.println(cut5.getCompiledSQL());
			}
		}, COMPILED_STATEMENT_RECORDING_SUBDIR);
	}
}
