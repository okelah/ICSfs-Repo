package ics.pdf.swing;

import java.util.ArrayList;
import java.util.List;

public class ConfigTest {

    public static void main(String[] args) {
        // int[] nums = new int[] { 0, 0, 0, 0 };
        BanksConfig.getInstance().setProperty("Test", new int[] { 0, 0, 0, 0 });

        List<Integer> out = (ArrayList<Integer>) BanksConfig.getInstance().getProperty("Test");

        System.out.println(out);
    }
}
