package app.ruuun.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.ruuun.R

// Bundle these TTFs in res/font (SIL OFL, from fonts.google.com). No downloadable fonts: app is offline.
//   baloo2_extrabold.ttf, nunito_regular.ttf, nunito_semibold.ttf, nunito_bold.ttf, nunito_extrabold.ttf
// If they are not available yet, set both families to FontFamily.Default and remove the R.font imports.
val Baloo2 = FontFamily(Font(R.font.baloo2_extrabold, FontWeight.ExtraBold))
val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

private fun display(size: Int, line: Int) =
    TextStyle(fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold, fontSize = size.sp, lineHeight = line.sp)

private fun body(size: Int, weight: FontWeight, line: Int = (size * 1.35).toInt()) =
    TextStyle(fontFamily = Nunito, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp)

object RuuunText {
    val HeroNumber = display(60, 60)      // Home "425 m"
    val BigNumber = display(44, 46)       // large figures
    val Greeting = display(30, 34)        // "Hello, Runner", stat tile values
    val ScreenTitle = display(28, 34)     // Journey, Badges, History
    val SubTitle = display(24, 28)        // Log a run, Settings headers
    val CardTitle = display(22, 27)       // chapter and week titles
    val SectionTitle = display(20, 26)
    val StatValue = display(28, 31)

    val Cta = body(19, FontWeight.ExtraBold)
    val BodyLarge = body(17, FontWeight.SemiBold)
    val BodyStrong = body(16, FontWeight.ExtraBold)
    val Body = body(15, FontWeight.SemiBold)
    val Secondary = body(14, FontWeight.Normal)
    val Label = body(13, FontWeight.Bold)
    val Overline = body(13, FontWeight.ExtraBold).copy(letterSpacing = 0.08.em) // render uppercase
    val Pill = body(12, FontWeight.ExtraBold).copy(letterSpacing = 0.04.em)
    val DistanceInput = display(30, 34)
}

val RuuunTypography = Typography(
    displayLarge = RuuunText.HeroNumber,
    displayMedium = RuuunText.BigNumber,
    headlineLarge = RuuunText.Greeting,
    headlineMedium = RuuunText.ScreenTitle,
    headlineSmall = RuuunText.SubTitle,
    titleLarge = RuuunText.CardTitle,
    titleMedium = RuuunText.SectionTitle,
    bodyLarge = RuuunText.BodyLarge,
    bodyMedium = RuuunText.Body,
    bodySmall = RuuunText.Secondary,
    labelLarge = RuuunText.Cta,
    labelMedium = RuuunText.Label,
    labelSmall = RuuunText.Pill,
)
