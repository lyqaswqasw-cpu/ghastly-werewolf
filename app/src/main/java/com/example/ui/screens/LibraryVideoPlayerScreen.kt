package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.ChannelEngagementStats
import com.example.data.model.PlaybackRequest
import com.example.ui.components.OrderedColorfulDivider
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulBottomBarBlack
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulLiveRed
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulTeamAwayCyan
import com.example.ui.theme.FoulTextSecondary
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@OptIn(UnstableApi::class)
private enum class PlayerAspectMode(val resizeMode: Int, val labelAr: String) {
    FIT(AspectRatioFrameLayout.RESIZE_MODE_FIT, "الأبعاد الأصلية"),
    ZOOM(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, "تكبير الأطراف"),
    FILL(AspectRatioFrameLayout.RESIZE_MODE_FILL, "ملء الشاشة")
}

/**
 * Real Video Quality Track inspected directly from the active ExoPlayer Media3 stream
 */
@Immutable
private data class RealVideoQualityOption(
    val id: String,
    val trackGroup: TrackGroup,
    val trackIndex: Int,
    val width: Int,
    val height: Int,
    val bitrateBps: Int,
    val frameRate: Float,
    val shortBadge: String,
    val detailSubtitle: String
)

private fun formatResolutionShortBadge(width: Int, height: Int): String {
    val effectiveHeight = if (height > 0) height else (width * 9) / 16
    return when {
        effectiveHeight >= 2160 || width >= 3840 -> "4K UHD"
        effectiveHeight >= 1440 || width >= 2560 -> "2K QHD"
        effectiveHeight >= 1080 || width >= 1920 -> "1080p FHD"
        effectiveHeight >= 720 || width >= 1280 -> "720p HD"
        effectiveHeight >= 540 -> "576p SD"
        effectiveHeight >= 480 -> "480p SD"
        effectiveHeight >= 360 -> "360p"
        effectiveHeight > 0 -> "${effectiveHeight}p"
        else -> "HD"
    }
}

private fun formatQualityDetailSubtitle(width: Int, height: Int, bitrateBps: Int, frameRate: Float): String {
    val parts = mutableListOf<String>()
    if (width > 0 && height > 0) {
        parts.add("${width}×${height}")
    } else if (height > 0) {
        parts.add("${height}p")
    }
    if (frameRate >= 48f) {
        parts.add("${frameRate.toInt()}fps")
    }
    if (bitrateBps > 0) {
        val mbps = bitrateBps / 1_000_000f
        if (mbps >= 1f) {
            parts.add(String.format(Locale.US, "%.1f Mbps", mbps))
        } else {
            val kbps = bitrateBps / 1_000
            if (kbps > 0) parts.add("$kbps Kbps")
        }
    }
    return parts.joinToString(" • ").ifBlank { "بث مباشر أصلي" }
}

