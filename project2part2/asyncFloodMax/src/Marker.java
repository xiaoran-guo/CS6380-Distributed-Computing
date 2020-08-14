import java.util.concurrent.atomic.AtomicInteger;

public class Marker {
    private AtomicInteger numOfTerminatedProc;
    private AtomicInteger numOfMessage;
    public boolean termination;

    //Constructor of Marker
    public Marker() {
        this.numOfTerminatedProc = new AtomicInteger();
        this.numOfMessage = new AtomicInteger();
        this.termination = false;
    }

    // Add the number of messages sent for the run.
    public synchronized void addNumOfMessage() {
        this.numOfMessage.incrementAndGet();
    }

    // Get the number of messages sent for the run.
    public synchronized int getNumOfMessage() {
        return this.numOfMessage.intValue();
    }

    // Add the number of terminated process.
    public synchronized void addNumOfTerminatedProc() {
        this.numOfTerminatedProc.incrementAndGet();
    }
    // Get the number of terminated process.
    public synchronized int getNumOfTerminatedProc() {
        return this.numOfTerminatedProc.intValue();
    }
}
