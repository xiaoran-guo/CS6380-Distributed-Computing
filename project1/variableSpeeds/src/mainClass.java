import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;

public class mainClass {
    public static void main(String arg[]) {
        // Initialize two inputs.
        int numOfProc = 0;
        int[] idArray = new int[0];

        // Read input from file input.dat.
        File file = new File("input.dat");
        BufferedReader reader = null;
        String thisLine = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            int rowNum = 0;
            while ((thisLine = reader.readLine()) != null) {
                if (rowNum == 0) {
                    numOfProc = Integer.parseInt(thisLine);
                    //System.out.println(numOfProc);
                    rowNum++;
                } else {
                    String[] idArrayInStr = thisLine.trim().split("\\s+");
                    idArray = new int[numOfProc];
                    for (int i = 0; i < numOfProc; i++) {
                        idArray[i] = Integer.parseInt(idArrayInStr[i]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create an array to keep track of the smallest UID it has seen.
        int[] smallestId = new int[numOfProc];
        Arrays.fill(smallestId, -1);
        // Create an array to keep track of the token sent in some round.
        int[] token = smallestId.clone();

        // Use CyclicBarrier to make threads wait for each other.
        CyclicBarrier cyclicBarrier = new CyclicBarrier(numOfProc);

        // Initialize a masterThread to solve the issue of sharing variables between multiple different threads.
        masterThread master = new masterThread(token, smallestId, cyclicBarrier);

        // Create n threads.
        for (int i = 0; i < numOfProc; i++) {
            processes process = new processes(master, idArray[i], i, numOfProc);
            Thread thread = new Thread(process);
            thread.start();
        }
    }
}
