package com.allow.food4needy.profile.info

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allow.food4needy.R
import com.allow.food4needy.common.EventObserver
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.Status
import com.allow.food4needy.domain.User
import kotlinx.android.synthetic.main.fragment_info.*

class ProfileInfoFragment : Fragment() {

    companion object {
        fun newInstance(): ProfileInfoFragment = ProfileInfoFragment()
    }

    interface ProfileInfoFragmentInteractionListener {
//        fun showUserImage(imageUrl: String)
    }

    private var mInteractionListener: ProfileInfoFragmentInteractionListener? = null

    private lateinit var mViewModel: ProfileInfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(fab_save) {
            post {
                val marginParams = swipeRefresh.layoutParams as ViewGroup.MarginLayoutParams
                marginParams.setMargins(
                        marginParams.leftMargin,
                        marginParams.topMargin,
                        marginParams.rightMargin,
                        height)
                swipeRefresh.requestLayout()
            }
            setOnClickListener {
                if (!formValidated) return@setOnClickListener

                mViewModel.userLiveData.value?.let {
                    if (changed(it)) {

                        val updated = it.copy(
                                name = info_username.text.toString(),
                                address = info_address.text.toString(),
                                country = info_country.text.toString(),
                                phone = info_phone.text.toString(),
                                dateOfBirth = info_date_of_birth.text.toString(),
                                email = info_email.text.toString()
                        )

                        mViewModel.updateProfileInfo(updated)
                    }


                }
            }
        }

        swipeRefresh.run {
            setOnRefreshListener {
                mViewModel.userLiveData.getUser()
            }
        }
        mViewModel = ViewModelProviders.of(this).get(ProfileInfoViewModel::class.java)
        mViewModel.userLiveData.observe(this, Observer {
            it?.let {
                showUserInfo(it)
            }
        })
        mViewModel.loadingLiveData.observe(this, Observer {
            it?.peekContent()?.let {
                swipeRefresh.isRefreshing = it
            }
        })
        mViewModel.showSnackBarLiveData.observe(this, EventObserver {
            showSnackBar(it)
        })

        mViewModel.updateProfileInfoLiveData.observe(this, EventObserver {
            processUpdateProfileInfoResponse(it)
        })
    }

    private fun changed(updated: User): Boolean {
        return with(updated) {
            (name != info_username.text.toString()) or
                    (address != info_address.text.toString()) or
                    (country != info_country.text.toString()) or
                    (phone != info_phone.text.toString()) or
                    (dateOfBirth != info_date_of_birth.text.toString()) or
                    (email != info_email.text.toString())
        }
    }

    private fun processUpdateProfileInfoResponse(response: Response<User, String>) {
        when (response.status) {
            Status.LOADING -> renderUpdateProfileInfoLoadingState()

            Status.SUCCESS -> response.data?.let{ renderUpdateProfileInfoDataState() }

            Status.ERROR -> response.error?.let{ renderUpdateProfileInfoErrorState() }
        }
    }

    private fun renderUpdateProfileInfoLoadingState() {
//        showLoading(true)
    }

    private fun renderUpdateProfileInfoDataState() {
        showSnackBar(true)
    }

    private fun renderUpdateProfileInfoErrorState() {
        showSnackBar(false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is ProfileInfoFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement ProfileInfoFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }

    private fun showUserInfo(user: User) {
//        mInteractionListener?.showUserImage(user.imageUrl)
        info_username.setText(user.name)
        info_address.setText(user.address)
        info_country.setText(user.country)
        info_phone.setText(user.phone)
        info_date_of_birth.setText(user.dateOfBirth)
        info_email.setText(user.email)
    }

    private fun showSnackBar(success: Boolean) {
        Snackbar.make(scrollView,
                if (!success) getString(R.string.error_could_not_get_user) else getString(R.string.success_user_updated),
                Snackbar.LENGTH_SHORT).show()
    }

    private var formValidated: Boolean = false
        get() {
            field = true
            if (TextUtils.isEmpty(info_username.text)) {
                info_username.error = getString(R.string.error_user_details_name_is_required)
                field = false
            }
            if (TextUtils.isEmpty(info_phone.text)) {
                info_phone.error = getString(R.string.error_user_details_phone_is_required)
                field = false
            }
            return field
        }

}
