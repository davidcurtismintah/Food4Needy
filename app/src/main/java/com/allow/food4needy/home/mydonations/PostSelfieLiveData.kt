package com.allow.food4needy.home.mydonations

import android.arch.lifecycle.LiveData
import android.graphics.Bitmap
import android.util.Log
import com.allow.food4needy.BuildConfig
import com.allow.food4needy.common.Event
import com.allow.food4needy.domain.Response
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.*


class PostSelfieLiveData : LiveData<Event<Response<String, String>>>() {

    fun makePost(bitmap: Bitmap) {
        value = Event(Response.loading())

        val uuid = UUID.randomUUID().toString()
        val imageRef = FirebaseStorage.getInstance().getReference(uuid)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data)
                .addOnSuccessListener { taskSnapshot ->
                    if (BuildConfig.DEBUG) {
                        Timber.d("postSelfie:onSuccess:${taskSnapshot.metadata?.reference?.path}")
                    }
                    value = Event(Response.success("Success"))
                }
                .addOnFailureListener { e ->
                    Timber.w(e, "postSelfie:onError")
                    value = Event(Response.error("$e"))
                }
    }
}