package com.allow.food4needy.start.skipsignin


import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allow.food4needy.Food4NeedyApplication

import com.allow.food4needy.R
import com.allow.food4needy.domain.User
import com.allow.food4needy.domain.UserRole
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_skip_sign_in.*

class SkipSignInFragment : Fragment() {

    interface SkipSignInFragmentInteractionListener {
        fun showHomeActivity(userRole: UserRole)
    }

    private var mInteractionListener: SkipSignInFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_skip_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Food4NeedyApplication.userManager.addUser(
                                user = User(id = FirebaseAuth.getInstance().currentUser!!.uid, role = UserRole.GUEST),
                                actionOnSuccess = {
                                    mInteractionListener?.showHomeActivity(UserRole.GUEST)
                                },
                                actionOnError = { }
                        )
                    } else {
                        Snackbar.make(auth_done_view, R.string.unknown_error, Snackbar.LENGTH_LONG).show()
                    }
                }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is SkipSignInFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement ${SkipSignInFragmentInteractionListener::class.java.simpleName}")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }


}
