package com.cappielloantonio.tempo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.cappielloantonio.tempo.databinding.ItemHorizontalHomeSectorBinding
import com.cappielloantonio.tempo.model.HomeSector

class HomeSectorHorizontalAdapter :
    RecyclerView.Adapter<HomeSectorHorizontalAdapter.ViewHolder?>() {
    private var sectors: MutableList<HomeSector>

    init {
        this.sectors = mutableListOf<HomeSector?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemHorizontalHomeSectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeSectorHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sector = sectors.get(position)

        holder.item.homeSectorTitleCheckBox.text = sector.sectorTitle
        holder.item.homeSectorTitleCheckBox.setChecked(sector.isVisible)
    }

    override fun getItemCount(): Int {
        return sectors.size
    }

    var items: MutableList<HomeSector>
        get() = this.sectors
        set(sectors) {
            this.sectors = sectors
            notifyDataSetChanged()
        }

    fun getItem(id: Int): HomeSector? {
        return sectors.get(id)
    }

    inner class ViewHolder internal constructor(var item: ItemHorizontalHomeSectorBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            this.item.homeSectorTitleCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                onCheck(
                    isChecked
                )
            })
        }

        private fun onCheck(isChecked: Boolean) {
            sectors.get(getBindingAdapterPosition()).isVisible = isChecked
        }
    }
}
