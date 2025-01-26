package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSnapActivity extends AppCompatActivity {

    ImageView photoView;
    ImageButton backBtn, checkBtn;
    ProgressBar progressBar;
    LinearLayout sourceLin;
    TextView sourceTxt;
    EditText descEdtx;
    String snap_id;
    Spannable mspanable;
    int hashTagIsComing = 0;
    Switch allowCommentSwitch;
    String sourceStr;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_snap);

        snap_id = getIntent().getStringExtra("snap_id");

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        sourceLin = findViewById(R.id.editsnap_addSourceLin);
        sourceTxt = findViewById(R.id.editsnap_addSourceTxt);
        allowCommentSwitch = findViewById(R.id.editsnap_commentSwitch);
        photoView = findViewById(R.id.editsnap_imageview);
        backBtn = findViewById(R.id.editsnap_backBtn);
        checkBtn = findViewById(R.id.editsnap_completeBtn);
        progressBar = findViewById(R.id.editsnap_progressbar);
        descEdtx = findViewById(R.id.editsnap_desc);

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> hashtags = new ArrayList<>();
                String[] words = descEdtx.getText().toString().split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].startsWith("#")) {
                        hashtags.add(words[i]);
                    }
                }
                if (hashtags.size() <= 20) {
                    updateSnap();
                } else {
                    Toast.makeText(EditSnapActivity.this, getString(R.string.hashtagsonasnap), Toast.LENGTH_SHORT).show();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        sourceLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(EditSnapActivity.this);
                bottomSheetDialog.setContentView(R.layout.bottomsheet_createpost_source);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                ImageButton closeBtn, finishBtn;
                EditText editText;
                closeBtn = bottomSheetDialog.findViewById(R.id.bcs_closeBtn);
                finishBtn = bottomSheetDialog.findViewById(R.id.bcs_completeBtn);
                editText = bottomSheetDialog.findViewById(R.id.bcs_edtx);
                editText.setText(sourceStr);

                editText.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            if (editText.getText().toString().trim().length() != 0) {
                                sourceStr = editText.getText().toString().replaceAll("\\s+$", "");
                                sourceTxt.setVisibility(View.VISIBLE);
                                sourceTxt.setText(sourceStr);
                            } else {
                                sourceTxt.setVisibility(View.GONE);
                                sourceStr = null;
                            }
                            bottomSheetDialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
                finishBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editText.getText().toString().trim().length() != 0) {
                            sourceStr = editText.getText().toString().replaceAll("\\s+$", "");
                            sourceTxt.setVisibility(View.VISIBLE);
                            sourceTxt.setText(sourceStr);
                        } else {
                            sourceTxt.setVisibility(View.GONE);
                            sourceStr = null;
                        }
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.show();
            }
        });


        getSnapInfo();
    }

    private void updateSnap() {
        checkBtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        descEdtx.setEnabled(false);
        backBtn.setEnabled(false);

        /*List<String> taglist = new ArrayList<>();
        String regexPattern = "(#\\w+)";
        Pattern p = Pattern.compile(regexPattern);
        Matcher m = p.matcher(descEdtx.getText().toString());
        while (m.find()){
            String hashtag = m.group(1);
            String tag = hashtag.substring(1);
            taglist.add(tag);
        }*/

        Map<String, Object> snapMap = new HashMap<>();
        snapMap.put("description", descEdtx.getText().toString());
        if (sourceStr != null) {
            snapMap.put("source", sourceStr);
        } else {
            snapMap.put("source", null);
        }
        //snapMap.put("tag", taglist);
        if (allowCommentSwitch.isChecked()) {
            snapMap.put("allow_comment", true);
        } else {
            snapMap.put("allow_comment", false);
        }
        firestore.collection("snap").document(snap_id).update(snapMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EditSnapActivity.this, getString(R.string.editcomplete), Toast.LENGTH_SHORT).show();
                        finish();
                        /*if (HomeFollowingFragment.mContext != null) {
                            HomeFollowingFragment.updateSnap(HomeFollowingFragment.mContext);
                            finish();
                        } else {
                            startActivity(new Intent(EditSnapActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }*/
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditSnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        checkBtn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        descEdtx.setEnabled(true);
                        backBtn.setEnabled(true);
                    }
                });
    }

    private void getSnapInfo() {
        firestore.collection("snap").document(snap_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String author_id, content_id;
                        author_id = documentSnapshot.getString("author_id");
                        content_id = documentSnapshot.getString("content_id");
                        String content_url = "https://cdn.idolsnap.net/snap/" + author_id + "/" + content_id + "/" + content_id + "_1080x1080";
                        Glide.with(EditSnapActivity.this)
                                .load(content_url)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .error(R.drawable.ic_placeholder)
                                .into(photoView);
                        setTags(descEdtx, documentSnapshot.getString("description"));
                        if (documentSnapshot.getString("source") != null) {
                            sourceTxt.setVisibility(View.VISIBLE);
                            sourceStr = documentSnapshot.getString("source");
                            sourceTxt.setText(sourceStr);
                        } else {
                            sourceTxt.setVisibility(View.GONE);
                        }
                        if (documentSnapshot.getBoolean("allow_comment") == true) {
                            allowCommentSwitch.setChecked(true);
                        } else {
                            allowCommentSwitch.setChecked(false);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditSnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void changeTheColor(String s, int start, int end) {
        mspanable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setTags(EditText editText, String description) {
        SpannableString string = new SpannableString(description);

        int start = -1;
        for (int i = 0; i < description.length(); i++) {
            if (description.charAt(i) == '#') {
                start = i;
            } else if (description.charAt(i) == ' ' || description.charAt(i) == '\n' || (i == description.length() - 1 && start != -1)) {
                if (start != -1) {
                    if (i == description.length() - 1) {
                        i++; // case for if hash is last word and there is no
                        // space after word
                    }

                    final String tag = description.substring(start, i);
                    string.setSpan(new ClickableSpan() {

                        @Override
                        public void onClick(View widget) {
                            //Log.d("Hash", String.format("Clicked %s!", tag));
                            //Toast.makeText(EditSnapActivity.this, tag, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            // link color
                            ds.setColor(Color.parseColor("#FB3958"));
                            ds.setUnderlineText(false);
                        }
                    }, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = -1;
                }
            }
        }

        editText.setMovementMethod(LinkMovementMethod.getInstance());
        editText.setText(string);

        mspanable = descEdtx.getText();
        descEdtx.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String startChar = null;
                try {
                    startChar = Character.toString(s.charAt(start));
                    //Log.i(getClass().getSimpleName(), "CHARACTER OF NEW WORD: " + startChar);
                } catch (Exception ex) {
                    startChar = "";
                }
                if (startChar.equals("#")) {
                    changeTheColor(s.toString().substring(start), start, start + count);
                    hashTagIsComing++;
                }
                if (startChar.equals(" ")) {
                    hashTagIsComing = 0;
                }
                if (hashTagIsComing != 0) {
                    changeTheColor(s.toString().substring(start), start, start + count);
                    hashTagIsComing++;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}