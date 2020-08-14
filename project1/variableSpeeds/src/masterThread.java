import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class masterThread {
    private int count = 0;
    private int[] token;
    private int[] smallestId;
    private int idOfLeader = -1;
    private CyclicBarrier cyclicBarrier;
    private Lock readersWriterLock = new ReentrantLock();

    // Constructor
    masterThread(int[] token, int[] smallestId, CyclicBarrier cyclicBarrier) {
        this.token = token;
        this.smallestId = smallestId;
        this.cyclicBarrier = cyclicBarrier;
    }

    boolean signal(int id, int index, int numOfProc) {
        // Output its id and the id of the leader on the screen, and then terminate.
        if (idOfLeader > 0) {
            if (idOfLeader == id) {
                System.out.println("My id is " + id + ", and the id of leader is " + idOfLeader + ". I'm the leader.");
            } else {
                System.out.println("My id is " + id + ", and the id of leader is " + idOfLeader + ". I'm not the leader.");
            }
            return true;
        }

        // Initialize the array of smallestId.
        if (smallestId[index] == -1) {
            smallestId[index] = id;
            return false;
        }

        // Use lock to solve concurrency issue.calculate the number of round.
        readersWriterLock.lock();
        count++;
        // Calculate the number of round.
        int round = (int) Math.ceil((double) count / smallestId.length);
        // Use token array to keep track of the token sent in some round.
        if (round % Math.pow(2, smallestId[index]) == 0) {
            token[(index + 1) % numOfProc] =  smallestId[index];
        }
        readersWriterLock.unlock();

        // Set CyclicBarrier to let all threads start new round at the same time.
        try {
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        // Use token array to update the smallest UID it has seen.
        readersWriterLock.lock();
        if ((token[index] < id && token[index] != -1) || (token[index] == id)) {
            // If the id received equals to itself, it is the leader.
            if (token[index] == id) {
                idOfLeader = id;
            }
            smallestId[index] = token[index];
        }
        // Restore token.
        token[index] = -1;
        readersWriterLock.unlock();

        // Set CyclicBarrier to let all threads start new round at the same time.
        try {
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        return false;
    }
}
