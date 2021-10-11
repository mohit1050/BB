package com.mm.bb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.karumi.dexter.Dexter
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_view_invoices.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kariot.pdfinvoice.toast
import java.io.File
import java.lang.Exception

class ActivityViewInvoices : AppCompatActivity() {

    private lateinit var invoicePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_invoices)

        if (intent.hasExtra("invoice_path")) {
            invoicePath = intent.getStringExtra("invoice_path").toString()
//            Toast.makeText(this, "path: $invoicePath", Toast.LENGTH_LONG).show()


            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                        if (report.areAllPermissionsGranted()) {

                            GlobalScope.launch {

                                // copying this code imports the following file
                                // import com.karumi.dexter.BuildConfig
                                // remove it manually as this needs authority:BuildConfig.APPLICATION_ID ...

                                val file = File(invoicePath)
                                val path: Uri = FileProvider.getUriForFile(
                                    applicationContext,
                                    BuildConfig.APPLICATION_ID + ".provider", file
                                )

                                try {
//                                val intent = Intent(Intent.ACTION_VIEW)
//                                intent.setDataAndType(path, "application/pdf")
//                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                                startActivity(intent)


                                    pdfView.fromUri(path)
                                        .pages(
                                            0,
                                            2,
                                            1,
                                            3,
                                            3,
                                            3
                                        ) // all pages are displayed by default
                                        .enableSwipe(true) // allows to block changing pages using swipe
                                        .swipeHorizontal(false)
                                        .enableDoubletap(true)
                                        .defaultPage(0)

                                        // called on single tap, return true if handled, false to toggle scroll handle visibility
                                        .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                                        .password(null)
                                        .scrollHandle(null)
                                        .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                                        // spacing between pages in dp. To define spacing color, set view background
                                        .spacing(0)
                                        .load();


                                } catch (e: ActivityNotFoundException) {
                                    toast("There is no PDF Viewer ")
                                }
                            }
                        } else {
                            toast("permissions missing :(")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()


        }
    }

    fun shareInvoice(view: android.view.View) {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(invoicePath))
        val chooser = Intent.createChooser(intent, "Share Invoice using..")
        startActivity(chooser)

    }

    fun deleteInvoice(view: android.view.View) {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete invoice")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Delete") { _, _ ->
            try {
                val file: File = File(invoicePath)
                val deleted = file.delete()
                Log.d("log_tag", "deleted: $deleted")
                Toast.makeText(this, "Invoice deleted", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }

            finish()
        }
        builder.setNeutralButton("Cancel") { _, _ ->
//            Toast.makeText(applicationContext, "cancel", Toast.LENGTH_LONG).show()

        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }
}