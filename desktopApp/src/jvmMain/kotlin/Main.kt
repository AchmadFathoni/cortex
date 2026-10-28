import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.cortex.desktop.DesktopAppModule
import com.cortex.ui.app.MainApp
import org.koin.plugin.module.dsl.startKoin

fun main() {
    startKoin<DesktopAppModule> {
        allowOverride(true)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cortex",
            state = WindowState(width = 440.dp, height = 760.dp),
        ) {
            MainApp()
        }
    }
}
