package com.allow.food4needy.adddonation

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.allow.food4needy.R
import com.allow.food4needy.adddonation.DonorAddDonationFragment.AddDonationFragmentInteractionListener
import com.allow.food4needy.adddonation.RestaurantAddDonationFragment.RestaurantAddDonationFragmentInteractionListener
import com.allow.food4needy.common.addFragment
import com.allow.food4needy.domain.UserRole
import kotlinx.android.synthetic.main.activity_add_donation.*


class AddDonationActivity : AppCompatActivity(),
        AddDonationFragmentInteractionListener,
        RestaurantAddDonationFragmentInteractionListener {

    private lateinit var mViewModel: AddDonationActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_donation)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mViewModel = ViewModelProviders.of(this).get(AddDonationActivityViewModel::class.java)

        mViewModel.userRoleLiveData.observe(this, Observer {
            if (supportFragmentManager.findFragmentById(R.id.fragment) == null) {
                addFragment(R.id.fragment, when (it) {
                    UserRole.DONOR -> DonorAddDonationFragment()
                    UserRole.RESTAURANT -> RestaurantAddDonationFragment()
                    else -> throw IllegalArgumentException("User role can only be ${UserRole.DONOR} or ${UserRole.RESTAURANT}")
                })
            }
        })
    }

    override fun finishActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
                upIntent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                when {
                    upIntent == null -> throw IllegalStateException("No Parent Activity Intent")
                    NavUtils.shouldUpRecreateTask(this, upIntent) -> {
                        TaskStackBuilder.create(this)
                                .addNextIntentWithParentStack(upIntent)
                                .startActivities()
                    }
                    else -> {
                        NavUtils.navigateUpTo(this, upIntent)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
