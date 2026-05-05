package com.ham.tools.ui.llm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
 * 对话模型：下拉选择预设 ID；最后一项为自定义，选中后显示文本框写入任意模型名。
 * [modelId] 为持久化的模型字符串；变更通过 [onModelIdChange] 写回。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmModelSelectorField(
    modelId: String,
    onModelIdChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    presetModels: List<String> = LlmPresetModels.PRESETS,
) {
    val presets = remember(presetModels) { presetModels }
    val customLabel = stringResource(R.string.settings_llm_model_custom)
    val isPreset = modelId in presets
    var expanded by remember { mutableStateOf(false) }
    val dropdownDisplay = if (isPreset) modelId else customLabel

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = dropdownDisplay,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.settings_llm_model)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                presets.forEach { id ->
                    DropdownMenuItem(
                        text = { Text(id) },
                        onClick = {
                            onModelIdChange(id)
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(customLabel) },
                    onClick = {
                        if (isPreset) onModelIdChange("")
                        expanded = false
                    }
                )
            }
        }
        if (!isPreset) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = modelId,
                onValueChange = onModelIdChange,
                label = { Text(stringResource(R.string.settings_llm_custom_model_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
