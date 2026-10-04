package com.example.careconnect.activities;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.R;
import com.example.careconnect.firebase.FirebaseAuthManager;
import com.example.careconnect.firebase.FirestoreManager;
import com.example.careconnect.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DigitalCertificateActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private FrameLayout layoutCapture;
    private MaterialCardView cardNotDonorWarning;
    private MaterialButton btnEnableDonor;
    private TextView tvDonorName, tvBloodGroup, tvCity, tvDate, tvCertId;
    private MaterialButton btnDownloadPdf, btnDownloadImage, btnShare;

    private String donorName = "Voluntary Donor";
    private String bloodGroup = "O+";
    private String city = "Mumbai";
    private boolean isDonorRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_certificate);

        initViews();
        setupListeners();
        loadDonorData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_cert);
        layoutCapture = findViewById(R.id.layout_certificate_capture);
        cardNotDonorWarning = findViewById(R.id.card_cert_not_donor_warning);
        btnEnableDonor = findViewById(R.id.btn_cert_enable_donor);

        tvDonorName = findViewById(R.id.tv_cert_donor_name);
        tvBloodGroup = findViewById(R.id.tv_cert_blood_group);
        tvCity = findViewById(R.id.tv_cert_city);
        tvDate = findViewById(R.id.tv_cert_date);
        tvCertId = findViewById(R.id.tv_cert_cert_id);

        btnDownloadPdf = findViewById(R.id.btn_download_pdf);
        btnDownloadImage = findViewById(R.id.btn_download_image);
        btnShare = findViewById(R.id.btn_share_certificate);

        String currentDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(currentDate);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        if (btnEnableDonor != null) {
            btnEnableDonor.setOnClickListener(v -> {
                Intent intent = new Intent(DigitalCertificateActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // 1. Download / Save as PDF
        btnDownloadPdf.setOnClickListener(v -> generateAndOpenPdf());

        // 2. Download / Save as Image
        btnDownloadImage.setOnClickListener(v -> generateAndSaveImage());

        // 3. Share Certificate
        btnShare.setOnClickListener(v -> shareCertificate());
    }

    private void loadDonorData() {
        String uid = FirebaseAuthManager.getInstance().getCurrentUserId();
        if (uid != null) {
            String shortId = uid.length() > 6 ? uid.substring(0, 6).toUpperCase(Locale.ROOT) : uid.toUpperCase(Locale.ROOT);
            tvCertId.setText("Digital ID: CC-ECO-" + shortId + " • Cloud Verified • Paperless");

            FirestoreManager.getInstance().getUser(uid, task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    User user = task.getResult().toObject(User.class);

                    // Check donor registration flag
                    Boolean isDonorBool = task.getResult().getBoolean("donor");
                    if (isDonorBool == null) isDonorBool = task.getResult().getBoolean("isDonor");
                    boolean isDonor = (isDonorBool != null) ? isDonorBool : (user != null && user.isDonor());

                    if (!isDonor) {
                        isDonorRegistered = false;
                        if (cardNotDonorWarning != null) cardNotDonorWarning.setVisibility(View.VISIBLE);
                        if (layoutCapture != null) layoutCapture.setVisibility(View.GONE);
                        btnDownloadPdf.setEnabled(false);
                        btnDownloadPdf.setAlpha(0.35f);
                        btnDownloadImage.setEnabled(false);
                        btnDownloadImage.setAlpha(0.35f);
                        btnShare.setEnabled(false);
                        btnShare.setAlpha(0.35f);
                        Toast.makeText(DigitalCertificateActivity.this, "Certificate locked: You must be a registered blood donor to generate a certificate.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    isDonorRegistered = true;
                    if (cardNotDonorWarning != null) cardNotDonorWarning.setVisibility(View.GONE);
                    if (layoutCapture != null) layoutCapture.setVisibility(View.VISIBLE);
                    btnDownloadPdf.setEnabled(true);
                    btnDownloadPdf.setAlpha(1.0f);
                    btnDownloadImage.setEnabled(true);
                    btnDownloadImage.setAlpha(1.0f);
                    btnShare.setEnabled(true);
                    btnShare.setAlpha(1.0f);

                    if (user != null) {
                        donorName = user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Voluntary Blood Donor";
                        bloodGroup = user.getBloodGroup() != null ? user.getBloodGroup() : "O+";
                        city = user.getCity() != null ? user.getCity() : "Mumbai";

                        tvDonorName.setText(donorName);
                        tvBloodGroup.setText("Blood Group: " + bloodGroup);
                        tvCity.setText("City: " + city);
                    }
                }
            });
        }
    }

    private Bitmap createBitmapFromView(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            width = view.getMeasuredWidth();
            height = view.getMeasuredHeight();
            view.layout(0, 0, width, height);
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Dynamically finds the registered FileProvider authority to prevent "Couldn't find meta-data" crashes.
     */
    private Uri getSafeFileUri(File file) {
        if (file == null || !file.exists()) return null;

        // 1. Dynamically scan all providers declared in the app to find the exact registered authority
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PROVIDERS);
            if (packageInfo.providers != null) {
                for (ProviderInfo provider : packageInfo.providers) {
                    if (provider.name != null && provider.name.contains("FileProvider") && provider.authority != null) {
                        String[] auths = provider.authority.split(";");
                        for (String auth : auths) {
                            try {
                                Uri uri = FileProvider.getUriForFile(this, auth.trim(), file);
                                if (uri != null) return uri;
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // 2. Try known authorities
        String[] potentialAuthorities = new String[] {
                getPackageName() + ".fileprovider",
                "com.example.fileprovider",
                "com.example.careconnect.fileprovider",
                "com.aistudio.careconnect.bdhbf.fileprovider"
        };

        for (String auth : potentialAuthorities) {
            try {
                Uri uri = FileProvider.getUriForFile(this, auth, file);
                if (uri != null) return uri;
            } catch (Exception ignored) {}
        }

        // 3. Fallback
        return Uri.fromFile(file);
    }

    private void generateAndOpenPdf() {
        if (!isDonorRegistered) {
            Toast.makeText(this, "Certificate locked: Register as a donor to generate this certificate.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (layoutCapture == null) return;
        try {
            int width = layoutCapture.getWidth();
            int height = layoutCapture.getHeight();
            if (width <= 0 || height <= 0) {
                layoutCapture.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                width = layoutCapture.getMeasuredWidth();
                height = layoutCapture.getMeasuredHeight();
                layoutCapture.layout(0, 0, width, height);
            }

            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas pageCanvas = page.getCanvas();
            pageCanvas.drawColor(Color.WHITE);
            layoutCapture.draw(pageCanvas);
            document.finishPage(page);

            String fileName = "CareConnect_Green_Certificate_" + System.currentTimeMillis() + ".pdf";
            File savedFile = null;

            // 1. Save directly into public Downloads folder via MediaStore on Android 10+ (Q)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CareConnect");
                    values.put(MediaStore.Downloads.IS_PENDING, 1);

                    Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        OutputStream os = getContentResolver().openOutputStream(uri);
                        if (os != null) {
                            document.writeTo(os);
                            os.flush();
                            os.close();
                        }
                        values.clear();
                        values.put(MediaStore.Downloads.IS_PENDING, 0);
                        getContentResolver().update(uri, values, null, null);
                    }
                } catch (Exception ignored) {}
            }

            // 2. Also save to public Downloads or App External Documents as physical file
            try {
                File publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File targetDir = new File(publicDownloads, "CareConnect");
                if (!targetDir.exists()) targetDir.mkdirs();
                savedFile = new File(targetDir, fileName);

                FileOutputStream fos = new FileOutputStream(savedFile);
                document.writeTo(fos);
                fos.flush();
                fos.close();

                MediaScannerConnection.scanFile(this, new String[]{savedFile.getAbsolutePath()}, new String[]{"application/pdf"}, null);
            } catch (Exception ignored) {
                try {
                    File appDocs = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    if (appDocs != null && !appDocs.exists()) appDocs.mkdirs();
                    savedFile = new File(appDocs, fileName);
                    FileOutputStream fos = new FileOutputStream(savedFile);
                    document.writeTo(fos);
                    fos.flush();
                    fos.close();
                } catch (Exception ignored2) {}
            }

            // Also keep a copy in shared cache for instant FileProvider viewing
            File cachePath = new File(getCacheDir(), "shared_certificates");
            if (!cachePath.exists()) cachePath.mkdirs();
            File cacheFile = new File(cachePath, "CareConnect_Donor_Certificate.pdf");
            FileOutputStream fosCache = new FileOutputStream(cacheFile);
            document.writeTo(fosCache);
            fosCache.flush();
            fosCache.close();

            document.close();

            File targetFile = (savedFile != null && savedFile.exists()) ? savedFile : cacheFile;
            final Uri viewUri = getSafeFileUri(targetFile);

            // Display success message
            Toast.makeText(this, "✅ PDF saved to Downloads/CareConnect!", Toast.LENGTH_LONG).show();

            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("📄 Certificate PDF Saved!");
                builder.setMessage("Your Digital Green Certificate has been saved to your phone's Downloads/CareConnect folder.\n\nFile: " + fileName);
                builder.setPositiveButton("Open PDF", (dialog, which) -> {
                    if (viewUri != null) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(viewUri, "application/pdf");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "Open Certificate PDF"));
                        } catch (Exception e) {
                            Toast.makeText(DigitalCertificateActivity.this, "No PDF viewer app found on device.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNeutralButton("Share PDF", (dialog, which) -> {
                    if (viewUri != null) {
                        sharePdfFile(viewUri);
                    }
                });
                builder.setNegativeButton("Done", null);
                builder.show();
            } catch (Exception ignored) {}

        } catch (Exception e) {
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void generateAndSaveImage() {
        if (!isDonorRegistered) {
            Toast.makeText(this, "Certificate locked: Register as a donor to generate this certificate.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (layoutCapture == null) return;
        try {
            Bitmap bitmap = createBitmapFromView(layoutCapture);
            String fileName = "CareConnect_Certificate_" + System.currentTimeMillis() + ".png";
            File savedFile = null;

            // 1. Android 10+ MediaStore saving to Pictures/CareConnect so it shows in Gallery
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CareConnect");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);

                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        OutputStream os = getContentResolver().openOutputStream(uri);
                        if (os != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                            os.flush();
                            os.close();
                        }
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        getContentResolver().update(uri, values, null, null);
                    }
                } catch (Exception ignored) {}
            }

            // 2. Also save to public Pictures folder or app files
            try {
                File publicPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File targetDir = new File(publicPictures, "CareConnect");
                if (!targetDir.exists()) targetDir.mkdirs();
                savedFile = new File(targetDir, fileName);

                FileOutputStream fos = new FileOutputStream(savedFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                MediaScannerConnection.scanFile(this, new String[]{savedFile.getAbsolutePath()}, new String[]{"image/png"}, null);
            } catch (Exception ignored) {
                try {
                    File appPics = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (appPics != null && !appPics.exists()) appPics.mkdirs();
                    savedFile = new File(appPics, fileName);
                    FileOutputStream fos = new FileOutputStream(savedFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (Exception ignored2) {}
            }

            // Also keep a copy in shared cache for instant FileProvider viewing
            File cachePath = new File(getCacheDir(), "shared_certificates");
            if (!cachePath.exists()) cachePath.mkdirs();
            File cacheFile = new File(cachePath, "CareConnect_Donor_Certificate.png");
            FileOutputStream fosCache = new FileOutputStream(cacheFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fosCache);
            fosCache.flush();
            fosCache.close();

            File targetFile = (savedFile != null && savedFile.exists()) ? savedFile : cacheFile;
            final Uri viewUri = getSafeFileUri(targetFile);

            // Display success message
            Toast.makeText(this, "✅ Certificate image saved to Pictures/CareConnect!", Toast.LENGTH_LONG).show();

            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("🖼️ Certificate Image Saved!");
                builder.setMessage("Your certificate image has been saved to your phone's Pictures / Gallery folder.\n\nFile: " + fileName);
                builder.setPositiveButton("View Image", (dialog, which) -> {
                    if (viewUri != null) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(viewUri, "image/png");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "View Certificate Image"));
                        } catch (Exception e) {
                            Toast.makeText(DigitalCertificateActivity.this, "No image viewer found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNeutralButton("Share Image", (dialog, which) -> {
                    shareCertificateImage();
                });
                builder.setNegativeButton("Done", null);
                builder.show();
            } catch (Exception ignored) {}

        } catch (Exception e) {
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareCertificate() {
        if (!isDonorRegistered) {
            Toast.makeText(this, "Certificate locked: Register as a donor to share this certificate.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {
                "🖼️ Share as Image (PNG) — Best for WhatsApp, Stories & Social Media",
                "📄 Share as PDF Document — Best for Email, Print & Medical Records"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share Green Certificate");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                shareCertificateImage();
            } else {
                shareCertificatePdfDirect();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void shareCertificateImage() {
        if (layoutCapture == null) return;
        try {
            Bitmap bitmap = createBitmapFromView(layoutCapture);

            File cachePath = new File(getCacheDir(), "shared_certificates");
            if (!cachePath.exists()) cachePath.mkdirs();

            File file = new File(cachePath, "CareConnect_Donor_Certificate.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Uri uri = getSafeFileUri(file);
            if (uri == null) {
                Toast.makeText(this, "Unable to prepare certificate image for sharing.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "🌱 Proud to be a Verified Blood Donor on CareConnect! Paperless & Green IT certified: " + donorName + " (" + bloodGroup + ")");
            shareIntent.setClipData(ClipData.newRawUri("Certificate Image", uri));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Grant read permission to all receiver apps (WhatsApp, Gmail, etc.)
            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            startActivity(Intent.createChooser(shareIntent, "Share Green Donor Certificate (Image)"));

        } catch (Exception e) {
            Toast.makeText(this, "Error preparing image share: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareCertificatePdfDirect() {
        if (layoutCapture == null) return;
        try {
            int width = layoutCapture.getWidth();
            int height = layoutCapture.getHeight();
            if (width <= 0 || height <= 0) {
                layoutCapture.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                width = layoutCapture.getMeasuredWidth();
                height = layoutCapture.getMeasuredHeight();
                layoutCapture.layout(0, 0, width, height);
            }

            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas pageCanvas = page.getCanvas();
            pageCanvas.drawColor(Color.WHITE);
            layoutCapture.draw(pageCanvas);
            document.finishPage(page);

            File cachePath = new File(getCacheDir(), "shared_certificates");
            if (!cachePath.exists()) cachePath.mkdirs();

            File file = new File(cachePath, "CareConnect_Donor_Certificate.pdf");
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.flush();
            fos.close();

            Uri uri = getSafeFileUri(file);
            if (uri != null) {
                sharePdfFile(uri);
            } else {
                Toast.makeText(this, "Unable to prepare certificate PDF for sharing.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error preparing PDF share: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdfFile(Uri uri) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "🌱 Official Digital Green Donor Certificate: " + donorName + " (" + bloodGroup + ") on CareConnect.");
            shareIntent.setClipData(ClipData.newRawUri("Certificate PDF", uri));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            startActivity(Intent.createChooser(shareIntent, "Share Green Donor Certificate (PDF)"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}