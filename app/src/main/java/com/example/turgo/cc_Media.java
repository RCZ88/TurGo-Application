package com.example.turgo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_Media#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_Media extends Fragment implements checkFragmentCompletion{
    RecyclerView rv_courseLogo, rv_courseBanner, rv_uploadedCourseImages;
    Button btn_uploadCourseLogo, btn_uploadCourseBanner, btn_uploadImages, btn_removeAllCourseImageUploaded;
    TextView tv_emptyCourseImageUpload;
    private static final int PICK_FILE_REQUEST = 100;
    private Uri selectedFileUri;
    private OnFileSelectedListener currentUploadCallback;
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

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
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
        btn_removeAllCourseImageUploaded = view.findViewById(R.id.btn_CC_removeAllImage);

        tv_emptyCourseImageUpload = view.findViewById(R.id.tv_CC_MediaUploadEmpty);

        setDefaultCourseIconOptions(cc);
        setDefaultCourseBannerOptions(cc);
        ViewGroup.LayoutParams params = rv_uploadedCourseImages.getLayoutParams();
        params.height = dpToPx(80);
        rv_uploadedCourseImages.setLayoutParams(params);
        Tool.handleEmpty(true, rv_uploadedCourseImages, tv_emptyCourseImageUpload);



//        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference(FirebaseNode.BUILT_IN_COURSE_BANNER.getPath());
//        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<String>cloudinaryLinks = new ArrayList<>();
//                for(DataSnapshot link: snapshot.getChildren()){
//                    cloudinaryLinks.add((String)link.getValue());
//                }
//
//                ImageAdapter iaCB = new ImageAdapter(requireContext(), cloudinaryLinks);
//                iaCB.setOnItemClickListener(new OnItemClickListener<>() {
//                    @Override
//                    public void onItemClick(String cloudLink) {
//                        cc.courseBannerCloudinary = cloudLink;
//                        int position = cloudinaryLinks.indexOf(cloudLink);
//                        iaCB.setSelectedPosition(position);
//                    }
//
//                    @Override
//                    public void onItemLongClick(String item) {
//
//                    }
//                });
//                rv_courseBanner.setAdapter(iaCB);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        dbr = FirebaseDatabase.getInstance().getReference(FirebaseNode.BUILT_IN_COURSE_LOGO.getPath());
//        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<String>cloudinaryLinks = new ArrayList<>();
//                for(DataSnapshot link: snapshot.getChildren()){
//                    cloudinaryLinks.add((String)link.getValue());
//                }
//
//                ImageAdapter iaCL = new ImageAdapter(requireContext(), cloudinaryLinks);
//                iaCL.setOnItemClickListener(new OnItemClickListener<String>() {
//                    @Override
//                    public void onItemClick(String cloudLink) {
//                        cc.courseIconCloudinary = cloudLink;
//                        int position = cloudinaryLinks.indexOf(cloudLink);
//                        iaCL.setSelectedPosition(position);
//                    }
//
//                    @Override
//                    public void onItemLongClick(String item) {
//
//                    }
//                });
//                rv_courseBanner.setAdapter(iaCL);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        RTDBManager<fileFirebase> rtdbManager = new RTDBManager<>();
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();  // Use class field directly
                        if (selectedFileUri != null){
                            Log.d("cc_Media", "File Selected: " + selectedFileUri.getPath());

                            // Capture final variables for thread use
//                            Uri pickedUri = selectedFileUri;
//                            CloudinaryUploadCallback callback = currentUploadCallback;
                            String fileName = Tool.getFileName(requireContext(), selectedFileUri);
                            Log.d("cc_Media", "File '" + fileName + "' Successfully Selected!");
                            currentUploadCallback.onFileSelected(fileName,selectedFileUri);

//                            new Thread(() -> {
//
//
//
//                                try {
//                                    Tool.uploadToCloudinary(Tool.uriToFile(pickedUri, requireContext()), new ObjectCallBack<>() {
//                                        @Override
//                                        public void onObjectRetrieved(String object) {
//                                            if(callback != null) {
//                                                callback.onUploadComplete(object, fileName, pickedUri);
//
//                                                requireActivity().runOnUiThread(() -> {
//                                                    Toast.makeText(requireContext(),
//                                                            "Upload successful: " + object,
//                                                            Toast.LENGTH_LONG).show();
//                                                });
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onError(DatabaseError error) {
//                                            Log.d("cc_Media", "Error Database: " + error);
//                                        }
//                                    });
//                                } catch (IOException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }).start();

                        } else {
                            Log.d("cc_Media", "File Selected is Null");
                        }
                    }
                });

        btn_uploadCourseLogo.setOnClickListener(v -> openFilePicker((fileName, uri) -> {
            cc.courseIcon = uri;
            ArrayList<Uri>files = new ArrayList<>();
            files.add(uri);
            ImageAdapter iaCLU = new ImageAdapter(files);
//                iaCL.getUris().add(uri);
            iaCLU.setSelectedPosition(0);
            iaCLU.setOnItemClickListener(new OnItemClickListener<>() {
                @Override
                public void onItemClick(Integer item) {

                }

                @Override
                public void onItemLongClick(Integer item) {
                    requireActivity().runOnUiThread(() -> {
                        cc.courseIcon = null;
                        iaCLU.getUris().clear();

                        setDefaultCourseIconOptions(cc);
                        Toast.makeText(requireContext(), "Icon removed", Toast.LENGTH_SHORT).show();

                    });

                }
            });
            requireActivity().runOnUiThread(()->{
                rv_courseLogo.setAdapter(iaCLU);
            });
        }));
        btn_uploadCourseBanner.setOnClickListener(v -> openFilePicker((fileName, uri) -> {
            cc.courseBanner = uri;
            ArrayList<Uri>files = new ArrayList<>();
            files.add(uri);
            ImageAdapter iaCBU = new ImageAdapter(files);
            iaCBU.setSelectedPosition(0);
            iaCBU.setOnItemClickListener(new OnItemClickListener<>() {
                @Override
                public void onItemClick(Integer item) {

                }

                @Override
                public void onItemLongClick(Integer item) {
                    requireActivity().runOnUiThread(() ->{
                        cc.courseIcon = null;
                        iaCBU.getUris().clear();

                        // Reset to default adapter
                        setDefaultCourseBannerOptions(cc);

                        Toast.makeText(requireContext(), "Banner removed", Toast.LENGTH_SHORT).show();

                    });

                }
            });
            requireActivity().runOnUiThread(() ->{
                rv_courseBanner.setAdapter(iaCBU);
            });
        }));
        ImageAdapter iaCIM = new ImageAdapter(new ArrayList<>());
        iaCIM.setOnItemClickListener(new OnItemClickListener<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(Integer item) {

            }

            @Override
            public void onItemLongClick(Integer item) {
                iaCIM.getUris().remove((int)item);
                iaCIM.notifyDataSetChanged();
                cc.courseImages.remove((int)item);
                Tool.handleEmpty(iaCIM.getUris().isEmpty(), rv_uploadedCourseImages, tv_emptyCourseImageUpload);
            }
        });
        rv_uploadedCourseImages.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        rv_uploadedCourseImages.setAdapter(iaCIM);
        btn_uploadImages.setOnClickListener(v -> {
            openFilePicker((fileName, uri) -> {
                Log.d("cc_Media", "File Received from openFilePicker: " + fileName);
                cc.courseImages.add(uri);
                iaCIM.getUris().add(uri);
                iaCIM.notifyDataSetChanged();
                Tool.handleEmpty(false, rv_uploadedCourseImages, tv_emptyCourseImageUpload);
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                rv_uploadedCourseImages.setLayoutParams(params);
//                    try {
//                        file.updateDB();
//                    } catch (NoSuchMethodException | InvocationTargetException |
//                             IllegalAccessException | java.lang.InstantiationException e) {
//                        throw new RuntimeException(e);
//                    }
//                    fileFirebase ff = new fileFirebase();
//                    ff.importObjectData(file);
//                    rtdbManager.storeData(FirebaseNode.COURSE_IMAGES.getPath(), ff.getFileID(), ff, "Uploaded course image", "Uploaded course image");
            });

        });
        btn_removeAllCourseImageUploaded.setOnClickListener(view1 -> {
            cc.courseImages.clear();
            iaCIM.getUris().clear();
            iaCIM.notifyDataSetChanged();
            Tool.handleEmpty(true, rv_uploadedCourseImages, tv_emptyCourseImageUpload);
        });
        return view;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setDefaultCourseIconOptions(CreateCourse cc) {
        ArrayList<Integer>imageIds = Tool.getListIdFromXmlArray(requireContext(), R.array.logo_list);
        ImageAdapter iaCL = new ImageAdapter(imageIds, 0);
        ArrayList<Integer> finalImageIds = imageIds;
        iaCL.setOnItemClickListener(new OnItemClickListener<>() {
            @Override
            public void onItemClick(Integer item) {
                cc.courseIconCloudinary = getCloudinaryOfImageId(item);
                int index = finalImageIds.indexOf(item);
                iaCL.setSelectedPosition(index);
            }

            @Override
            public void onItemLongClick(Integer item) {

            }
        });
        rv_courseLogo.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_courseLogo.setAdapter(iaCL);
    }
    private void setDefaultCourseBannerOptions(CreateCourse cc){
        ArrayList<Integer> imageIds;
        imageIds = Tool.getListIdFromXmlArray(requireContext(), R.array.banner_list);
        ImageAdapter iaCB = new ImageAdapter(imageIds, 0);
        ArrayList<Integer> finalImageIds1 = imageIds;
        iaCB.setOnItemClickListener(new OnItemClickListener<>() {
            @Override
            public void onItemClick(Integer item) {
                cc.courseBannerCloudinary = getCloudinaryOfImageId(item);
                int index = finalImageIds1.indexOf(item);
                iaCB.setSelectedPosition(index);
            }

            @Override
            public void onItemLongClick(Integer item) {

            }
        });
        rv_courseBanner.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_courseBanner.setAdapter(iaCB);
    }

    private void openFilePicker(OnFileSelectedListener cuc) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // You can limit it to "image/*" or "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        currentUploadCallback = cuc;
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }
    private String getCloudinaryOfImageId(int id){
        //bruteforce for now:
        if(id == R.drawable.idea_course_icon){
            return "https://res.cloudinary.com/daccry0jr/image/upload/v1764169228/idea_course_icon_woo2qa.png";
        }else if(id == R.drawable.language_course_icon){
            return "https://res.cloudinary.com/daccry0jr/image/upload/v1764169229/langaunge_course_icon_hdyza5.png";
        }else if(id == R.drawable.music_course_icon){
            return "https://res.cloudinary.com/daccry0jr/image/upload/v1764169228/music_course_icon_iitknr.png";
        }else if (id == R.drawable.science_course_icon){
            return "https://res.cloudinary.com/daccry0jr/image/upload/v1764169228/music_course_icon_iitknr.png";
        }else if(id == R.drawable.banner_red){
            return "https://res.cloudinary.com/daccry0jr/image/upload/v1760587847/fekyq44pgqymklluoaoi.png";
        }else if(id == R.drawable.banner_blue){
            return "https://res.cloudinary.com/daccry0jr/image/upload/v1760587817/hyncdevsv92vkf4xl5te.png";
        }else if(id == R.drawable.banner_green){
            return "https://res.cloudinary.com/daccry0jr/image/upload/v1760587835/tldbom7l9jfnrsf8zzu2.png";
        }
        return null;
    }


    @Override
    public boolean checkIfCompleted() {
        CreateCourse cc = (CreateCourse) requireActivity();
        if(cc.courseBanner == null && cc.courseBannerCloudinary == null){
            Toast.makeText(requireContext(), "Please Select a Course Banner!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(cc.courseIcon == null && cc.courseIconCloudinary == null){
            Toast.makeText(requireContext(), "Please Select a Course Icon!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(cc.courseImages == null){
            Toast.makeText(requireContext(), "Please add at least one Course Image", Toast.LENGTH_SHORT).show();
            return  false;
        }
        return true;

    }
}