package com.example.scorpapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.scorpapp.R
import com.example.scorpapp.databinding.PersonItemBinding
import com.example.scorpapp.source.Person
import com.example.scorpapp.util.UniqueList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonAdapter :
    RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {
    class PersonViewHolder(var view: PersonItemBinding) : RecyclerView.ViewHolder(view.root)

    init {
        setHasStableIds(true)
    }

    private var people = UniqueList<Person,Int>()
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<PersonItemBinding>(
            inflater,
            R.layout.person_item, parent, false
        )
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.view.person = people[position]
    }

    override fun getItemCount(): Int {
        return people.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshAdapter(newList: List<Person>) {
        adapterScope.launch {
            people.clear()
            people.addAll(newList)
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    fun updateAdapter(newList: List<Person>) {
        val position = people.size + 1
        people.addAll(newList)
        notifyItemRangeInserted(position, newList.size)
    }

    override fun getItemId(position: Int): Long {
        return people[position].id.toLong()
    }
}