package ics.pdf.swing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ConfigTest {

    public static void main(String[] args) {
        // int[] nums = new int[] { 0, 0, 0, 0 };
        BanksConfig.getInstance().setProperty("Test", new int[] { 0, 0, 0, 0 });

        List<Integer> out = (ArrayList<Integer>) BanksConfig.getInstance().getProperty("Test");

        System.out.println(out);

        String incommingMsg = "print=true ,     edit=false , sign=true,test=";
        StringTokenizer st = new StringTokenizer(incommingMsg.replaceAll("\\s+", ""), ",");

        Map list = new HashMap();

        while (st.hasMoreTokens()) {
            String element = (String) st.nextElement();
            StringTokenizer newSt = new StringTokenizer(element, "=");

            System.out.println(element);

            System.out.print(newSt.nextElement());
            System.out.print("<>");
            System.out.println(newSt.nextElement());
        }
    }
}
