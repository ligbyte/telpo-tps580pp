package com.stkj.cashier.app.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.stkj.cashier.R
import com.stkj.cashier.bean.FaceChooseItemEntity
import com.stkj.cashier.util.StarUtils


class FaceChooseListAdapter(data: List<FaceChooseItemEntity>?) :
    BaseQuickAdapter<FaceChooseItemEntity, BaseViewHolder>(
        R.layout.item_face_choose,
        data as MutableList<FaceChooseItemEntity>?
    ) {
    @SuppressLint("DefaultLocale")
    override fun convert(helper: BaseViewHolder, item: FaceChooseItemEntity) {


        helper
            .setText(R.id.tv_face_name,   item.getUsername())
            .setText(R.id.tv_face_phone,  StarUtils.phoneStar(item.getPhone()));

        Glide.with(context).load(item.headUrl).into(helper.getView<View>(R.id.iv_icon) as ImageView)

        var itemPosition = getItemPosition(item)
        if (itemPosition == mSelectedPosition) {
            helper.getView<ImageView>(R.id.iv_status).visibility = View.VISIBLE
            helper.getView<RelativeLayout>(R.id.rlItem).isSelected = true
        } else {
            helper.getView<ImageView>(R.id.iv_status).visibility = View.GONE
            helper.getView<RelativeLayout>(R.id.rlItem).isSelected = false
        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    private var mSelectedPosition = -1 // 初始化为-1表示没有选中项
    // 构造函数和其他方法...

    // 构造函数和其他方法...
    fun setSelectedPosition(position: Int) {
        mSelectedPosition = position
        notifyDataSetChanged() // 通知更新列表
    }

    fun getSelectedPosition(): Int {
        return mSelectedPosition
    }
    fun getSelectedItem(): FaceChooseItemEntity {
        return getItem(mSelectedPosition)
    }
}