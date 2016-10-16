package org.montezuma.test.traffic.recording;

import org.montezuma.test.traffic.Common;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspectControl;

import java.io.File;

public class TrafficRecorder {
  public void runRecording(Runnable productionCodeRunner, String recordingSubDir) {
    try {
      RecordingAspectControl.getInstance().turnOff();
      RecordingAspectControl.getInstance().setRecordingSubdir(recordingSubDir);
      final File recordingPath = new File(Common.BASE_RECORDING_PATH, recordingSubDir);
      recordingPath.mkdirs();
      cleanDir(recordingPath);
      RecordingAspectControl.getInstance().turnOn();

      productionCodeRunner.run();
    } finally {
    	RecordingAspectControl.getInstance().turnOff();
    }
  }

  private static void cleanDir(File recordingPath) {
    rmDir(recordingPath);
    recordingPath.mkdirs();
  }

  private static void rmDir(File recordingPath) {
    for (File file : recordingPath.listFiles()) {
      if (file.isDirectory())
        rmDir(file);

      file.delete();
    }
  }
}
