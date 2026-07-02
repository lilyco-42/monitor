package com.lyco.monitor

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.lyco.monitor.ui.theme.MonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonitorTheme {
                Monitor()
            }
        }
    }
}

@Composable
fun Monitor() {
    // 状态管理
    var isMonitoring by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("18:00") }
    var endTime by remember { mutableStateOf("6:00") }
    
    // 视频源地址
    // 使用一个在大陆更稳定的视频源，例如阿里云的示例或 W3C 镜像
    var videoUrl by remember { mutableStateOf("https://media.w3.org/2010/05/sintel/trailer.mp4") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing) // 自动避开系统栏（包括导航栏、状态栏）
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 左侧：视频监控区 ---
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .background(Color.Black) // 视频未加载时显示黑色背景
        ) {
            if (isMonitoring && videoUrl.isNotEmpty()) {
                // 仅在开启监测且有 URL 时显示播放器
                VideoPlayer(videoUrl)
            } else {
                // 停止监测时的显示
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("请点击“开启监测”按钮", color = Color.Gray)
                }
            }

            // 上层：漂浮控制按钮（方向键）
            // 建议：为按钮添加半透明背景，使其在视频上方更清晰，并增加边距
            
            // 向上
            IconButton(
                onClick = { /* TODO: 向上移动 */ },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, "上", tint = Color.White)
            }
            
            // 向下
            IconButton(
                onClick = { /* TODO: 向下移动 */ },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, "下", tint = Color.White)
            }
            
            // 向左：增加 start padding 以避开系统退出手势/按键
            IconButton(
                onClick = { /* TODO: 向左移动 */ },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp) // 增加边距，防止与系统返回键重合
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "左", tint = Color.White)
            }
            
            // 向右
            IconButton(
                onClick = { /* TODO: 向右移动 */ },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "右", tint = Color.White)
            }
        }

        // --- 右侧：控制面板 ---
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("监测系统状态", style = MaterialTheme.typography.titleMedium)
            // 开关按钮
            Button(
                onClick = { isMonitoring = !isMonitoring },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMonitoring) Color.Red else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(if (isMonitoring) "停止监测" else "开启监测")
            }
            Spacer(modifier = Modifier.height(20.dp))

            // 状态灯图标：开启绿色，关闭灰色
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isMonitoring) Color.Green else Color.Gray
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 自动化设置卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "定时监测时段",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 开始与结束时间（同排显示，明确时间范围）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("开始", style = MaterialTheme.typography.labelSmall) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true
                        )

                        Text("至", style = MaterialTheme.typography.bodySmall)

                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("结束", style = MaterialTheme.typography.labelSmall) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "视频源地址",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall,
                        label = { Text("URL", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val TAG = "VideoPlayerDebug"

    // 创建并管理 ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "ExoPlayer Error: ${error.message}, Code: ${error.errorCode}")
                }
                override fun onPlaybackStateChanged(state: Int) {
                    Log.d(TAG, "Playback State: $state")
                }
            })
        }
    }

    // 当 URL 改变时更新 MediaItem
    LaunchedEffect(url) {
        if (url.isNotEmpty()) {
            Log.d(TAG, "ExoPlayer loading URL: $url")
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // 释放资源
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true // 显示控制栏
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
