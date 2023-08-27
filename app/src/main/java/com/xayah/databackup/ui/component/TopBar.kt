package com.xayah.databackup.ui.component

import androidx.activity.ComponentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.xayah.databackup.ui.activity.main.router.MainRoutes
import com.xayah.databackup.ui.activity.main.router.currentRoute
import com.xayah.databackup.ui.theme.ColorScheme
import com.xayah.databackup.ui.token.AnimationTokens
import com.xayah.databackup.ui.token.CommonTokens
import com.xayah.databackup.ui.token.TopBarTokens
import com.xayah.databackup.util.ConstantUtil
import androidx.compose.material3.ColorScheme as MaterialColorScheme

@Composable
fun GuideTopBar(title: String, icon: ImageVector) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorScheme.onSurfaceVariant(),
            modifier = Modifier
                .size(CommonTokens.IconMediumSize)
                .paddingBottom(CommonTokens.PaddingSmall)
        )
        TopBarTitle(text = title)
    }
}

@ExperimentalMaterial3Api
@Composable
fun SlotScope.MainTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    val context = LocalContext.current
    val routes = ConstantUtil.MainBottomBarRoutes
    val currentRoute = navController.currentRoute()

    CenterAlignedTopAppBar(
        title = { TopBarTitle(text = MainRoutes.ofTitle(context, currentRoute)) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            Crossfade(targetState = currentRoute, label = AnimationTokens.CrossFadeLabel) { route ->
                if ((route in routes).not())
                    ArrowBackButton {
                        navController.popBackStack()
                    }
            }
        },
    )
}

@ExperimentalMaterial3Api
@Composable
fun ListTopBar(scrollBehavior: TopAppBarScrollBehavior, title: String) {
    val context = LocalContext.current
    CenterAlignedTopAppBar(
        title = { TopBarTitle(text = title) },

        scrollBehavior = scrollBehavior,
        navigationIcon = {
            ArrowBackButton {
                (context as ComponentActivity).finish()
            }
        },
    )
}

fun MaterialColorScheme.applyTonalElevation(backgroundColor: Color, elevation: Dp): Color {
    return if (backgroundColor == surface) {
        surfaceColorAtElevation(elevation)
    } else {
        backgroundColor
    }
}

@Composable
internal fun containerColor(
    containerColor: Color, scrolledContainerColor: Color, colorTransitionFraction: Float
): Color {
    return lerp(
        containerColor,
        scrolledContainerColor,
        FastOutLinearInEasing.transform(colorTransitionFraction)
    )
}

@ExperimentalMaterial3Api
@Composable
internal fun ColumnExtendedTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    onArrowBackPressed: () -> Unit,
    content: @Composable (appBarContainerColor: Color) -> Unit = {}
) {
    // Sets the app bar's height offset to collapse the entire bar's height when content is
    // scrolled.
    val heightOffsetLimit = with(LocalDensity.current) { -TopBarTokens.ContainerHeight.toPx() }
    SideEffect {
        if (scrollBehavior.state.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior.state.heightOffsetLimit = heightOffsetLimit
        }
    }

    val containerColor = MaterialTheme.colorScheme.surface
    val scrolledContainerColor = MaterialTheme.colorScheme.applyTonalElevation(
        backgroundColor = containerColor,
        elevation = TopBarTokens.OnScrollContainerElevation
    )

    // Obtain the container color from the TopAppBarColors using the `overlapFraction`. This
    // ensures that the colors will adjust whether the app bar behavior is pinned or scrolled.
    // This may potentially animate or interpolate a transition between the container-color and the
    // container's scrolled-color according to the app bar's scroll state.
    val colorTransitionFraction = scrollBehavior.state.overlappedFraction
    val fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    val appBarContainerColor by animateColorAsState(
        targetValue = containerColor(containerColor, scrolledContainerColor, fraction),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ColorAnimation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = appBarContainerColor
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                // clip after padding so we don't show the title over the inset area
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .paddingTop(TopBarTokens.Padding)
                    .paddingHorizontal(TopBarTokens.Padding),
                contentAlignment = Alignment.CenterStart
            ) {
                ArrowBackButton(onArrowBackPressed)
                TopBarTitle(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, text = title)
            }
            content(appBarContainerColor = appBarContainerColor)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SlotScope.ManifestTopBar(scrollBehavior: TopAppBarScrollBehavior, title: String, selectedTabIndex: MutableState<Int>, titles: List<String>) {
    ColumnExtendedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = title,
        onArrowBackPressed = { navController.popBackStack() }
    ) { appBarContainerColor ->
        TabRow(selectedTabIndex = selectedTabIndex.value,
            containerColor = appBarContainerColor,
            divider = {}) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex.value == index,
                    onClick = { selectedTabIndex.value = index },
                    text = { TabText(text = title) }
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ProcessingTopBar(scrollBehavior: TopAppBarScrollBehavior, title: String) {
    val context = LocalContext.current
    CenterAlignedTopAppBar(
        title = { TopBarTitle(text = title) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            ArrowBackButton {
                (context as ComponentActivity).finish()
            }
        },
    )
}

@ExperimentalMaterial3Api
@Composable
fun CompletionTopBar(scrollBehavior: TopAppBarScrollBehavior, title: String) {
    val context = LocalContext.current
    CenterAlignedTopAppBar(
        title = { TopBarTitle(text = title) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            ArrowBackButton {
                (context as ComponentActivity).finish()
            }
        },
    )
}
