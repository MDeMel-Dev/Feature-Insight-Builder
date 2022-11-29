import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import com.android.common.FeatureInsightComposeOverlayDialog
import com.android.common.InsightComposableAreaSpecs
import com.android.common.TargetViewSpecs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val VIEW_DRAW_DELAY = 800

class FeatureInsightBuilder(
  private val fragmentManager: FragmentManager,
  private val lifecycleCoroutineScope: LifecycleCoroutineScope
) {

  fun buildInsight(
    targetView: View,
    isViewStateReadyCheck: () -> Boolean,
    insightComposableAreaSpecs: InsightComposableAreaSpecs,
    content: @Composable() () -> Unit
  ) {

    targetView.viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {

          if (isViewStateReadyCheck.invoke()) {

            targetView.viewTreeObserver.removeOnGlobalLayoutListener(this)

            lifecycleCoroutineScope.launch {
              delay(VIEW_DRAW_DELAY.toLong())

              val coordinatesXY = IntArray(2)
              targetView.getLocationOnScreen(coordinatesXY)

              if (coordinatesXY[0] > 0) {
                FeatureInsight(
                  targetViewSpecs = getTargetViewSpecs(targetView),
                  insightComposableAreaSpecs = insightComposableAreaSpecs,
                  content = content
                )
              }
            }
          }
        }
      })
  }

  fun getTargetViewSpecs(targetView: View): TargetViewSpecs {
    val rectRef = Rect()
    targetView.getGlobalVisibleRect(rectRef)

    Log.d(
      "view position",
      "mane position x Rect \n WIDTH ${rectRef.width()}" +
        " Height ${rectRef.height()}  coords ${rectRef.centerX()} and ${rectRef.centerY()} " +
        " \n left right top bottom ${rectRef.left}, ${rectRef.right}, ${rectRef.top}, ${rectRef.bottom}"
    )

    return TargetViewSpecs(
      startX = rectRef.left,
      endX = rectRef.right,
      startY = rectRef.top,
      endY = rectRef.bottom,
      width = rectRef.width(),
      height = rectRef.height()
    )
  }

  inner class FeatureInsight(
    targetViewSpecs: TargetViewSpecs,
    insightComposableAreaSpecs: InsightComposableAreaSpecs,
    content: @Composable() () -> Unit
  ) {

    init {

      val dialog = FeatureInsightComposeOverlayDialog.newInstance(
        targetViewSpecs,
        insightComposableAreaSpecs,
        content
      )

      dialog.show(fragmentManager, "FeatureInsightComposeOverlayDialog")
    }
  }
}
