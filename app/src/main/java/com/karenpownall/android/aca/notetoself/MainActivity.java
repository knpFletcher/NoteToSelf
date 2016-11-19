package com.karenpownall.android.aca.notetoself;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NoteAdapter mNoteAdapter;
    private boolean mSound;
    private int mAnimOption;
    private SharedPreferences mPrefs;
    Animation mAnimFlash;
    Animation mFadeIn;

    int mIdBeep = -1;
    SoundPool mSp;

    //region overridden methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instantiate sound pool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSp = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        try {
            //Create objects of the 2 required classes
            AssetManager assetManager = this.getAssets();
            AssetFileDescriptor descriptor;

            //Load our FX in memory for use
            descriptor = assetManager.openFd("beep.ogg");
            mIdBeep = mSp.load(descriptor, 0);
        } catch (IOException e) {
            //Print an error message to the console
        }
        Log.e("error", "failed to load sound files");


        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogNewNote dialog = new DialogNewNote();
                dialog.show(getSupportFragmentManager(), "");
            }
        });
        */

        /*
        1. Initialize mNoteAdapter
        2. Get a reference to ListView
        3. Bind them together
         */

        mNoteAdapter = new NoteAdapter();
        ListView listNote = (ListView) findViewById(R.id.listView);
        listNote.setAdapter(mNoteAdapter);

        //so we can long click it
        listNote.setLongClickable(true);

        //now to detect long clicks and delete the note
        listNote.setOnItemLongClickListener(new
            AdapterView.OnItemLongClickListener(){

            public boolean onItemLongClick(AdapterView<?> adapter, View view,
                                           int whichItem, long id){
                //ask NoteAdapter to delete this entry
                mNoteAdapter.deleteNote(whichItem);
                return true;
            }
        });

        //Handle clicks on the ListView
        listNote.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            //inner anonymous class
            @Override
            public void onItemClick(AdapterView<?>adapter, View view, int whichItem, long id){
                if (mSound){
                    mSp.play(mIdBeep, 1, 1, 0, 0, 1);
                }
                /*
                Create a temporary Note
                which is a reference to the Note
                that has been clicked
                 */
                Note tempNote = mNoteAdapter.getItem(whichItem);

                //Create a new dialog window
                DialogShowNote dialog = new DialogShowNote();
                //Send in a reference to the note to be shown
                dialog.sendNoteSelected(tempNote);

                //show the dialog window with the note in it
                dialog.show(getFragmentManager(), "");
            }
        }); //end OnItemClickLIstener
    } //end of onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    } //end of OnCreateOptionsMenu()

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_add) {
            DialogNewNote dialog = new DialogNewNote();
            dialog.show(getFragmentManager(), "456");
            return true;
        }
        return super.onOptionsItemSelected(item);
    } // end of onOptionsItemSelected()

    @Override
    protected void onResume(){
        super.onResume();

        mPrefs = getSharedPreferences("Note to Self", MODE_PRIVATE);
        mSound = mPrefs.getBoolean("sound", true);
        mAnimOption = mPrefs.getInt("anim option", SettingsActivity.FAST);

        mAnimFlash = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flash);
        mFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        // Set the rate of flash based on settings
        if(mAnimOption == SettingsActivity.FAST){

            mAnimFlash.setDuration(100);
            Log.i("anim = ",""+ mAnimOption);
        }else if(mAnimOption == SettingsActivity.SLOW){

            Log.i("anim = ",""+ mAnimOption);
            mAnimFlash.setDuration(1000);
        }

        mNoteAdapter.notifyDataSetChanged();
    } //end of onResume

    @Override
    protected void onPause(){
        super.onPause();
        mNoteAdapter.saveNotes();
    }

    // endregion overridden methods

    // region class methods

    /*
    When DialogNewNote calls this method, it will pass it straight to
    the addNote method in the NoteAdapter class, and will be added to
    ArrayList (noteList). The adapter will be notified of the change as
    well, which will then trigger the BaseAdapter class to do its work and
    keep the view up-to-date.
     */

    public void createNewNote(Note n) {
        mNoteAdapter.addNote(n);
    } // end of createNewNote()
    // end region class methods

    //region inner classes
    /*
    Here we create an inner class called NoteAdapter that extends
    BaseAdapter. This class holds an ArrayList called noteList and the
    getItem method returns a Note object.
     */

    public class NoteAdapter extends BaseAdapter{
        private JSONSerializer mSerializer;

        // Declare and initialize a List that will hold notes
        List<Note> noteList = new ArrayList<Note>();

        public NoteAdapter(){
            mSerializer = new JSONSerializer("NoteToSelf.json",
                    MainActivity.this.getApplicationContext());
            mSerializer = new JSONSerializer("NoteToSelf.json",
                    MainActivity.this.getApplicationContext());
            try {
                noteList = mSerializer.load();
            } catch (Exception e){
                noteList = new ArrayList<Note>();
                Log.e("Error loading notes: ", "", e);
            } //end try catch
        } //end constructor

        // Get the number of notes in the list (ArrayList)
        @Override
        public int getCount(){
            return noteList.size();
        }

        // Get an item at a particular index (whichItem) in the array
        @Override
        public Note getItem(int whichItem){
            return noteList.get(whichItem);
        }

        // Get the id of an item in the array
        @Override
        public long getItemId(int whichItem){
            return whichItem;
        }

        /*
        The view object reference is an instance of the list item that is
        necessary to be displayed as evaluated by BaseAdapter, and whichItem is
        the position in the ArrayList of the Note object that needs to be
        displayed in it.
         */
        @Override
        public View getView(int whichItem, View view, ViewGroup viewGroup){
            //implement this method next
            //Has view been inflated already
            if (view == null){
                //If not, do so here
                //first create a LayoutInflater
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //Now instantiate view using inflater.inflate
                //using the listitem layout
                view = inflater.inflate(R.layout.listitem, viewGroup, false);
                //the false parameter is necessary, because of the way we want to use listitem
            } //End if

            //grab a reference to all TextView and ImageView widgets
            TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            TextView txtDescription = (TextView) view.findViewById(R.id.txtDescription);
            ImageView ivImportant = (ImageView) view.findViewById(R.id.imageViewImportant);
            ImageView ivTodo = (ImageView) view.findViewById(R.id.imageViewTodo);
            ImageView ivIdea = (ImageView) view.findViewById(R.id.imageViewIdea);

            //hide imageView widges that aren't relevant
            Note tempNote = noteList.get(whichItem);

            // To animate or not to animate
            if (tempNote.isImportant() && mAnimOption != SettingsActivity.NONE ) {
                view.setAnimation(mAnimFlash);

            }else{
                view.setAnimation(mFadeIn);
            }

            if (!tempNote.isImportant()){
                ivImportant.setVisibility(View.GONE);
            }
            if (!tempNote.isTodo()){
                ivTodo.setVisibility(View.GONE);
            }
            if (!tempNote.isIdea()){
                ivIdea.setVisibility(View.GONE);
            }

            //add text to the heading and description
            txtTitle.setText(tempNote.getTitle());
            txtDescription.setText(tempNote.getDescription());
            return view;
        } //end of View getView

         /*
        This method adds a new note to our array list.
        notifyDataSetChanged() will tell NoteAdapter that the data in
        noteList has changed and that the ListView might need to be
        updated.
         */

        public void addNote(Note n){
            noteList.add(n);
            notifyDataSetChanged();
        } //end of addNote

        public void saveNotes(){
            try{
                mSerializer.save(noteList);
            } catch (Exception e){
                Log.e("Error Saving Notes", "", e);
            }
        } //end of saveNotes()

        public void deleteNote(int n){
            noteList.remove(n);
            notifyDataSetChanged();
        }
    } //end of NoteAdapter.class

    //endregion inner classes

} //end of MainActivity.class
