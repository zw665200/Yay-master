package com.ql.recovery.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/12/6 14:36
 */
data class GameCategory(
    val description: String,
    val icon: String,
    val id: Int,
    val name: String,
    val maps: List<MapInfo>
)

data class MapInfo(
    val id: Int,
    val name: String,
    val icon: String,
    val description: String,
    val data: String
)

data class Story(
    val id: Int,
    val dialog: MapDialog,
    val coin: Int,
    val role: String,
    val background: String,
    val picture: String?,
    val sound: String,
    val options: List<Options>
)

data class MapDialog(
    val zh: String,
    val en: String,
)

data class Options(
    val title: Title,
    val id: Int,
    val picture: String?
)

data class Title(
    val zh: String,
    val en: String
)

