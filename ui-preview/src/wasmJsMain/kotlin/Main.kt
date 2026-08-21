import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(
        viewportContainerId = "ComposeTarget"
    ) {
        PreviewApp()
    }
}

@Composable
private fun PreviewApp() {
    MaterialTheme {
        Surface {
            ReportsPreview()
        }
    }
}
