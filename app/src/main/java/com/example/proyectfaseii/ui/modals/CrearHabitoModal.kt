package com.example.proyectfaseii.ui.modals

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.firebase.FirestoreManager
import com.example.proyectfaseii.data.models.Area
import com.example.proyectfaseii.data.models.Goal
import com.example.proyectfaseii.data.models.Habito
import com.example.proyectfaseii.ml.TextScanner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*


class CrearHabitoModal : BottomSheetDialogFragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etStartDate: TextInputEditText
    private lateinit var etEndDate: TextInputEditText
    private lateinit var btnAddReminder: Button
    private lateinit var btnSave: Button
    private lateinit var chipGroupReminders: ChipGroup
    private lateinit var chipGroupWeekdays: ChipGroup
    private lateinit var tvDaysLabel: TextView

    private lateinit var groupRecurrence: MaterialButtonToggleGroup
    private lateinit var btnDaily: MaterialButton
    private lateinit var btnWeekly: MaterialButton
    private lateinit var btnMonthly: MaterialButton
    private lateinit var btnYearly: MaterialButton
    private lateinit var btnScanText: Button
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>

    private var habitoExistente: Habito? = null



    private val reminderList = mutableListOf<String>()
    private val selectedDays = mutableListOf<String>()

    private val dayInitials = listOf("S", "M", "T", "W", "T", "F", "S")
    private val dayKeys = listOf("sun", "mon", "tue", "wed", "thu", "fri", "sat")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_crear_habito, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etName = view.findViewById(R.id.et_name)
        etStartDate = view.findViewById(R.id.et_start_date)
        etEndDate = view.findViewById(R.id.et_end_date)
        btnAddReminder = view.findViewById(R.id.btn_add_reminder)
        btnSave = view.findViewById(R.id.btn_save)
        chipGroupReminders = view.findViewById(R.id.chip_group_reminders)
        chipGroupWeekdays = view.findViewById(R.id.chip_group_weekdays)
        tvDaysLabel = view.findViewById(R.id.tv_days_label)
        groupRecurrence = view.findViewById(R.id.group_recurrence)
        btnDaily = view.findViewById(R.id.btn_daily)
        btnWeekly = view.findViewById(R.id.btn_weekly)
        btnMonthly = view.findViewById(R.id.btn_monthly)
        btnYearly = view.findViewById(R.id.btn_yearly)

        setupRecurrenceButtons()
        setupWeekDays()
        setupDatePickers()
        setupReminderPicker()
        setupSave()
        btnDaily.isChecked = true

        btnScanText = view.findViewById(R.id.btnScanText)

        btnScanText.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(requireContext().packageManager) != null) {
                takePictureLauncher.launch(cameraIntent)
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    TextScanner.scanFromBitmap(
                        bitmap = imageBitmap,
                        onSuccess = { scannedText ->
                            etName.setText(scannedText.lines().firstOrNull()?.trim() ?: "")
                            Toast.makeText(requireContext(), "Texto detectado", Toast.LENGTH_SHORT).show()
                        },
                        onError = { e ->
                            Toast.makeText(requireContext(), "Error al escanear: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        habitoExistente = arguments?.getSerializable("habito") as? Habito
        habitoExistente?.let { habit ->
            etName.setText(habit.name)
            etStartDate.setText(habit.start_date)
            etEndDate.setText(habit.end_date ?: "")
            reminderList.addAll(habit.remind)
            selectedDays.addAll(habit.time_of_day)

            habit.remind.forEach { addReminderChip(it) }

            val recurrenceId = when (habit.recurrence) {
                "Daily" -> R.id.btn_daily
                "Weekly" -> R.id.btn_weekly
                "Monthly" -> R.id.btn_monthly
                "Yearly" -> R.id.btn_yearly
                else -> R.id.btn_daily
            }
            groupRecurrence.check(recurrenceId)

            // Marcar días seleccionados si es Weekly
            for (i in 0 until chipGroupWeekdays.childCount) {
                val chip = chipGroupWeekdays.getChildAt(i) as? Chip
                val key = dayKeys.getOrNull(i)
                if (chip != null && key in habit.time_of_day) chip.isChecked = true
            }

            btnSave.text = "Actualizar"
        }



    }

    private fun setupRecurrenceButtons() {
        groupRecurrence.addOnButtonCheckedListener { _, checkedId, _ ->
            val isWeekly = checkedId == btnWeekly.id
            chipGroupWeekdays.visibility = if (isWeekly) View.VISIBLE else View.GONE
            tvDaysLabel.visibility = if (isWeekly) View.VISIBLE else View.GONE
        }
    }

    private fun setupWeekDays() {
        dayInitials.forEachIndexed { index, day ->
            val chip = Chip(requireContext()).apply {
                text = day
                isCheckable = true
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                val key = dayKeys[index]
                if (isChecked) selectedDays.add(key) else selectedDays.remove(key)
            }
            chipGroupWeekdays.addView(chip)
        }
    }

    private fun setupDatePickers() {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val showPicker: (onSelected: (String) -> Unit) -> Unit = { onSelected ->
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .build()
            picker.addOnPositiveButtonClickListener {
                onSelected(dateFormatter.format(Date(it)))
            }
            picker.show(parentFragmentManager, "fecha_picker")
        }

        etStartDate.setOnClickListener {
            showPicker { etStartDate.setText(it) }
        }

        etEndDate.setOnClickListener {
            showPicker { etEndDate.setText(it) }
        }
    }

    private fun setupReminderPicker() {
        btnAddReminder.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(),
                { _, hour, minute ->
                    val time = String.format("%02d:%02d", hour, minute)
                    reminderList.add(time)
                    addReminderChip(time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun addReminderChip(time: String) {
        val chip = Chip(requireContext()).apply {
            text = time
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                reminderList.remove(time)
                chipGroupReminders.removeView(this)
            }
        }
        chipGroupReminders.addView(chip)
    }

    private fun setupSave() {
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val checkedId = groupRecurrence.checkedButtonId
            Toast.makeText(requireContext(), "id: $checkedId", Toast.LENGTH_SHORT).show()

            val recurrence = when (checkedId) {
                R.id.btn_daily -> "Daily"
                R.id.btn_weekly -> "Weekly"
                R.id.btn_monthly -> "Monthly"
                R.id.btn_yearly -> "Yearly"
                else -> ""
            }

            val startDate = etStartDate.text.toString().trim()
            val endDate = etEndDate.text.toString().trim()


            if (name.isEmpty() || recurrence.isEmpty() || startDate.isEmpty()) {
                Toast.makeText(requireContext(), "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (recurrence == "Daily") {
                selectedDays.clear()
                selectedDays.addAll(listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun"))
            }

            val idHabit = habitoExistente?.id ?: UUID.randomUUID().toString()


            val habito = Habito(
                id = idHabit,
                name = name,
                recurrence = recurrence,
                start_date = startDate,
                end_date = endDate,
                created_date = startDate,
                remind = reminderList,
                time_of_day = selectedDays,
                goal = Goal(
                    unit_type = "veces",
                    value = 1.0,
                    periodicity = recurrence
                ),
                area = Area(name = "General"),
                log_method = "manual"
            )

            FirestoreManager.saveOrUpdateHabit(habito) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "✅ Hábito guardado", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "❌ Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    companion object {
        fun newInstance(habito: Habito): CrearHabitoModal {
            val modal = CrearHabitoModal()
            val args = Bundle()
            args.putSerializable("habito", habito)
            modal.arguments = args
            return modal
        }
    }

}
