package com.example.usk.glotus_final.SuperviserApp.ReceptionFiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usk.glotus_final.R;
import com.example.usk.glotus_final.SuperviserApp.BluetoothPrintService.Other;
import com.example.usk.glotus_final.SuperviserApp.SuperviserListFiles.SuperviserListActivity;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class Etiketka extends AppCompatActivity{
    private MenuItem btn_next;
    private PdfData data;
    private LinearLayout firstPageLayout;
    private LinearLayout firstPartLayout;
    private LinearLayout secondPartLayout;
    private TextView mesto;
    private int kolvoMest;
    private PrintedPdfDocument document;

    RelativeLayout relativeLayout;
    Button send;
    File imageFile;

    private static String formattedDate;

    private File destFile;
    private final static String FONT="/assets/fonts/PTC55F.ttf";
    private final static String DEST="EtiketkaFile.pdf";
    private final static String DESTFILE="EtiketkaFile1.pdf";
    private File dest2;

    private float pdfWidth;
    private float pdfHeight;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etiketka);

        Intent intent=getIntent();
        data=(PdfData) intent.getExtras().getSerializable("pdfData");
        buildText(data);
        kolvoMest=Integer.parseInt(data.getKolvoMest());
        setDateFormat();

        send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                //createPDF();
                //printDocument(imageFile,kolvoMest+1);
                try {
                    fillPdf();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

                printDocument(destFile,kolvoMest+1);
            }
        });
    }

    public void buildText(PdfData pd){
        TextView gorodOtkuda=findViewById(R.id.gorod_otkuda);
        TextView gorodKuda=findViewById(R.id.gorod_kuda);
        TextView imya_otprav=findViewById(R.id.imya_otprav);
        TextView imya_poluch=findViewById(R.id.imya_poluch);
        TextView kolichMest=findViewById(R.id.kolich);
        TextView ves=findViewById(R.id.ves);
        TextView obiem=findViewById(R.id.obiem);
        TextView raspechatal=findViewById(R.id.admin);
        TextView idAndDate=findViewById(R.id.date);
        TextView gorodOtkuda1=findViewById(R.id.gorod_otkuda1);
        TextView gorodKuda1=findViewById(R.id.gorod_kuda1);
        TextView imya_otprav1=findViewById(R.id.imya_otprav1);
        TextView imya_poluch1=findViewById(R.id.imya_poluch1);
        mesto=findViewById(R.id.mesto);
        TextView raspechatal1=findViewById(R.id.admin1);
        TextView idAndDate1=findViewById(R.id.date1);

        relativeLayout=findViewById(R.id.relative1);
        firstPageLayout=findViewById(R.id.mainLay);
        firstPartLayout=findViewById(R.id.linLay);
        secondPartLayout=findViewById(R.id.linLay1);
        send = findViewById(R.id.send);

        gorodOtkuda.setText(pd.getFromCity());
        gorodKuda.setText(pd.getToCity());
        imya_otprav.setText(pd.getOtpravitel());
        imya_otprav1.setText(pd.getOtpravitel());
        imya_poluch.setText(pd.getPoluchatel());
        imya_poluch1.setText(pd.getPoluchatel());
        kolichMest.setText(pd.getKolvoMest());
        ves.setText(pd.getVes());
        obiem.setText(pd.getObiem());
        raspechatal.setText(Admin.name);
        idAndDate.setText(pd.getNumZakaz()+" от "+formattedDate);
        gorodOtkuda1.setText(pd.getFromCity());
        gorodKuda1.setText(pd.getToCity());
        mesto.setText("1 из "+pd.getKolvoMest());
        raspechatal1.setText(Admin.name);
        idAndDate1.setText(pd.getNumZakaz()+" от "+formattedDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.pdf_menu, menu);

        btn_next=menu.findItem(R.id.btn_next);
        btn_next.setVisible(true);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.btn_next){
            Intent myIntent = new Intent(Etiketka.this, ExpedPage.class);
            myIntent.putExtra("pdfExped",data);
            startActivity(myIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void fillPdf() throws IOException, DocumentException {
        destFile=createFile();

        PdfReader reader=new PdfReader(getResources().openRawResource(R.raw.etiketsrc));
        OutputStream outputStream=new FileOutputStream(destFile);
        PdfStamper pdfStamper=new PdfStamper(reader,outputStream);
        AcroFields acroFields=pdfStamper.getAcroFields();

        setAcroFields(acroFields,reader);

        /*CREATE PAGES BY KOLVO MEST*/
        generatePdfPages(reader,pdfStamper);
        //createPagesInPDF(reader,outputStream);

        pdfStamper.setFormFlattening(true);
        pdfStamper.close();
        reader.close();
        outputStream.close();

    }

    public void fillPdf2() throws IOException, DocumentException {
        dest2=createFile2();
        File dir=getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String path=dir+"/"+DEST;

        System.out.println("//////////////////////////"+path);

        PdfReader reader=new PdfReader(path);
        OutputStream outputStream=new FileOutputStream(dest2);
        PdfStamper pdfStamper=new PdfStamper(reader,outputStream);
        AcroFields acroFields=pdfStamper.getAcroFields();

        setAcroFields(acroFields,reader);

        pdfStamper.setFormFlattening(true);
        pdfStamper.close();
        reader.close();
        outputStream.close();
    }

    public void generatePdfPages(PdfReader reader,PdfStamper pdfStamper){
        for(int i=0; i<kolvoMest-1;i++){
            int pages=reader.getNumberOfPages();
            pdfStamper.insertPage(pages + 1, reader.getPageSizeWithRotation(1));
            pdfStamper.replacePage(reader,2,pages + 1);
        }
    }

    public void setAcroFields(AcroFields acroFields,PdfReader reader) throws IOException, DocumentException {
        BaseFont bf=BaseFont.createFont(FONT,"CP1251",true);
        bf.addSubsetRange(BaseFont.CHAR_RANGE_CYRILLIC);
        acroFields.addSubstitutionFont(bf);

        acroFields.setField("otkuda",data.getFromCity());
        acroFields.setField("kuda",data.getToCity());
        acroFields.setField("otpravitel",data.getOtpravitel());
        acroFields.setField("poluchatel",data.getPoluchatel());
        acroFields.setField("kolvo",data.getKolvoMest());
        acroFields.setField("ves",data.getVes());
        acroFields.setField("obiem",data.getObiem());
        acroFields.setField("raspechatal",Admin.name);
        acroFields.setField("numdog",data.getNumZakaz());
        acroFields.setField("date",formattedDate);
        acroFields.setField("kolvoiz","1");

        AcroFields fields = reader.getAcroFields();
        Set<String> fldNames = fields.getFields().keySet();

        for (String fldName : fldNames) {
            System.out.println( fldName + ": " + fields.getField( fldName ) );
        }
    }

    public void createPagesInPDF(PdfReader reader,OutputStream outputStream) throws IOException, DocumentException {
        Document doc=new Document();
        PdfCopy copy=new PdfSmartCopy(doc,outputStream);
        int pages=reader.getNumberOfPages();
        PdfImportedPage importedPage=copy.getImportedPage(reader,2);
        doc.open();
        for(int i=0; i<kolvoMest-1;i++) {
            if(i>=1){
                copy.addPage(importedPage);
            }else {
                copy.addPage(copy.getImportedPage(reader, 1));
            }
//            pdfStamper.insertPage(pages + 1, reader.getPageSizeWithRotation(1));
//            pdfStamper.replacePage(reader,2,pages + 1);
        }
        doc.close();
    }

    public void clonePages(PdfReader reader,File dest) throws IOException, DocumentException {
        Document doc=new Document();
        PdfCopy copy=new PdfSmartCopy(doc,new FileOutputStream(dest));
        doc.open();

        int n=reader.getNumberOfPages();

        for(int i=1; i<=n;i++) {
            PdfImportedPage importedPage = copy.getImportedPage(reader, i);
            for (int j = 0; j < 2; j++) {
                copy.addPage(importedPage);
            }
        }
    }

    public File createFile(){
        File dir=getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File newFile=new File(dir,DEST);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        return newFile;
    }

    public File createFile2(){
        File dir=getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File newFile=new File(dir,DESTFILE);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        return newFile;
    }

    public void setDateFormat(){
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        formattedDate = df.format(c);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Bitmap getViewBitmap(View v) {
        Bitmap bitmap = Bitmap.createBitmap(
                1100,
                1100,
                Bitmap.Config.RGBA_F16
        );

        bitmap.setDensity(1300);
        Canvas canvas = new Canvas(bitmap);

        v.draw(canvas);
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createPDF () {
        int width = 280;
        int height = 280;

        PrintAttributes printAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                setMediaSize(PrintAttributes.MediaSize.NA_LETTER).
                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                build();
        document = new PrintedPdfDocument(this,printAttrs);


        for(int i=0;i<kolvoMest+1;i++) {
            mesto.setText(i+" из "+kolvoMest);
            Bitmap bitmap=getViewBitmap(firstPartLayout);
            Bitmap secondPart=getViewBitmap(secondPartLayout);
            // Bitmap rszBitmap = resizeBitmap(bitmap, width, height);
            // Bitmap rszBitmap2 = resizeBitmap(secondPart, width, height);

            // Bitmap rszBitmap = get_Resized_Bitmap(bitmap, width, height);
            // Bitmap rszBitmap2 = get_Resized_Bitmap(secondPart, width, height);

            Bitmap rszBitmap = bitmap;
            Bitmap rszBitmap2 = secondPart;

            int xx = rszBitmap.getWidth();
            int yy = rszBitmap.getHeight();
            int secXX = rszBitmap2.getWidth();
            int secYY = rszBitmap2.getHeight();

            createCanvas(rszBitmap, rszBitmap2, xx, yy, secXX, secYY, i);
        }

        saveOnDevice(document);
    }

    public void createCanvas(Bitmap btm, Bitmap btm1, int width,int height, int width1, int height1, int i){
        if(i==0){
            android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(width, height, i + 1).create();
            android.graphics.pdf.PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas=page.getCanvas();
            canvas.drawBitmap(btm,0,0,null);
            document.finishPage(page);
        }else if(i>0){
            android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(width1, height1, i + 1).create();
            android.graphics.pdf.PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas=page.getCanvas();
            canvas.drawBitmap(btm1,0,0,null);
            document.finishPage(page);
        }
    }

    public void saveOnDevice(PrintedPdfDocument document){
        File mFolder;
        String fileName="Etiketka.pdf";
        try {
            mFolder = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            imageFile = new File(mFolder,fileName);
            if (!mFolder.exists()) {
                mFolder.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(imageFile);
            document.writeTo(out);
            document.close();
            out.close();
            Toast.makeText(this,"Результат сохранен", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "При сохранении возникла ошибка", Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap resizeBitmap(Bitmap bitmap, int newWidth,int newHeight){
        Bitmap resizedBitmap;
        resizedBitmap = Other.resizeImage(bitmap, newWidth, newHeight);
        return resizedBitmap;
    }

    public Bitmap get_Resized_Bitmap(Bitmap bmp, int newHeight, int newWidth) {
        Bitmap newBitmap= Bitmap.createScaledBitmap(bmp, newHeight, newWidth, true);
        return newBitmap ;
    }

    public void printDocument(File file,int totalPage){
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        String jobName = this.getString(R.string.app_name) + " Document";
        printManager.print(jobName, new MyPrintDocumentAdapter(file,totalPage), null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888,true);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(resizedBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        return resizedBitmap;
    }

    /*    public Bitmap getViewBitmap(View v){
            v.setDrawingCacheEnabled(true);
            v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            v.buildDrawingCache(true);
            Bitmap b = Bitmap.createBitmap(
                    getResources().getDisplayMetrics().densityDpi*v.getHeight(),
                    getResources().getDisplayMetrics().densityDpi*v.getWidth(),
                    Bitmap.Config.ARGB_8888
                    );
            Canvas c = new Canvas(b);
            v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            v.draw(c);
            v.setDrawingCacheEnabled(false);

            return b;
        }
    */
}
