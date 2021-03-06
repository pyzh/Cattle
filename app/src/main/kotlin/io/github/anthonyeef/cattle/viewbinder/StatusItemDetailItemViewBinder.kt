package io.github.anthonyeef.cattle.viewbinder

import android.annotation.SuppressLint
import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import io.github.anthonyeef.cattle.R
import io.github.anthonyeef.cattle.data.statusData.Status
import io.github.anthonyeef.cattle.extension.goneIf
import io.github.anthonyeef.cattle.utils.StatusParsingUtils
import io.github.anthonyeef.cattle.utils.TimeUtils
import me.drakeet.multitype.ItemViewBinder
import org.jetbrains.anko.find

/**
 *
 */
class StatusItemDetailItemViewBinder : ItemViewBinder<Status, StatusItemDetailItemViewBinder.StatusItemDetailViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): StatusItemDetailViewHolder {
        return StatusItemDetailViewHolder(inflater.inflate(R.layout.view_item_status_detail, parent, false))
    }


    override fun onBindViewHolder(holder: StatusItemDetailViewHolder, item: Status) {
        holder.bindStatusData(item)
    }


    inner class StatusItemDetailViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val avatar: CircleImageView by lazy { itemView.find<CircleImageView>(R.id.avatar) }
        private val userName: TextView by lazy { itemView.find<TextView>(R.id.user_display_name) }
        private val userId: TextView by lazy { itemView.find<TextView>(R.id.user_id) }
        private val statusCreateTime: TextView by lazy { itemView.find<TextView>(R.id.status_create_time) }
        private val statusLeftSpacing: Space by lazy { itemView.find<Space>(R.id.left_spacing) }
        private val statusConnector: View by lazy { itemView.find<View>(R.id.connector) }
        private val statusContent: TextView by lazy { itemView.find<TextView>(R.id.status_content) }
        private val statusPhoto: ImageView by lazy { itemView.find<ImageView>(R.id.status_photo) }

        @SuppressLint("SetTextI18n")
        fun bindStatusData(item: Status) {
            Glide.with(itemView.context)
                    .load(item.user?.profileImageUrlLarge)
                    .into(avatar)

            statusPhoto.goneIf(item.photo == null)
            item.photo?.let {
                Glide.with(itemView.context)
                    .load(it.largeurl)
                    .into(statusPhoto)
            }

            userName.text = item.user?.screenName
            userId.text = "@" + item.user?.id
            statusCreateTime.text = TimeUtils.getTime(item.createdAt) +
                    "\n\r" + TimeUtils.getDate(item.createdAt)
            if (item.text.isNotEmpty()) {
                StatusParsingUtils.setStatus(statusContent, item.text)
            }

            statusLeftSpacing.goneIf(item.isSingle)
            statusConnector.goneIf(item.isSingle)
        }
    }
}