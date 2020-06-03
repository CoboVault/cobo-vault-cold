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

package com.cobo.cold.ui.fragment.main;

import android.view.View;

import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.fragment.main.electrum.SignedTxFragment;

import static com.cobo.cold.ui.fragment.main.PsbtTxConfirmFragment.showExportPsbtDialog;

public class PsbtSignedTxFragment extends SignedTxFragment {
    @Override
    protected void displaySignResult(TxEntity txEntity) {
        mBinding.txDetail.qr.setVisibility(View.GONE);
        mBinding.txDetail.broadcastGuide.setVisibility(View.GONE);
    }

    @Override
    protected void showExportDialog() {
        showExportPsbtDialog(mActivity, txEntity.getTxId(),
                txEntity.getSignedHex(), this::navigateUp);
    }
}
