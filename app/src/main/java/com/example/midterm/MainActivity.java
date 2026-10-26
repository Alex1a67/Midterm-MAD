package com.example.midterm;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etStudentName, etStudentId;
    private CheckBox cbEnrolled;
    private Button btnAdd, btnEdit, btnDelete, btnView;
    private ListView lvStudents;

    private StudentDBHelper dbHelper;
    private ArrayList<long[]> rowIds = new ArrayList<>();
    private ArrayList<String[]> studentList = new ArrayList<>();
    private StudentAdapter adapter;

    private long selectedRowId = -1;x

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etStudentName = findViewById(R.id.etStudentName);
        etStudentId = findViewById(R.id.etStudentId);
        cbEnrolled = findViewById(R.id.cbEnrolled);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnView = findViewById(R.id.btnView);
        lvStudents = findViewById(R.id.lvStudents);

        dbHelper = new StudentDBHelper(this);
        adapter = new StudentAdapter();
        lvStudents.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addStudent());
        btnEdit.setOnClickListener(v -> editStudent());
        btnDelete.setOnClickListener(v -> deleteStudent());
        btnView.setOnClickListener(v -> loadStudents());

        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            selectedRowId = rowIds.get(position)[0];
            String[] data = studentList.get(position);
            etStudentName.setText(data[0]);
            etStudentId.setText(data[1]);
            cbEnrolled.setChecked(data[2].equals("Yes"));

            btnEdit.setEnabled(true);
            btnDelete.setEnabled(true);
        });
    }

    private void addStudent() {
        String name = etStudentName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        boolean enrolled = cbEnrolled.isChecked();

        if (name.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.isStudentIdExists(studentId, -1)) {
            Toast.makeText(this, "Student ID already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.insertStudent(name, studentId, enrolled);
        if (result != -1) {
            Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show();
            clearFields();
            loadStudents();
        } else {
            Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show();
        }
    }

    private void editStudent() {
        if (selectedRowId == -1) {
            Toast.makeText(this, "Select a student first", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etStudentName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        boolean enrolled = cbEnrolled.isChecked();

        if (name.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.isStudentIdExists(studentId, selectedRowId)) {
            Toast.makeText(this, "Student ID already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        int rows = dbHelper.updateStudent(selectedRowId, name, studentId, enrolled);
        if (rows > 0) {
            Toast.makeText(this, "Student updated", Toast.LENGTH_SHORT).show();
            clearFields();
            loadStudents();
        } else {
            Toast.makeText(this, "Failed to update student", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteStudent() {
        if (selectedRowId == -1) {
            Toast.makeText(this, "Select a student first", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    int rows = dbHelper.deleteStudent(selectedRowId);
                    if (rows > 0) {
                        Toast.makeText(this, "Student deleted", Toast.LENGTH_SHORT).show();
                        clearFields();
                        loadStudents();
                    } else {
                        Toast.makeText(this, "Failed to delete student", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void loadStudents() {
        studentList.clear();
        rowIds.clear();
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(StudentDBHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(StudentDBHelper.COL_NAME));
                String studentId = cursor.getString(cursor.getColumnIndexOrThrow(StudentDBHelper.COL_STUDENT_ID));
                int enrolled = cursor.getInt(cursor.getColumnIndexOrThrow(StudentDBHelper.COL_ENROLLED));

                rowIds.add(new long[]{id});
                studentList.add(new String[]{name, studentId, enrolled == 1 ? "Yes" : "No"});
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void clearFields() {
        etStudentName.setText("");
        etStudentId.setText("");
        cbEnrolled.setChecked(false);
        selectedRowId = -1;
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    private class StudentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return studentList.size();
        }

        @Override
        public Object getItem(int position) {
            return studentList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return rowIds.get(position)[0];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.student_list_item, parent, false);
            }

            String[] data = studentList.get(position);

            TextView tvName = convertView.findViewById(R.id.tvName);
            TextView tvStudentId = convertView.findViewById(R.id.tvStudentId);
            TextView tvEnrolled = convertView.findViewById(R.id.tvEnrolled);

            tvName.setText(data[0]);
            tvStudentId.setText("ID: " + data[1]);
            tvEnrolled.setText("Enrolled: " + data[2]);

            return convertView;
        }
    }
}
