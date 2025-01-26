package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Model.Comment;
import com.euichankim.idolsnapandroid.R;
import com.euichankim.idolsnapandroid.ViewHolder.SnapComment_ViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class SnapCommentActivity extends AppCompatActivity {

    ImageButton backBtn, commentBtn;
    EditText commentEdtx;
    CircleImageView profileImg;
    ProgressBar commentProgressBar;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    String snap_id, snap_author_id;
    ConstraintLayout commentCons, noCommentCons;
    ArrayList<Comment> commentList;
    public static Context mContext;
    public static Query mQuery;
    public static FirestorePagingAdapter<Comment, SnapComment_ViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_comment);
        SnapCommentActivity.mContext = SnapCommentActivity.this;

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        snap_id = intent.getStringExtra("snap_id");

        backBtn = findViewById(R.id.comment_backBtn);
        profileImg = findViewById(R.id.comment_profileImg);
        commentProgressBar = findViewById(R.id.comment_progressbar);
        swipeRefreshLayout = findViewById(R.id.comment_swiperefresh);
        commentBtn = findViewById(R.id.comment_sendComment);
        commentBtn.setVisibility(View.GONE);
        commentEdtx = findViewById(R.id.comment_commentEdtx);
        commentCons = findViewById(R.id.comment_edtxCons);
        commentCons.setVisibility(View.GONE);
        noCommentCons = findViewById(R.id.comment_noCommentCons);
        noCommentCons.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.comment_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(SnapCommentActivity.this, LinearLayoutManager.VERTICAL, false));
        commentList = new ArrayList<>();
        mQuery = firestore.collection("snap")
                .document(snap_id).collection("comment")
                .whereEqualTo("visibility_private", false)
                .orderBy("commented_at", Query.Direction.DESCENDING);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateComment();
            }
        });

        commentEdtx.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (commentEdtx.getText().toString().trim().length() != 0) {
                    commentBtn.setVisibility(View.VISIBLE);
                } else {
                    commentBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentProgressBar.setVisibility(View.VISIBLE);
                commentBtn.setVisibility(View.INVISIBLE);
                addComment(commentEdtx.getText().toString());
            }
        });

        getComment();
        getUserProfile();
        getSnapInfo();
    }

    private void getSnapInfo() {
        firestore.collection("snap").document(snap_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        snap_author_id = documentSnapshot.getString("author_id");
                        if (documentSnapshot.getBoolean("allow_comment") == true) {
                            commentCons.setVisibility(View.VISIBLE);
                            noCommentCons.setVisibility(View.GONE);
                            commentEdtx.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        } else {
                            commentCons.setVisibility(View.GONE);
                            noCommentCons.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        snap_author_id = "";
                        commentCons.setVisibility(View.VISIBLE);
                        Toast.makeText(SnapCommentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserProfile() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String profile_img_id = documentSnapshot.getString("profile_img_id");
                if (profile_img_id.equals("default") || profile_img_id == null) {
                    Glide.with(SnapCommentActivity.this)
                            .load(R.drawable.ic_profile_default)
                            .into(profileImg);
                } else {
                    String content_url = "https://cdn.idolsnap.net/user/" + mAuth.getCurrentUser().getUid() + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                    Glide.with(SnapCommentActivity.this)
                            .load(content_url)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .error(R.drawable.ic_placeholder)
                            .into(profileImg);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SnapCommentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void updateComment() {
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Comment>()
                .setLifecycleOwner((LifecycleOwner) SnapCommentActivity.mContext)
                .setQuery(mQuery, pagingConfig, Comment.class)
                .build();
        mAdapter.updateOptions(options);
    }

    public void getComment() {
        // Init Paging Configuration
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Comment>()
                .setLifecycleOwner((LifecycleOwner) SnapCommentActivity.this)
                .setQuery(mQuery, pagingConfig, Comment.class)
                .build();

        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<Comment, SnapComment_ViewHolder>(options) {
            @NonNull
            @Override
            public SnapComment_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_comment, parent, false);
                return new SnapComment_ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull SnapComment_ViewHolder viewHolder, int i, @NonNull Comment comment) {
                // Bind to ViewHolder
                viewHolder.bind(comment, snap_id, snap_author_id);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull SnapComment_ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                swipeRefreshLayout.setRefreshing(false);
                //shimmerLayout.setVisibility(View.GONE);
                //shimmerLayout.stopShimmer();
            }
        };

        mAdapter.addOnPagesUpdatedListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (mAdapter.getItemCount() == 0) {
                    swipeRefreshLayout.setRefreshing(false);
                    //shimmerLayout.setVisibility(View.GONE);
                    //shimmerLayout.stopShimmer();
                }
                return null;
            }
        });

        recyclerView.setAdapter(mAdapter);
    }

    private void addComment(String commentTxt) {
        Comment comment = new Comment(snap_id, mAuth.getCurrentUser().getUid(), commentTxt, FieldValue.serverTimestamp(), false);
        firestore.collection("snap").document(snap_id).collection("comment").add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Map<String, Object> comment_id = new HashMap<>();
                        comment_id.put("comment_id", documentReference.getId());
                        firestore.collection("snap").document(snap_id).collection("comment").document(documentReference.getId())
                                .update(comment_id).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //updateComment();
                                            getComment();
                                            commentEdtx.setText("");
                                            commentProgressBar.setVisibility(View.GONE);
                                            //Toast.makeText(CommentActivity.this, getString(R.string.commentuploaded), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        commentEdtx.setText("");
                                        commentProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(SnapCommentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        commentEdtx.setText("");
                        commentProgressBar.setVisibility(View.GONE);
                        Toast.makeText(SnapCommentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void finish() {
        if (isTaskRoot()) {
            startActivity(new Intent(SnapCommentActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            super.finish();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}