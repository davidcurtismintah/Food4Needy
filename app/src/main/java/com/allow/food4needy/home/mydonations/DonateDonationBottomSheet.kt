package com.allow.food4needy.home.mydonations

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allow.food4needy.R
import kotlinx.android.synthetic.main.view_donate_thank_you.view.*

class DonateDonationBottomSheet : BottomSheetDialogFragment() {

    interface DonateDonationBottomSheetInteractionListener {
        fun resetCurrentDonation()
        fun showCameraView()
    }

    companion object {
        const val ARG_VOLUNTEER_NAME = "volunteer_name"
        fun getInstance(volunteerName: String): DonateDonationBottomSheet {
            return DonateDonationBottomSheet().apply {
                with(Bundle()) {
                    putString(ARG_VOLUNTEER_NAME, volunteerName)
                    arguments = this
                }
            }
        }
    }

    private lateinit var mParentFragmentModel: MyDonationsViewModel

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.view_donate_thank_you, container, false)

        mParentFragmentModel = ViewModelProviders.of(parentFragment!!).get(MyDonationsViewModel::class.java)
        mParentFragmentModel.currentDonation.observe(this, Observer {
            it?.let { donation ->
                view.thank_you_volunteer_name.text = donation.volunteer
            }
        })

        view.thank_you_take_selfie.setOnClickListener {
            dismissAllowingStateLoss()
            (parentFragment as? DonateDonationBottomSheetInteractionListener)?.resetCurrentDonation()
            (parentFragment as? DonateDonationBottomSheetInteractionListener)?.showCameraView()
        }
        view.no_thank_you.setOnClickListener {
            dismissAllowingStateLoss()
            (parentFragment as? DonateDonationBottomSheetInteractionListener)?.resetCurrentDonation()
        }
        return view
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        (parentFragment as? DonateDonationBottomSheetInteractionListener)?.resetCurrentDonation()
    }

}