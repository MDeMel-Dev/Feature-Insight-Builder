import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.android.common.R
import com.android.InsightComposableAreaSpecs
import com.android.TargetViewSpecs

enum class ArrowPointerDirection { UP, DOWN }

const val STATUS_BAR_HEIGHT = 24
const val CORNER_RADIUS = 4

@Composable
fun FeatureInsightOverylay(
  targetViewSpecs: TargetViewSpecs,
  insightComposableAreaSpecs: InsightComposableAreaSpecs,
  insight: @Composable () -> Unit
) {

  val pointerSpace = dimensionResource(id = R.dimen.pointer_space_to_component)

  val currentCoordinates by remember {
    mutableStateOf(
      IntOffset(
        targetViewSpecs.startX,
        targetViewSpecs.startY
      )
    )
  }

  val screenConfiguration = LocalConfiguration.current

  val pointerDirection by remember {
    mutableStateOf(
      {
        when {
          (screenConfiguration.screenHeightDp.dpToPx / 2) > targetViewSpecs.startY -> ArrowPointerDirection.UP
          else -> ArrowPointerDirection.DOWN
        }
      })
  }

  Box(modifier = Modifier.fillMaxSize()) {

    ScreenSurfaceWithHighlightedArea(
      targetStartX = targetViewSpecs.startX,
      targetStartY = targetViewSpecs.startY,
      targetWidth = targetViewSpecs.width,
      targetHeight = targetViewSpecs.height,
      modifier = Modifier.fillMaxSize(),
      pointerDirection = pointerDirection.invoke()
    )

    DynamicallyPositionableComposable(
      currentCoordinates,
      pointerSpace,
      insightComposableAreaSpecs.areaHeight,
      pointerDirection.invoke()
    ) {

      when (pointerDirection.invoke()) {
        ArrowPointerDirection.UP -> {
          TopInsightSection(
            currentCoordinates = currentCoordinates,
            targetWidth = targetViewSpecs.width,
            insight = insight
          )
        }
        ArrowPointerDirection.DOWN -> {
          BottomInsightSection(
            currentCoordinates = currentCoordinates,
            targetWidth = targetViewSpecs.width,
            insight = insight
          )
        }
      }
    }
  }
}

@Composable
fun DynamicallyPositionableComposable(
  currentCoordinates: IntOffset,
  pointerSpace: Dp,
  composableAreaHeight: Int,
  pointerDirection: ArrowPointerDirection,
  content: @Composable () -> Unit
) {

  val popupPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
      anchorBounds: IntRect,
      windowSize: IntSize,
      layoutDirection: LayoutDirection,
      popupContentSize: IntSize
    ): IntOffset {
      return currentCoordinates.copy(
        x = 0,
        y = if (pointerDirection == ArrowPointerDirection.UP) {
          currentCoordinates.y + pointerSpace.value.toInt().dpToPx
        } else {
          currentCoordinates.y - pointerSpace.value.toInt().dpToPx - composableAreaHeight.dpToPx
        }
      )
    }
  }

  Popup(popupPositionProvider = popupPositionProvider, content = content)
}

@Composable
fun ScreenSurfaceWithHighlightedArea(
  targetStartX: Int,
  targetStartY: Int,
  targetWidth: Int,
  targetHeight: Int,
  modifier: Modifier,
  pointerDirection: ArrowPointerDirection
) {
  Canvas(modifier = modifier, onDraw = {
    highlightAreaPath.invoke(
      targetStartX,
      targetStartY,
      targetWidth,
      targetHeight,
      CORNER_RADIUS,
    )
      .let { pathToHighlight ->

        clipPath(pathToHighlight, clipOp = ClipOp.Difference) {
          drawRect(SolidColor(Color.Black.copy(alpha = 0.5f)))
        }
      }
  })
}

val highlightAreaPath: (
  targetStartX: Int,
  targetStartY: Int,
  targetWidth: Int,
  targetHeight: Int,
  cornerRadius: Int
) -> Path = { targetStartX,
  targetStartY,
  targetWidth,
  targetHeight,
  cornerRadius ->
  Path().apply {
    addRoundRect(
      RoundRect(
        Rect(
          Offset(
            x = targetStartX.toFloat(),
            y = targetStartY.toFloat() - STATUS_BAR_HEIGHT.dpToPx.toFloat()
          ),
          Size(
            width = targetWidth.toFloat(),
            height = targetHeight.toFloat()
          )
        ),
        CornerRadius(cornerRadius.dpToPx.toFloat())
      )
    )
  }
}

