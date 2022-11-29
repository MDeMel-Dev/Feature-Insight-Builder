import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import com.android.FeatureInsightOverylay
import com.android.InsightComposableAreaSpecs
import com.android.TargetViewSpecs

class FeatureInsightComposeOverlayDialog(
  private val targetViewSpecs: TargetViewSpecs,
  private val insightComposableAreaSpecs: InsightComposableAreaSpecs,
  private val content: @Composable () -> Unit
) : DialogFragment() {

  companion object {
    @JvmStatic
    fun newInstance(
      targetViewSpecs: TargetViewSpecs,
      insightComposableAreaSpecs: InsightComposableAreaSpecs,
      content: @Composable () -> Unit
    ): FeatureInsightComposeOverlayDialog {
      val dialog = FeatureInsightComposeOverlayDialog(
        targetViewSpecs,
        insightComposableAreaSpecs,
        content
      )
      return dialog
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(
        ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
      )
      setContent {
        FeatureInsightOverylay(
          targetViewSpecs,
          insightComposableAreaSpecs
        ) {
          content.invoke()
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    dialog?.apply {
      requestWindowFeature(Window.FEATURE_NO_TITLE)
    }
    dialog?.window?.apply {
      clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
      setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
      )
    }
    super.onCreate(savedInstanceState)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    setStyle(STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar)
    return super.onCreateDialog(savedInstanceState)
  }
}
