package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

/**
 * 서버 `HydrogenStationResponse` 대응. 식별자는 문자열 [chrstnMno]다.
 * 좌표 필드명은 서버 스키마 표기 그대로 경도 [lon], 위도 [let](= latitude)이며 가격은 [ntslPc](원/kg)다.
 */
data class HydrogenStationDto(
    @SerializedName("chrstn_mno")
    val chrstnMno: String,
    @SerializedName("chrstn_nm")
    val chrstnNm: String,
    @SerializedName("road_nm_addr")
    val roadNmAddr: String?,
    @SerializedName("lon")
    val lon: Double?,
    @SerializedName("let")
    val let: Double?,
    @SerializedName("ntsl_pc")
    val ntslPc: Int?,
    @SerializedName("oper_yn")
    val operYn: String?,
    @SerializedName("rltm_info_yn")
    val rltmInfoYn: String?,
)
