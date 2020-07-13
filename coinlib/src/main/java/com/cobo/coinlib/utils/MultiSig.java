package com.cobo.coinlib.utils;

public class MultiSig {
    public enum Account {
        P2SH("m/45'","p2sh"),
        P2SH_P2WSH("m/48'/0'/0'/1'","p2wsh-p2sh"),
        P2WSH("m/48'/0'/0'/2'","p2wsh");

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
            return null;
        }
    }
}
