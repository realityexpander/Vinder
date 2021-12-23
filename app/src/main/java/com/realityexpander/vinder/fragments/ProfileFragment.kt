package com.realityexpander.vinder.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.realityexpander.vinder.HostContextI
import com.realityexpander.vinder.R
import com.realityexpander.vinder.activities.VinderActivity
import com.realityexpander.vinder.databinding.FragmentProfileBinding
import com.realityexpander.vinder.interfaces.UpdateUiI
import com.realityexpander.vinder.models.User
import com.realityexpander.vinder.utils.*
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : BaseFragment(), UpdateUiI {

    private var _bind: FragmentProfileBinding? = null
    private val bind: FragmentProfileBinding
        get() = _bind!!

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid
    private val userDatabase = FirebaseDatabase.getInstance()
        .reference
        .child(DATA_USERS_COLLECTION)
        .child(userId!!)

    private var pickedImageUri: Uri? =
        null // uri to an image file on android device before updating status
    private var savedProfileImageUrl = "" // url saved in DB after user updates profile

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _bind = FragmentProfileBinding.inflate(inflater, container, false)
        return bind.root
    }

    @SuppressLint("ClickableViewAccessibility") // progressLayout event blocker
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.apply {
            // After process death, pass this System-created fragment to HostContext
            hostContextI?.onAndroidFragmentCreated(this@ProfileFragment)

            // not needed yet
            // onViewStateRestored(savedInstanceState)
        }

        bind.photoIv.setOnClickListener {
            startImagePickerActivity { imageUri ->
                pickedImageUri = imageUri

                // preview the status Image, only save upon user performing status update
                bind.photoIv.loadUrl(pickedImageUri.toString(), R.drawable.default_user)
            }
        }

        bind.progressLayout.setOnTouchListener{ _, _ -> true }
        bind.applyButton.setOnClickListener { onApply() }
        bind.signoutButton.setOnClickListener { (activity as HostContextI).onSignout() }

        onUpdateUI()
    }

    override fun onUpdateUI() {
        if (!isAdded) return

        bind.progressLayout.visibility = View.VISIBLE

        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                bind.progressLayout.visibility = View.GONE
            }

            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (isAdded) {
                    val user = userSnapshot.getValue(User::class.java)

                    bind.nameEt.setText(user?.username, TextView.BufferType.EDITABLE)
                    bind.emailEt.setText(user?.email, TextView.BufferType.EDITABLE)
                    bind.ageEt.setText(user?.age, TextView.BufferType.EDITABLE)
                    if (user?.gender == PREFERENCE_GENDER_MALE) {
                        bind.radioMan1.isChecked = true
                    }
                    if (user?.gender == PREFERENCE_GENDER_FEMALE) {
                        bind.radioWoman1.isChecked = true
                    }
                    if (user?.preferredGender == PREFERENCE_GENDER_MALE) {
                        bind.radioMan2.isChecked = true
                    }
                    if (user?.preferredGender == PREFERENCE_GENDER_FEMALE) {
                        bind.radioWoman2.isChecked = true
                    }
                    if (!user?.profileImageUrl.isNullOrEmpty()) {
                        bind.photoIv.loadUrl(user?.profileImageUrl!!)

                        savedProfileImageUrl = user.profileImageUrl
                        pickedImageUri = null // Cancel the previous picked image (if there is one)
                    }
                    bind.progressLayout.visibility = View.GONE
                }
            }
        })
    }

    private fun onApply() {
        if (bind.nameEt.text.toString().isEmpty() ||
            bind.emailEt.text.toString().isEmpty() ||
            bind.genderGroup.checkedRadioButtonId == -1 ||
            bind.preferredGenderGroup.checkedRadioButtonId == -1
        ) {
            Toast.makeText(
                context,
                getString(R.string.error_profile_incomplete),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val name = bind.nameEt.text.toString()
            val age = bind.ageEt.text.toString()
            val email = bind.emailEt.text.toString()
            val gender =
                if (bind.radioMan1.isChecked) PREFERENCE_GENDER_MALE
                else PREFERENCE_GENDER_FEMALE
            val preferredGender =
                if (bind.radioMan2.isChecked) PREFERENCE_GENDER_MALE
                else PREFERENCE_GENDER_FEMALE

            userDatabase.child(DATA_USER_USERNAME).setValue(name)
            userDatabase.child(DATA_USER_AGE).setValue(age)
            userDatabase.child(DATA_USER_EMAIL).setValue(email)
            userDatabase.child(DATA_USER_GENDER).setValue(gender)
            userDatabase.child(DATA_USER_GENDER_PREFERENCE).setValue(preferredGender)

            // If the user picked a new profile, save it
            storePickedProfileImage()

            // Nav to Swipe via the activity
            (activity as HostContextI).profileComplete()
        }
    }

    // Setup image picker (must be setup before onResume/onStart)
    private var imagePickerResultCallback: (uri: Uri) -> Unit = {}
    private val imagePickerForResultLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                imagePickerResultCallback(uri)
            }
        }

    private fun startImagePickerActivity(imagePickerResultCallback: (uri: Uri) -> Unit) {
        this.imagePickerResultCallback = imagePickerResultCallback
        imagePickerForResultLauncher.launch(arrayOf("image/*")) // Launch Image Picker
    }

    private fun storePickedProfileImage() {
        if (pickedImageUri != null && userId != null) {
            val filePath = FirebaseStorage.getInstance()
                .reference
                .child("profileImage")
                .child(userId)
            var bitmap: Bitmap? = null

            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                    activity?.application?.contentResolver,
                    pickedImageUri
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 20, baos)
            val data = baos.toByteArray()

            val uploadTask = filePath.putBytes(data)
            uploadTask.addOnFailureListener { e -> e.printStackTrace() }
            uploadTask.addOnSuccessListener { taskSnapshot ->
                filePath.downloadUrl
                    .addOnSuccessListener { uri ->
                        savedProfileImageUrl = uri.toString()

                        userDatabase.child(DATA_USER_PROFILE_IMAGE_URL)
                            .setValue(savedProfileImageUrl)
                        bind.photoIv.loadUrl(savedProfileImageUrl)
                        pickedImageUri = null
                    }
                    .addOnFailureListener { e -> e.printStackTrace() }
            }
        }
    }
}