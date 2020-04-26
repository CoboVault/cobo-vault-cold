/*
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cobo.cold.encryption.exception;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class EncryptionCoreException extends RuntimeException {
    private static final SparseArrayCompat<String> mDefineMap;

    static {
        mDefineMap = new SparseArrayCompat<>();
        mDefineMap.put(0xFFAA, "ERT_UnderAttack [attacked mode]");
        mDefineMap.put(0xFFFF, "ERT_Unauthorized [authorize with 0102 first]");
        mDefineMap.put(0x0001, "ERT_InitRngFail [random generator initialization failed]");
        mDefineMap.put(0x0002, "ERT_InitFlashFail [flash initialization failed]");
        mDefineMap.put(0x0003, "ERT_InitUartFail [uart initialization failed]");
        mDefineMap.put(0x0004, "ERT_InitTimerFail [timer initialization failed]");
        mDefineMap.put(0x0005, "ERT_InvalidKey [invalid key]");
        mDefineMap.put(0x0006, "ERT_RngFail [failed to get random number]");
        mDefineMap.put(0x0007, "ERT_SFlashFail [secure Flash operation failed]");
        mDefineMap.put(0x0008, "ERT_MallocFail: [heap application failed]");
        mDefineMap.put(0x0009, "ERT_GenKeyFail: [key generation failed]");
        mDefineMap.put(0x000A, "ERT_ECDSASignFail: [ecdsa signature failed]");
        mDefineMap.put(0x000B, "ERT_ECDSAVerifyFail: [ecdsa verification failed]");
        mDefineMap.put(0x000C, "ERT_SecpEncryptFail: [secp encryption failed]");
        mDefineMap.put(0x000D, "ERT_SecpDecryptFail: [secp decryption failed]");
        mDefineMap.put(0x000E, "ERT_CheckSumFail: [checksum verification failed]");
        mDefineMap.put(0x000F, "ERT_CheckMD5Fail: [md5 verification failed]");
        mDefineMap.put(0x0010, "ERT_FuncParamInvalid: [function parameter error]");
        mDefineMap.put(0x0011, "ERT_CommTimeOut: [serial communication timeout]");
        mDefineMap.put(0x0012, "ERT_CommInvalidCMD: [serial communication command is not recognized]");
        mDefineMap.put(0x0013, "ERT_CommFailEncrypt: [serial communication encryption flag byte error]");
        mDefineMap.put(0x0014, "ERT_CommFailLen: [serial communication length byte error]");
        mDefineMap.put(0x0015, "ERT_CommFailEtx: [serial communication ETX byte error]");
        mDefineMap.put(0x0016, "ERT_CommFailLrc: [serial communication LRC verification failed]");
        mDefineMap.put(0x0017, "ERT_CommFailTLV: [serial communication TLV internal error]");
        mDefineMap.put(0x0018, "ERT_CommFailParam: [serial communication parameter error]");
        mDefineMap.put(0x0019, "ERT_3DESFail: [serial communication 3DES encryption and decryption error]");
        mDefineMap.put(0x001A, "ERT_tlvArray_to_buf: [tlv array to buf error]");
        mDefineMap.put(0x001B, "ERT_StorageFail: [flash storage error]");
        mDefineMap.put(0x001C, "ERT_CKD_Fail: [ckd part error]");
        mDefineMap.put(0x001D, "ERT_VerConflict: [firmware version conflict]");
        mDefineMap.put(0x001E, "ERT_GetStatsFail: [get firmware status error]");
        mDefineMap.put(0x001F, "ERT_HDPathIllegal: [hdpath illegal]");
        mDefineMap.put(0x0020, "ERT_WithoutPermission: [permission no match]");
        mDefineMap.put(0x0021, "ERT_RecIDFail: [recoveryParam error]");
        mDefineMap.put(0x0022, "ERT_NeedPreCMD: [Need pre-command]");
        mDefineMap.put(0x0023, "ERT_MnemonicNotMatch: [mnemonic verification does not match]");
        mDefineMap.put(0x0024, "ERT_UnexpectedFail: [should not occur]");
    }

    private int errorCode;
    private String errorMessage;

    public EncryptionCoreException(int code, @Nullable String error) {
        super(toJsonString(code, error));
        this.errorCode = code;
        this.errorMessage = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @NonNull
    private static String toJsonString(int code, @Nullable String error) {
        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("code", code);
            jsonObject.put("define", mDefineMap.get(code, "Not Found"));
            jsonObject.put("error", TextUtils.isEmpty(error) ? "empty error message" : error);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
}
