package com.allow.food4needy.home.nearbyvolunteers

import android.support.v7.widget.RecyclerView
import android.view.View
import com.allow.food4needy.R
import com.allow.food4needy.common.loadImage
import com.allow.food4needy.domain.User
import kotlinx.android.synthetic.main.list_item_volunteers.view.*

class NearbyVolunteersItemViewHolder(itemView: View, val callButtonAction: (User) -> Unit) : RecyclerView.ViewHolder(itemView) {
    val volunteerImage = itemView.volunteer_image!!
    val volunteerName = itemView.volunteer_name!!
    val volunteerAddress = itemView.volunteer_address!!
    val makeCall = itemView.callActionImage!!

    fun bindViews(volunteer: User) {
        with(volunteer) {
            volunteerImage.loadImage(imageUrl, true, R.drawable.ic_face_black_24dp)
            volunteerName.text = name
            volunteerAddress.text = address

            makeCall.setOnClickListener {
                callButtonAction(volunteer)
            }
        }
    }

    fun unbindViews(){
        // no op
    }

}
