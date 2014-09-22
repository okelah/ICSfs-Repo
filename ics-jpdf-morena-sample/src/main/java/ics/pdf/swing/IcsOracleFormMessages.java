package ics.pdf.swing;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class IcsOracleFormMessages {

    public static IcsOracleFormPropertiesMsg decypherIcsOracleFormPrpertiesMsg(String msg) {
        String incommingMsg = msg.toUpperCase();
        StringTokenizer st = new StringTokenizer(incommingMsg.replaceAll("\\s+", ""), ",");

        Map<String, String> list = new HashMap<String, String>();
        IcsOracleFormMessages dd = new IcsOracleFormMessages();
        IcsOracleFormPropertiesMsg msgObject = dd.new IcsOracleFormPropertiesMsg();

        try {
            while (st.hasMoreTokens()) {
                String element = (String) st.nextElement();
                StringTokenizer newSt = new StringTokenizer(element, "=");

                System.out.println(element);

                String key = (String) newSt.nextElement();
                if (newSt.hasMoreElements()) {
                    String value = (String) newSt.nextElement();
                    if (key.equals(IcsOracleFormPropertiesMsg.MODE)) {
                        msgObject.setMode(value);
                    } else if (key.equals(IcsOracleFormPropertiesMsg.SIGNATURE)) {
                        msgObject.setSignature(Boolean.valueOf(value));
                    } else if (key.equals(IcsOracleFormPropertiesMsg.PRINT)) {
                        msgObject.setPrint(Boolean.valueOf(value));
                    } else if (key.equals(IcsOracleFormPropertiesMsg.STAMP)) {
                        msgObject.setStamp(Boolean.valueOf(value));
                    }

                } else {
                    System.out.println("ERROR ... key [" + key + "] does not have value");
                }
            }
        } catch (java.util.NoSuchElementException e) {
            System.err.println("Message Error: [" + incommingMsg.replaceAll("\\s+", "") + "]");
        }

        return msgObject;
    }

    public class IcsOracleFormPropertiesMsg {
        private String mode = IcsPdfNotesBean.MODE_VIEW;
        private boolean print = false;
        private boolean stamp = false;
        private boolean signature = false;
        static final String MODE = "MODE";
        static final String PRINT = "PRINT";
        static final String STAMP = "STAMP";

        static final String SIGNATURE = "SIGN";

        IcsOracleFormPropertiesMsg() {
        }

        public boolean isPrint() {
            return print;
        }

        public void setPrint(boolean print) {
            this.print = print;
        }

        public boolean isStamp() {
            return stamp;
        }

        public void setStamp(boolean stamp) {
            this.stamp = stamp;
        }

        public boolean isSignature() {
            return signature;
        }

        public void setSignature(boolean signature) {
            this.signature = signature;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return "IcsOracleFormPropertiesMsg [mode=" + mode + ", print=" + print + ", stamp=" + stamp
                    + ", signature=" + signature + "]";
        }
    }
}
