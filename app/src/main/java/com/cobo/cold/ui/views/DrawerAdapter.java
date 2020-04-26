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

package com.cobo.cold.ui.views;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.DrawerItemBinding;
import com.cobo.cold.fingerprint.FingerprintKit;

import java.util.Arrays;
import java.util.List;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.Holder> {

    public OnItemClickListener listener;
    private final List<DrawerItem> dataList = Arrays.asList(
            new DrawerItem(R.id.drawer_wallet, R.drawable.drawer_wallet, R.string.drawer_menu_my_vault),
            new DrawerItem(R.id.drawer_manage, R.drawable.drawer_asset_manager, R.string.drawer_menu_add_remove),
            new DrawerItem(R.id.drawer_sync, R.drawable.drawer_asset_observation, R.string.drawer_menu_sync),
            new DrawerItem(R.id.drawer_settings, R.drawable.drawer_setting, R.string.drawer_menu_setting),
            new DrawerItem(R.id.drawer_about, R.drawable.drawer_about, R.string.drawer_menu_about),
            new DrawerItem(R.id.drawer_id, R.drawable.drawer_id, 0)
    );

    public DrawerAdapter(int currentFragmentIndex) {
        dataList.forEach(drawerItem -> {
            if (drawerItem.index == currentFragmentIndex) {
                drawerItem.select = true;
            }
        });
    }

    @BindingAdapter("title")
    public static void setTitle(TextView view, DrawerItem item) {
        boolean supportFingerprint = FingerprintKit.isHardwareDetected(view.getContext());
        view.setText(item.titleRes);
        if (item.index == R.id.drawer_settings) {
            if (!Utilities.hasUserClickFingerprint(view.getContext())
                    || (supportFingerprint && !Utilities.hasUserClickPatternLock(view.getContext()))) {
                BadgeFactory.create(view.getContext())
                        .setWidthAndHeight(10, 10)
                        .setBadgeBackground(Color.RED)
                        .setBadgeGravity(Gravity.END | Gravity.TOP)
                        .setShape(BadgeView.SHAPE_CIRCLE)
                        .setSpace(10, 0)
                        .bind(view);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size() - 1;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DrawerItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.drawer_item, parent, false);

        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final DrawerItem item = dataList.get(position);
        if (position == dataList.size() - 1) {
            holder.binding.setDrawerItem(item);
            holder.binding.icon.setImageResource(item.iconRes);
            holder.binding.getRoot().setClickable(false);
            holder.binding.text.setText(Utilities.getVaultId(holder.binding.getRoot().getContext()));
            holder.binding.text.setTextColor(holder.binding.getRoot().getContext().getColor(R.color.id_color));
        } else {
            holder.binding.setDrawerItem(item);
            holder.binding.executePendingBindings();
            holder.binding.getRoot().setOnClickListener(v -> {
                dataList.forEach(i -> i.select = false);
                item.select = true;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.itemClick(item.index);
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectIndex(int index) {
        for (DrawerItem item : dataList) {
            item.select = false;
        }
        dataList.forEach(drawerItem -> {
            if (drawerItem.index == index) {
                drawerItem.select = true;
            }
        });
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void itemClick(int position);
    }

    public class DrawerItem {
        public final int index;
        public final int iconRes;
        public final int titleRes;
        public boolean select;

        DrawerItem(int index, int iconRes, int titleRes) {
            this.index = index;
            this.iconRes = iconRes;
            this.titleRes = titleRes;
        }
    }

    class Holder extends RecyclerView.ViewHolder {
        final DrawerItemBinding binding;

        Holder(DrawerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setIsRecyclable(false);
        }
    }
}
