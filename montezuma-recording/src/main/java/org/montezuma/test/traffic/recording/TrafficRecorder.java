package org.montezuma.test.traffic.recording;

import org.montezuma.test.traffic.Common;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.io.File;

public class TrafficRecorder {
  public void runRecording(Runnable productionCodeRunner, String recordingSubDir) {
    try {
      RecordingAspect.turnOff();
      RecordingAspect.recordingSubDir = recordingSubDir;
      final File recordingPath = new File(Common.BASE_RECORDING_PATH, recordingSubDir);
      recordingPath.mkdirs();
      cleanDir(recordingPath);
      RecordingAspect.turnOn();

      productionCodeRunner.run();
    } finally {
      RecordingAspect.turnOff();
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
