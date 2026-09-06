package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.local.KomorebiDatabase
import com.example.data.local.KomorebiRepository
import com.example.pdf.PdfHelper
import com.example.ui.navigation.KomorebiNavGraph
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: KomorebiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KomorebiDatabase.getDatabase(applicationContext)
        repository = KomorebiRepository(applicationContext, database)

        // Seed initial notes and generate sample PDF in background
        lifecycleScope.launch(Dispatchers.IO) {
            val samplePdf = PdfHelper.getOrCreateSampleMathPdf(applicationContext)
            repository.seedInitialDataIfEmpty(samplePdf.absolutePath)
        }

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KomorebiNavGraph(repository = repository)
                }
            }
        }
    }
}
