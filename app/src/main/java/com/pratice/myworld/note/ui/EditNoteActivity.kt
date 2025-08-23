package com.pratice.myworld.note.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.pratice.myworld.AlarmReceiver
import com.pratice.myworld.databinding.ActivityEditNoteBinding

import com.pratice.myworld.note.db.Notes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditNoteBinding

    private var notes: Notes? = null
    private var isOldNotes = false
    private var deadlineCalendar: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inti()
    }
    private fun inti() {
        notes = intent.getSerializableExtra("old_notes") as? Notes
        notes?.let {
            binding.editTextNoteTitle.setText(it.title)
            binding.editTextNote.setText(it.notes)
            binding.textViewDeadline.setText(it.deadline)
            isOldNotes = true
        }
        binding.back.setOnClickListener {
           onBackPressed()
        }
        binding.btnSetDeadline.setOnClickListener {
            pickDeadlineDateTime()
        }
        binding.check.setOnClickListener {
            val title = binding.editTextNoteTitle.text.toString()
            val description = binding.editTextNote.text.toString()
            val deadLine = binding.textViewDeadline.text.toString()

            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter the description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (deadLine.isEmpty()){
                Toast.makeText(this, "Please enter the Deadline Date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val format = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
            val date = Date()
            if (!isOldNotes) {
                notes = Notes()
            }

            notes?.apply {
                this.title = title
                this.notes = description
                this.date = format.format(date)
                this.deadline = deadlineCalendar?.let {
                    format.format(it.time)
                } ?: ""
            }


            deadlineCalendar?.let {
                scheduleTaskReminder(it.timeInMillis, notes?.title ?: "Note")
            }

            val intent = Intent().apply {
                putExtra("note", notes)
            }
            setResult(Activity.RESULT_OK, intent)

            finish()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleTaskReminder(deadlineMillis: Long, noteTitle: String) {
        val reminderTime = deadlineMillis - 2 * 60 * 1000  // 2 minutes before deadline
        if (reminderTime <= System.currentTimeMillis()) {
            return // deadline already passed
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("message", "Please complete your task: $noteTitle")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt(),  // unique ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
        )
    }


    private fun pickDeadlineDateTime() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        deadlineCalendar = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        }
                        val format = SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault())
                        binding.textViewDeadline.text = format.format(deadlineCalendar!!.time)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }
}