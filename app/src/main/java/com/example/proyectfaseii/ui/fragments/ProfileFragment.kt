package com.example.proyectfaseii.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.firebase.FirestoreManager
import com.example.proyectfaseii.ui.activities.LoginActivity
import com.example.proyectfaseii.ui.modals.EditarPerfilModal
import com.example.proyectfaseii.utils.SharedPrefManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var tvNombre: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var imgPerfil: ImageView
    private lateinit var btnEditarFoto: FloatingActionButton
    private lateinit var btnEditarPerfil: Button
    private lateinit var btnCerrarSesion: Button

    private lateinit var tvTotalHabits: TextView
    private lateinit var tvHabitsCompleted: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvLongestStreak: TextView
    private lateinit var switchTheme: Switch

    private lateinit var userId: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var editarPerfilLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val shared = SharedPrefManager.getInstance(requireContext())
        firestore = FirebaseFirestore.getInstance()
        userId = shared.getUserId() ?: return

        // Bind views
        tvNombre = view.findViewById(R.id.tv_username)
        tvDescripcion = view.findViewById(R.id.tv_description)
        tvCorreo = view.findViewById(R.id.tv_email)
        imgPerfil = view.findViewById(R.id.img_profile)
        btnEditarFoto = view.findViewById(R.id.btn_edit_photo)
        btnEditarPerfil = view.findViewById(R.id.btn_edit_profile)
        btnCerrarSesion = view.findViewById(R.id.btn_logout)

        tvTotalHabits = view.findViewById(R.id.tv_total_habits)
        tvHabitsCompleted = view.findViewById(R.id.tv_habits_completed)
        tvCurrentStreak = view.findViewById(R.id.tv_current_streak)
        tvLongestStreak = view.findViewById(R.id.tv_longest_streak)
        switchTheme = view.findViewById(R.id.switch_theme)

        tvCorreo.text = shared.getUserEmail()
        tvNombre.text = shared.getUserName()
        tvDescripcion.text = shared.getUserDescription() ?: "Sin descripción"

        setupThemeSwitch()
        setupLogout()
        loadUserStats()
        cargarPerfilDesdeFirestore()

        // Registrar launcher para edición
        editarPerfilLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val nuevoNombre = data.getStringExtra("nombreActualizado") ?: return@registerForActivityResult
                val nuevaDescripcion = data.getStringExtra("descripcionActualizada") ?: ""

                tvNombre.text = nuevoNombre
                tvDescripcion.text = nuevaDescripcion

                val updates = mapOf(
                    "nombre" to nuevoNombre,
                    "descripcion" to nuevaDescripcion
                )

                firestore.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        parentFragmentManager.setFragmentResultListener("perfilActualizado", viewLifecycleOwner) { _, result ->
            tvNombre.text = result.getString("nombre")
            tvDescripcion.text = result.getString("descripcion")
        }

        btnEditarPerfil.setOnClickListener {
            val modal = EditarPerfilModal.newInstance(
                tvNombre.text.toString(),
                tvDescripcion.text.toString()
            )

            modal.show(parentFragmentManager, "EditarPerfilModal")
        }

        btnEditarFoto.setOnClickListener {
            Toast.makeText(requireContext(), "Función de cambiar foto aún no implementada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserStats() {
        FirestoreManager.getCurrentUserProfile { user ->
            tvTotalHabits.text = "Total hábitos: ${user.totalHabits}"
            tvHabitsCompleted.text = "Completados: ${user.habitsCompleted}"
            tvCurrentStreak.text = "Racha actual: ${user.currentStreak} 🔥"
            tvLongestStreak.text = "Mayor racha: ${user.longestStreak} 🏆"
        }
    }

    private fun cargarPerfilDesdeFirestore() {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                tvNombre.text = doc.getString("nombre") ?: "Sin nombre"
                tvDescripcion.text = doc.getString("descripcion") ?: "Sin descripción"
            }
    }

    private fun setupThemeSwitch() {
        val shared = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isDark = shared.getBoolean("dark_mode", false)
        switchTheme.isChecked = isDark
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            shared.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupLogout() {
        btnCerrarSesion.setOnClickListener {
            SharedPrefManager.getInstance(requireContext()).clear()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
