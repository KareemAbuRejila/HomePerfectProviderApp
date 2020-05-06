package com.codeshot.home_perfect_provider.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import cc.cloudist.acplibrary.ACProgressBaseDialog

import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.adapters.AdditionsAdapter
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.common.Common.PROVIDERS_REF
import com.codeshot.home_perfect_provider.common.Common.SERVICES_REF
import com.codeshot.home_perfect_provider.databinding.FragmentProfileBinding
import com.codeshot.home_perfect_provider.databinding.FragmentRequestsBinding
import com.codeshot.home_perfect_provider.models.Addition
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.models.Service
import com.codeshot.home_perfect_provider.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import io.ghyeok.stickyswitch.widget.StickySwitch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProfileFragment : Fragment() {
    private lateinit var fragmentProfileBinding: FragmentProfileBinding

    private val Gellary_Key = 7000
    var update = false
    private var imageUri: Uri? = null
    var providerImgUrl: String? = null
    var selectedService: Service? = null
    var additions = ArrayList<Addition>()
    val additionsAdapter = AdditionsAdapter()
    private lateinit var loadingDialog: ACProgressBaseDialog

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return fragmentProfileBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.connected.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) fragmentProfileBinding.btnSaveDialog.visibility = VISIBLE
            else fragmentProfileBinding.btnSaveDialog.visibility = GONE
        })
        loadingDialog = Common.LOADING_DIALOG(requireContext())
