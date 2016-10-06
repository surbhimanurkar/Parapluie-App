package in.parapluie.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import in.parapluie.model.HelpQuestion;
import in.parapluie.app.R;
import com.google.firebase.database.Query;

/**
 * Created by surbhimanurkar on 10-03-2016.
 */
public class HelpQuestionsAdapter extends FirebaseListAdapter<HelpQuestion> {

    private Activity activity;
    private EditText inputText;

    public HelpQuestionsAdapter(Query ref, Activity activity, int layout, EditText inputText) {
        super(ref, HelpQuestion.class, layout, activity);
        this.activity = activity;
        this.inputText = inputText;
    }

    /**
     * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
     *
     * @param view A view instance corresponding to the layout we passed to the constructor.
     * @param helpQuestion An instance representing the current state of help question
     */
    @Override
    @SuppressLint("NewApi")
    protected void populateView(View view, final HelpQuestion helpQuestion, String key) {
        TextView helpTextView = (TextView) view.findViewById(R.id.question);
        helpTextView.setText(helpQuestion.getQuestion());
        helpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputText.setText(helpQuestion.getQuestion());
            }
        });
    }
}
