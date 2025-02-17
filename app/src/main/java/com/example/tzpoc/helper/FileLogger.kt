package com.example.tzpoc.helper

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
object FileLogger {
    private const val DIRECTORY_NAME = "MyAppLogs"
    private const val FILE_NAME = "api_log.txt"

    fun log(context: Context, message: String) {
        try {
            val logDirectory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                DIRECTORY_NAME
            )

            if (!logDirectory.exists()) {
                logDirectory.mkdirs()
            }

            val logFile = File(logDirectory, FILE_NAME)

            val timestamp =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val logMessage = "[$timestamp] $message\n"

            val outputStream = FileOutputStream(logFile, true)
            val writer = OutputStreamWriter(outputStream)
            writer.write(logMessage)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

