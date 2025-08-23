package com.pratice.myworld.note.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pratice.myworld.R
import com.pratice.myworld.note.db.Notes
import java.util.Locale
import kotlin.random.Random

class NoteAdapter(private val context: Context,
                  private var notesList: List<Notes>,
                  private val listener: NotesClickListener
):RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val expandedPositionSet: MutableSet<Int> = mutableSetOf()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(isExpanded: Boolean) {
            textViewContent.maxLines = if (isExpanded) Int.MAX_VALUE else 2
        }

        val textViewContent: TextView = itemView.findViewById(R.id.textViewContent)
        val textViewTitle: TextView = itemView.findViewById(R.id.titel)
        val textViewDate: TextView = itemView.findViewById(R.id.textDate)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val textViewDeadline: TextView = itemView.findViewById(R.id.deadline)
        val progressTimeline: ProgressBar = itemView.findViewById(R.id.progressTimeline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.note_card, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: NoteViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentNote = notesList[position]
        holder.textViewTitle.text = currentNote.title
        holder.textViewContent.text = currentNote.notes
        holder.textViewDate.text = currentNote.date
        holder.cardView.setCardBackgroundColor(holder.itemView.resources.getColor(getRandomColor(), null))

        val isExpanded = expandedPositionSet.contains(position)
        holder.bind(isExpanded)

        // 🆕 Show deadline text
        holder.textViewDeadline.text =
            if (currentNote.deadline != null) "Deadline: ${currentNote.deadline}"
            else "No deadline"

        // 🆕 Calculate progress timeline
        if (currentNote.deadline != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val start = dateFormat.parse(currentNote.date)?.time ?: 0
                val end = dateFormat.parse(currentNote.deadline)?.time ?: 0
                val now = System.currentTimeMillis()

                if (end > start) {
                    val progress = (((now - start).toFloat() / (end - start)) * 100).toInt()
                    holder.progressTimeline.progress = progress.coerceIn(0, 100)
                } else {
                    holder.progressTimeline.progress = 0
                }
            } catch (e: Exception) {
                holder.progressTimeline.progress = 0
            }
        } else {
            holder.progressTimeline.progress = 0
        }


        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val wasExpanded = expandedPositionSet.contains(position)
                if (wasExpanded) {
                    expandedPositionSet.remove(position)
                } else {
                    expandedPositionSet.add(position)
                }
                notifyItemChanged(position)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                listener.onClick(notesList[holder.adapterPosition])
                return true
            }
        })

        holder.cardView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        holder.cardView.setOnClickListener {
            listener.onClick(notesList[holder.adapterPosition])
        }

        holder.cardView.setOnLongClickListener {
            listener.onLongPress(notesList[holder.adapterPosition], holder.cardView)
            true
        }
    }

    private fun getRandomColor(): Int {
        val listColor = listOf(
            R.color.note1,
            R.color.note2,
            R.color.note3,
            R.color.note4,
            R.color.note5,
            R.color.note6,
            R.color.note7
        )
        val randomIndex = Random.nextInt(listColor.size)
        return listColor[randomIndex]
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newNote: List<Notes>) {
        notesList =newNote
        notifyDataSetChanged()
    }


    interface NotesClickListener {
        fun onClick(notes: Notes?)
        fun onLongPress(notes: Notes?, cardView: CardView?)
    }


}