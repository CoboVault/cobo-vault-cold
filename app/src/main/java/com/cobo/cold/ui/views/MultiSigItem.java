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

package com.cobo.cold.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.cobo.cold.R;
import com.cobo.cold.databinding.MultisigItemBinding;

public class MultiSigItem extends RelativeLayout {

    String title;
    String remindText;
    private MultisigItemBinding binding;
    public MultiSigItem(Context context) {
        this(context,null);
    }

    public MultiSigItem(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MultiSigItem(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray mTypedArray=context.obtainStyledAttributes(attributeSet, R.styleable.MultiSigItem);
        title = mTypedArray.getString(R.styleable.MultiSigItem_title);
        remindText = mTypedArray.getString(R.styleable.MultiSigItem_remind_text);
        mTypedArray.recycle();
        initView(context);
    }

    public void initView(Context context) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.multisig_item,this,true);
        binding.title.setText(title);
        binding.remind.setText(remindText);
        binding.executePendingBindings();
    }

    public void setRemindText(String text) {
        this.remindText = text;
        binding.remind.setText(remindText);
    }
}
