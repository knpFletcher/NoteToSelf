package com.karenpownall.android.aca.notetoself;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kkpwnall on 9/15/16.
 */
public class DialogShowNote extends DialogFragment {

    //member variables
    private Note mNote;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        /*
        1. Declare an initialize an instance of AlertDialog.Builder
        2. Declare and initialize LayoutInflater and then use it to
           create a View object that contains the layout for the dialog.
           In this case, it is the layout from dialog_show_note.xml
        3. Get a reference to each of the UI widgets and set the text
           properties on txtTitle and textDescription from the appropriate
           member variables of mNote, which was initialized in sendNoteSelected.
         */

        //1.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //2.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_show_note, null);

        //3.
        TextView txtTitle = (TextView) dialogView.findViewById(R.id.txtTitle);
        TextView txtDescription = (TextView) dialogView.findViewById(R.id.txtDescription);

        txtTitle.setText(mNote.getTitle());
        txtDescription.setText(mNote.getDescription());

        ImageView ivImportant = (ImageView) dialogView.findViewById(R.id.imageViewImportant);
        ImageView ivTodo = (ImageView) dialogView.findViewById(R.id.imageViewTodo);
        ImageView ivIdea = (ImageView) dialogView.findViewById(R.id.imageViewIdea);

        /*
        This code checks whether the note being shown is important and then shows
        or hides ivImportant ImageView accordingly.  It also does the same thing for
        ivTodo and ivIdea.
         */

        // Check for importance
        if (!mNote.isImportant()){
            ivImportant.setVisibility(View.GONE);
        }

        // Check for todos
        if (!mNote.isTodo()){
            ivTodo.setVisibility(View.GONE);
        }

        // Check for ideas
        if (!mNote.isIdea()){
            ivIdea.setVisibility(View.GONE);
        }

        /*
        This next block of code will listen for a click on the button, and dismiss (close)
        the dialog window when the user clicks.  This is done with an anonymous class
        in the onClick method.
         */

        Button btnOK = (Button) dialogView.findViewById(R.id.btnOK);
        builder.setView(dialogView).setTitle("Your Note");
        btnOK.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dismiss();
            }
        });

        return builder.create();
    }

    /*
    This method will be called by MainActivity and it will
    pass in the Note object the user has clicked on.
     */
    public void sendNoteSelected(Note noteSelected){
        mNote = noteSelected;
    }
}
