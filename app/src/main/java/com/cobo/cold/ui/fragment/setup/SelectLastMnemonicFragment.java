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

package com.cobo.cold.ui.fragment.setup;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.cobo.coinlib.MnemonicUtils;
import com.cobo.cold.R;
import com.cobo.cold.databinding.LastWordBinding;
import com.cobo.cold.databinding.SelectLastMnemonicBinding;
import com.cobo.cold.ui.common.BaseBindingAdapter;

import java.util.List;
import java.util.Objects;

public class SelectLastMnemonicFragment extends SetupVaultBaseFragment<SelectLastMnemonicBinding> {

    private String words;
    private WordsAdapter adapter;
    private String selectWord;

    @Override
    protected int setView() {
        return R.layout.select_last_mnemonic;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.setViewModel(viewModel);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.confirm.setOnClickListener(this::confirm);
        Bundle bundle = Objects.requireNonNull(getArguments());
        words = bundle.getString("words");
        List<String> validLastWords = MnemonicUtils.calculateLastWord(words);
        selectWord = validLastWords.get(0);
        adapter = new WordsAdapter(mActivity);
        adapter.setItems(validLastWords);
        mBinding.words.setAdapter(adapter);
    }

    public void handleClick(String s) {
        selectWord = s;
        adapter.notifyDataSetChanged();
    }

    private void confirm(View view) {
        Bundle data = new Bundle();
        data.putString("words", words + " " + selectWord);
        data.putString("last_word", selectWord);
        data.putBoolean("seed_pick", true);
        navigate(R.id.action_to_generateMnemonicFragment, data);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    class WordsAdapter extends BaseBindingAdapter<String, LastWordBinding> {

        public WordsAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.last_word;
        }

        @Override
        protected void onBindItem(LastWordBinding binding, String item) {
            binding.getRoot().setOnClickListener(v -> handleClick(item));
            binding.setWord(item);
            binding.setChecked(item.equals(selectWord));
        }
    }

}
