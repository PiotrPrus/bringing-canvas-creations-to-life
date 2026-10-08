package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.mesh.MeshGradientTitle
import dev.piotrprus.canvascreations.presentation.demos.mesh.MeshPreview
import dev.piotrprus.canvascreations.presentation.demos.mesh.rememberAnimatedMesh
import dev.piotrprus.canvascreations.presentation.demos.mesh.rememberGridMesh
import dev.piotrprus.canvascreations.presentation.demos.mesh.rememberSimpleMesh
import dev.piotrprus.canvascreations.presentation.ui.MediaFrame
import dev.piotrprus.canvascreations.presentation.ui.MorphingCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.SlideSubtitle
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle

// Mesh gradients: MeshGradientPainter, following the samples on
// developer.android.com/develop/ui/compose/graphics/draw/mesh-gradient
// with a warm palette. Live code: demos/mesh/MeshDemos.kt.

private const val MESH_SIMPLE_CODE = """
val gradientPainter = remember {
    MeshGradientPainter(rows = 1, columns = 1) {
        setVertex(0, 0, Offset(0f, 0f), Red)      // top-left
        setVertex(0, 1, Offset(1f, 0f), Sunshine) // top-right
        setVertex(1, 0, Offset(0f, 1f), Peach)    // bottom-left
        setVertex(1, 1, Offset(1f, 1f), Crimson)  // bottom-right
    }
}

Box(
    modifier = Modifier
        .aspectRatio(16 / 9f)
        .fillMaxWidth()
        .paint(gradientPainter)
)
"""

private const val MESH_GRID_POINTS_CODE = """
val points = listOf(
    Offset(0.0f, 0.0f), Offset(0.3f, 0.0f), Offset(0.7f, 0.0f), Offset(1.0f, 0.0f),
    Offset(0.0f, 0.3f), Offset(0.2f, 0.4f), Offset(0.7f, 0.2f), Offset(1.0f, 0.3f),
    Offset(0.0f, 0.7f), Offset(0.3f, 0.8f), Offset(0.7f, 0.6f), Offset(1.0f, 0.7f),
    Offset(0.0f, 1.0f), Offset(0.3f, 1.0f), Offset(0.7f, 1.0f), Offset(1.0f, 1.0f),
)
val gradientPainter = remember {
    MeshGradientPainter(rows = 3, columns = 3) {
        // 4 × 4 vertices: setVertex(row, column, points[i], color)
    }
}
"""

private const val MESH_GRID_VERTICES_CODE = """
val points = listOf(/* 4 × 4 offsets */)
val gradientPainter = remember {
    MeshGradientPainter(rows = 3, columns = 3) {
        setVertex(0, 0, points[0], Sunshine)
        setVertex(0, 1, points[1], Amber)
        setVertex(0, 2, points[2], Sunshine)
        setVertex(0, 3, points[3], Red)

        setVertex(1, 0, points[4], Coral)
        setVertex(1, 1, points[5], Sunshine)
        setVertex(1, 2, points[6], Coral)
        setVertex(1, 3, points[7], Red)

        setVertex(2, 0, points[8], Crimson)
        setVertex(2, 1, points[9], Coral)
        setVertex(2, 2, points[10], Red)
        setVertex(2, 3, points[11], Crimson)

        setVertex(3, 0, points[12], Red)
        setVertex(3, 1, points[13], Crimson)
        setVertex(3, 2, points[14], Coral)
        setVertex(3, 3, points[15], Sunshine)
    }
}
"""

private const val MESH_ANIMATION_CODE = """
val infiniteTransition = rememberInfiniteTransition(label = "meshMovement")
val offset by infiniteTransition.animateFloat(
    initialValue = -0.15f,
    targetValue = 0.15f,
    animationSpec = infiniteRepeatable(
        animation = tween(2000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
    ),
    label = "offset",
)
// keyed on offset, so the painter follows the animation
val gradientPainter = remember(offset) {
    MeshGradientPainter(rows = 3, columns = 3) {
        // 4 × 4 vertices, the inner four moved by offset
    }
}
"""

private const val MESH_ANIMATED_VERTICES_CODE = """
val infiniteTransition = rememberInfiniteTransition(label = "meshMovement")
val offset by infiniteTransition.animateFloat(/* -0.15f ↔ 0.15f */)
// keyed on offset, so the painter follows the animation
val gradientPainter = remember(offset) {
    MeshGradientPainter(rows = 3, columns = 3) {
        setVertex(0, 0, Offset(0.0f, 0.0f), Crimson)
        setVertex(0, 1, Offset(0.3f, 0.0f), Peach)
        setVertex(0, 2, Offset(0.7f, 0.0f), Amber)
        setVertex(0, 3, Offset(1.0f, 0.0f), Sunshine)
        setVertex(1, 0, Offset(0.0f, 0.3f), Red)
        setVertex(1, 1, Offset(0.2f, 0.4f) + Offset(offset, offset), Coral)
        setVertex(1, 2, Offset(0.7f, 0.2f) + Offset(offset, offset), Peach)
        setVertex(1, 3, Offset(1.0f, 0.3f), Crimson)
        setVertex(2, 0, Offset(0.0f, 0.7f), Coral)
        setVertex(2, 1, Offset(0.3f, 0.8f) + Offset(offset, 0f), Red)
        setVertex(2, 2, Offset(0.7f, 0.6f) + Offset(offset, 0f), Sunshine)
        setVertex(2, 3, Offset(1.0f, 0.7f), Amber)
        setVertex(3, 0, Offset(0.0f, 1.0f), Sunshine)
        setVertex(3, 1, Offset(0.3f, 1.0f), Amber)
        setVertex(3, 2, Offset(0.7f, 1.0f), Red)
        setVertex(3, 3, Offset(1.0f, 1.0f), Crimson)
    }
}
"""

