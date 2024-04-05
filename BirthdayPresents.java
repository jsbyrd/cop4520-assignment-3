import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class BirthdayPresents {
  public static final int NUM_SERVANTS = 4;
  public static final int NUM_PRESENTS = 100000;
  public static void main(String[] args) throws InterruptedException {
    Integer[] unorderedPresents = new Integer[NUM_PRESENTS];
    for (int i = 0; i < NUM_PRESENTS; i++) {
      unorderedPresents[i] = i + 1;
    }
    List<Integer> presentsList = Arrays.asList(unorderedPresents);
    Collections.shuffle(presentsList);
    presentsList.toArray();

    // Shared resources
    ThreadSafeLinkedList list = new ThreadSafeLinkedList();
    Counter counter = new Counter();
    // Create and start threads
    Thread[] threads = new ServantThread[NUM_SERVANTS];
    final long executionStartTime = System.currentTimeMillis();
    for (int i = 0; i < NUM_SERVANTS; i++) {
      threads[i] = new ServantThread(presentsList, list, counter, NUM_PRESENTS);
      threads[i].start();
    }
    // Wait for threads to die
    for (int i = 0; i < NUM_SERVANTS; i++) {
      threads[i].join();
    }
    final long executionEndTime = System.currentTimeMillis();
    System.out.println("Total execution time: " + (executionEndTime - executionStartTime) + "ms");

    // Should be 500,000
    System.out.println("Total presents added: " + counter.getFinished());
    System.out.println("Total presents removed: " + counter.getRemoved());

    // To be sure, iterate through linked list, the only two nodes in it should be the sentinel nodes
    Node curr = list.head;
    while (curr != null) {
      System.out.println(curr.present);
      curr = curr.nextNode;
    }
  }
}

class ServantThread extends Thread {
  private List<Integer> presentsList;
  private ThreadSafeLinkedList list;
  private Counter counter;
  private int numPresents;
  private boolean addPresentMode;

  public ServantThread(List<Integer> presentsList, ThreadSafeLinkedList list, Counter counter, int numPresents) {
    this.presentsList = presentsList;
    this.list = list;
    this.counter = counter;
    this.numPresents = numPresents;
    this.addPresentMode = true;
  }

  @Override
  public void run() {
    int currIndex = counter.getAndIncrementIndex();
    Random rand = new Random();
    while (currIndex < numPresents || counter.getRemoved() < numPresents) {
      // Remove a present if either it's time to do so or if there are no more presents to add
      if (!addPresentMode || currIndex >= numPresents) {
        int toBeRemoved = rand.nextInt(numPresents) + 1;
        boolean isRemoved = list.remove(toBeRemoved);
        if (isRemoved) counter.incrementRemoved();
        addPresentMode = true;
      }
      // Otherwise add present
      else {
        boolean isAdded = list.add(presentsList.get(currIndex));
        if (isAdded) {
          counter.incrementFinished();
          currIndex = counter.getAndIncrementIndex();
          addPresentMode = false;
        }
      }
      // Very small chance to check for contain
      if (Math.random() > 0.9998) {
        int toBeChecked = rand.nextInt(numPresents) + 1;
        if (list.contains(toBeChecked)) {
          System.out.println("List contains " + toBeChecked);
        }
        else {
          System.out.println("List does not contain " + toBeChecked);
        }
      }

    }
  }
}

class Counter {
  private int index;
  private int finished;
  private int removed;

  public Counter() {
    this.index = -1;
    this.finished = 0;
    this.removed = 0;
  }
  public synchronized int getAndIncrementIndex() {
    return ++index;
  }
  public synchronized void incrementFinished() {
    finished++;
  }
  public synchronized void incrementRemoved() {
    removed++;
  }
  public int getFinished() {
    return finished;
  }
  public int getRemoved() {
    return removed;
  }
}

class ThreadSafeLinkedList {
  public Node head;
  private int numPresents;

  public ThreadSafeLinkedList() {
    // Set sentinel nodes
    this.head = new Node(Integer.MIN_VALUE);
    this.head.nextNode = new Node(Integer.MAX_VALUE);
    this.numPresents = 0; // Doesn't count sentinel nodes
  }

  private boolean validate(Node pred, Node curr) {
    return !pred.isMarked() && !curr.isMarked() && pred.nextNode == curr;
  }

  public boolean add(int present) {
    while (true) {
      Node pred = this.head;
      Node curr = head.nextNode;
      while (curr.present < present) {
        pred = curr;
        curr = curr.nextNode;
      }
      pred.lock();
      try {
        curr.lock();
        try {
          if (validate(pred, curr)) {
            if (curr.present == present) {
              return false;
            }
            else {
              Node node = new Node(present);
              node.nextNode = curr;
              pred.nextNode = node;
              this.numPresents++;
              return true;
            }
          }
        }
        finally {
          curr.unlock();
        }
      }
      finally {
        pred.unlock();
      }
    }
  }

  public boolean remove(int present) {
    while (true) {
      Node pred = head;
      Node curr = head.nextNode;
      while (curr.present < present) {
        pred = curr;
        curr = curr.nextNode;
      }
      pred.lock();
      try {
        curr.lock();
        try {
          if (validate(pred, curr)) {
            if (curr.present != present) {
              return false;
            }
            else {
              curr.setMarked(true);
              pred.nextNode = curr.nextNode;
              this.numPresents--;
              return true;
            }
          }
        }
        finally {
          curr.unlock();
        }
      }
      finally {
        pred.unlock();
      }
    }
  }

  public boolean contains(int present) {
    Node curr = head;
    while (curr.present < present) {
      curr = curr.nextNode;
    }
    return curr.present == present && !curr.isMarked();
  }

  public int getNumPresents() {
    return this.numPresents;
  }
}

class Node {
  public int present; // Present can double as a key since its unique
  public Node nextNode;
  public Lock lock;
  public boolean marked;
  public boolean tagged;

  public Node (int present) { // usual constructor
    this.present = present;
    this.nextNode = null;
    this.lock = new ReentrantLock();
    this.marked = false;
    this.tagged = false;
  }

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }

//  public int getPresent() {
//    return this.present;
//  }
//
//  public Node getNextNode() {
//    return this.nextNode;
//  }
//
//  public void setNextNode(Node nextNode) {
//    this.nextNode = nextNode;
//  }

  public void setTagged(boolean bool) {
    this.tagged = bool;
  }

  public boolean isTagged() {
    return this.tagged;
  }

  public void setMarked(boolean bool) {
    this.marked = bool;
  }

  public boolean isMarked() {
    return this.marked;
  }
}
