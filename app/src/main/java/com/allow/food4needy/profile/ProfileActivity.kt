package com.allow.food4needy.profile

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.*
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.allow.food4needy.R
import com.allow.food4needy.common.loadImage
import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.home.HomeActivity
import com.allow.food4needy.profile.about.AboutActivity
import com.allow.food4needy.profile.donationssummary.DonationsSummaryFragment.DonationsSummaryFragmentInteractionListener
import com.allow.food4needy.profile.info.ProfileInfoFragment.ProfileInfoFragmentInteractionListener
import kotlinx.android.synthetic.main.activity_profile.*
import timber.log.Timber

class ProfileActivity : AppCompatActivity(),
        ProfileInfoFragmentInteractionListener,
        DonationsSummaryFragmentInteractionListener {

    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, ProfileActivity::class.java)
        }

    }

    private lateinit var mViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = ""

        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        mViewModel.userRoleLiveData.observe(this, Observer {
            Timber.d("> role: $it")
            it?.let {
                val roleAdapter = when (it) {
                    UserRole.GUEST -> GuestRoleAdapter()
                    UserRole.DONOR -> DonorRoleAdapter(this)
                    UserRole.VOLUNTEER -> VolunteerRoleAdapter(this)
                    UserRole.RESTAURANT -> RestaurantRoleAdapter(this)
                }
                val sectionsPagerAdapter = ProfilePagerAdapter(supportFragmentManager, roleAdapter)
                view_pager.adapter = sectionsPagerAdapter
                view_pager.offscreenPageLimit = 0
                tab_layout.setupWithViewPager(view_pager)
            }
        })
        mViewModel.userImageLiveData.observe(this, Observer {
            it?.let {
                toolbar_image.loadImage(it, true, R.drawable.ic_face_black_24dp)
            }
        })
        mViewModel.userNameLiveData.observe(this, Observer {
            it?.let {
//                title = it
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HomeActivity.RC_ADD_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(coordinator, "Donation Added", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
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
            R.id.menu_about ->{
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun showUserImage(imageUrl: String) {
//        toolbar_image.loadImage(imageUrl, true, R.drawable.ic_face_black_24dp)
//    }

//    class ProfilePagerAdapter(fm: FragmentManager, ctx: Context) : FragmentStatePagerAdapter(fm) {
//
//        class PagerItem(val fragment: Fragment, val title: String)
//
//        private val mPagerItems = arrayOf(
//                PagerItem(ProfileInfoFragment.newInstance(), ctx.getString(R.string.heading_info)),
//                PagerItem(DonationsSummaryFragment.newInstance(), ctx.getString(R.string.heading_donations)),
//                PagerItem(PlaceHolderFragment.newInstance("My Rewards"), ctx.getString(R.string.heading_rewards))
//        )
//
//        override fun getPageTitle(position: Int): CharSequence? = mPagerItems[position].title
//
//        override fun getItem(position: Int): Fragment = mPagerItems[position].fragment
//
//        override fun getCount(): Int = mPagerItems.size
//    }


    inner class ProfilePagerAdapter(fm: FragmentManager, val roleAdapter: RoleAdapter) : FragmentStatePagerAdapter(fm) {

        override fun getPageTitle(position: Int): CharSequence? = roleAdapter.getPageTitle(position)

        override fun getItem(position: Int): Fragment = roleAdapter.getItem(position)

        override fun getCount() = roleAdapter.getCount()
    }

}
