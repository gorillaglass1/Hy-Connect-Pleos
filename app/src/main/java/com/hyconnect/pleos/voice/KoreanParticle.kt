package com.hyconnect.pleos.voice

/**
 * 한글 받침 유무에 따라 조사를 골라 붙인다(음성 멘트 자연스러움용).
 * 마지막 글자가 한글이 아니면 받침 없는 경우(예: 를/는/가)를 기본으로 쓴다.
 *
 * @param withBatchim 받침이 있을 때 붙일 조사(예: "을", "은", "이")
 * @param withoutBatchim 받침이 없을 때 붙일 조사(예: "를", "는", "가")
 */
fun String.withJosa(withBatchim: String, withoutBatchim: String): String {
    val last = trimEnd().lastOrNull() ?: return this + withoutBatchim
    val hasBatchim = if (last in '가'..'힣') {
        (last.code - 0xAC00) % 28 != 0
    } else {
        false
    }
    return this + if (hasBatchim) withBatchim else withoutBatchim
}
