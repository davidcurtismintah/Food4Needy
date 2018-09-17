package com.allow.food4needy.home.mydonations

import android.graphics.Color
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.allow.food4needy.R
import com.allow.food4needy.common.*
import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.DonationFrequency
import com.allow.food4needy.domain.DonationState
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.list_item_my_donations.view.*

class MyDonationsItemViewHolder(itemView: View,
                                private val actionOnDonate: (Donation) -> Unit,
                                private val actionOnDelete: (Donation) -> Unit,
                                private val actionOnExpire: (Donation) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    val foodBanner = itemView.food_banner!!
    val foodName = itemView.food_name!!
    val foodWeight = itemView.food_weight!!
    val foodLocation = itemView.food_location!!
    val foodProgress = itemView.progress_bar!!
    val foodExpiryDate = itemView.food_expiry_date!!
    val foodFrequency = itemView.donation_frequency!!

    fun bindViews(key: String, donation: Donation) {
        with(donation) {
            id = key
            foodBanner.loadImage(
                    imageUrl,
                    true,
                    when (donation.frequency) {
                        DonationFrequency.ONETIME -> R.color.colorRedLight
                        DonationFrequency.DAILY -> R.color.colorBlueGreyLight
                        DonationFrequency.WEEKLY -> R.color.colorBlueLight
                        DonationFrequency.MONTHLY -> R.color.colorGreenLight
                        DonationFrequency.ANNUALLY -> R.color.colorDeepOrangeLight
                    }
            )
            foodName.text = item
            foodLocation.text = address

            if (donation.frequency == DonationFrequency.ONETIME) {
                foodWeight.visibility = View.VISIBLE
                foodProgress.visibility = View.VISIBLE
                foodExpiryDate.visibility = View.VISIBLE

                foodWeight.text = weight.toKg()

                with(expiry) {
                    toProgress(created).let { progress ->
                        foodProgress.progress = progress

                        toExpired(progress).let { expired ->
                            foodProgress.progressDrawable = foodProgress.progressDrawable.mutate().apply {
                                setColorFilter(if (expired) Color.RED else ContextCompat.getColor(itemView.context, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN)
                            }

                            if (expired) {
                                if (state != DonationState.EXPIRED) {
                                    actionOnExpire(donation)
                                    itemView.donateActionImage.isEnabled = false
                                    itemView.deleteActionImage.isEnabled = false
                                }
                            }
                        }

                        foodExpiryDate.text = toExpiryText(progress, itemView.context.getString(R.string.donation_expired_text))
                    }
                }

            } else {
                foodWeight.visibility = View.GONE
                foodProgress.visibility = View.GONE
                foodExpiryDate.visibility = View.GONE
            }

            itemView.run {
                donateActionImage.setOnClickListener {
                    if (donation.volunteerId.isEmpty()) {
                        Snackbar.make(it, it.context.getString(R.string.my_donation_no_volunteer_to_pick_up_text), Snackbar.LENGTH_LONG).show()
                    } else {
                        actionOnDonate(donation)
                    }
                }
                deleteActionImage.setOnClickListener {
                    actionOnDelete(donation)
                }
            }

            foodFrequency.chipText = when (frequency) {
                DonationFrequency.ONETIME -> DonationFrequency.ONETIME.name
                DonationFrequency.DAILY -> DonationFrequency.DAILY.name
                DonationFrequency.WEEKLY -> DonationFrequency.WEEKLY.name
                DonationFrequency.MONTHLY -> DonationFrequency.MONTHLY.name
                DonationFrequency.ANNUALLY -> DonationFrequency.ANNUALLY.name
            }
        }
    }
}



