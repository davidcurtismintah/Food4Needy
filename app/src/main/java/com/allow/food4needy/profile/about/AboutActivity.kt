package com.allow.food4needy.profile.about

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.app.TaskStackBuilder
import android.support.v7.widget.AppCompatTextView
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import com.allow.food4needy.R
import com.allow.food4needy.common.createLinkedText
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        terms_of_use.text = createLinkedText(this, getString(R.string.terms_reminder), 32, 44, getString(R.string.terms_and_conditions_url))
        terms_of_use.movementMethod = LinkMovementMethod.getInstance()
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
