package app.ruuun.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.SkyShade
import app.ruuun.ui.theme.Surface as SurfaceColor

/** Side gutter on the blue header and the sheet below it. */
val Gutter = 18.dp

/** How round the top of the content sheet is, where it meets the blue. */
private val SHEET_RADIUS = 28.dp

/**
 * The blue header zone at the top of a screen, reaching up behind the status bar. It
 * ends in the rounded top of the light content sheet, so whatever follows sits on the
 * sheet. Given a minimum height, the content is centred in the extra space.
 */
@Composable
fun BlueHeader(
    modifier: Modifier = Modifier,
    topPadding: Dp = 12.dp,
    bottomPadding: Dp = 18.dp,
    centred: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Sky)
            .statusBarsPadding(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = Gutter, end = Gutter, top = topPadding, bottom = bottomPadding),
            content = content,
        )
        if (centred) {
            // Pushes the sheet to the bottom when the header is asked to be taller.
            Spacer(Modifier.weight(1f))
        }
        SheetTop()
    }
}

/** The rounded top edge of the content sheet, on blue. */
@Composable
fun SheetTop(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(Sky),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(SHEET_RADIUS)
                .clip(RoundedCornerShape(topStart = SHEET_RADIUS, topEnd = SHEET_RADIUS))
                .background(Ground),
        )
    }
}

/** A tab's title on the blue header, with optional chips on the right. */
@Composable
fun HeaderTitleRow(title: String, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = RuuunText.ScreenTitle,
            color = Navy,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        trailing()
    }
}

/** White chunky chip that sits on the blue header. */
@Composable
fun HeaderChip(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Chunky(
        color = SurfaceColor,
        shade = SkyShade,
        border = null,
        shape = RoundedCornerShape(14.dp),
        depth = 4.dp,
        onClick = onClick,
        onClickLabel = onClickLabel,
        modifier = modifier.heightIn(min = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content,
        )
    }
}

/** An icon chip on the header, such as Settings or Back. */
@Composable
fun HeaderIconChip(icon: ImageVector, description: String, onClick: () -> Unit, size: Dp = 48.dp) {
    Chunky(
        color = SurfaceColor,
        shade = SkyShade,
        border = null,
        shape = RoundedCornerShape(14.dp),
        depth = 4.dp,
        onClick = onClick,
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = Navy, modifier = Modifier.size(22.dp))
    }
}

/**
 * The header for screens opened from a tab (Log a run, Settings, a run): a back chip,
 * the title, and anything on the right, on the same blue as the tabs.
 */
@Composable
fun SubScreenHeader(
    title: String,
    onBack: () -> Unit,
    backIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    backDescription: String = "Back",
    subtitle: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    BlueHeader(topPadding = 10.dp, bottomPadding = 6.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HeaderIconChip(backIcon, backDescription, onBack)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = RuuunText.SubTitle,
                    color = Navy,
                    modifier = Modifier.semantics { heading() },
                )
                if (subtitle != null) {
                    Text(subtitle, style = RuuunText.Label, color = app.ruuun.ui.theme.SkyDeep)
                }
            }
            trailing()
        }
    }
}

/** White speech card with a solid blue edge, for what Ruu says on the header. */
@Composable
fun SpeechCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Chunky(
        color = SurfaceColor,
        shade = SkyShade,
        border = null,
        shape = RoundedCornerShape(20.dp),
        depth = 5.dp,
        modifier = modifier,
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

/**
 * Ruu in a round white frame with a solid blue edge underneath. [background] is drawn
 * behind Ruu (a world scene); Ruu is scaled up from the top so the head and body fill
 * the circle. Decorative: the words next to it say the same.
 */
@Composable
fun FoxCircle(
    @DrawableRes pose: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    @DrawableRes background: Int? = null,
    backgroundColor: Color = app.ruuun.ui.theme.SkyTint,
    zoom: Float = 1.45f,
    breathe: Boolean = false,
) {
    val ring = size * 0.034f
    val depth = size * 0.045f
    Chunky(
        color = SurfaceColor,
        shade = SkyShade,
        border = null,
        shape = CircleShape,
        depth = depth,
        // The edge adds to the height, so the frame itself stays a circle.
        modifier = modifier.size(width = size, height = size + depth),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(ring)
                .clip(CircleShape)
                .background(backgroundColor),
        ) {
            if (background != null) {
                Image(
                    painter = painterResource(background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            RuuImage(
                pose = pose,
                height = size,
                breathe = breathe,
                zoom = zoom,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}
