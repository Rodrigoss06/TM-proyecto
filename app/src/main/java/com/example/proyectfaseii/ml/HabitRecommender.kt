package com.example.proyectfaseii.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class HabitRecommender(private val context: Context) {

    private val interpreter: Interpreter by lazy {
        Interpreter(loadModelFile())
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("habit_recommender.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictHabit(
        area: String,
        frecuencia: String,
        racha: Int,
        completados: Int,
        diasActivos: Int
    ): String {
        val input = preprocess(area, frecuencia, racha, completados, diasActivos)
        val output = Array(1) { FloatArray(5) }
        interpreter.run(input, output)

        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        return habitLabels[maxIndex]
    }

    private fun preprocess(
        area: String,
        frecuencia: String,
        racha: Int,
        completados: Int,
        diasActivos: Int
    ): Array<FloatArray> {
        val input = FloatArray(10) // 3 área + 3 frecuencia + 3 números
        when (area.lowercase()) {
            "salud" -> input[0] = 1f
            "productividad" -> input[1] = 1f
            "creatividad" -> input[2] = 1f
        }
        when (frecuencia.lowercase()) {
            "daily" -> input[3] = 1f
            "weekly" -> input[4] = 1f
            "monthly" -> input[5] = 1f
        }
        input[6] = racha.toFloat()
        input[7] = completados.toFloat()
        input[8] = diasActivos.toFloat()

        return arrayOf(input)
    }

    companion object {
        val habitLabels = listOf("Meditar", "Leer", "Caminar", "Beber agua", "Estirarse")
    }
}
