import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class asyncFloodMax {
    public static void main(String[] args) {
        // Create a marker to record the number of messages, the number of terminated processes and one boolean
        // that indicates if the algorithm can be terminated.
        Marker marker = new Marker();
        // barrier
        CyclicBarrier cb;

        try {
            // Read the number of processes from file.
            Scanner scanner = new Scanner(new File("sample-input.dat"));
            int n = scanner.nextInt();
            cb = new CyclicBarrier(n + 1);
            int[] listOfUid = new int[n];
            for (int i = 0; i < n; i++) {
                listOfUid[i] = scanner.nextInt();
            }


            // Initialize processes.
            Process[] listOfProc = new Process[n];
            for (int i = 0; i < n; i++) {
                listOfProc[i] = new Process(listOfUid[i], marker, cb);
            }

            // Record every process's outgoing and incoming links.
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int connect = scanner.nextInt();
                    if (connect == 1) {
                        MessageQueue link = new MessageQueue(listOfProc[i].uid, listOfProc[j].uid);
                        listOfProc[i].outgoingLinks.add(link);
                        listOfProc[j].incomingLinks.add(link);
                    }
                }
            }

            // Start every process.
            for (int i = 0; i < n; i++) {
                Thread process = new Thread(listOfProc[i]);
                process.start();
            }

            // Check if the algorithm can be terminated.
            while (!marker.termination) {
                if (marker.getNumOfTerminatedProc() >= n) {
                    marker.termination = true;
                    System.out.println("The total number of messages is " + marker.getNumOfMessage() + ".");
                }
                if(cb.getNumberWaiting() == n) {
                    cb.await();
                }
            }
            System.exit(0);
        } catch (FileNotFoundException | InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
