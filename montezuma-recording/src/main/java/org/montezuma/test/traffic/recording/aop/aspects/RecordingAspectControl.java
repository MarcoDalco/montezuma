package org.montezuma.test.traffic.recording.aop.aspects;

import java.io.File;

public class RecordingAspectControl {
	protected static final RecordingAspectControl	instance	= new RecordingAspectControl();

	boolean																				stop			= false;
	public File																		recordingDir;
	{
		String recordingsDirProperty = System.getProperty("montezuma.recordings.dir");
		recordingDir = new File(recordingsDirProperty == null ? "" : recordingsDirProperty);
	}
	final boolean log = true;

	public static RecordingAspectControl getInstance() {
		return instance;
	}

	public void turnOff() {
		stop = true;
	}

	public void turnOn() {
		stop = false;
	}

	public void setRecordingDir(File recordingDir) {
		this.recordingDir = recordingDir;
	}
}
