package com.codeshot.home_perfect_provider.ui.dialogs


import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.common.Common.PROVIDERS_REF
import com.codeshot.home_perfect_provider.common.Common.SERVICES_REF
import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.adapters.AdditionsAdapter
import com.codeshot.home_perfect_provider.databinding.DialogUpdateUserInfoBinding
import com.codeshot.home_perfect_provider.models.Addition
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.models.Service
import com.codeshot.home_perfect_provider.ui.home.HomeActivity
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

/**
 * A simple [Fragment] subclass.
 */
class DialogUpdateUserInfo : DialogFragment {
    private lateinit var dialogUpdateUserInfoBinding: DialogUpdateUserInfoBinding
    private val Gellary_Key = 7000
    var update = false
    private var imageUri: Uri? = null
    var providerImgUrl: String? = null
    var selectedService: Service? = null
    var additions = ArrayList<Addition>()
    val additionsAdapter = AdditionsAdapter()

    constructor() : super()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullDialogTheme)
        // Access a Cloud Firestore instance from your Activity
        super.setCancelable(false)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val infalter2 = LayoutInflater.from(ContextThemeWrapper(activity, R.style.FullDialogTheme))
        dialogUpdateUserInfoBinding =
            DialogUpdateUserInfoBinding.inflate(infalter2, container, false)
        return dialogUpdateUserInfoBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialogUpdateUserInfoBinding.ccp.registerPhoneNumberTextView(dialogUpdateUserInfoBinding.edtPhone)

        dialogUpdateUserInfoBinding.fullDialogClose.setOnClickListener { v: View? ->
            super.dismiss()
        }
        dialogUpdateUserInfoBinding.edtUserNameDialog.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (hasFocus)
                    dialogUpdateUserInfoBinding.btnSaveDialog.visibility = VISIBLE
            }

        dialogUpdateUserInfoBinding.btnSaveDialog
            .setOnClickListener {
                if (!update)
                    saveProviderData()
                else
                    updateProviderData()

            }
        dialogUpdateUserInfoBinding.imgUserImage.setOnClickListener { showImageInDialog() }
        // Source can be CACHE, SERVER, or DEFAULT.
        val source = Source.CACHE
        PROVIDERS_REF.document(CURRENT_USER_KEY)
            .get(source).addOnSuccessListener { document ->
                if (!document!!.exists()) {
                    dialogUpdateUserInfoBinding.fullDialogClose.visibility = View.GONE
                }
            }
        dialogUpdateUserInfoBinding.btnAddAddition.setOnClickListener {
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
        dialogUpdateUserInfoBinding.edtUserNameDialog.addTextChangedListener {
            data["userName"] = it.toString()
        }
        dialogUpdateUserInfoBinding.edtPerHour.addTextChangedListener {
            data["perHour"] = it.toString().toDouble()
        }
        dialogUpdateUserInfoBinding.tvDate.addTextChangedListener {
            data["birthDay"] = it.toString()
        }
        dialogUpdateUserInfoBinding.tvAge.addTextChangedListener {
            data["age"] = it.toString().toInt()
        }
        dialogUpdateUserInfoBinding.genderSwitch
            .onSelectedChangeListener = object : StickySwitch.OnSelectedChangeListener {
            override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                data["gender"] = text
            }
        }
        dialogUpdateUserInfoBinding.edtCity.addTextChangedListener {
            address["city"] = it.toString()
        }
        dialogUpdateUserInfoBinding.edtHome.addTextChangedListener {
            address["home"] = it.toString()
        }
        dialogUpdateUserInfoBinding.edtStreet.addTextChangedListener {
            address["street"] = it.toString()
        }
        dialogUpdateUserInfoBinding.edtFlaor.addTextChangedListener {
            address["level"] = it.toString()
        }
    }

    private fun updateProviderData() {
        val providerRef = PROVIDERS_REF.document(CURRENT_USER_KEY)
        providerRef.update(data).addOnSuccessListener {
            providerRef.update("address", address).addOnSuccessListener {
                providerRef.update("additions", additions).addOnFailureListener {
                    Toast.makeText(activity, "Sucess", Toast.LENGTH_SHORT).show()
                    data.clear()
                    address.clear()
                }
            }

        }


    }

    private fun setAdditions() {
        additionsAdapter.setType(0)
        dialogUpdateUserInfoBinding.additionsAdapter = additionsAdapter
        dialogUpdateUserInfoBinding.edtAdditionName.setOnEditorActionListener() { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addAddition()
                true
            } else
                false
        }
        dialogUpdateUserInfoBinding.edtAdditionPrice.setOnEditorActionListener() { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addAddition()
                true
            } else
                false
        }
    }

    var gender = ""
    private fun setUpGender() {
        dialogUpdateUserInfoBinding.genderSwitch.setDirection(StickySwitch.Direction.LEFT)
        dialogUpdateUserInfoBinding.genderSwitch.onSelectedChangeListener =
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
        dialogUpdateUserInfoBinding.tvDate.text = dateFormat.toString()

        dialogUpdateUserInfoBinding.tvDate.setOnClickListener {
            val calendarDialog = DatePickerDialog(
                dialog!!.context,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, month)
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    birthay = SimpleDateFormat("dd MMM YYYY", Locale.getDefault()).format(c.time)
                    dialogUpdateUserInfoBinding.tvDate.text = birthay
                    dialogUpdateUserInfoBinding.tvAge.text = calculateAge(c.timeInMillis).toString()

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
                                .into(dialogUpdateUserInfoBinding.imgUserImage)
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
        if (dialogUpdateUserInfoBinding.edtUserNameDialog.text!!.isNotEmpty()) {
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
                                dialogUpdateUserInfoBinding.edtUserNameDialog.text.toString()
                            val service = selectedService!!.id
                            val phoneNo = FirebaseAuth.getInstance().currentUser!!.phoneNumber

                            val age = dialogUpdateUserInfoBinding.tvAge.text.toString().toInt()
                            val bod = dialogUpdateUserInfoBinding.tvDate.text.toString()
                            val perHour =
                                dialogUpdateUserInfoBinding.edtPerHour.text.toString().toDouble()

                            val city = dialogUpdateUserInfoBinding.edtCity.text.toString()
                            val street = dialogUpdateUserInfoBinding.edtStreet.text.toString()
                            val home = dialogUpdateUserInfoBinding.edtHome.text.toString()
                            val level = dialogUpdateUserInfoBinding.edtFlaor.text.toString()
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
                                            super.dismiss()
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


        } else dialogUpdateUserInfoBinding.edtUserNameDialog.error = "Please Enter Your UserName"
    }

    private fun getProviderData() {
        val acProgressBaseDialog = ACProgressFlower.Builder(context)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Please Wait ....!")
            .fadeColor(Color.DKGRAY).build()
        acProgressBaseDialog.show()
        PROVIDERS_REF.document(Common.CURRENT_USER_KEY)
            .get().addOnSuccessListener {
                if (it.exists()) {
                    update = true
                    dialogUpdateUserInfoBinding.btnSaveDialog.setText(R.string.update)
                    val provider = it.toObject(Provider::class.java)
                    providerImgUrl = provider!!.personalImageUri
                    additions = provider.additions
                    dialogUpdateUserInfoBinding.provider = provider
//                    Picasso.get().load(providerImgUrl).placeholder(R.drawable.ic_person_black_24dp).into(dialogUpdateUserInfoBinding.imgUserImage)
//                    dialogUpdateUserInfoBinding.edtPerHour.setText(provider.perHour.toString())
//                    dialogUpdateUserInfoBinding.edtUserNameDialog.setText(provider.userName.toString())
//                    dialogUpdateUserInfoBinding.edtUserPhoneDialog.setText(provider.phone.toString())
                    dialogUpdateUserInfoBinding.edtUserPhoneDialog.isEnabled = false
//                    dialogUpdateUserInfoBinding.tvDate.setText(provider.birthDay.toString())
//                    dialogUpdateUserInfoBinding.tvAge.setText(provider.age.toString())
//                    dialogUpdateUserInfoBinding.edtCity.setText(provider.address["city"])
//                    dialogUpdateUserInfoBinding.edtStreet.setText(provider.address["street"])
//                    dialogUpdateUserInfoBinding.edtHome.setText(provider.address["home"])
//                    dialogUpdateUserInfoBinding.edtFlaor.setText(provider.address["level"])
                    additionsAdapter.setList(additions)
                    dialogUpdateUserInfoBinding.serviceSpinner.visibility = View.GONE
                    selectedService = Service(provider.serviceId, provider.serviceType)
                    if (provider.gender == "Male")
                        dialogUpdateUserInfoBinding.genderSwitch.setDirection(direction = StickySwitch.Direction.LEFT)
                    else
                        dialogUpdateUserInfoBinding.genderSwitch.setDirection(direction = StickySwitch.Direction.RIGHT)
                    dialogUpdateUserInfoBinding.btnSaveDialog.visibility = VISIBLE
                    acProgressBaseDialog.dismiss()
                    setUpView()


                } else {
                    acProgressBaseDialog.dismiss()
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
        val spinner = dialogUpdateUserInfoBinding.serviceSpinner
        SERVICES_REF.get().addOnSuccessListener { document ->
            val servicesList = document.toObjects(Service::class.java)
            val services = ArrayList<Service>()
            services.add(Service("", "Choose Service", ""))
            servicesList.forEach {
                services.add(it)
            }
            val servicesAdapter =
                ArrayAdapter<Service>(context!!, android.R.layout.simple_spinner_item, services)
            servicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = servicesAdapter
            spinner.onItemSelectedListener = ServiceSpinnerListener()


        }

    }

    private fun addAddition() {
        if (dialogUpdateUserInfoBinding.edtAdditionName.text!!.isNotEmpty() &&
            dialogUpdateUserInfoBinding.edtAdditionPrice.text!!.isNotEmpty()
        ) {

            val name = dialogUpdateUserInfoBinding.edtAdditionName.text.toString()
            val price = dialogUpdateUserInfoBinding.edtAdditionPrice.text.toString().toDouble()
            val addition = Addition(name, price)
            additions.add(addition)
            additionsAdapter.setList(additions)
            dialogUpdateUserInfoBinding.edtAdditionName.setText("")
            dialogUpdateUserInfoBinding.edtAdditionPrice.setText("")

        } else {
            if (dialogUpdateUserInfoBinding.edtAdditionName.text!!.isEmpty()) {
                dialogUpdateUserInfoBinding.edtAdditionName.error = "reqired"
            }
            if (dialogUpdateUserInfoBinding.edtAdditionPrice.text!!.isEmpty()) {
                dialogUpdateUserInfoBinding.edtAdditionPrice.error = "reqired"
            }
        }
    }

    inner class ServiceSpinnerListener : OnItemSelectedListener {
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
                Picasso.get().load(imageUri).into(dialogUpdateUserInfoBinding.imgUserImage)
            } else {
                val localImageURI = data.data!!
                uploadProviderImage(localImageURI)
            }
        }
    }

    override fun onDetach() {
        val parent = activity as HomeActivity
        parent.checkProviderData()
        super.onDetach()
    }
}
