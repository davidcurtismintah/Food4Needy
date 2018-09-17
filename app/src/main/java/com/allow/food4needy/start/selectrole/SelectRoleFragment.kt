package com.allow.food4needy.start.selectrole


import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.os.Bundle
import android.support.animation.DynamicAnimation
import android.support.animation.SpringAnimation
import android.support.animation.SpringForce
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allow.food4needy.R
import com.allow.food4needy.domain.UserRole
import kotlinx.android.synthetic.main.fragment_select_role.view.*
import javax.inject.Inject


class SelectRoleFragment @Inject constructor() : Fragment(), SelectRoleView {

    interface SelectRoleFragmentInteractionListener {
        fun showUserDetailsFragment(userRole: UserRole)
    }

    private var interactionListener: SelectRoleFragmentInteractionListener? = null

    companion object {
        fun newInstance() = SelectRoleFragment()
    }

    private val selectRolePresenter by lazy { SelectRolePresenterImpl(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_select_role, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.donor_layout.setOnClickListener { selectRolePresenter.setUpUserDetailsFor(UserRole.DONOR) }
        view.volunteer_layout.setOnClickListener { selectRolePresenter.setUpUserDetailsFor(UserRole.VOLUNTEER) }
        view.restaurant_layout.setOnClickListener { selectRolePresenter.setUpUserDetailsFor(UserRole.RESTAURANT) }

        view.role_text.apply {
            visibility = View.GONE
        }
        view.donor_item_card.apply {
            visibility = View.GONE
        }
        view.volunteer_item_card.apply {
            visibility = View.GONE
        }
        view.restaurant_item_card.apply {
            visibility = View.GONE
        }

        if (savedInstanceState == null) {
            view.select_role_layout.post {
                if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return@post
                view.role_text.run {
                    visibility = View.VISIBLE
                    translationY = -y
                    val bounceAnim = SpringAnimation(view.role_text, DynamicAnimation.TRANSLATION_Y)
                    val spring = SpringForce().apply {
                        finalPosition = 0f
                        stiffness = SpringForce.STIFFNESS_LOW
                        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                    }
                    bounceAnim.spring = spring
                    bounceAnim.start()
                }

                view.donor_item_card.run {
                    visibility = View.VISIBLE
                    scaleX = 0f
                    scaleY = 0f
                    animate()
                            .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                            .scaleX(1f)
                            .scaleY(1f)
                }

                view.volunteer_item_card.run {
                    visibility = View.VISIBLE
                    scaleX = 0f
                    scaleY = 0f
                    translationY = -(y - view.donor_item_card.y)
                    animate()
                            .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                }

                view.restaurant_item_card.run {
                    visibility = View.VISIBLE
                    scaleX = 0f
                    scaleY = 0f
                    translationY = -(y - view.donor_item_card.y)
                    animate()
                            .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                }
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        interactionListener = if (context is SelectRoleFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement SelectRoleFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        interactionListener = null
    }

    override fun showUserDetailsView(userRole: UserRole) {
        interactionListener?.showUserDetailsFragment(userRole)
    }
}
