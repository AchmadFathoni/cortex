package com.cortex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.cortex.core.WidgetUpdater
import com.cortex.ui.app.MainApp
import com.cortex.widget.AndroidWidgetBackgroundController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {
    private val widgetUpdater: WidgetUpdater by inject()
    private val backgroundController: AndroidWidgetBackgroundController by inject()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { backgroundController.saveImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backgroundController.picker = { pickImage.launch("image/*") }
        widgetUpdater.notifyDataChanged()
        setContent {
            MainApp()
        }
    }

    override fun onDestroy() {
        backgroundController.picker = null
        super.onDestroy()
    }
}
