package ics.pdf.swing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class IcsOracleFormMessages {

    // lang -> ar / en / fr
    // current_date format -> dd/mm/yyyy

    public static IcsOracleFormPropertiesMsg decypherIcsOracleFormPrpertiesMsg(String msg) {
        String incommingMsg = msg.toUpperCase();
        // StringTokenizer st = new StringTokenizer(incommingMsg.replaceAll("\\s+", ""), ",");
        StringTokenizer st = new StringTokenizer(incommingMsg, ",");

        IcsOracleFormPropertiesMsg msgObject = new IcsOracleFormMessages().new IcsOracleFormPropertiesMsg();

        try {
            while (st.hasMoreTokens()) {
                String element = (String) st.nextElement();
                StringTokenizer newSt = new StringTokenizer(element, "=");

                String key = ((String) newSt.nextElement()).replaceAll("\\s+", "");
                if (newSt.hasMoreElements()) {
                    String value = (String) newSt.nextElement();
                    if (key.equals(IcsOracleFormPropertiesMsg.MODE)) {
                        msgObject.setMode(value.replaceAll("\\s+", ""));
                    } else if (key.equals(IcsOracleFormPropertiesMsg.SIGNATURE)) {
                        msgObject.setSignature(Boolean.valueOf(value.replaceAll("\\s+", "")));
                    } else if (key.equals(IcsOracleFormPropertiesMsg.PRINT)) {
                        msgObject.setPrint(Boolean.valueOf(value.replaceAll("\\s+", "")));
                    } else if (key.equals(IcsOracleFormPropertiesMsg.STAMP)) {
                        msgObject.setStamp(Boolean.valueOf(value.replaceAll("\\s+", "")));
                    } else if (key.equals(IcsOracleFormPropertiesMsg.USER)) {
                        msgObject.setUserName(value);
                    } else if (key.equals(IcsOracleFormPropertiesMsg.LANG)) {
                        msgObject.setLanguage(value.replaceAll("\\s+", ""));
                    } else if (key.equals(IcsOracleFormPropertiesMsg.CURRENT_DATE)) {
                        try {
                            Date currentDate = new SimpleDateFormat("dd/mm/yyyy").parse(value);
                            msgObject.setCurrentDate(currentDate);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }

                } else {
                    System.err.println("ERROR ... key [" + key + "] does not have value");
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
        private String userName;
        private Date currentDate;
        private String language = "en";
        static final String MODE = "MODE";
        static final String PRINT = "PRINT";
        static final String STAMP = "STAMP";
        static final String SIGNATURE = "SIGN";
        static final String USER = "USER";
        static final String CURRENT_DATE = "CURRENT_DATE";
        static final String LANG = "LANG";

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

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Date getCurrentDate() {
            return currentDate;
        }

        public void setCurrentDate(Date currentDate) {
            this.currentDate = currentDate;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("IcsOracleFormPropertiesMsg [mode=");
            builder.append(mode);
            builder.append(", print=");
            builder.append(print);
            builder.append(", stamp=");
            builder.append(stamp);
            builder.append(", signature=");
            builder.append(signature);
            builder.append(", userName=");
            builder.append(userName);
            builder.append(", currentDate=");
            builder.append(currentDate);
            builder.append(", language=");
            builder.append(language);
            builder.append("]");
            return builder.toString();
        }

    }
}
