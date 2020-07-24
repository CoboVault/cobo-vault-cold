package com.cobo.cold.ui.fragment.main;

public enum QrScanPurpose {
    WEB_AUTH("webAuth",true),
    ADDRESS("address",false),
    MULTISIG_TX("multisig_tx",false),
    IMPORT_MULTISIG_WALLET("importMultiSigWallet",true),
    COLLECT_XPUB("collect_xpub",false),
    UNDEFINE("undefine",false);


    private String purpose;
    private boolean isAnimateQr;
    QrScanPurpose(String purpose, boolean isAnimateQr) {
        this.purpose = purpose;
        this.isAnimateQr = isAnimateQr;
    }

    public static QrScanPurpose ofPurpose(String purpose) {
        for (QrScanPurpose value : QrScanPurpose.values()) {
            if (value.purpose.equals(purpose)) {
                return value;
            }
        }

        return UNDEFINE;
    }

    public boolean isAnimateQr() {
        return isAnimateQr;
    }

    public String purpose() {
        return purpose;
    }

}
