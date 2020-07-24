package com.cobo.cold.ui.fragment.multisig;

import com.cobo.cold.viewmodel.CollectXpubViewModel;

public interface CollectXpubClickHandler {
    void onClickDelete(CollectXpubViewModel.XpubInfo info);
    void onClickScan(CollectXpubViewModel.XpubInfo info);
    void onClickSdcard(CollectXpubViewModel.XpubInfo info);
}
