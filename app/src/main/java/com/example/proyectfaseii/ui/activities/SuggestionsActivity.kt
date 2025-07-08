package com.example.proyectfaseii.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.models.Habito
import com.example.proyectfaseii.ui.adapters.SuggestionsAdapter
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.IOException

class SuggestionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SuggestionsAdapter
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestions)

        recyclerView = findViewById(R.id.rvSuggestions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SuggestionsAdapter(emptyList()) { habit ->
            Toast.makeText(this, "Hábito seleccionado: ${habit.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        try {
            tflite = Interpreter(loadModelFile("habit_recommender.tflite"))
            val suggestions = inferHabitSuggestions()
            adapter.updateData(suggestions)
        } catch (e: IOException) {
            Toast.makeText(this, "Error al cargar el modelo", Toast.LENGTH_LONG).show()
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun inferHabitSuggestions(): List<Habito> {
        // Suponemos entrada de 9 características (como hábitos completados, frecuencia, etc.)
        val input = Array(1) { FloatArray(9) { Math.random().toFloat() } } // dummy data
        val output = Array(1) { FloatArray(5) } // 5 posibles hábitos recomendados

        tflite.run(input, output)

        // Simulación de hábitos
        val habitTemplates = listOf(
            Habito(name = "Leer 10 minutos"),
            Habito(name = "Beber agua"),
            Habito(name = "Ejercicio 15 minutos"),
            Habito(name = "Planificar el día"),
            Habito(name = "Estiramientos mañaneros")
        )

        // Tomamos los top 3 más relevantes
        val topIndexes = output[0]
            .mapIndexed { i, score -> i to score }
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }

        return topIndexes.map { habitTemplates[it] }
    }
}
