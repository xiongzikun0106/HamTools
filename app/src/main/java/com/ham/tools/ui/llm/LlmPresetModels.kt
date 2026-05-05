package com.ham.tools.ui.llm

/**
 * 各云平台「OpenAI 兼容 `…/chat/completions`」常用的 **模型 ID** 速查。
 *
 * **说明**
 * - 在下拉里选中某一 **预设模型** 时，应用会将「API 端点」同步为该厂商 **文档中常见的默认兼容 Base URL**（仍需自备对应平台的 Key）。
 * - 若你使用 **Azure OpenAI、企业代理、聚合(OpenRouter)、或其它地域节点**，预设 URL 可能不适用——请在端点框里 **手动改成**控制台给的地址。
 * - 「自定义」模型或非下拉预设的名称 **不会** 改动端点。
 *
 * **DeepSeek**：`deepseek-chat` / `deepseek-reasoner` 已预告将于 **2026-07** 前后停用，请用 **V4** 型号。
 *
 * 方舟豆包等型号可能带日期后缀变更；控制台 Endpoint 若与下列默认值不一致（地域、专线），请自选「自定义」模型 ID 并改写端点。
 */
object LlmPresetModels {

    private const val EP_OPENAI = "https://api.openai.com/v1"
    private const val EP_DEEPSEEK = "https://api.deepseek.com/v1"
    private const val EP_MINIMAX = "https://api.minimax.io/v1"
    private const val EP_ZHIPU = "https://open.bigmodel.cn/api/paas/v4"
    /** 中国大陆兼容模式示例；国际节点请见阿里云控制台 */
    private const val EP_DASHSCOPE_COMPAT_CN = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    private const val EP_MOONSHOT = "https://api.moonshot.ai/v1"
    /** 火山方舟常见示例（北京）；实际以控制台「在线推理」Endpoint 为准 */
    private const val EP_VOLC_ARK_CN_BEIJING = "https://ark.cn-beijing.volces.com/api/v3"
    private const val EP_HUNYUAN = "https://api.hunyuan.cloud.tencent.com/v1"
    private const val EP_MISTRAL = "https://api.mistral.ai/v1"

    /** 预设模型 ID → 该厂商常用兼容 Base URL（无尾随 `/`，与应用内拼接 `/chat/completions` 一致） */
    private val PRESET_MODEL_DEFAULT_ENDPOINT: Map<String, String> = mapOf(
        "gpt-5.5" to EP_OPENAI,
        "gpt-5.4-mini" to EP_OPENAI,
        "deepseek-v4-pro" to EP_DEEPSEEK,
        "deepseek-v4-flash" to EP_DEEPSEEK,
        "MiniMax-M2.7" to EP_MINIMAX,
        "MiniMax-M2.7-highspeed" to EP_MINIMAX,
        "glm-4.7" to EP_ZHIPU,
        "glm-4.6" to EP_ZHIPU,
        "qwen3-max" to EP_DASHSCOPE_COMPAT_CN,
        "qwen3.5-flash" to EP_DASHSCOPE_COMPAT_CN,
        "kimi-k2.6" to EP_MOONSHOT,
        "kimi-k2.5" to EP_MOONSHOT,
        "doubao-seed-1-6-251015" to EP_VOLC_ARK_CN_BEIJING,
        "doubao-seed-2-0-pro-260215" to EP_VOLC_ARK_CN_BEIJING,
        "hunyuan-turbos-latest" to EP_HUNYUAN,
        "hunyuan-lite" to EP_HUNYUAN,
        "mistral-large-latest" to EP_MISTRAL,
        "ministral-8b-latest" to EP_MISTRAL,
    )

    val PRESETS: List<String> = PRESET_MODEL_DEFAULT_ENDPOINT.keys.toList()

    /**
     * 若 [modelId] 为本对象登记的预设模型，返回推荐的兼容 Base URL；否则返回 `null`（不改动界面已有端点）。
     */
    fun suggestedEndpointForPresetModel(modelId: String): String? =
        if (modelId.isBlank()) null else PRESET_MODEL_DEFAULT_ENDPOINT[modelId]
}
