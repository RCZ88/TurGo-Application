package com.example.turgo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_Media#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_Media extends Fragment {
    RecyclerView rv_courseLogo, rv_courseBanner, rv_uploadedCourseImages;
    Button btn_uploadCourseLogo, btn_uploadCourseBanner, btn_uploadImages, btn_removeImage;
    private static final int PICK_FILE_REQUEST = 100;
    private Uri selectedFileUri;
    private CloudinaryUploadCallback currentUploadCallback;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public cc_Media() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment cc_Media.
     */
    // TODO: Rename and change types and number of parameters
    public static cc_Media newInstance(String param1, String param2) {
        cc_Media fragment = new cc_Media();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cc__media, container, false);
        CreateCourse cc = (CreateCourse) requireActivity();
        rv_courseBanner  = view.findViewById(R.id.rv_CC_courseBannerOptions);
        rv_courseLogo = view.findViewById(R.id.rv_CC_courseLogoOptions);
        rv_uploadedCourseImages = view.findViewById(R.id.rv_uploadedPreviewImage);

        btn_uploadCourseBanner = view.findViewById(R.id.btn_CC_uploadBanner);
        btn_uploadCourseLogo = view.findViewById(R.id.btn_CC_uploadLogo);
        btn_uploadImages = view.findViewById(R.id.btn_CC_uploadPreviewImage);
        btn_removeImage = view.findViewById(R.id.btn_CC_removeUploadedImages);

        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference(FirebaseNode.BUILT_IN_COURSE_BANNER.getPath());
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String>cloudinaryLinks = new ArrayList<>();
                for(DataSnapshot link: snapshot.getChildren()){
                    cloudinaryLinks.add((String)link.getValue());
                }

                ImageAdapter iaCB = new ImageAdapter(requireContext(), cloudinaryLinks);
                iaCB.setOnItemClickListener(cloudLink -> {
                    cc.courseBannerCloudinary = cloudLink;
                    int position = cloudinaryLinks.indexOf(cloudLink);
                    iaCB.setSelectedPosition(position);
                });
                rv_courseBanner.setAdapter(iaCB);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dbr = FirebaseDatabase.getInstance().getReference(FirebaseNode.BUILT_IN_COURSE_LOGO.getPath());
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String>cloudinaryLinks = new ArrayList<>();
                for(DataSnapshot link: snapshot.getChildren()){
                    cloudinaryLinks.add((String)link.getValue());
                }

                ImageAdapter iaCL = new ImageAdapter(requireContext(), cloudinaryLinks);
                iaCL.setOnItemClickListener(cloudLink -> {
                    cc.courseIconCloudinary = cloudLink;
                    int position = cloudinaryLinks.indexOf(cloudLink);
                    iaCL.setSelectedPosition(position);
                });
                rv_courseBanner.setAdapter(iaCL);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        RTDBManager<file> rtdbManager = new RTDBManager<>();
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedFileUri = result.getData().getData();
                        String apiKey = BuildConfig.CLOUDINARY_API_KEY;
                        String cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME;
                        String apiSecret = BuildConfig.CLOUDINARY_API_SECRET;

                        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                            "cloud_name", cloudName,
                                "api_key", apiKey,
                                "api_secret", apiSecret
                        ));
                        new Thread(() ->{
                            try {
                                assert selectedFileUri != null;
                                InputStream inputStream = requireContext().getContentResolver()
                                        .openInputStream(selectedFileUri);
                                String fileName = Tool.getFileName(requireContext(), selectedFileUri);
                                Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.asMap("resource_type", "auto"));
                                String cloudinaryUrl = (String) uploadResult.get("secure_url");
                                currentUploadCallback.onUploadComplete(cloudinaryUrl, fileName);

                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(),
                                            "Upload successful: " + cloudinaryUrl,
                                            Toast.LENGTH_LONG).show();
                                });

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        // Example: display file name or upload
                        Toast.makeText(requireContext(),
                                "Selected: " + selectedFileUri.toString(),
                                Toast.LENGTH_SHORT).show();

                        // TODO: Upload to Cloudinary/Firebase
                    }
                });
        btn_uploadCourseLogo.setOnClickListener(v -> openFilePicker(new CloudinaryUploadCallback() {
            @Override
            public void onUploadComplete(String cloudinaryUrl, String fileName) {
                file file = new file(fileName, cloudinaryUrl, cc.admin, LocalDateTime.now());
                cc.courseIcon = file;
//                rtdbManager.storeData(FirebaseNode.UPLOADED_COURSE_LOGO.getPath(), file.getID(), file, "Uploaded course logo", "Uploaded course logo");
            }

            @Override
            public void onUploadFailed(Exception error) {

            }
        }));
        btn_uploadCourseBanner.setOnClickListener(v -> openFilePicker(new CloudinaryUploadCallback() {
            @Override
            public void onUploadComplete(String cloudinaryUrl, String fileName) {
                file file = new file(fileName, cloudinaryUrl, cc.admin, LocalDateTime.now());
                cc.courseBanner = file;
//                rtdbManager.storeData(FirebaseNode.UPLOADED_COURSE_BANNER.getPath(), file.getID(), file, "Uploaded course banner", "Uploaded course banner");
            }

            @Override
            public void onUploadFailed(Exception error) {

            }
        }));
        btn_uploadImages.setOnClickListener(v -> {
            openFilePicker(new CloudinaryUploadCallback() {
                @Override
                public void onUploadComplete(String cloudinaryUrl, String fileName) {
                    file file = new file(fileName, cloudinaryUrl, cc.admin, LocalDateTime.now());
                    rtdbManager.storeData(FirebaseNode.COURSE_IMAGES.getPath(), file.getID(), file, "Uploaded course image", "Uploaded course image");
                }

                @Override
                public void onUploadFailed(Exception error) {

                }
            });

        });
        btn_removeImage.setOnClickListener(view1 -> {

        });
        return view;
    }

    private void openFilePicker(CloudinaryUploadCallback cuc) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // You can limit it to "image/*" or "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        currentUploadCallback = cuc;
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }


}