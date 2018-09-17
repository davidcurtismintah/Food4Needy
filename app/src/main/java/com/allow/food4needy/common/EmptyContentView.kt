package com.allow.food4needy.common

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.allow.food4needy.R

class EmptyContentView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    companion object {
        const val NO_LABEL = 0
        const val NO_IMAGE = 0
    }

    private val mImageView: ImageView
    private val mDescriptionView: TextView
    private val mActionView: TextView

    private var mOnActionButtonClickedListener: (()->Unit)? = null

    val isShowingContent: Boolean
        get() = (mImageView.visibility == View.VISIBLE
                || mDescriptionView.visibility == View.VISIBLE
                || mActionView.visibility == View.VISIBLE)

//    /** Listener to call when action button is clicked.  */
//    interface OnEmptyViewActionButtonClickedListener {
//        fun onEmptyViewActionButtonClicked()
//    }

    init {

        inflateLayout()

        // Don't let touches fall through the empty view.
        isClickable = true
        mImageView = findViewById(R.id.emptyListViewImage)
        mDescriptionView = findViewById(R.id.emptyListViewMessage)
        mActionView = findViewById(R.id.emptyListViewAction)
        mActionView.setOnClickListener(this)
    }

    fun setDescription(@StringRes resourceId: Int) {
        if (resourceId == NO_LABEL) {
            mDescriptionView.text = null
            mDescriptionView.visibility = View.GONE
        } else {
            mDescriptionView.setText(resourceId)
            mDescriptionView.visibility = View.VISIBLE
        }
    }

    fun setImage(@DrawableRes resourceId: Int) {
        if (resourceId == NO_IMAGE) {
            mImageView.setImageDrawable(null)
            mImageView.visibility = View.GONE
        } else {
            mImageView.setImageResource(resourceId)
            mImageView.visibility = View.VISIBLE
        }
    }

    fun setActionLabel(@StringRes resourceId: Int) {
        if (resourceId == NO_LABEL) {
            mActionView.text = null
            mActionView.visibility = View.GONE
        } else {
            mActionView.setText(resourceId)
            mActionView.visibility = View.VISIBLE
        }
    }

    fun setActionClickedListener(listener: (()->Unit)?) {
        mOnActionButtonClickedListener = listener
    }

    override fun onClick(v: View) {
        mOnActionButtonClickedListener?.invoke()
    }

    protected fun inflateLayout() {
        orientation = LinearLayout.VERTICAL
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_empty_content, this)
    }

}
