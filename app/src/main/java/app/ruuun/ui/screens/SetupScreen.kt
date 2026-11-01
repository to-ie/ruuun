package app.ruuun.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.ruuun.R
import app.ruuun.data.RunnerSettings
import app.ruuun.logic.Formatters
import app.ruuun.logic.RunInput
import app.ruuun.ui.components.InfoPanel
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.SegmentedPicker
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.InputBorder
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.OrangeTintLight
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor

private data class StartingPlan(val label: String, val metres: Int) {
    val caption: String get() = "Your first mission: ${Formatters.distance(metres)}"
}

private val PLANS = listOf(
    StartingPlan("New to running", RunnerSettings.START_NEW_M),
    StartingPlan("Getting going", RunnerSettings.START_MOVING_M),
    StartingPlan("Already running", RunnerSettings.START_JOGGING_M),
)

/** One screen, about a minute. Nickname, starting distance, runs per week. */
@Composable
fun SetupScreen(onFinish: (nickname: String, startingTargetM: Int, weeklyGoal: Int) -> Unit) {
    var nickname by remember { mutableStateOf("") }
    var planIndex by remember { mutableIntStateOf(0) }
    var weeklyGoal by remember { mutableIntStateOf(RunnerSettings.DEFAULT_WEEKLY_GOAL) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Surface(color = SurfaceColor, modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ruuun_logo),
                    contentDescription = stringResource(R.string.logo_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(210.dp),
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    "Set up your running",
                    style = RuuunText.SubTitle,
                    color = Navy,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Takes a minute. Grab an adult if you want a hand.",
                    style = RuuunText.Body,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(Modifier.padding(Dimens.ScreenPadding)) {
            Text("Nickname (optional)", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it.take(20) },
                placeholder = { Text(RunnerSettings.DEFAULT_NICKNAME, color = TextMuted) },
                singleLine = true,
                shape = Shapes.Input,
                textStyle = RuuunText.BodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor,
                    focusedIndicatorColor = Navy,
                    unfocusedIndicatorColor = InputBorder,
                    focusedTextColor = Navy,
                    unfocusedTextColor = Navy,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimens.SectionGap))
            Text("Where are you starting?", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(10.dp))
            PLANS.forEachIndexed { index, plan ->
                val isSelected = index == planIndex
                Surface(
                    onClick = { planIndex = index },
                    shape = Shapes.Card,
                    color = if (isSelected) OrangeTintLight else SurfaceColor,
                    border = BorderStroke(2.dp, if (isSelected) Navy else Border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .semantics {
                            selected = isSelected
                            role = Role.RadioButton
                        },
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(plan.label, style = RuuunText.BodyStrong, color = Navy)
                        Text(plan.caption, style = RuuunText.Body, color = TextMuted)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("Runs each week", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(10.dp))
            SegmentedPicker(
                options = listOf(1, 2, 3),
                selected = weeklyGoal,
                onSelect = { weeklyGoal = it },
                label = { "$it" },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "You can change this later in Settings.",
                style = RuuunText.Secondary,
                color = TextMuted,
            )

            Spacer(Modifier.height(Dimens.SectionGap))
            InfoPanel(
                "Walking is part of running, and rest days make you faster. Stop and tell an " +
                    "adult if you feel pain, dizziness or unwell.",
            )

            Spacer(Modifier.height(24.dp))
            PrimaryCta(
                text = "Start running",
                onClick = {
                    onFinish(
                        RunInput.cleanNickname(nickname),
                        PLANS[planIndex].metres,
                        weeklyGoal,
                    )
                },
            )
            Spacer(Modifier.height(Dimens.ScreenPadding))
        }
    }
}
