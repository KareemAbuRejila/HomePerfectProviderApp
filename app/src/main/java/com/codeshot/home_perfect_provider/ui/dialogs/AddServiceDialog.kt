package com.codeshot.home_perfect_provider.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.codeshot.home_perfect_provider.common.Common.SERVICES_REF
import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.databinding.DialogAddServiceBinding
import com.codeshot.home_perfect_provider.models.Service
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class AddServiceDialog : DialogFragment() {
    private lateinit var dialogAddServiceBinding: DialogAddServiceBinding

    private val Gellery_Key = 7000
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogAddServiceBinding = DialogAddServiceBinding.inflate(inflater, container, false)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialogAddServiceBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialogAddServiceBinding.imgService.setOnClickListener {
            chooseImage()
        }
        dialogAddServiceBinding.btnSaveService.setOnClickListener {
            saveServiceData()
        }

    }

    private fun saveServiceData() {
        if (dialogAddServiceBinding.edtService.text!!.isNotEmpty()) {

            val serviceName=dialogAddServiceBinding.edtService.text.toString()

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Set Service Image")
            progressDialog.setMessage("Please wait....")
            progressDialog.setCancelable(false)
            progressDialog.show()
            val imagesStorageRef = FirebaseStorage.getInstance().reference.child("Services Images")
            val filePath =
                imagesStorageRef.child(serviceName + ".jpg")
            val uploadTask = filePath.putFile(imageUri!!)
            uploadTask.addOnProgressListener { taskSnapshot ->
                    var persante = 0.0
                    persante =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.bytesTransferred
                    progressDialog.setMessage("$persante %")
                }
                .continueWithTask {
                    filePath.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        val service = Service(
                            serviceName,
                            serviceName,
                            downloadUri
                        )
                        SERVICES_REF.document(serviceName)
                            .set(service).addOnSuccessListener {
                                progressDialog.dismiss()
                                super.dismiss()
                            }
                    } else {
                        // Handle failures
                        // ...
                        val errotMsg = task.exception.toString()
                        Toast.makeText(context, "Error " + errotMsg, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss()
                    }
                }

        } else dialogAddServiceBinding.edtService.error = "Please Enter Your UserName"
    }


    private fun showImageInDialog() {
        val builder = AlertDialog.Builder(context,android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setPositiveButton("View") { dialog, which ->
            showImageViewer()
        }.setNegativeButton("Edit") { dialog, which ->
            chooseImage()
        }
        builder.show()
    }

    private fun showImageViewer() {
        if (imageUri != null) {
            val imageDialog =
                AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val imageViewLayout: View = inflater.inflate(R.layout.item_imagesview, null)
            imageDialog.setView(imageViewLayout)
            val imageViewDialog =
                imageViewLayout.findViewById<ImageView>(R.id.imageViewer)
            Picasso.get().load(imageUri).into(imageViewDialog)
            imageDialog.show()
        } else Toast.makeText(context, "Image Not Founded", Toast.LENGTH_SHORT).show()

    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select your Pic"),
            Gellery_Key
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Gellery_Key && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data!!
            Picasso.get().load(imageUri).into(dialogAddServiceBinding.imgService)
        }
    }
}