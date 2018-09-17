package com.allow.food4needy.home.mydonations

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.allow.food4needy.R
import com.allow.food4needy.domain.DonationFrequency

class DeleteDonationDialog : DialogFragment() {

    private var mDonationFrequency: DonationFrequency = DonationFrequency.ONETIME

    companion object {
        const val ARG_DONATION_FREQUENCY = "ARG_DONATION_FREQUENCY"

        fun newInstance(donationFrequency: DonationFrequency): DeleteDonationDialog {
            val args = Bundle()
            args.putSerializable(ARG_DONATION_FREQUENCY, donationFrequency)

            val fragment = DeleteDonationDialog()
            fragment.arguments = args
            return fragment
        }
    }

    interface DeleteDonationDialogFragmentInteractionListener {
        fun resetCurrentDonation()
        fun deleteDonation()
        fun cancelDeleteDonation()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val args = arguments
        if (args != null) {
            mDonationFrequency = args.getSerializable(ARG_DONATION_FREQUENCY) as DonationFrequency
        }

        if (savedInstanceState != null) {
            mDonationFrequency = savedInstanceState.getSerializable(ARG_DONATION_FREQUENCY) as DonationFrequency
        }

        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(R.string.dialog_delete_donation)
                .setMessage(
                        when (mDonationFrequency) {
                            DonationFrequency.ONETIME -> getString(R.string.dialog_delete_donation_confirmation_onetime)
                            DonationFrequency.DAILY -> getString(R.string.dialog_delete_donation_confirmation_daily)
                            DonationFrequency.WEEKLY -> getString(R.string.dialog_delete_donation_confirmation_weekly)
                            DonationFrequency.MONTHLY -> getString(R.string.dialog_delete_donation_confirmation_monthly)
                            DonationFrequency.ANNUALLY -> getString(R.string.dialog_delete_donation_confirmation_annually)
                        })
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    (parentFragment as? DeleteDonationDialogFragmentInteractionListener)?.deleteDonation()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    (parentFragment as? DeleteDonationDialogFragmentInteractionListener)?.resetCurrentDonation()
                }
                .setOnCancelListener {
                    (parentFragment as? DeleteDonationDialogFragmentInteractionListener)?.resetCurrentDonation()
                }
        // Create the AlertDialog object and return it
        return builder.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(ARG_DONATION_FREQUENCY, mDonationFrequency)
    }
}

