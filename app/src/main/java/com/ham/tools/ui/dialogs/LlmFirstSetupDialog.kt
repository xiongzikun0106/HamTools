package com.ham.tools.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ham.tools.R

/**
 * 首次进入主界面后提示配置 LLM；若跳过则仅可使用手动通联录入。
 */
@Composable
fun LlmFirstSetupDialog(
    initialEndpoint: String,
    initialApiKey: String,
    initialModel: String,
    onSave: (endpoint: String, apiKey: String, model: String) -> Unit,
    onManualOnly: () -> Unit
) {
    var endpoint by remember(initialEndpoint) { mutableStateOf(initialEndpoint) }
    var apiKey by remember(initialApiKey) { mutableStateOf(initialApiKey) }
    var model by remember(initialModel) { mutableStateOf(initialModel) }

    AlertDialog(
        onDismissRequest = { /* 强制用户二选一 */ },
        title = {
            Text(
                text = stringResource(R.string.llm_first_setup_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.llm_first_setup_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text(stringResource(R.string.llm_first_setup_endpoint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(stringResource(R.string.llm_first_setup_api_key)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text(stringResource(R.string.llm_first_setup_model)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(endpoint, apiKey, model) },
                enabled = apiKey.isNotBlank(),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(stringResource(R.string.llm_first_setup_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onManualOnly) {
                Text(stringResource(R.string.llm_first_setup_manual_only))
            }
        }
    )
}
