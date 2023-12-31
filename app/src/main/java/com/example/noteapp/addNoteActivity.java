package com.example.noteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.noteapp.Model.Notes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class addNoteActivity extends AppCompatActivity {

    EditText editText_title,editText_notes;
    ImageView imageView_save;
    Notes notes;
    Boolean isOldNotes = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        editText_title = findViewById(R.id.editText_title);
        editText_notes = findViewById(R.id.editText_notes);
        imageView_save = findViewById(R.id.imageView_save);

        notes = new Notes();
        try {
            notes = (Notes) getIntent().getSerializableExtra("old_notes");
            editText_title.setText(notes.getTitle());
            editText_notes.setText(notes.getNotes());
            imageView_save.setImageResource(R.drawable.update);
            isOldNotes = true;
        }catch (Exception e){
            e.printStackTrace();
        }

        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editText_title.getText().toString().trim();
                String description = editText_notes.getText().toString().trim();
                if (description.isEmpty()){
                    Toast.makeText(addNoteActivity.this, "Please add some Notes..", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat format = new SimpleDateFormat("EEE d MMM yyyy HH:mm a");
                Date date = new Date();

                if (!isOldNotes){
                    notes = new Notes();
                }
                notes.setTitle(title);
                notes.setNotes(description);
                notes.setDate(format.format(date));

                Log.d("Note :",title);

                Intent intent = new Intent();
                intent.putExtra("notes",notes);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
    }
}