private const val MESH_TEXT_CODE = """
Text(
    text = "Bringing Your Canvas Creations\n to Life with Jetpack Compose",
    modifier = Modifier
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawIntoCanvas { canvas ->
                canvas.saveLayer(size.toRect(), Paint().apply { blendMode = BlendMode.SrcAtop })
                with(meshPainter) { draw(size) }
                canvas.restore()
            }
        }
)
"""

/** Docs sample 1: four vertices, four colors, one Modifier.paint. Step 1 marks the vertices. */
val meshBasicsSlide = Slide(steps = 1) {
    MeshSlide(title = "Mesh gradient", subtitle = "MeshGradientPainter") {
        CodeAndPreview(
            codeWidth = 940.dp,
            code = { MorphingCodeBlock(code = MESH_SIMPLE_CODE, fontSize = 20.sp, modifier = it) },
            preview = { MeshPreview(rememberSimpleMesh(), Modifier.fillMaxSize(), showVertices = step >= 1) },
        )
    }
}

/** Docs sample 3: a 3×3 grid with moved inner points; step 1 shows where each color sits. */
val meshGridSlide = Slide(steps = 1) {
    val vertices = step >= 1
    MeshSlide(title = "Mesh gradient", subtitle = "3 × 3 grid") {
        CodeAndPreview(
            codeWidth = if (vertices) 620.dp else 1050.dp,
            code = {
                MorphingCodeBlock(
                    code = if (vertices) MESH_GRID_VERTICES_CODE else MESH_GRID_POINTS_CODE,
                    fontSize = 17.sp,
                    modifier = it,
                )
            },
            preview = { MeshPreview(rememberGridMesh(), Modifier.fillMaxSize(), showVertices = vertices) },
        )
    }
}

/** Docs sample 4: the inner four vertices ride an infinite transition; step 1 shows them moving. */
val meshAnimatedSlide = Slide(steps = 1) {
    val vertices = step >= 1
    MeshSlide(title = "Mesh gradient", subtitle = "animated vertices") {
        CodeAndPreview(
            codeWidth = 960.dp,
            code = {
                MorphingCodeBlock(
                    code = if (vertices) MESH_ANIMATED_VERTICES_CODE else MESH_ANIMATION_CODE,
                    fontSize = 18.sp,
                    modifier = it,
                )
            },
            preview = { MeshPreview(rememberAnimatedMesh(), Modifier.fillMaxSize(), showVertices = vertices) },
        )
    }
}

/** The payoff: the talk title painted with the animated mesh. */
val meshTextSlide = Slide(steps = 1) {
    MeshSlide(title = "Mesh gradient", subtitle = "on text") {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The title starts large and centred, then lifts and shrinks to make room for the code.
            val lift by animateDpAsState(if (step == 0) 170.dp else 0.dp, tween(700))
            val scale by animateFloatAsState(if (step == 0) 1.4f else 1f, tween(700))
            Spacer(modifier = Modifier.height(lift))
            MeshGradientTitle(
                fontSize = 66.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            )
            Spacer(modifier = Modifier.height(48.dp))
            Reveal(at = 1) {
                MorphingCodeBlock(code = MESH_TEXT_CODE, fontSize = 23.sp)
            }
        }
    }
}

/** Code on the left, a 16:9 mesh preview filling the rest of the row. */
@Composable
private fun CodeAndPreview(
    codeWidth: Dp,
    code: @Composable (Modifier) -> Unit,
    preview: @Composable () -> Unit,
) {
    val width by animateDpAsState(codeWidth, tween(700))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        code(Modifier.width(width))
        MediaFrame(modifier = Modifier.weight(1f).aspectRatio(16 / 9f)) {
            preview()
        }
    }
}

/** Slide chrome shared by the mesh slides. */
@Composable
private fun MeshSlide(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    SlideSurface {
        SlideTitle(title)
        SlideSubtitle(subtitle)
        Spacer(modifier = Modifier.height(40.dp))
        content()
    }
}

val meshSlides: List<Slide> = listOf(
    meshBasicsSlide,
    meshGridSlide,
    meshAnimatedSlide,
    meshTextSlide,
)
