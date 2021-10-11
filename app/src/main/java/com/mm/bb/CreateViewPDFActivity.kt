package com.mm.bb

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.room.Room
import com.example.bb_items_roomdb.RoomDB.AppDb
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_create_view_p_d_f.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kariot.pdfinvoice.ModelItems
import me.kariot.pdfinvoice.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class CreateViewPDFActivity : AppCompatActivity() {

    // implement functionality to open pdf within app which will help to share snack bar that it is emailed
    // if email exists else share explicitly with intent whit other apps

    var dataList = ArrayList<HashMap<String, String>>()

    val colorPrimary = BaseColor(40, 116, 240)
    val FONT_SIZE_DEFAULT = 12f
    val FONT_SIZE_SMALL = 8f
    var basfontLight: BaseFont =
        BaseFont.createFont("assets/fonts/app_font_light.ttf", "UTF-8", BaseFont.EMBEDDED)
    var appFontLight = Font(basfontLight, FONT_SIZE_SMALL)

    var basfontRegular: BaseFont =
        BaseFont.createFont("assets/fonts/app_font_regular.ttf", "UTF-8", BaseFont.EMBEDDED)
    var appFontRegular = Font(basfontRegular, FONT_SIZE_DEFAULT)
    var appFontRegularSmall = Font(basfontRegular, 10f)


    var basfontSemiBold: BaseFont =
        BaseFont.createFont("assets/fonts/app_font_semi_bold.ttf", "UTF-8", BaseFont.EMBEDDED)
    var appFontSemiBold = Font(basfontSemiBold, 24f)


    var basfontBold: BaseFont =
        BaseFont.createFont("assets/fonts/app_font_bold.ttf", "UTF-8", BaseFont.EMBEDDED)
    var appFontBold = Font(basfontBold, FONT_SIZE_DEFAULT)

    private lateinit var invoicePath: String

    val PADDING_EDGE = 40f
    val TEXT_TOP_PADDING = 3f
    val TABLE_TOP_PADDING = 10f
    val TEXT_TOP_PADDING_EXTRA = 30f
    val BILL_DETAILS_TOP_PADDING = 50f
    val data = ArrayList<ModelItems>()

    private lateinit var cName: String
    private lateinit var cBusinessName: String
    private lateinit var cContact: String
    private lateinit var cBuildingStreet: String
    private lateinit var cLandmark: String
    private lateinit var cCityPin: String
    private lateinit var cWebsite: String

    private lateinit var uName: String
    private lateinit var uBusinessName: String
    private lateinit var uContact: String
    private lateinit var uBuildingStreet: String
    private lateinit var uLandmark: String
    private lateinit var uCityPin: String
    private lateinit var uWebsite: String
    private lateinit var uEmail: String
    private lateinit var uInvoiceNo: String
    private lateinit var invoiceName: String
    private lateinit var totalAmount: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_view_p_d_f)

        //get scanned customer details which are retrieved from firebase after scanning
        val sharedPref = getSharedPreferences("userData", Context.MODE_PRIVATE)
        cName = sharedPref.getString("sName", "").toString()
        cBusinessName = sharedPref.getString("sBusinessName", "").toString()
        cContact = sharedPref.getString("sContact", "").toString()
        cBuildingStreet = sharedPref.getString("sBuildingStreet", "").toString()
        cLandmark = sharedPref.getString("sLandmark", "").toString()
        cCityPin = sharedPref.getString("sCityPin", "").toString()
        cWebsite = sharedPref.getString("sWebsite", "").toString()

        uName = sharedPref.getString("uName", "").toString()
        uBusinessName = sharedPref.getString("uBusinessName", "").toString()
        uContact = sharedPref.getString("uContact", "").toString()
        uBuildingStreet = sharedPref.getString("uBuildingStreet", "").toString()
        uLandmark = sharedPref.getString("uLandmark", "").toString()
        uCityPin = sharedPref.getString("uCityPin", "").toString()
        uWebsite = sharedPref.getString("uWebsite", "").toString()
        uEmail = sharedPref.getString("uEmail", "").toString()
        uInvoiceNo = sharedPref.getString("uInvoiceNo", "1").toString()

        Log.d("INVOICE", "cName: $cName")
        Log.d("INVOICE", "uName: $uName")


        initData()

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                    if (report.areAllPermissionsGranted()) {

                        GlobalScope.launch {

                            appFontRegular.color = BaseColor.WHITE
                            appFontRegular.size = 10f
                            invoiceName = "$uInvoiceNo $cName"
                            val doc = Document(PageSize.A4, 0f, 0f, 0f, 0f)
                            invoicePath =
                                getExternalFilesDir(null).toString() + "/$invoiceName.pdf" //location where the pdf will store


//                            invoicePath = Environment.getExternalStorageDirectory().path + File.separator + "BillingBytes/$invoiceName.pdf"

                            Log.d("loc", invoicePath)
                            val writer = PdfWriter.getInstance(doc, FileOutputStream(invoicePath))
                            doc.open()


                            //Header Column Init with width nad no. of columns
                            initInvoiceHeader(doc)

                            doc.setMargins(0f, 0f, PADDING_EDGE, PADDING_EDGE)
                            initBillDetails(doc)

//                        addLine(writer)

                            initTableHeader(doc)
                            initItemsTable(doc)
                            initPriceDetails(doc)
                            initFooter(doc)
                            doc.close()

                            val invoiceno = uInvoiceNo.toInt() + 1
                            sharedPref.edit().putString("uInvoiceNo", invoiceno.toString()).apply()


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
                                    .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
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


    // i guess this is no. of tables
    // here the logic of reading data from db and passing it to PDF tables to write will be handled
    private fun initData() {
        dataList.clear()
        val db = Room.databaseBuilder(applicationContext, AppDb::class.java, "BookDB").build()
        val thread = Thread {
            db.abstractItemDao().getAllItems().forEach()
            {
                val itemName = it.itemName
                val quantity = it.quantity
                val gst = it.gst
                val amount = it.amount

                data.add(
                    ModelItems(
                        itemName,
                        quantity,
                        "$gst%",
                        amount
                    )
                )
            }
        }
        thread.start()
    }


    // That BLUE Header
    private fun initInvoiceHeader(doc: Document) {
        val d = resources.getDrawable(R.drawable.logo_billing_bytes)
        val bitDw = d as BitmapDrawable
        val bmp = bitDw.bitmap
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = Image.getInstance(stream.toByteArray())
        val headerTable = PdfPTable(3)
        headerTable.setWidths(
            floatArrayOf(
                1.3f,
                1f,
                1f
            )
        ) // adds 3 colomn horizontally
        headerTable.isLockedWidth = true
        headerTable.totalWidth = PageSize.A4.width // set content width to fill document
        val cell = PdfPCell(Image.getInstance(image)) // Logo Cell
        cell.border = Rectangle.NO_BORDER // Removes border
        cell.paddingTop = TEXT_TOP_PADDING_EXTRA // sets padding
        cell.paddingRight = TABLE_TOP_PADDING
        cell.paddingLeft = PADDING_EDGE
        cell.horizontalAlignment = Rectangle.ALIGN_LEFT
        cell.paddingBottom = TEXT_TOP_PADDING_EXTRA

        cell.backgroundColor = colorPrimary // sets background color
        cell.horizontalAlignment = Element.ALIGN_CENTER
        headerTable.addCell(cell) // Adds first cell with logo

        val contactTable =
            PdfPTable(1) // new vertical table for contact details
        val phoneCell =
            PdfPCell(
                Paragraph(
                    uContact,
                    appFontRegular
                )
            )
        phoneCell.border = Rectangle.NO_BORDER
        phoneCell.horizontalAlignment = Element.ALIGN_RIGHT
        phoneCell.paddingTop = TEXT_TOP_PADDING

        contactTable.addCell(phoneCell)

        val emailCellCell = PdfPCell(Phrase(uEmail, appFontRegular))
        emailCellCell.border = Rectangle.NO_BORDER
        emailCellCell.horizontalAlignment = Element.ALIGN_RIGHT
        emailCellCell.paddingTop = TEXT_TOP_PADDING

        contactTable.addCell(emailCellCell)

        val webCell = PdfPCell(Phrase(uWebsite, appFontRegular))
        webCell.border = Rectangle.NO_BORDER
        webCell.paddingTop = TEXT_TOP_PADDING
        webCell.horizontalAlignment = Element.ALIGN_RIGHT

        contactTable.addCell(webCell)


        val headCell = PdfPCell(contactTable)
        headCell.border = Rectangle.NO_BORDER
        headCell.horizontalAlignment = Element.ALIGN_RIGHT
        headCell.verticalAlignment = Element.ALIGN_MIDDLE
        headCell.backgroundColor = colorPrimary
        headerTable.addCell(headCell)

        val address = PdfPTable(1)
        val line1 = PdfPCell(
            Paragraph(
                uBuildingStreet,
                appFontRegular
            )
        )
        line1.border = Rectangle.NO_BORDER
        line1.paddingTop = TEXT_TOP_PADDING
        line1.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line1)

        val line2 = PdfPCell(Paragraph(uLandmark, appFontRegular))
        line2.border = Rectangle.NO_BORDER
        line2.paddingTop = TEXT_TOP_PADDING
        line2.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line2)

        val line3 = PdfPCell(Paragraph(uCityPin, appFontRegular))
        line3.border = Rectangle.NO_BORDER
        line3.paddingTop = TEXT_TOP_PADDING
        line3.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line3)


        val addressHeadCell = PdfPCell(address)
        addressHeadCell.border = Rectangle.NO_BORDER
        addressHeadCell.setLeading(22f, 25f)
        addressHeadCell.horizontalAlignment = Element.ALIGN_RIGHT
        addressHeadCell.verticalAlignment = Element.ALIGN_MIDDLE
        addressHeadCell.backgroundColor = colorPrimary
        addressHeadCell.paddingRight = PADDING_EDGE
        headerTable.addCell(addressHeadCell)

        doc.add(headerTable)
    }


    // Item table column names BLUE ones
    private fun initTableHeader(doc: Document) {

        doc.add(Paragraph("\n\n")) //adds blank line to place table header above Item table column-names BLUE ones

        val titleTable = PdfPTable(4)
        titleTable.isLockedWidth = true
        titleTable.totalWidth = PageSize.A4.width
        titleTable.setWidths(floatArrayOf(2.5f, 1f, .6f, 1.1f))
        appFontBold.color = colorPrimary

        val itemCell = PdfPCell(Phrase("Item Name", appFontBold))
        itemCell.border = Rectangle.NO_BORDER
        itemCell.paddingTop = TABLE_TOP_PADDING
        itemCell.paddingBottom = TABLE_TOP_PADDING
        itemCell.paddingLeft = PADDING_EDGE
        titleTable.addCell(itemCell)


        val quantityCell = PdfPCell(Phrase("Quantity", appFontBold))
        quantityCell.border = Rectangle.NO_BORDER
        quantityCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        quantityCell.paddingBottom = TABLE_TOP_PADDING
        quantityCell.paddingTop = TABLE_TOP_PADDING
        titleTable.addCell(quantityCell)


        val vat = PdfPCell(Phrase("GST %", appFontBold))
        vat.border = Rectangle.NO_BORDER
        vat.horizontalAlignment = Rectangle.ALIGN_RIGHT
        vat.paddingBottom = TABLE_TOP_PADDING
        vat.paddingTop = TABLE_TOP_PADDING
        titleTable.addCell(vat)

        val netAmount = PdfPCell(Phrase("Amount", appFontBold))
        netAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
        netAmount.border = Rectangle.NO_BORDER
        netAmount.paddingTop = TABLE_TOP_PADDING
        netAmount.paddingBottom = TABLE_TOP_PADDING
        netAmount.paddingRight = PADDING_EDGE
        titleTable.addCell(netAmount)
        doc.add(titleTable)
    }


    // Items in Item table
    private fun initItemsTable(doc: Document) {
        val itemsTable = PdfPTable(4)
        itemsTable.isLockedWidth = true
        itemsTable.totalWidth = PageSize.A4.width
        itemsTable.setWidths(floatArrayOf(2.5f, 1f, .6f, 1.1f))

        for (item in data) {
            itemsTable.deleteBodyRows()

            val itemdetails = PdfPTable(1)

            val itemName = PdfPCell(Phrase(item.itemName, appFontRegular))
            itemName.border = Rectangle.NO_BORDER

//            val itemDesc = PdfPCell(Phrase(item.itemDesc, appFontLight))
//            itemDesc.border = Rectangle.NO_BORDER
            itemdetails.addCell(itemName)
//            itemdetails.addCell(itemDesc)

            val itemCell = PdfPCell(itemdetails)
            itemCell.border = Rectangle.NO_BORDER
            itemCell.paddingTop = TABLE_TOP_PADDING
            itemCell.paddingLeft = PADDING_EDGE
            itemsTable.addCell(itemCell)


            val quantityCell = PdfPCell(Phrase(item.quantity.toString(), appFontRegular))
            quantityCell.border = Rectangle.NO_BORDER
            quantityCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
            quantityCell.paddingTop = TABLE_TOP_PADDING
            itemsTable.addCell(quantityCell)

            val vat = PdfPCell(Phrase(item.gst.toString(), appFontRegular))
            vat.border = Rectangle.NO_BORDER
            vat.horizontalAlignment = Rectangle.ALIGN_RIGHT
            vat.paddingTop = TABLE_TOP_PADDING
            itemsTable.addCell(vat)

//            val netAmount = PdfPCell(Phrase("AED ${item.netAmount}", appFontRegular))
            val netAmount = PdfPCell(Phrase(item.netAmount, appFontRegular))
            netAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
            netAmount.border = Rectangle.NO_BORDER
            netAmount.paddingTop = TABLE_TOP_PADDING
            netAmount.paddingRight = PADDING_EDGE
            itemsTable.addCell(netAmount)
            doc.add(itemsTable)
        }
    }


    // Bill Details
    private fun initBillDetails(doc: Document) {
        val billDetailsTable =
            PdfPTable(3)  // table to show customer address, invoice, date and total amount
        billDetailsTable.setWidths(
            floatArrayOf(
                2f,
                1.82f,
                2f
            )
        )
        billDetailsTable.isLockedWidth = true
        billDetailsTable.paddingTop = 30f

        billDetailsTable.totalWidth =
            PageSize.A4.width // set content width to fill document
        val customerAddressTable = PdfPTable(1)
        appFontRegular.color = BaseColor.GRAY
        appFontRegular.size = 8f


        //********
        // Billed to and client address stuff.
        val txtBilledToCell = PdfPCell(
            Phrase(
                "Billed To",
                appFontLight
            )
        )
        txtBilledToCell.border = Rectangle.NO_BORDER
        customerAddressTable.addCell(
            txtBilledToCell
        )
        appFontRegular.size = FONT_SIZE_DEFAULT
        appFontRegular.color = BaseColor.BLACK

        appFontRegularSmall.size = 10f
        appFontRegularSmall.color = BaseColor.BLACK

        val clientAddressCell1 = PdfPCell(
            Paragraph(
                cName,
                appFontRegular
            )
        )
        clientAddressCell1.border = Rectangle.NO_BORDER
        clientAddressCell1.paddingTop = TEXT_TOP_PADDING
        customerAddressTable.addCell(clientAddressCell1)

        val clientAddressCell2 = PdfPCell(
            Paragraph(
                cBuildingStreet,
                appFontRegularSmall
            )
        )
        clientAddressCell2.border = Rectangle.NO_BORDER
        clientAddressCell2.paddingTop = TEXT_TOP_PADDING
        customerAddressTable.addCell(clientAddressCell2)


        val clientAddressCell3 = PdfPCell(
            Paragraph(
                cLandmark,
                appFontRegularSmall
            )
        )
        clientAddressCell3.border = Rectangle.NO_BORDER
        clientAddressCell3.paddingTop = TEXT_TOP_PADDING
        customerAddressTable.addCell(clientAddressCell3)


        val clientAddressCell4 = PdfPCell(
            Paragraph(
                cCityPin,
                appFontRegularSmall
            )
        )
        clientAddressCell4.border = Rectangle.NO_BORDER
        clientAddressCell4.paddingTop = TEXT_TOP_PADDING
        customerAddressTable.addCell(clientAddressCell4)

        val billDetailsCell1 = PdfPCell(customerAddressTable)
        billDetailsCell1.border = Rectangle.NO_BORDER

        billDetailsCell1.paddingTop = BILL_DETAILS_TOP_PADDING

        billDetailsCell1.paddingLeft = PADDING_EDGE

        billDetailsTable.addCell(billDetailsCell1)
        //***************

        val invoiceNumAndData = PdfPTable(1)
        appFontRegular.color = BaseColor.LIGHT_GRAY
        appFontRegular.size = 8f
        val txtInvoiceNumber = PdfPCell(Phrase("Invoice Number", appFontLight))
        txtInvoiceNumber.paddingTop = BILL_DETAILS_TOP_PADDING
        txtInvoiceNumber.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(txtInvoiceNumber)
        appFontRegular.color = BaseColor.BLACK
        appFontRegular.size = 12f
        val invoiceNumber = PdfPCell(Phrase(uInvoiceNo, appFontRegular))  // yy mmdd num
        invoiceNumber.border = Rectangle.NO_BORDER
        invoiceNumber.paddingTop = TEXT_TOP_PADDING
        invoiceNumAndData.addCell(invoiceNumber)

        appFontRegular.color = BaseColor.LIGHT_GRAY
        appFontRegular.size = 5f
        val txtDate = PdfPCell(Phrase("Date Of Issue", appFontLight))
        txtDate.paddingTop = TEXT_TOP_PADDING_EXTRA
        txtDate.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(txtDate)

        appFontRegular.color = BaseColor.BLACK
        appFontRegular.size = FONT_SIZE_DEFAULT
        val currentDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val dateCell = PdfPCell(Phrase(currentDate, appFontRegular))
        dateCell.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(dateCell)

        val dataInvoiceNumAndData = PdfPCell(invoiceNumAndData)
        dataInvoiceNumAndData.border = Rectangle.NO_BORDER
        billDetailsTable.addCell(dataInvoiceNumAndData)

        val totalPriceTable = PdfPTable(1)
        val txtInvoiceTotal = PdfPCell(Phrase("Invoice Total", appFontLight))
        txtInvoiceTotal.paddingTop = BILL_DETAILS_TOP_PADDING
        txtInvoiceTotal.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtInvoiceTotal.border = Rectangle.NO_BORDER
        totalPriceTable.addCell(txtInvoiceTotal)

        appFontSemiBold.color = colorPrimary
        val totalAomountCell = PdfPCell(Phrase(totalAmount, appFontSemiBold))
        totalAomountCell.border = Rectangle.NO_BORDER
        totalAomountCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalPriceTable.addCell(totalAomountCell)
        val dataTotalAmount = PdfPCell(totalPriceTable)
        dataTotalAmount.border = Rectangle.NO_BORDER
        dataTotalAmount.paddingRight = PADDING_EDGE
        dataTotalAmount.verticalAlignment = Rectangle.ALIGN_BOTTOM

        billDetailsTable.addCell(dataTotalAmount)
        doc.add(billDetailsTable)
    }


    // Total Prices


    // write to pdf
    private fun initPriceDetails(doc: Document) {

        // get from data from db
        val db = Room.databaseBuilder(applicationContext, AppDb::class.java, "BookDB").build()
//        lateinit var subTotal: String
//        lateinit var taxTotal: String
//        lateinit var total: String

//        CoroutineScope(Dispatchers.IO).launch {
        val subTotal = db.abstractItemDao().getSubTotal().toString()
        val taxTotal = db.abstractItemDao().getTaxTotal().toString()
        totalAmount = (subTotal.toInt() + taxTotal.toInt()).toString()

//            withContext(Dispatchers.Main) {


        Log.d("101", "subTotal $subTotal")
        Log.d("101", "taxTotal $taxTotal")
        Log.d("101", "total $totalAmount")

        val priceDetailsTable = PdfPTable(2)
        priceDetailsTable.totalWidth = PageSize.A4.width
        priceDetailsTable.setWidths(floatArrayOf(7f, 2f))
        priceDetailsTable.isLockedWidth = true

        appFontRegular.color = colorPrimary
        val txtSubTotalCell = PdfPCell(Phrase("Sub Total : ", appFontRegular))
        txtSubTotalCell.border = Rectangle.NO_BORDER
        txtSubTotalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtSubTotalCell.paddingTop = TEXT_TOP_PADDING_EXTRA
        priceDetailsTable.addCell(txtSubTotalCell)
        appFontBold.color = BaseColor.BLACK

        val totalPriceCell = PdfPCell(Phrase(subTotal, appFontBold))
//        val totalPriceCell = PdfPCell(Phrase("333", appFontBold))
        totalPriceCell.border = Rectangle.NO_BORDER
        totalPriceCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalPriceCell.paddingTop = TEXT_TOP_PADDING_EXTRA
        totalPriceCell.paddingRight = PADDING_EDGE
        priceDetailsTable.addCell(totalPriceCell)


        val txtTaxCell = PdfPCell(Phrase("Tax Total : ", appFontRegular))
        txtTaxCell.border = Rectangle.NO_BORDER
        txtTaxCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtTaxCell.paddingTop = TEXT_TOP_PADDING
        priceDetailsTable.addCell(txtTaxCell)

        val totalTaxCell = PdfPCell(Phrase(taxTotal, appFontBold))
//        val totalTaxCell = PdfPCell(Phrase("222", appFontBold))
        totalTaxCell.border = Rectangle.NO_BORDER
        totalTaxCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalTaxCell.paddingTop = TEXT_TOP_PADDING
        totalTaxCell.paddingRight = PADDING_EDGE
        priceDetailsTable.addCell(totalTaxCell)

        val txtTotalCell = PdfPCell(Phrase("TOTAL : ", appFontRegular))
        txtTotalCell.border = Rectangle.NO_BORDER
        txtTotalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtTotalCell.paddingTop = TEXT_TOP_PADDING
        txtTotalCell.paddingBottom = TEXT_TOP_PADDING
        txtTotalCell.paddingLeft = PADDING_EDGE
        priceDetailsTable.addCell(txtTotalCell)
        appFontBold.color = colorPrimary
        val totalCell = PdfPCell(Phrase(totalAmount, appFontBold))
//        val totalCell = PdfPCell(Phrase("1234", appFontBold))
        totalCell.border = Rectangle.NO_BORDER
        totalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalCell.paddingTop = TEXT_TOP_PADDING
        totalCell.paddingBottom = TEXT_TOP_PADDING
        totalCell.paddingRight = PADDING_EDGE
        priceDetailsTable.addCell(totalCell)

        doc.add(priceDetailsTable)
//            }
//        }
    }


    // Thankyou footer
    private fun initFooter(doc: Document) {
        appFontRegular.color = colorPrimary
        val footerTable = PdfPTable(1)
        footerTable.totalWidth = PageSize.A4.width
        footerTable.isLockedWidth = true
        val thankYouCell =
            PdfPCell(Phrase("THANK YOU FOR YOUR BUSINESS", appFontRegular))
        thankYouCell.border = Rectangle.NO_BORDER
        thankYouCell.paddingLeft = PADDING_EDGE
        thankYouCell.paddingTop = PADDING_EDGE
        thankYouCell.horizontalAlignment = Rectangle.ALIGN_CENTER
        footerTable.addCell(thankYouCell)
        doc.add(footerTable)
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        finish()
    }

    fun shareInvoice(view: android.view.View) {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(invoicePath))
        val chooser = Intent.createChooser(intent, "Share Invoice using..")
        startActivity(chooser)

    }
}