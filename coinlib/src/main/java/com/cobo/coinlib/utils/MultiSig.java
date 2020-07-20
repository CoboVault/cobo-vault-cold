package com.cobo.coinlib.utils;

public class MultiSig {
    public enum Account {
        P2SH("m/45'","P2SH"),
        P2WSH_P2SH("m/48'/0'/0'/1'","P2WSH-P2SH"),
        P2WSH("m/48'/0'/0'/2'","P2WSH");

        private final String format;
        private String path;
        Account(String path, String format) {
            this.path = path;
            this.format = format;
        }

        public String getPath() {
            return path;
        }

        public String getFormat() {
            return format;
        }

        public static Account ofPath(String path) {
            for (Account value : Account.values()) {
                if (value.path.equals(path)) {
                    return value;
                }
            }
            return P2WSH;
        }

        public static Account ofFormat(String format) {
            for (Account value : Account.values()) {
                if (value.format.equalsIgnoreCase(format)) {
                    return value;
                }
            }
            return P2WSH;
        }
    }
}
