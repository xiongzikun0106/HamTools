package com.ham.tools.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ham.tools.R
import kotlinx.coroutines.launch

private val LargeCornerRadius = 28.dp

/**
 * 引导页面
 *
 * 用户首次打开应用时显示；若在第 3 页填写了呼号，则进入可选的 QRZ Logbook 连接步骤。
 */
@Composable
fun OnboardingScreen(
    onComplete: (OnboardingFinish) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var callsign by remember { mutableStateOf("") }
    var showQrStep by remember { mutableStateOf(false) }
    var qrzKey by remember { mutableStateOf("") }
    var qrzAutoSync by remember { mutableStateOf(false) }
    var qrzReplace by remember { mutableStateOf(false) }
    var verifiedOk by remember { mutableStateOf(false) }
    var showUnverifiedDialog by remember { mutableStateOf(false) }

    val verifyUi by viewModel.verifyUi.collectAsStateWithLifecycle()

    fun finishQrLater() {
        onComplete(OnboardingFinish(callsign = callsign.uppercase().trim()))
    }

    fun finishWithQrz(saveKey: Boolean) {
        val cs = callsign.uppercase().trim()
        if (!saveKey || qrzKey.isBlank()) {
            finishQrLater()
            return
        }
        onComplete(
            OnboardingFinish(
                callsign = cs,
                qrzApiKey = qrzKey.trim(),
                qrzAutoSync = qrzAutoSync,
                qrzInsertReplaceDuplicates = qrzReplace
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        if (showQrStep) finishQrLater()
                        else onComplete(OnboardingFinish(""))
                    }
                ) {
                    Text(
                        stringResource(
                            if (showQrStep) R.string.onboarding_skip_qrz else R.string.common_skip
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (showQrStep) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    QrzConnectStep(
                    callsign = callsign,
                    qrzKey = qrzKey,
                    onQrzKeyChange = {
                        qrzKey = it
                        verifiedOk = false
                        viewModel.clearVerifyUi()
                    },
                    qrzAutoSync = qrzAutoSync,
                    onQrzAutoSyncChange = { checked ->
                        qrzAutoSync = checked
                        if (!checked) qrzReplace = false
                    },
                    qrzReplace = qrzReplace,
                    onQrzReplaceChange = { qrzReplace = it },
                    verifyUi = verifyUi,
                    onVerifyClick = {
                        viewModel.verifyQrzKey(qrzKey, callsign.uppercase().trim())
                    }
                )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> WelcomePage()
                        1 -> FeaturesPage()
                        2 -> SetupPage(
                            callsign = callsign,
                            onCallsignChange = { callsign = it.uppercase() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateFloatAsState(
                            targetValue = if (isSelected) 24f else 8f,
                            animationSpec = tween(300),
                            label = "indicator_width"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            val cs = callsign.trim()
                            if (cs.isEmpty()) {
                                onComplete(OnboardingFinish(""))
                            } else {
                                showQrStep = true
                                verifiedOk = false
                                qrzKey = ""
                                qrzAutoSync = false
                                qrzReplace = false
                                viewModel.clearVerifyUi()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(LargeCornerRadius)
                ) {
                    Text(
                        text = if (pagerState.currentPage < 2) {
                            stringResource(R.string.common_continue)
                        } else {
                            stringResource(R.string.common_start)
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (showQrStep) {
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { finishQrLater() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.qrz_onboarding_later))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (qrzKey.isBlank()) {
                            finishQrLater()
                            return@Button
                        }
                        if (!verifiedOk) {
                            showUnverifiedDialog = true
                            return@Button
                        }
                        finishWithQrz(saveKey = true)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(LargeCornerRadius)
                ) {
                    Text(stringResource(R.string.qrz_onboarding_save_continue))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    LaunchedEffect(verifyUi) {
        verifiedOk = verifyUi is QrzVerifyUi.Success
    }

    if (showUnverifiedDialog) {
        AlertDialog(
            onDismissRequest = { showUnverifiedDialog = false },
            title = { Text(stringResource(R.string.qrz_unverified_title)) },
            text = { Text(stringResource(R.string.qrz_unverified_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnverifiedDialog = false
                        finishWithQrz(saveKey = true)
                    }
                ) {
                    Text(stringResource(R.string.qrz_unverified_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnverifiedDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun QrzConnectStep(
    callsign: String,
    qrzKey: String,
    onQrzKeyChange: (String) -> Unit,
    qrzAutoSync: Boolean,
    onQrzAutoSyncChange: (Boolean) -> Unit,
    qrzReplace: Boolean,
    onQrzReplaceChange: (Boolean) -> Unit,
    verifyUi: QrzVerifyUi,
    onVerifyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.qrz_onboarding_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.qrz_onboarding_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.qrz_onboarding_callsign_hint, callsign.uppercase()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = qrzKey,
            onValueChange = onQrzKeyChange,
            label = { Text(stringResource(R.string.qrz_api_key_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = qrzAutoSync,
                onCheckedChange = onQrzAutoSyncChange,
                enabled = qrzKey.isNotBlank()
            )
            Text(
                text = stringResource(R.string.qrz_auto_sync_label),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = qrzReplace,
                onCheckedChange = onQrzReplaceChange,
                enabled = qrzAutoSync && qrzKey.isNotBlank()
            )
            Text(
                text = stringResource(R.string.qrz_replace_duplicates_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVerifyClick,
            enabled = verifyUi !is QrzVerifyUi.Loading && qrzKey.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (verifyUi is QrzVerifyUi.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(stringResource(R.string.qrz_verify_connection))
        }

        when (verifyUi) {
            is QrzVerifyUi.Success -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.qrz_verify_success),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is QrzVerifyUi.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                val msg = when (verifyUi.message) {
                    "empty_key" -> stringResource(R.string.qrz_error_empty_key)
                    else -> verifyUi.message
                }
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📻",
                    fontSize = 56.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeaturesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_features_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        FeatureItem(
            icon = Icons.Outlined.Notifications,
            title = stringResource(R.string.onboarding_feature_logbook),
            description = stringResource(R.string.onboarding_feature_logbook_desc)
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureItem(
            icon = Icons.Outlined.LocationOn,
            title = stringResource(R.string.onboarding_feature_propagation),
            description = stringResource(R.string.onboarding_feature_propagation_desc)
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureItem(
            icon = Icons.Filled.Check,
            title = stringResource(R.string.onboarding_feature_tools),
            description = stringResource(R.string.onboarding_feature_tools_desc)
        )
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SetupPage(
    callsign: String,
    onCallsignChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_setup_callsign),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_setup_callsign_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = callsign,
            onValueChange = onCallsignChange,
            label = { Text(stringResource(R.string.onboarding_callsign_label)) },
            placeholder = { Text(stringResource(R.string.onboarding_callsign_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters
            ),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = callsign.isNotBlank(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_looks_good),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
