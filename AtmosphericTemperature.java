import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Random;

public class AtmosphericTemperature {
  public static final int NUM_HOURS = 100;
  public static final int NUM_THREADS = 8;
  public static final String FILENAME = "temperatures.txt";

  public static void main(String[] args) throws InterruptedException, IOException {
    int[] temperatureReadings = new int[(NUM_HOURS * 60) + 1];
    SensorCounter counter = new SensorCounter();

    Thread[] threads = new Sensor[NUM_THREADS];
    final long executionStartTime = System.currentTimeMillis();
    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new Sensor(temperatureReadings, counter, NUM_HOURS * 60);
      threads[i].start();
    }
    // Wait for threads to die
    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i].join();
    }

    // Write to temperature.txt
    File summary = new File(FILENAME);
    summary.createNewFile();
    FileWriter writer = new FileWriter(FILENAME);
    // Now that we have our data, make our report
    for (int i = 0; i < NUM_HOURS; i++) {
      writer.write("Hour " + (i + 1) + ":\n");
      int maxTemp = -1;
      int maxMinute = -1;
      PriorityQueue<Integer> minHeap = new PriorityQueue<>(60, (Integer a, Integer b) -> a - b);
      PriorityQueue<Integer> maxHeap = new PriorityQueue<>(60, (Integer a, Integer b) -> b - a);
      for (int j = 0; j < 60; j++) {
        int minute = (60 * i) + j;
        int temp = temperatureReadings[minute];
        minHeap.add(temp);
        maxHeap.add(temp);
        if (minute % 60 > 10) {
          int temp2 = temperatureReadings[minute - 10];
          if (Math.abs(temp - temp2) > maxTemp) {
            maxTemp = Math.abs(temp - temp2);
            maxMinute = minute;
          }
        }
      }
      writer.write("5 highest temperatures:");
      for (int k = 0; k < 5; k++) {
        writer.write(" " + maxHeap.poll() + "F");
      }
      writer.write("\n");
      writer.write("5 lowest temperatures:");
      for (int k = 0; k < 5; k++) {
        writer.write(" " + minHeap.poll() + "F");
      }
      writer.write("\n");
      writer.write("Biggest temperature difference between 10 minutes was " + maxTemp + "F from minute " + (maxMinute - 10) +
              " (" + temperatureReadings[maxMinute - 10] + "F) to minute " + maxMinute + " (" + temperatureReadings[maxMinute] + ")");
      writer.write("\n\n");

    }
    writer.close();
    final long executionEndTime = System.currentTimeMillis();
    System.out.println("Total execution time: " + (executionEndTime - executionStartTime) + "ms");
  }
}

class Sensor extends Thread {
  private int[] temperatureReadings;
  private SensorCounter counter;
  private int totalMinutes;

  public Sensor(int[] temperatureReadings, SensorCounter counter, int totalMinutes) {
    this.temperatureReadings = temperatureReadings;
    this.counter = counter;
    this.totalMinutes = totalMinutes;
  }

  @Override
  public void run() {
    int minute = counter.getAndIncrement();
    Random rand = new Random();
    while (minute < totalMinutes) {
      int randomTemp = rand.nextInt(270) - 100;
      temperatureReadings[minute] = randomTemp;
      minute = counter.getAndIncrement();
    }
  }
}

class SensorCounter {
  private int minute;

  public SensorCounter() {
    this.minute = 1;
  }

  public synchronized int getAndIncrement() {
    return minute++;
  }
}
