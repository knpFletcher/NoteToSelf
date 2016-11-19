package com.karenpownall.android.aca.notetoself;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by kkpwnall on 9/15/16.
 */
public class DialogNewNote extends DialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Declare and initialize an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /*
        Initialize a LayoutInflater object, which we'll use to inflate our
        XML layout. (Turn our XML Layout into a Java Object.)
        inflater.inflate basically replaces setContentView for our dialog
        Then we create and inflate a new View, which will then contain all the
        UI elements from our dialog_new_note.xml layout file.
        */

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_note, null);

        /*
        Here we get references to each of the UI widgets in our layout.  Many of
        the objects are declared final because they will be used in an anonymous
        class. This is required.
         */

        final EditText editTitle = (EditText) dialogView.findViewById(R.id.editTitle);
        final EditText editDescription = (EditText) dialogView.findViewById(R.id.editDescription);
        final CheckBox checkBoxIdea = (CheckBox) dialogView.findViewById(R.id.checkBoxIdea);
        final CheckBox checkBoxTodo = (CheckBox) dialogView.findViewById(R.id.checkBoxTodo);
        final CheckBox checkBoxImportant = (CheckBox) dialogView.findViewById(R.id.checkBoxImportant);
        Button btnCancel = (Button) dialogView.findViewById(R.id.btnCancel);
        Button btnOK = (Button) dialogView.findViewById(R.id.btnOK);

        /*
        Now we set the message of the dialog using builder. Then we write an
        anonymous class to handle clicks on btnCancel. In the overridden onClick
        method, we simply call dismiss(), which is a public method of DialogFragment,
        to close the dialog window.
         */

        builder.setView(dialogView).setMessage("Add a new note");

        //Handle cancel button
        btnCancel.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dismiss();
            }
        });

        /*
        Now we add an anonymous class to handle what happens when the user clicks on
        the OK button (btnOK)
         */

        //Handle OK button
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Create a new note
                Note newNote = new Note();

                //Set its variables to match the users entries on the form
                newNote.setTitle(editTitle.getText().toString());
                newNote.setDescription(editDescription.getText().toString());
                newNote.setIdea(checkBoxIdea.isChecked());
                newNote.setTodo(checkBoxTodo.isChecked());
                newNote.setImportant(checkBoxImportant.isChecked());

                //Get a reference to MainActivity
                MainActivity callingActivity = (MainActivity) getActivity();

                //Pass newNote back to MainActivity
                callingActivity.createNewNote(newNote);

                //Quit the dialog
                dismiss();

            }
        });

        return builder.create();
    }
}
