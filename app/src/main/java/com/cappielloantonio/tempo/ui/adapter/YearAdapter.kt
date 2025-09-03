package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cappielloantonio.tempo.databinding.ItemHomeYearBinding
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.util.Constants

class YearAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<YearAdapter.ViewHolder?>() {
    private var years: MutableList<Int?>

    init {
        this.years = mutableListOf<Int?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemHomeYearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return YearAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val year = years.get(position)!!

        holder.item.yearLabel.text = year.toString()
    }

    override fun getItemCount(): Int {
        return years.size
    }

    fun getItem(position: Int): Int? {
        return years.get(position)
    }

    fun setItems(years: MutableList<Int?>) {
        this.years = years
        notifyDataSetChanged()
    }

    inner class ViewHolder internal constructor(var item: ItemHomeYearBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putString(Constants.MEDIA_BY_YEAR, Constants.MEDIA_BY_YEAR)
            bundle.putInt("year_object", years.get(getBindingAdapterPosition())!!)

            click.onYearClick(bundle)
        }
    }
}
