/*
 *
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
 *
 */

package com.cobo.cold.ui.fragment.multisig;

import com.cobo.cold.viewmodel.CollectXpubViewModel;

public interface CollectXpubClickHandler {
    void onClickDelete(CollectXpubViewModel.XpubInfo info);
    void onClickScan(CollectXpubViewModel.XpubInfo info);
    void onClickSdcard(CollectXpubViewModel.XpubInfo info);
}
