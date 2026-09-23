package com.example.geonote.ui.add

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.geonote.data.database.AppDatabase
import com.example.geonote.data.repository.TravelRepository
import com.example.geonote.data.api.RetrofitClient
import com.example.geonote.databinding.ActivityAddEntryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddEntryActivity : AppCompatActivity() {

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = applicationContext.cacheDir
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private lateinit var binding: ActivityAddEntryBinding
    private val viewModel: AddEntryViewModel by viewModels {
        val db = AppDatabase.getDatabase(application)
        val repo = TravelRepository(db.travelEntryDao(), RetrofitClient.openMeteoApi, RetrofitClient.nominatimApi)

        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                AddEntryViewModel(repo, RetrofitClient.openMeteoApi, RetrofitClient.nominatimApi) as T
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedDate: Long = System.currentTimeMillis()
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var imageUri: String? = null
    private var photoUri: Uri? = null

    // Gestion des résultats (Activity Result API moderne)
    // Dans takePicture callback
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            // Vérifier que le fichier existe
            val contentResolver = applicationContext.contentResolver
            try {
                val inputStream = contentResolver.openInputStream(photoUri!!)
                inputStream?.close() // Si on peut l'ouvrir, c'est bon

                binding.ivPreview.setImageURI(photoUri)
                binding.ivPreview.visibility = View.VISIBLE
                imageUri = photoUri.toString()
                Toast.makeText(this, "📷 Photo prise", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Échec photo", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it.toString()
            binding.ivPreview.load(it) { crossfade(true) }
            binding.ivPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupUI()
        observeViewModel()
    }

    private fun showCameraOptions() {
        val items = arrayOf("📷 Prendre une photo", "🖼 Choisir depuis la galerie")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Source de l'image")
            .setItems(items) { _, which ->
                if (which == 0) checkCameraAndLaunch() else pickImage.launch("image/*")
            }
            .show()
    }

    private fun checkCameraAndLaunch() {
        when {
            // Android 13+ : pas de permission CAMERA requise pour TakePicture()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                launchCamera()
            }
            // Android 6-12 : vérifier permission runtime
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            // Demander la permission
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        // Créer un fichier temporaire dans le cache de l'app
        val photoFile = try {
            createImageFile()
        } catch (e: IOException) {
            Toast.makeText(this, "Erreur création fichier: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertir en URI via FileProvider → stocker dans une val locale immuable
        val uriForCamera = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )

        // Mettre à jour la propriété de classe pour usage ultérieur
        photoUri = uriForCamera

        // Lancer la caméra avec la variable locale (smart cast autorisé)
        takePicture.launch(uriForCamera)
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Date Picker
        binding.btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    binding.btnDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(calendar.time)
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // GPS
        binding.btnLocation.setOnClickListener { requestLocation() }

        // Caméra / Galerie
        binding.btnCamera.setOnClickListener {
            showCameraOptions()
        }

        // Sauvegarde
        binding.btnSave.setOnClickListener { validateAndSave() }
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation()
        }
    }

    private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) getCurrentLocation() else Toast.makeText(this, "Permission GPS refusée", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentLocation() {
        binding.progressBar.visibility = View.VISIBLE
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                binding.progressBar.visibility = View.GONE
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    binding.tvLocationStatus.text = "📍 ${String.format("%.4f, %.4f", currentLatitude, currentLongitude)}"
                    binding.btnLocation.text = "Position OK"
                    binding.btnLocation.isEnabled = false
                } else {
                    Toast.makeText(this, "Position indisponible. Activez le GPS.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SecurityException) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Erreur permission GPS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndSave() {
        val title = binding.etTitle.text.toString()
        val desc = binding.etDescription.text.toString()

        if (title.isEmpty()) {
            binding.tilTitle.error = "Le titre est obligatoire"
            return
        }
        binding.tilTitle.error = null

        binding.btnSave.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        viewModel.saveEntry(title, desc, selectedDate, currentLatitude, currentLongitude, imageUri)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.saveResult.collect { success ->
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                if (success) {
                    Toast.makeText(this@AddEntryActivity, "Voyage enregistré !", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@AddEntryActivity, "❌ Vérifiez le titre", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}