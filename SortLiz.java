import java.io.*;
import java.util.Collections;
import java.util.TreeSet;

public class SortLiz {

    public static void main(String[] args) {
        try {
            TreeSet<Lizard> set = new TreeSet<>(Collections.reverseOrder());
            File f = new File(args[0]);
            BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-16"));
            String readLine = "";
            while (((readLine = b.readLine()) != null)) {
                String[] elements = readLine.split("\\s+");
                set.add(new Lizard(elements[1], elements[2], elements[6]));
            }
            int i = 0;
            System.out.println("NLOC CCN     Location");
            for (Lizard data : set) {
                System.out.println(data.NLOC + " " + data.getCCN() + " " + data.test);
                if (i == 9) {
                    System.out.println("\n Top ten above.. \n");
                }
                if (i >= 70) {
                    break;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Lizard implements Comparable<Lizard> {
        public String NLOC = "";
        public String CCN = "";
        public String test = "";

        public Lizard(String NLOC, String CCN, String test) {
            this.NLOC = NLOC;
            this.CCN = CCN;
            this.test = test;
        }

        public String getCCN() {
            return CCN;
        }

        @Override
        public int compareTo(Lizard data) {
            return Double.compare(Double.parseDouble(this.CCN), Double.parseDouble(data.getCCN()));
        }
    }
}