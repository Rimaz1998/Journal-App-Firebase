package ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rinzler.myjournal.JournalListActivity;
import com.rinzler.myjournal.R;
import com.rinzler.myjournal.model.Journal;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder>  {
    private Context context;
    private List<Journal> journalList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");

    //create a constructor

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.journal_row,parent, false);

        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder holder, int position) {
        //the journal list is been passed from the mainactivity  and we set it here for a single row
        Journal journal = journalList.get(position);
        String imageUrl;

        holder.titleRow.setText(journal.getTitle());
        holder.thoughtRow.setText(journal.getThought());
        holder.usernameRow.setText(journal.getUserName());
        imageUrl = journal.getImageUrl();

        //Todo : use piccasso library to download and show image

        Picasso.get()
                .load(imageUrl)
                .centerInside()
                //.placeholder(R.drawable.image3)
                .fit().into(holder.journalImage);
                //just in case there is no image
               // .placeholder(R.drawable.image3).fit().into(holder.journalImage);

        //Todo : add time ago (search in google to get answer)
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000);
        holder.dataAddedRow.setText(timeAgo);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView journalImage;
        private TextView titleRow;
        private TextView thoughtRow;
        private TextView dataAddedRow;
        private TextView usernameRow;
        private String username;
        private String userId;
        private ImageButton shareButton;


        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;
            journalImage = itemView.findViewById(R.id.journal_image);
            titleRow = itemView.findViewById(R.id.journal_title_row);
            thoughtRow = itemView.findViewById(R.id.journal_thought_row);
            dataAddedRow = itemView.findViewById(R.id.journal_timestamp_list);
            usernameRow = itemView.findViewById(R.id.journal_row_username);
            shareButton = itemView.findViewById(R.id.journal_row_share);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
