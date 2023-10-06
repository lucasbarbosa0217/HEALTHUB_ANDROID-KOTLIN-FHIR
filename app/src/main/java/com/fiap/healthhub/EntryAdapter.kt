package com.fiap.healthhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fiap.healthhub.Entry
import com.fiap.healthhub.R

class EntryAdapter(private val entryList: List<Entry>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.entry_item, parent, false)
        return EntryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val currentItem = entryList[position]

        holder.clinicalStatus.text = currentItem.clinicalStatusCode
        holder.recordedDate.text = currentItem.recordedDate
        holder.noteText.text = currentItem.noteText

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return entryList.size
    }

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clinicalStatus: TextView = itemView.findViewById(R.id.clinicalStatus)
        val recordedDate: TextView = itemView.findViewById(R.id.recordedDate)
        val noteText: TextView = itemView.findViewById(R.id.noteText)
    }
}
