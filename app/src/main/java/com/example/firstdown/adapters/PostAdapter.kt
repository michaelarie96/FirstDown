package com.example.firstdown.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.R
import com.example.firstdown.databinding.ItemPostBinding
import com.example.firstdown.model.Post
import com.example.firstdown.utilities.ImageLoader

class PostAdapter(private var posts: List<Post> = emptyList()) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // Interface for handling post interactions
    interface PostInteractionListener {
        fun onLikeClicked(post: Post, position: Int)
    }

    var listener: PostInteractionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        // Use ViewBinding to inflate the layout
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    // Update posts with DiffUtil for efficient updates
    fun updatePosts(newPosts: List<Post>) {
        // Calculate the differences between old and new lists
        val diffCallback = PostDiffCallback(posts, newPosts)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Update the data
        this.posts = newPosts

        // Dispatch the updates to the adapter
        diffResult.dispatchUpdatesTo(this)
    }

    fun getCurrentPosts(): List<Post> {
        return posts
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            with(binding) {
                // Set text values
                tvUserName.text = post.userName
                tvTimeAgo.text = post.timeAgo
                tvPostContent.text = post.content
                tvLikes.text = post.likes.toString()

                // Handle profile image
                ImageLoader.loadImage(post.userProfileImage, imgProfile)

                // Handle post image visibility and loading
                if (post.imageUrl != null) {
                    imgPostContent.visibility = android.view.View.VISIBLE
                    ImageLoader.loadImage(post.imageUrl, imgPostContent)
                } else {
                    imgPostContent.visibility = android.view.View.GONE
                }

                btnLike.setImageResource(
                    if (post.likedByCurrentUser) R.drawable.ic_like_filled else R.drawable.ic_like
                )

                // Set up click listeners
                btnLike.setOnClickListener {
                    listener?.onLikeClicked(post, adapterPosition)
                }
            }
        }
    }

    // DiffUtil callback for efficient RecyclerView updates
    private class PostDiffCallback(
        private val oldList: List<Post>,
        private val newList: List<Post>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Posts are the same if they have the same ID
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Check if all content is the same (for determining if the item needs to be redrawn)
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem.userName == newItem.userName &&
                    oldItem.timeAgo == newItem.timeAgo &&
                    oldItem.content == newItem.content &&
                    oldItem.likes == newItem.likes &&
                    oldItem.comments == newItem.comments &&
                    oldItem.imageUrl == newItem.imageUrl
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            // This method is optional but can be used for partial updates
            // Return a payload object that describes what changed for more efficient updates
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}