@Composable
fun PointingDownArrow(offsetX: Int, modifier: Modifier) {
  Box(
    modifier = modifier
      .offset(x = (offsetX).pxToDp.dp)
      .clip(downwardPointerShape)
      .size(dimensionResource(id = R.dimen.pointer_size))
      .background(Color.White)
  )
}

@Composable
fun PointingUpArrow(offsetX: Int, modifier: Modifier) {
  Box(
    modifier = modifier
      .offset(x = (offsetX).pxToDp.dp)
      .clip(upwardPointerShape)
      .size(dimensionResource(id = R.dimen.pointer_size))
      .background(Color.White)
  )
}

@Suppress("MagicNumber")
val downwardPointerShape: Shape = object : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    val baseWidth = 222.354f
    val baseHeight = 195.2298f

    val path = Path()

    path.moveTo(124.9998f, 187.2876f)
    path.cubicTo(118.8268f, 197.8771f, 103.5272f, 197.8772f, 97.3542f, 187.2876f)
    path.lineTo(2.2012f, 24.0579f)
    path.cubicTo(-4.0167f, 13.3913f, 3.6775f, 0f, 16.0241f, 0f)
    path.lineTo(206.3299f, 0f)
    path.cubicTo(218.6765f, 0f, 226.3707f, 13.3913f, 220.1527f, 24.0579f)
    path.lineTo(124.9998f, 187.2876f)
    path.close()

    return Outline.Generic(
      path
        .asAndroidPath()
        .apply {
          transform(
            Matrix().apply {
              setScale(size.width / baseWidth, size.height / baseHeight)
            }
          )
        }
        .asComposePath()
    )
  }
}

val upwardPointerShape: Shape = object : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    val baseWidth = 222.354f
    val baseHeight = 195.2298f

    val path = Path()

    path.moveTo(97.3541f, 7.9421f)
    path.cubicTo(103.5272f, -2.6474f, 118.8268f, -2.6474f, 124.9998f, 7.9421f)
    path.lineTo(220.1527f, 171.1719f)
    path.cubicTo(226.3707f, 181.8384f, 218.6765f, 195.2298f, 206.3299f, 195.2298f)
    path.lineTo(16.0241f, 195.2298f)
    path.cubicTo(3.6775f, 195.2298f, -4.0167f, 181.8384f, 2.2012f, 171.1719f)
    path.lineTo(97.3541f, 7.9421f)
    path.close()

    return Outline.Generic(
      path
        .asAndroidPath()
        .apply {
          transform(
            Matrix().apply {
              setScale(size.width / baseWidth, size.height / baseHeight)
            }
          )
        }
        .asComposePath()
    )
  }
}

@Composable
fun BottomInsightSection(
  currentCoordinates: IntOffset,
  targetWidth: Int,
  insight: @Composable() () -> Unit
) {
  Column(
    modifier = Modifier
      .wrapContentHeight()
      .fillMaxWidth()
  ) {

    Card(
      modifier = Modifier
        .wrapContentSize()
        .align(Alignment.CenterHorizontally)
        .offset(y = dimensionResource(id = R.dimen.spacing_quarter)),
      shape = RoundedCornerShape(dimensionResource(id = R.dimen.page_padding_half))
    ) {
      insight.invoke()
    }

    PointingDownArrow(
      offsetX = currentCoordinates.x + targetWidth / 2,
      modifier = Modifier
    )
  }
}

@Composable
fun TopInsightSection(
  currentCoordinates: IntOffset,
  targetWidth: Int,
  insight: @Composable() () -> Unit
) {
  Column(
    modifier = Modifier
      .wrapContentHeight()
      .fillMaxWidth()
  ) {

    PointingUpArrow(
      offsetX = currentCoordinates.x + targetWidth / 2,
      modifier = Modifier
    )

    Card(
      modifier = Modifier
        .wrapContentSize()
        .align(Alignment.CenterHorizontally)
        .offset(y = -dimensionResource(id = R.dimen.spacing_quarter)),
      shape = RoundedCornerShape(dimensionResource(id = R.dimen.page_padding_half))
    ) {
      insight.invoke()
    }
  }
}