//        fragmentProfileBinding.ccp.registerPhoneNumberTextView(fragmentProfileBinding.edtPhone)
//        fragmentProfileBinding.fullDialogClose.setOnClickListener { v: View? ->
//            super.dismiss()
//        }
        fragmentProfileBinding.edtUserNameDialog.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (hasFocus)
                    fragmentProfileBinding.btnSaveDialog.visibility = VISIBLE
            }

        fragmentProfileBinding.btnSaveDialog
            .setOnClickListener {
                if (!update)
                    saveProviderData()
                else
                    updateProviderData()

            }
        fragmentProfileBinding.imgUserImage.setOnClickListener { showImageInDialog() }
        // Source can be CACHE, SERVER, or DEFAULT.
        val source = Source.CACHE
        PROVIDERS_REF.document(CURRENT_USER_KEY)
            .get(source).addOnSuccessListener { document ->
                if (!document!!.exists()) {
//                    fragmentProfileBinding.fullDialogClose.visibility = View.GONE
                }
            }
        fragmentProfileBinding.btnAddAddition.setOnClickListener {
            addAddition()
        }

        setServiceSpinner()
        setAdditions()
        getCurrentDate()
        setUpGender()
        getProviderData()
    }


    val data = HashMap<String, Any>()
    val address = HashMap<String, String>()

    private fun setUpView() {
        fragmentProfileBinding.edtUserNameDialog.addTextChangedListener {
            data["userName"] = it.toString()
        }
        fragmentProfileBinding.edtPerHour.addTextChangedListener {
            data["perHour"] = it.toString().toDouble()
        }
        fragmentProfileBinding.tvDate.addTextChangedListener {
            data["birthDay"] = it.toString()
        }
        fragmentProfileBinding.tvAge.addTextChangedListener {
            data["age"] = it.toString().toInt()
        }
        fragmentProfileBinding.genderSwitch
            .onSelectedChangeListener = object : StickySwitch.OnSelectedChangeListener {
            override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                data["gender"] = text
            }
        }
        fragmentProfileBinding.edtCity.addTextChangedListener {
            address["city"] = it.toString()
        }
        fragmentProfileBinding.edtHome.addTextChangedListener {
            address["home"] = it.toString()
        }
        fragmentProfileBinding.edtStreet.addTextChangedListener {
            address["street"] = it.toString()
        }
        fragmentProfileBinding.edtFlaor.addTextChangedListener {
            address["level"] = it.toString()
        }
    }

    private fun updateProviderData() {
        loadingDialog.show()
        val providerRef = PROVIDERS_REF.document(CURRENT_USER_KEY)
        providerRef.update(data).addOnSuccessListener {
            providerRef.update("address", address).addOnSuccessListener {
                providerRef.update("additions", additions).addOnSuccessListener {
                    data.clear()
                    address.clear()
                    loadingDialog.dismiss()
                }.addOnFailureListener { loadingDialog.dismiss() }
            }.addOnFailureListener { loadingDialog.dismiss() }

        }


    }

    private fun setAdditions() {
        additionsAdapter.setType(0)
        fragmentProfileBinding.additionsAdapter = additionsAdapter
        fragmentProfileBinding.edtAdditionName.setOnEditorActionListener() { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addAddition()
                true
            } else
                false
        }
        fragmentProfileBinding.edtAdditionPrice.setOnEditorActionListener() { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addAddition()
                true
            } else
                false
        }
    }

    var gender = "Male"
    private fun setUpGender() {
        fragmentProfileBinding.genderSwitch.setDirection(StickySwitch.Direction.LEFT)
        fragmentProfileBinding.genderSwitch.onSelectedChangeListener =
            object : StickySwitch.OnSelectedChangeListener {
                override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                    gender = text
                }
            }

    }

    var birthay = ""
    var age: Int = 0
    private fun getCurrentDate() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMM YYYY", Locale.getDefault()).format(calendar.time)
        fragmentProfileBinding.tvDate.text = dateFormat.toString()

        fragmentProfileBinding.tvDate.setOnClickListener {
            val calendarDialog = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, month)
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    birthay = SimpleDateFormat("dd MMM YYYY", Locale.getDefault()).format(c.time)
                    fragmentProfileBinding.tvDate.text = birthay
                    fragmentProfileBinding.tvAge.text = calculateAge(c.timeInMillis).toString()

                },
                Calendar.YEAR,
                Calendar.MONTH,
                Calendar.DAY_OF_MONTH
            )
            calendarDialog.datePicker.maxDate = Date().time
            calendarDialog.show()
        }


    }

    private fun calculateAge(timeInMillis: Long): Int {
        val dob = Calendar.getInstance()
        dob.timeInMillis = timeInMillis
        val today = Calendar.getInstance()
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.DAY_OF_MONTH] < dob[Calendar.DAY_OF_MONTH]) {
            age--
        }
        this.age = age
        return age
    }

    private fun uploadProviderImage(localImageURI: Uri) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Set Profile Image")
        progressDialog.setMessage("Please wait....")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val imagesStorageRef = FirebaseStorage.getInstance().reference.child("Providers Images")
        val filePath = imagesStorageRef.child(Common.CURRENT_USER_PHONE + ".jpg")
        val uploadTask = filePath.putFile(localImageURI)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val parsente = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.bytesTransferred
            progressDialog.setMessage(parsente.toString() + " %")
        }
            .continueWithTask {
                filePath.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()
                    PROVIDERS_REF.document(CURRENT_USER_KEY)
                        .update("personalImageUri", downloadUri)
                        .addOnSuccessListener {
                            Picasso.get().load(downloadUri)
                                .into(fragmentProfileBinding.imgUserImage)
                            progressDialog.dismiss()
                        }
                } else {
                    val errorMsg = task.exception.toString()
                    Toast.makeText(context, "Error $errorMsg", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss()
                }
            }

    }

    private fun saveProviderData() {
        if (fragmentProfileBinding.edtUserNameDialog.text!!.isNotEmpty()) {
            if (selectedService != null) {
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle("Set Profile Image")
                progressDialog.setMessage("Please wait....")
                progressDialog.setCancelable(false)
                progressDialog.show()
                val imagesStorageRef =
                    FirebaseStorage.getInstance().reference.child("Providers Images")
                val filePath = imagesStorageRef.child(Common.CURRENT_USER_PHONE + ".jpg")
                val uploadTask = filePath.putFile(imageUri!!)
                val address = HashMap<String, String>()

                uploadTask.addOnProgressListener { taskSnapshot ->
                    var persante = 0.0
                    persante =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.bytesTransferred
                    progressDialog.setMessage(persante.toString() + " %")
                }
                    .continueWithTask {
                        filePath.downloadUrl
                    }
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result.toString()
                            val userName =
                                fragmentProfileBinding.edtUserNameDialog.text.toString()
                            val service = selectedService!!.id
                            val phoneNo = FirebaseAuth.getInstance().currentUser!!.phoneNumber

                            val age = fragmentProfileBinding.tvAge.text.toString().toInt()
                            val bod = fragmentProfileBinding.tvDate.text.toString()
                            val perHour =
                                fragmentProfileBinding.edtPerHour.text.toString().toDouble()

                            val city = fragmentProfileBinding.edtCity.text.toString()
                            val street = fragmentProfileBinding.edtStreet.text.toString()
                            val home = fragmentProfileBinding.edtHome.text.toString()
                            val level = fragmentProfileBinding.edtFlaor.text.toString()
                            address["city"] = city
                            address["street"] = street
                            address["home"] = home
                            address["level"] = level

                            val provider = Provider(
                                personalImageUri = downloadUri,
                                userName = userName,
                                phone = phoneNo,
                                birthDay = bod,
                                gender = gender,
                                age = age,
                                serviceId = service,
                                serviceType = service,
                                perHour = perHour,
                                address = address,
                                additions = additions
                            )
                            PROVIDERS_REF.document(Common.CURRENT_USER_KEY)
                                .set(provider, SetOptions.merge()).addOnSuccessListener {
                                    SERVICES_REF.document(selectedService!!.id!!)
                                        .update(
                                            "providers",
                                            FieldValue.arrayUnion(CURRENT_USER_KEY)
                                        )
                                        .addOnSuccessListener {
                                            progressDialog.dismiss()
//                                            super.dismiss()
                                        }
                                }
                        } else {
                            // Handle failures
                            // ...
                            val errotMsg = task.exception.toString()
                            Toast.makeText(context, "Error " + errotMsg, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss()
                        }
                    }
            } else Toast.makeText(context, "Service required", Toast.LENGTH_SHORT).show()


        } else fragmentProfileBinding.edtUserNameDialog.error = "Please Enter Your UserName"
    }

    private fun getProviderData() {
        if (FirebaseAuth.getInstance().currentUser!!.phoneNumber != ""
            && FirebaseAuth.getInstance().currentUser!!.phoneNumber != null
        ) {
            fragmentProfileBinding.edtUserPhoneDialog.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
            fragmentProfileBinding.edtUserPhoneDialog.isEnabled = false
        }
        loadingDialog.show()
        PROVIDERS_REF.document(Common.CURRENT_USER_KEY)
            .get().addOnSuccessListener {
                if (it.exists()) {
                    update = true
                    fragmentProfileBinding.btnSaveDialog.setText(R.string.update)
                    val provider = it.toObject(Provider::class.java)
                    providerImgUrl = provider!!.personalImageUri
                    additions = provider.additions
                    fragmentProfileBinding.provider = provider
                    fragmentProfileBinding.edtUserPhoneDialog.isEnabled = false
                    additionsAdapter.setList(additions)
                    fragmentProfileBinding.serviceSpinner.visibility = GONE
                    selectedService = Service(provider.serviceId, provider.serviceType)
                    if (provider.gender == "Male")
                        fragmentProfileBinding.genderSwitch.setDirection(direction = StickySwitch.Direction.LEFT)
                    else
                        fragmentProfileBinding.genderSwitch.setDirection(direction = StickySwitch.Direction.RIGHT)
                    fragmentProfileBinding.btnSaveDialog.visibility = VISIBLE
                    loadingDialog.dismiss()
                    setUpView()


                } else {
                    loadingDialog.dismiss()
                    return@addOnSuccessListener
                }

            }
    }

    private fun showImageInDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose!")
        if (imageUri != null || providerImgUrl != null) {
            builder.setPositiveButton("View") { dialog, which ->
                if (imageUri != null) {
                    showImageViewer(imageUri.toString())
                } else if (providerImgUrl != null) {
                    showImageViewer(providerImgUrl!!)

                } else Toast.makeText(context, "Image Not Founded", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Edit") { dialog, which ->
                chooseImage()
            }
        } else {
            builder.setTitle("Choose Personal Image").setMessage("please Select you Image !")
                .setPositiveButton("Add") { dialog, which ->
                    chooseImage()
                }
        }

        builder.show()
    }

    private fun showImageViewer(imageUri: String) {
        val imageDialog =
            AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val imageViewLayout: View = inflater.inflate(R.layout.item_imagesview, null)
        imageDialog.setView(imageViewLayout)
        val imageViewDialog =
            imageViewLayout.findViewById<ImageView>(R.id.imageViewer)
        Picasso.get().load(imageUri).into(imageViewDialog)
        imageDialog.show()
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select your Pic"),
            Gellary_Key
        )
    }

    private fun setServiceSpinner() {
        val spinner = fragmentProfileBinding.serviceSpinner
        SERVICES_REF.get().addOnSuccessListener { document ->
            val servicesList = document.toObjects(Service::class.java)
            val services = ArrayList<Service>()
            services.add(Service("", "Choose Service", ""))
            servicesList.forEach {
                services.add(it)
            }
            val servicesAdapter =
                ArrayAdapter<Service>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    services
                )
            servicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = servicesAdapter
            spinner.onItemSelectedListener = ServiceSpinnerListener()


        }

    }

    private fun addAddition() {
        if (fragmentProfileBinding.edtAdditionName.text!!.isNotEmpty() &&
            fragmentProfileBinding.edtAdditionPrice.text!!.isNotEmpty()
        ) {

            val name = fragmentProfileBinding.edtAdditionName.text.toString()
            val price = fragmentProfileBinding.edtAdditionPrice.text.toString().toDouble()
            val addition = Addition(name, price)
            additions.add(addition)
            additionsAdapter.setList(additions)
            fragmentProfileBinding.edtAdditionName.setText("")
            fragmentProfileBinding.edtAdditionPrice.setText("")

        } else {
            if (fragmentProfileBinding.edtAdditionName.text!!.isEmpty()) {
                fragmentProfileBinding.edtAdditionName.error = "reqired"
            }
            if (fragmentProfileBinding.edtAdditionPrice.text!!.isEmpty()) {
                fragmentProfileBinding.edtAdditionPrice.error = "reqired"
            }
        }
    }

    inner class ServiceSpinnerListener : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            if (parent!!.selectedItemPosition == 0)
                Toast.makeText(parent.context, "Service required", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(parent.context, "Service required", Toast.LENGTH_SHORT).show()

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (parent!!.selectedItemPosition != 0) {
                val service = parent.selectedItem as Service
                selectedService = service
                Toast.makeText(parent.context, selectedService!!.id, Toast.LENGTH_LONG).show()

            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Gellary_Key && resultCode == Activity.RESULT_OK && data != null) {
            if (!update) {
                imageUri = data.data!!
                Picasso.get().load(imageUri).into(fragmentProfileBinding.imgUserImage)
            } else {
                val localImageURI = data.data!!
                uploadProviderImage(localImageURI)
            }
        }
    }

}
