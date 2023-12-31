package com.example.noteapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.noteapp.Adapters.NotesListAdapter;
import com.example.noteapp.Database.MainDAO;
import com.example.noteapp.Database.RoomDB;
import com.example.noteapp.Model.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{
    RecyclerView recyclerView;
    ImageView addNote;
    NotesListAdapter adapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    SearchView searchView;
    Notes selectedNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_home);
        addNote = findViewById(R.id.addNote);
        searchView = findViewById(R.id.searchView_home);

        database = RoomDB.getInstance(MainActivity.this);
        notes = database.mainDAO().getAll();

        updateRecyclerView(notes);

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, addNoteActivity.class);
                startActivityForResult(intent,101);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }
    void filter(String newText){
        List<Notes> filteredNotes = new ArrayList<>();
        for (Notes singleNotes : notes){
            if (singleNotes.getTitle().toLowerCase().contains(newText.toLowerCase())
            || singleNotes.getNotes().toLowerCase().contains(newText.toLowerCase())){
                filteredNotes.add(singleNotes);
            }
        }
        adapter.filteredNotes(filteredNotes);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){
            if (resultCode == Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("notes");
                Log.d("Final Result","Title :"+new_notes.getTitle()+", Notes :"+new_notes.getNotes());
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                updateRecyclerView(notes);
            }
        } else if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK){
                Notes new_Notes = (Notes) data.getSerializableExtra("notes");
                database.mainDAO().update(new_Notes.getID(),new_Notes.getTitle(),new_Notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                updateRecyclerView(notes);
            }
        }
    }

    private void updateRecyclerView(List<Notes> notes) {
        List<Notes> pinnedNotes = new ArrayList<>();
        List<Notes> unPinnedNotes = new ArrayList<>();
        List<Notes> finalNotes = new ArrayList<>();
        for (Notes singleNotes :notes){
            if (singleNotes.getPinned()){
                pinnedNotes.add(singleNotes);
            }else {
                unPinnedNotes.add(singleNotes);
            }
        }
        notes.clear();
        for (Notes pin:pinnedNotes){
            notes.add(pin);
        }
        for (Notes unPin:unPinnedNotes){
            notes.add(unPin);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL));
        adapter = new NotesListAdapter(MainActivity.this,notes,notesClickListener);
        recyclerView.setAdapter(adapter);
    }
    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this,addNoteActivity.class);
            intent.putExtra("old_notes",notes);
            startActivityForResult(intent,102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNotes = new Notes();
            selectedNotes = notes;
            showPopUp(cardView);
        }
    };

    private void showPopUp(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this,cardView);
        popupMenu.setOnMenuItemClickListener(MainActivity.this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.pin){
            if (selectedNotes.getPinned()){
                database.mainDAO().pin(selectedNotes.getID(),false);
                Toast.makeText(this, "UnPinned", Toast.LENGTH_SHORT).show();
            }else {
                database.mainDAO().pin(selectedNotes.getID(),true);
                Toast.makeText(this, "Pinned", Toast.LENGTH_SHORT).show();
            }
            notes.clear();
            notes.addAll(database.mainDAO().getAll());
            updateRecyclerView(notes);
            return true;
        } else if (itemId == R.id.delete) {
            database.mainDAO().delete(selectedNotes);
            notes.clear();
            notes.addAll(database.mainDAO().getAll());
            updateRecyclerView(notes);
            Toast.makeText(this, "Notes Deleted", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}