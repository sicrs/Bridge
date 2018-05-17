package moe.shizuku.bridge.adapter;

import android.content.pm.ResolveInfo;

import java.util.List;

import moe.shizuku.bridge.viewholder.ChooserItemViewHolder;
import moe.shizuku.support.recyclerview.BaseRecyclerViewAdapter;
import moe.shizuku.support.recyclerview.ClassCreatorPool;

/**
 * Created by Rikka on 2017/4/6.
 */

public class ChooserAdapter extends BaseRecyclerViewAdapter<ClassCreatorPool> {

    public ChooserAdapter(List<ResolveInfo> resolveInfo, boolean editMode) {
        super(resolveInfo);

        getCreatorPool().putRule(ResolveInfo.class, ChooserItemViewHolder.newCreator(editMode));
    }


    @Override
    public ClassCreatorPool onCreateCreatorPool() {
        return new ClassCreatorPool();
    }
}
