package com.jon.cotbeacon.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.cotbeacon.R

internal class ChatAdapter(context: Context) : RecyclerView.Adapter<ChatViewHolder>() {

    private val chats = ArrayList<ChatCursorOnTarget>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = inflater.inflate(viewType, parent, false)
        return ChatViewHolder(view, chats)
    }

    override fun getItemViewType(position: Int): Int {
        val isSelf = chats[position].isSelf
        return LAYOUTS.getValue(isSelf)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.initialise()
    }

    override fun getItemCount() = chats.size

    fun updateChats(newChats: List<ChatCursorOnTarget>) {
        chats.clear()
        chats.addAll(newChats)
        notifyDataSetChanged()
    }

    private companion object {
        val LAYOUTS = mapOf(
                true to R.layout.chat_recycler_item_mine,
                false to R.layout.chat_recycler_item_other
        )
    }
}
