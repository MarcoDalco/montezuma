package org.montezuma.test.traffic.recording.aop.aspects;

public class RecordingAspectControl {
	protected static final RecordingAspectControl	instance	= new RecordingAspectControl();

	boolean																stop			= false;
	public String													recordingSubDir;
	final boolean													log				= true;

	public static RecordingAspectControl getInstance() {
		return instance;
	}

	public void turnOff() {
		stop = true;
	}

	public void turnOn() {
		stop = false;
	}

	public void setRecordingSubdir(String recordingSubDir) {
		this.recordingSubDir = recordingSubDir;
	}
}
