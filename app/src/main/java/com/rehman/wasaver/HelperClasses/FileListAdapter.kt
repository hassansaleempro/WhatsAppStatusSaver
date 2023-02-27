package com.rehman.wasaver.HelperClasses

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rehman.wasaver.Activities.ViewImageActivity
import com.rehman.wasaver.Activities.ViewVideoActivity
import com.rehman.wasaver.R
import java.io.File

class FileListAdapter(private val list: ArrayList<File>, val context: Context) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val wpImage: ImageView = itemView.findViewById(R.id.wp_images)
        val wpDownload: TextView = itemView.findViewById(R.id.wp_download)
        val wpPlay: ImageView = itemView.findViewById(R.id.wp_play)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        if (currentItem.toUri().toString().endsWith(".mp4")) {
            holder.wpPlay.visibility = View.VISIBLE
        } else {
            holder.wpPlay.visibility = View.GONE
        }
        holder.wpDownload.visibility = View.GONE

        Glide.with(context.applicationContext).load(currentItem.path.toString())
            .into(holder.wpImage)

        holder.itemView.setOnClickListener {
            if (currentItem.toUri().toString().endsWith(".mp4")) {

                val intent = Intent(context, ViewVideoActivity::class.java)
                intent.putExtra("fileUri", currentItem.path.toString())
                val activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    context as Activity,
                    holder.itemView,
                    "ITEMS"
                )
                context.startActivity(intent, activityOptions.toBundle())
            } else {
                val intent = Intent(context, ViewImageActivity::class.java)
                intent.putExtra("fileUri", currentItem.path.toString())
                val activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    context as Activity,
                    holder.itemView,
                    "ITEMS"
                )
                context.startActivity(intent, activityOptions.toBundle())
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
}