@OptIn(UnstableApi::class)
@Composable
fun LibraryVideoPlayerScreen(
    playbackRequest: PlaybackRequest,
    channelStats: ChannelEngagementStats? = null,
    showChannelViewsAndLikes: Boolean = true,
    onToggleChannelLike: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Immediately lock into Landscape mode, keep screen awake, & enter immersive fullscreen
    DisposableEffect(activity) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val insetsController = if (window != null) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else null
        insetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = originalOrientation
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val candidateUrls = playbackRequest.candidateUrls
    var currentUrlIndex by remember(playbackRequest) { mutableIntStateOf(0) }
    var isBuffering by remember(playbackRequest) { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var allExtensionsFailed by remember(playbackRequest) { mutableStateOf(false) }

    var durationMs by remember(playbackRequest) { mutableLongStateOf(0L) }
    var positionMs by remember(playbackRequest) { mutableLongStateOf(0L) }
    var bufferedPositionMs by remember(playbackRequest) { mutableLongStateOf(0L) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableLongStateOf(0L) }

    var isControlsVisible by remember { mutableStateOf(true) }
    var aspectMode by remember { mutableStateOf(PlayerAspectMode.FIT) }
    var showAspectToast by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Real Stream Quality State (inspected from active ExoPlayer VideoSize & Tracks)
    var realDecodedWidth by remember(playbackRequest, currentUrlIndex) { mutableIntStateOf(0) }
    var realDecodedHeight by remember(playbackRequest, currentUrlIndex) { mutableIntStateOf(0) }
    var realStreamBitrate by remember(playbackRequest, currentUrlIndex) { mutableIntStateOf(0) }
    var realStreamFps by remember(playbackRequest, currentUrlIndex) { mutableFloatStateOf(0f) }
    var availableQualityTracks by remember(playbackRequest, currentUrlIndex) {
        mutableStateOf<List<RealVideoQualityOption>>(emptyList())
    }
    // null = Auto quality ("تلقائي"), non-null = locked to specific RealVideoQualityOption.id
    var selectedQualityTrackId by remember(playbackRequest, currentUrlIndex) {
        mutableStateOf<String?>(null)
    }
    var isQualityPopupOpen by remember { mutableStateOf(false) }

    // Double-tap / Button seek feedback (-10s or +10s)
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var seekFeedbackIsForward by remember { mutableStateOf(true) }
    var seekFeedbackTrigger by remember { mutableIntStateOf(0) }

    // Ultra-Smooth Anti-Stutter ExoPlayer Engine tuned for Live IPTV & High-Bitrate VOD
    val exoPlayer = remember(context, playbackRequest.isLiveChannel) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("VLC/3.0.20 LibVLC/3.0.20")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(if (playbackRequest.isLiveChannel) 10_000 else 15_000)
            .setReadTimeoutMs(if (playbackRequest.isLiveChannel) 18_000 else 30_000)
            .setKeepPostFor302Redirects(true)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val extractorsFactory = DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)
            .setTsExtractorFlags(
                DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES or
                    DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS
            )

        // Tuned LoadControl:
        // - Live Channels: Ultra-fast 850ms initial playback start for instant server switching + 45s buffer against drops
        // - Library VOD: Deep 75s forward buffer + 20s RAM back-buffer so -10s rewind & high-bitrate playback never stutter
        val loadControl = if (playbackRequest.isLiveChannel) {
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    /* minBufferMs = */ 14_000,
                    /* maxBufferMs = */ 45_000,
                    /* bufferForPlaybackMs = */ 850,
                    /* bufferForPlaybackAfterRebufferMs = */ 1_800
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
        } else {
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    /* minBufferMs = */ 25_000,
                    /* maxBufferMs = */ 75_000,
                    /* bufferForPlaybackMs = */ 1_400,
                    /* bufferForPlaybackAfterRebufferMs = */ 2_600
                )
                .setBackBuffer(
                    /* backBufferDurationMs = */ 20_000,
                    /* retainBackBufferFromKeyframe = */ true
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
        }

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory))
            .setLoadControl(loadControl)
            .setSeekBackIncrementMs(10_000L)
            .setSeekForwardIncrementMs(10_000L)
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            .build()
            .apply {
                playWhenReady = true
            }
    }

    // Load current candidate URL (Live Server or VOD mp4 -> mkv -> m3u8) cleanly and rapidly
    LaunchedEffect(currentUrlIndex, allExtensionsFailed, playbackRequest) {
        if (!allExtensionsFailed && currentUrlIndex in candidateUrls.indices) {
            isBuffering = true
            // Clear any previous manual video quality override when switching servers/streams
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .build()
            selectedQualityTrackId = null

            val urlToPlay = candidateUrls[currentUrlIndex].trim()
            val parsedUri = when {
                urlToPlay.startsWith("file://") -> Uri.parse(urlToPlay)
                urlToPlay.startsWith("/") -> Uri.fromFile(File(urlToPlay))
                else -> Uri.parse(urlToPlay)
            }
            val mediaItem = MediaItem.fromUri(parsedUri)
            exoPlayer.setMediaItem(mediaItem, /* resetPosition = */ true)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } else if (candidateUrls.isEmpty()) {
            isBuffering = false
            allExtensionsFailed = true
        }
    }

    // Helper to inspect tracks & active video format from ExoPlayer
    val updateRealQualityFromPlayer: (Tracks, VideoSize) -> Unit = remember(exoPlayer) {
        { tracks: Tracks, videoSize: VideoSize ->
            val discovered = mutableListOf<RealVideoQualityOption>()
            var activeFormatHeight = 0
            var activeFormatWidth = 0
            var activeFormatBitrate = 0
            var activeFormatFps = 0f

            for (groupIndex in 0 until tracks.groups.size) {
                val group = tracks.groups[groupIndex]
                if (group.type != C.TRACK_TYPE_VIDEO) continue
                val mediaTrackGroup = group.mediaTrackGroup
                for (trackIndex in 0 until group.length) {
                    val format = group.getTrackFormat(trackIndex)
                    val w = if (format.width > 0) format.width else 0
                    val h = if (format.height > 0) format.height else 0
                    val br = if (format.bitrate > 0) format.bitrate else 0
                    val fps = if (format.frameRate > 0f) format.frameRate else 0f

                    if (group.isTrackSelected(trackIndex)) {
                        if (h > 0) activeFormatHeight = h
                        if (w > 0) activeFormatWidth = w
                        if (br > 0) activeFormatBitrate = br
                        if (fps > 0f) activeFormatFps = fps
                    }

                    if (group.isTrackSupported(trackIndex) && (h > 0 || w > 0 || br > 0)) {
                        val badge = formatResolutionShortBadge(w, h)
                        val subtitle = formatQualityDetailSubtitle(w, h, br, fps)
                        val optionId = "${groupIndex}_${trackIndex}_${w}x${h}_$br"
                        discovered.add(
                            RealVideoQualityOption(
                                id = optionId,
                                trackGroup = mediaTrackGroup,
                                trackIndex = trackIndex,
                                width = w,
                                height = h,
                                bitrateBps = br,
                                frameRate = fps,
                                shortBadge = badge,
                                detailSubtitle = subtitle
                            )
                        )
                    }
                }
            }

            // Deduplicate by resolution + bitrate and sort from highest quality to lowest
            val sortedDistinct = discovered
                .distinctBy { "${it.height}_${it.width}_${it.bitrateBps / 50_000}" }
                .sortedWith(
                    compareByDescending<RealVideoQualityOption> { it.height }
                        .thenByDescending { it.width }
                        .thenByDescending { it.bitrateBps }
                )

            availableQualityTracks = sortedDistinct

            val finalWidth = if (videoSize.width > 0) videoSize.width else activeFormatWidth
            val finalHeight = if (videoSize.height > 0) videoSize.height else activeFormatHeight
            if (finalWidth > 0) realDecodedWidth = finalWidth
            if (finalHeight > 0) realDecodedHeight = finalHeight
            if (activeFormatBitrate > 0) realStreamBitrate = activeFormatBitrate
            if (activeFormatFps > 0f) realStreamFps = activeFormatFps
        }
    }

    // Attach ExoPlayer listener for playback state, real video resolution, multi-track quality detection, and fallback
    DisposableEffect(exoPlayer, candidateUrls) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        allExtensionsFailed = false
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        updateRealQualityFromPlayer(exoPlayer.currentTracks, exoPlayer.videoSize)
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                        isPlaying = false
                    }
                    Player.STATE_IDLE -> Unit
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    realDecodedWidth = videoSize.width
                    realDecodedHeight = videoSize.height
                }
                updateRealQualityFromPlayer(exoPlayer.currentTracks, videoSize)
            }

            override fun onTracksChanged(tracks: Tracks) {
                updateRealQualityFromPlayer(tracks, exoPlayer.videoSize)
            }

            override fun onPlayerError(error: PlaybackException) {
                // Silent & rapid fallback to next candidate URL (for VOD mp4 -> mkv -> m3u8)
                if (!playbackRequest.isLiveChannel && currentUrlIndex + 1 < candidateUrls.size) {
                    isBuffering = true
                    currentUrlIndex += 1
                } else {
                    isBuffering = false
                    allExtensionsFailed = true
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Poll playback position & buffer smoothly for VOD seekbar without interrupting user scrubbing
    LaunchedEffect(exoPlayer, isPlaying, playbackRequest.isLiveChannel, isScrubbing) {
        if (playbackRequest.isLiveChannel) return@LaunchedEffect
        while (true) {
            if (!isScrubbing) {
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            }
            bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L)
            val dur = exoPlayer.duration
            if (dur > 0L) durationMs = dur
            delay(250L)
        }
    }

    // Auto-hide controls after 4.5 seconds of active playback (unless scrubbing or quality popup is open)
    LaunchedEffect(isControlsVisible, isPlaying, isBuffering, isScrubbing, isQualityPopupOpen) {
        if (
            isControlsVisible &&
            isPlaying &&
            !isBuffering &&
            !allExtensionsFailed &&
            !isScrubbing &&
            !isQualityPopupOpen
        ) {
            delay(4500L)
            isControlsVisible = false
        }
    }

    // Hide seek feedback badge after 750ms
    LaunchedEffect(seekFeedbackTrigger) {
        if (seekFeedbackText != null) {
            delay(750L)
            seekFeedbackText = null
        }
    }

    // Hide aspect ratio toast after 1.2s
    LaunchedEffect(showAspectToast, aspectMode) {
        if (showAspectToast) {
            delay(1200L)
            showAspectToast = false
        }
    }

    // Helper for accurate ±10s seeking (Left = -10s Rewind, Right = +10s Forward)
    val seekByDeltaMs: (Long) -> Unit = { deltaMs ->
        val totalDur = exoPlayer.duration.takeIf { it > 0L } ?: durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE
        val targetPos = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, totalDur)
        exoPlayer.seekTo(targetPos)
        positionMs = targetPos
        scrubPositionMs = targetPos
        seekFeedbackIsForward = deltaMs > 0L
        seekFeedbackText = if (deltaMs > 0L) "+10 ثواني تقديم" else "-10 ثواني تأخير"
        seekFeedbackTrigger++
    }

    // Compute the real quality badge text displayed on the Quality Icon Button
    val realActiveResolutionBadge = remember(realDecodedWidth, realDecodedHeight, availableQualityTracks, selectedQualityTrackId, isBuffering) {
        val lockedOption = availableQualityTracks.firstOrNull { it.id == selectedQualityTrackId }
        when {
            lockedOption != null -> lockedOption.shortBadge
            realDecodedHeight > 0 || realDecodedWidth > 0 -> formatResolutionShortBadge(realDecodedWidth, realDecodedHeight)
            availableQualityTracks.isNotEmpty() -> availableQualityTracks.first().shortBadge
            isBuffering -> "فحص الجودة..."
            else -> "HD تلقائي"
        }
    }

    val realResolutionDimensionsText = remember(realDecodedWidth, realDecodedHeight, realStreamBitrate, realStreamFps) {
        formatQualityDetailSubtitle(realDecodedWidth, realDecodedHeight, realStreamBitrate, realStreamFps)
    }

    val hasMultipleQualities = availableQualityTracks.size > 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(playbackRequest.isLiveChannel, allExtensionsFailed, isQualityPopupOpen) {
                detectTapGestures(
                    onTap = {
                        if (isQualityPopupOpen) {
                            isQualityPopupOpen = false
                        } else {
                            isControlsVisible = !isControlsVisible
                        }
                    },
                    onDoubleTap = { offset ->
                        if (!playbackRequest.isLiveChannel && !allExtensionsFailed) {
                            val screenWidth = size.width
                            // Universal Video Timeline Convention:
                            // Right side of screen -> +10s forward (تقديم 10 ثواني)
                            // Left side of screen -> -10s backward (تأخير 10 ثواني)
                            if (offset.x > screenWidth / 2f) {
                                seekByDeltaMs(10_000L)
                            } else {
                                seekByDeltaMs(-10_000L)
                            }
                        }
                    }
                )
            }
    ) {
        // 1. Hardware-Accelerated Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = aspectMode.resizeMode
                    keepScreenOn = true
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player !== exoPlayer) {
                    playerView.player = exoPlayer
                }
                if (playerView.resizeMode != aspectMode.resizeMode) {
                    playerView.resizeMode = aspectMode.resizeMode
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Sleek Buffering Indicator
        if (isBuffering && !allExtensionsFailed) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = FoulBottomBarBlack.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.55f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = FoulPurpleBright,
                            strokeWidth = 3.5.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_loading_spinner")
                        )
                        Text(
                            text = if (playbackRequest.isLiveChannel) "جاري الاتصال بالسيرفر المباشر..." else "جاري تحضير العرض بأعلى سلاسة...",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Error State if all extensions / active server failed
        if (allExtensionsFailed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FoulBlackBg.copy(alpha = 0.94f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                    border = BorderStroke(1.dp, FoulLiveRed.copy(alpha = 0.6f)),
                    modifier = Modifier.widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "خطأ في التشغيل",
                            tint = FoulLiveRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "تعذر تشغيل هذا المصدر حالياً",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (playbackRequest.isLiveChannel && playbackRequest.servers.size > 1) {
                            Text(
                                text = "يمكنك اختيار سيرفر آخر فوراً من القائمة بالأسفل أو إعادة المحاولة",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(playbackRequest.servers) { idx, srv ->
                                    val isCurrent = idx == currentUrlIndex
                                    Surface(
                                        color = if (isCurrent) FoulPurpleBright else FoulCardElevated,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                allExtensionsFailed = false
                                                isBuffering = true
                                                currentUrlIndex = idx
                                            }
                                    ) {
                                        Text(
                                            text = srv.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    allExtensionsFailed = false
                                    isBuffering = true
                                    val urlToRetry = candidateUrls.getOrNull(currentUrlIndex)
                                    if (urlToRetry != null) {
                                        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(urlToRetry)), true)
                                        exoPlayer.prepare()
                                        exoPlayer.playWhenReady = true
                                    } else {
                                        currentUrlIndex = 0
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                                modifier = Modifier.testTag("player_retry_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("إعادة المحاولة", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = FoulCardElevated),
                                modifier = Modifier.testTag("player_error_back_button")
                            ) {
                                Text("رجوع", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 4. Double-Tap / Button Seek Feedback Badge (Pinned to Physical Left for -10s and Physical Right for +10s)
        AnimatedVisibility(
            visible = seekFeedbackText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(
                if (seekFeedbackIsForward) AbsoluteAlignment.CenterRight else AbsoluteAlignment.CenterLeft
            )
        ) {
            Surface(
                color = FoulPurpleDeep.copy(alpha = 0.90f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                modifier = Modifier.padding(horizontal = 42.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (seekFeedbackIsForward) Icons.Default.Forward10 else Icons.Default.Replay10,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = seekFeedbackText.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 5. Aspect Ratio Mode Toast Badge
        AnimatedVisibility(
            visible = showAspectToast,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 74.dp)
        ) {
            Surface(
                color = FoulPurpleDeep.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, FoulPurpleBright)
            ) {
                Text(
                    text = aspectMode.labelAr,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }

        // 6. Sleek Professional Player Controls Overlay
        AnimatedVisibility(
            visible = isControlsVisible && !allExtensionsFailed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.84f),
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.22f),
                                Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
            ) {
                // ==================== TOP BAR ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title, Subtitle & Back Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(FoulCardDark.copy(alpha = 0.85f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (playbackRequest.isLiveChannel) {
                                    Surface(
                                        color = FoulLiveRed,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                            Text(
                                                text = "مباشر",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = playbackRequest.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (playbackRequest.subtitle.isNotBlank()) {
                                Text(
                                    text = playbackRequest.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    // Top Bar Action Pills: Views & Likes (Live), Real Stream Quality Selector, Speed (VOD), Aspect Ratio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Live Channel Engagement Pills (Views Counter + Like Button)
                        if (playbackRequest.isLiveChannel && showChannelViewsAndLikes) {
                            val viewsCount = channelStats?.views ?: 0L
                            val likesCount = channelStats?.likes ?: 0L
                            val isLiked = channelStats?.isLikedByMe == true

                            Surface(
                                color = FoulCardDark.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .testTag("player_views_counter_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "عدد المشاهدات",
                                        tint = FoulTeamAwayCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$viewsCount",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = if (isLiked) FoulLiveRed.copy(alpha = 0.90f) else FoulPurpleDeep.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onToggleChannelLike() }
                                    .border(
                                        width = 1.dp,
                                        color = if (isLiked) Color.White else Color.White.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("player_like_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "إعجاب بالقناة",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$likesCount",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Real Stream Quality Indicator & Selector Button
                        // ("أيقونة بشكل تعرض الجودة الفعلية لرابط بث السيرفر وبشكل حقيقي و فعلي وإذا كان يحتوي على أكثر من جودة يضهر بشكل تلقائيا وإمكانية التحكم بجودات")
                        Surface(
                            color = if (isQualityPopupOpen) FoulPurpleBright else FoulPurpleDeep.copy(alpha = 0.90f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isQualityPopupOpen = !isQualityPopupOpen
                                }
                                .border(
                                    width = 1.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(FoulTeamAwayCyan, FoulPurpleBright, Color.White.copy(alpha = 0.5f))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("player_quality_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasMultipleQualities) Icons.Default.Tune else Icons.Default.HighQuality,
                                    contentDescription = "جودة البث الفعلية",
                                    tint = FoulTeamAwayCyan,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = if (selectedQualityTrackId == null && hasMultipleQualities) {
                                        "تلقائي • $realActiveResolutionBadge"
                                    } else {
                                        realActiveResolutionBadge
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (hasMultipleQualities) {
                                    Surface(
                                        color = FoulTeamAwayCyan.copy(alpha = 0.24f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${availableQualityTracks.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = FoulTeamAwayCyan,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Playback Speed Pill for Library VOD (1x -> 1.25x -> 1.5x -> 2x)
                        if (!playbackRequest.isLiveChannel) {
                            Surface(
                                color = FoulCardDark.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val nextSpeed = when (playbackSpeed) {
                                            1.0f -> 1.25f
                                            1.25f -> 1.5f
                                            1.5f -> 2.0f
                                            else -> 1.0f
                                        }
                                        playbackSpeed = nextSpeed
                                        exoPlayer.playbackParameters = PlaybackParameters(nextSpeed)
                                    }
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .testTag("player_speed_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "سرعة التشغيل",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${playbackSpeed}x",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Aspect Ratio / Screen Fit Toggle Button
                        Surface(
                            color = FoulCardDark.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    aspectMode = when (aspectMode) {
                                        PlayerAspectMode.FIT -> PlayerAspectMode.ZOOM
                                        PlayerAspectMode.ZOOM -> PlayerAspectMode.FILL
                                        PlayerAspectMode.FILL -> PlayerAspectMode.FIT
                                    }
                                    showAspectToast = true
                                }
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .testTag("player_aspect_ratio_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = when (aspectMode) {
                                        PlayerAspectMode.FIT -> Icons.Default.AspectRatio
                                        PlayerAspectMode.ZOOM -> Icons.Default.Fullscreen
                                        PlayerAspectMode.FILL -> Icons.Default.FullscreenExit
                                    },
                                    contentDescription = "تغيير أبعاد الشاشة",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = aspectMode.labelAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ==================== CENTER TRANSPORT CONTROLS (LTR ORDER) ====================
                // Enforced Left-to-Right layout so:
                // - Left Button = -10s Rewind (Replay10)
                // - Center Button = Play / Pause
                // - Right Button = +10s Forward (Forward10)
                // Matching the Left-to-Right video seekbar 100% naturally!
                if (!isBuffering) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(38.dp)
                        ) {
                            if (!playbackRequest.isLiveChannel) {
                                // LEFT: -10s Rewind Button
                                IconButton(
                                    onClick = { seekByDeltaMs(-10_000L) },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(FoulBottomBarBlack.copy(alpha = 0.78f))
                                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                        .testTag("player_replay_10_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay10,
                                        contentDescription = "تأخير 10 ثواني",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            // CENTER: Play / Pause Button
                            IconButton(
                                onClick = {
                                    if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                },
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(FoulPurpleBright, FoulPurpleDeep)
                                        )
                                    )
                                    .border(2.dp, Color.White, CircleShape)
                                    .testTag("player_center_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            if (!playbackRequest.isLiveChannel) {
                                // RIGHT: +10s Forward Button
                                IconButton(
                                    onClick = { seekByDeltaMs(10_000L) },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(FoulBottomBarBlack.copy(alpha = 0.78f))
                                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                        .testTag("player_forward_10_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forward10,
                                        contentDescription = "تقديم 10 ثواني",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==================== BOTTOM BAR ====================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    if (playbackRequest.isLiveChannel) {
                        // Ultra-Fast Live Channel Server Switcher & Real Stream Info Bar
                        Surface(
                            color = FoulBottomBarBlack.copy(alpha = 0.86f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = FoulPurpleBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "سيرفرات البث السريعة:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )

                                    if (playbackRequest.servers.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) {
                                            itemsIndexed(playbackRequest.servers) { idx, srv ->
                                                val selected = idx == currentUrlIndex
                                                Surface(
                                                    color = if (selected) FoulPurpleBright else FoulCardDark,
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            if (currentUrlIndex != idx || allExtensionsFailed) {
                                                                allExtensionsFailed = false
                                                                isBuffering = true
                                                                currentUrlIndex = idx
                                                            }
                                                        }
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (selected) Color.White else FoulDivider,
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .testTag("live_server_pill_$idx")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        if (selected) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(7.dp)
                                                                    .clip(CircleShape)
                                                                    .background(FoulLiveGreen)
                                                            )
                                                        }
                                                        Text(
                                                            text = srv.name,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = Color.White,
                                                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Real Verified Stream Resolution Pill on Right of Live Bottom Bar
                                Surface(
                                    color = FoulCardElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FoulTeamAwayCyan.copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { isQualityPopupOpen = !isQualityPopupOpen }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Hd,
                                            contentDescription = null,
                                            tint = FoulTeamAwayCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (realDecodedWidth > 0 && realDecodedHeight > 0) {
                                                "$realActiveResolutionBadge (${realDecodedWidth}×${realDecodedHeight})"
                                            } else {
                                                realActiveResolutionBadge
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // ==================== LIBRARY VOD PROFESSIONAL TIMELINE ====================
                        // Wrapped in LayoutDirection.Ltr so:
                        // - 00:00 starts on the Left and progresses to Total Duration on the Right
                        // - Dragging right advances the video (+), dragging left rewinds the video (-)
                        // - Scrubbing is 100% smooth without fighting position polling
                        val activeDisplayPositionMs = if (isScrubbing) scrubPositionMs else positionMs
                        val safeDurationMs = durationMs.coerceAtLeast(1L)
                        val progressFraction = (activeDisplayPositionMs.toFloat() / safeDurationMs.toFloat()).coerceIn(0f, 1f)
                        val bufferedFraction = (bufferedPositionMs.toFloat() / safeDurationMs.toFloat()).coerceIn(0f, 1f)
                        val remainingMs = (durationMs - activeDisplayPositionMs).coerceAtLeast(0L)

                        Surface(
                            color = FoulBottomBarBlack.copy(alpha = 0.84f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    // Custom Layered Seekbar with Buffer Track + Active Purple/Cyan Track + Smooth Thumb
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Background & Buffered Track Layer
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color.White.copy(alpha = 0.20f))
                                        ) {
                                            // Buffered Progress Bar
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(bufferedFraction)
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color.White.copy(alpha = 0.42f))
                                            )
                                            // Active Played Progress Bar
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(progressFraction)
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            colors = listOf(FoulPurpleBright, FoulTeamAwayCyan)
                                                        )
                                                    )
                                            )
                                        }

                                        // Interactive Smooth Slider on top
                                        Slider(
                                            value = activeDisplayPositionMs.toFloat().coerceIn(0f, safeDurationMs.toFloat()),
                                            onValueChange = { newVal ->
                                                isScrubbing = true
                                                scrubPositionMs = newVal.toLong()
                                            },
                                            onValueChangeFinished = {
                                                val targetSeek = scrubPositionMs.coerceIn(0L, safeDurationMs)
                                                exoPlayer.seekTo(targetSeek)
                                                positionMs = targetSeek
                                                isScrubbing = false
                                            },
                                            valueRange = 0f..safeDurationMs.toFloat(),
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color.White,
                                                activeTrackColor = Color.Transparent,
                                                inactiveTrackColor = Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("player_timeline_slider")
                                        )
                                    }

                                    // Time Labels Row (Left = Elapsed Time, Center = Real Quality & Scrub Preview, Right = Total / Remaining)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = formatDurationMs(activeDisplayPositionMs),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isScrubbing) FoulTeamAwayCyan else Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "/",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = formatDurationMs(durationMs),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White.copy(alpha = 0.82f),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        // Center Status Pill (Shows scrub target while dragging or real resolution info)
                                        Text(
                                            text = if (isScrubbing) {
                                                "الانتقال إلى ${formatDurationMs(scrubPositionMs)}"
                                            } else if (realDecodedWidth > 0 && realDecodedHeight > 0) {
                                                "الجودة الفعلية: $realActiveResolutionBadge (${realDecodedWidth}×${realDecodedHeight})"
                                            } else {
                                                "مرتين يميناً (+10ث) • مرتين يساراً (-10ث)"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isScrubbing) FoulGoldStar else Color.White.copy(alpha = 0.72f),
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = "-${formatDurationMs(remainingMs)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.78f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. COMPACT PHONE-FRIENDLY REAL STREAM QUALITY SELECTOR POPUP
        // ("إمكانية التحكم بجودات عند الضغط على الأيقونة ضهور الجودات وإمكانية الاختيار أو تركها تلقائيا ويكون بشكل عرض صغير كي يناسب الهاتف وبشكل جميل ومرتب جدا")
        AnimatedVisibility(
            visible = isQualityPopupOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, start = 18.dp, end = 18.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulBottomBarBlack.copy(alpha = 0.96f)),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(FoulTeamAwayCyan, FoulPurpleBright)
                    )
                ),
                modifier = Modifier
                    .widthIn(min = 230.dp, max = 285.dp)
                    .heightIn(max = 265.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .testTag("player_quality_popup")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Compact Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = FoulTeamAwayCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "جودة بث السيرفر الفعلية",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (realDecodedWidth > 0 && realDecodedHeight > 0) {
                                        "نشط الآن: $realActiveResolutionBadge • $realResolutionDimensionsText"
                                    } else {
                                        "نشط الآن: $realActiveResolutionBadge"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = FoulTeamAwayCyan
                                )
                            }
                        }

                        IconButton(
                            onClick = { isQualityPopupOpen = false },
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(FoulCardDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    OrderedColorfulDivider(thickness = 1.dp)

                    // Scrollable Compact Quality Options List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Option 1: Auto Quality ("تلقائي")
                        item(key = "quality_auto") {
                            val isAutoSelected = selectedQualityTrackId == null
                            Surface(
                                color = if (isAutoSelected) FoulPurpleDeep.copy(alpha = 0.85f) else FoulCardDark,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isAutoSelected) FoulPurpleBright else FoulDivider
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedQualityTrackId = null
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                            .build()
                                        isQualityPopupOpen = false
                                    }
                                    .testTag("quality_option_auto")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (isAutoSelected) FoulGoldStar else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "تلقائي (موصى به)",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = if (realDecodedHeight > 0) {
                                                    "يضبط الجودة تلقائياً ($realActiveResolutionBadge)"
                                                } else {
                                                    "أفضل استقرار وسرعة بدون تقطيع"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = FoulTextSecondary
                                            )
                                        }
                                    }
                                    if (isAutoSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FoulLiveGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Discovered Stream Quality Tracks
                        if (availableQualityTracks.isNotEmpty()) {
                            items(
                                items = availableQualityTracks,
                                key = { it.id }
                            ) { option ->
                                val isSelected = selectedQualityTrackId == option.id
                                Surface(
                                    color = if (isSelected) FoulPurpleDeep.copy(alpha = 0.85f) else FoulCardDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) FoulTeamAwayCyan else FoulDivider
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedQualityTrackId = option.id
                                            val override = TrackSelectionOverride(
                                                option.trackGroup,
                                                listOf(option.trackIndex)
                                            )
                                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                .buildUpon()
                                                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                                .addOverride(override)
                                                .build()
                                            isQualityPopupOpen = false
                                        }
                                        .testTag("quality_option_${option.height}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = option.shortBadge,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = option.detailSubtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = FoulTextSecondary
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = FoulLiveGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (realDecodedWidth > 0 && realDecodedHeight > 0) {
                            // Single native quality stream info row when stream doesn't expose separate HLS variant tracks
                            item(key = "native_single_quality") {
                                Surface(
                                    color = FoulCardDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FoulDivider),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "$realActiveResolutionBadge (جودة المصدر الأصلية)",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "الدقة الفعلية: ${realDecodedWidth}×${realDecodedHeight}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = FoulTextSecondary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FoulTeamAwayCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
