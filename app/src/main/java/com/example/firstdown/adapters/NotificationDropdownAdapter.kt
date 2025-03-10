package com.example.firstdown.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.R
import com.example.firstdown.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationDropdownAdapter(
    private var notifications: List<Notification> = emptyList(),
    private val listener: NotificationClickListener
) : RecyclerView.Adapter<NotificationDropdownAdapter.NotificationViewHolder>() {

    interface NotificationClickListener {
        fun onNotificationClicked(notification: Notification, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_dropdown, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_notification_message)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_notification_time)
        private val indicator: View = itemView.findViewById(R.id.notification_indicator)

        fun bind(notification: Notification) {
            tvMessage.text = notification.message

            // Format timestamp to a relative time string
            val timeAgo = getTimeAgo(notification.timestamp)
            tvTime.text = timeAgo

            // Show/hide unread indicator
            indicator.visibility = if (notification.isRead) View.INVISIBLE else View.VISIBLE

            // Set click listener
            itemView.setOnClickListener {
                listener.onNotificationClicked(notification, adapterPosition)
            }
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "Just now"
                diff < 3600_000 -> "${diff / 60_000} minutes ago"
                diff < 86400_000 -> "${diff / 3600_000} hours ago"
                diff < 604800_000 -> "${diff / 86400_000} days ago"
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }
}