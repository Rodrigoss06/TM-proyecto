package com.example.proyectfaseii.ui.modals

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.proyectfaseii.R
import com.example.proyectfaseii.utils.SharedPrefManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditarPerfilModal : BottomSheetDialogFragment() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnGuardar: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.modal_editar_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etNombre = view.findViewById(R.id.etNombre)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        btnGuardar = view.findViewById(R.id.btnGuardar)

        val nombreActual = arguments?.getString("nombre") ?: ""
        val descripcionActual = arguments?.getString("descripcion") ?: ""

        etNombre.setText(nombreActual)
        etDescripcion.setText(descripcionActual)

        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevaDescripcion = etDescripcion.text.toString().trim()
            if (nuevoNombre.isEmpty()) {
                etNombre.error = "El nombre no puede estar vacío"
                return@setOnClickListener
            }

            val sharedPrefs = SharedPrefManager.getInstance(requireContext())
            val userId = sharedPrefs.getUserId() ?: return@setOnClickListener

            val updates = mapOf(
                "nombre" to nuevoNombre,
                "descripcion" to nuevaDescripcion
            )

            FirebaseFirestore.getInstance().collection("usuarios").document(userId)
                .set(updates, SetOptions.merge()) // ← esto agrega si no existen
                .addOnSuccessListener {
                    sharedPrefs.saveUser(
                        id = userId,
                        nombre = nuevoNombre,
                        email = sharedPrefs.getUserEmail() ?: "",
                        descripcion = nuevaDescripcion
                    )
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult("perfilActualizado", Bundle().apply {
                        putString("nombre", nuevoNombre)
                        putString("descripcion", nuevaDescripcion)
                    })
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        fun newInstance(nombre: String, descripcion: String): EditarPerfilModal {
            return EditarPerfilModal().apply {
                arguments = Bundle().apply {
                    putString("nombre", nombre)
                    putString("descripcion", descripcion)
                }
            }
        }
    }
}
