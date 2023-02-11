package com.ql.recovery.yay.util

import android.content.Context
import org.json.JSONArray
import java.io.InputStream

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/12/16 14:30
 */
class Json {

    companion object {
        fun readFromFile(context: Context, fileName: String): JSONArray {
            try {
                val inputStream: InputStream = context.getAssets().open(fileName)
                val inputString = inputStream.bufferedReader().use { it.readText() }
                return JSONArray(inputString)

            } catch (e: Exception) {
                return JSONArray()
            }
        }

        fun readFromResources(context: Context, resource: Int): JSONArray {
            try {
                val inputStream: InputStream = context.resources.openRawResource(resource)
                val inputString = inputStream.bufferedReader().use { it.readText() }
                return JSONArray(inputString)

            } catch (e: Exception) {
                return JSONArray()
            }
        }
    }
}