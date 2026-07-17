package com.xwguanliqi.application

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var currentDir: File = Environment.getExternalStorageDirectory()
    private val filesAdapter = FilesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = filesAdapter

        if (checkPermission()) {
            loadDirectory(currentDir)
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadDirectory(currentDir)
        }
    }

    fun loadDirectory(dir: File) {
        currentDir = dir
        val files = dir.listFiles()?.toList() ?: emptyList()
        filesAdapter.submitList(
            files.sortedWith(compareBy<File> { it.isDirectory }.thenBy { it.name })
        )
    }

    inner class FilesAdapter : RecyclerView.Adapter<FilesAdapter.VH>() {
        private var files: List<File> = emptyList()

        fun submitList(list: List<File>) {
            files = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = layoutInflater.inflate(R.layout.item_file, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val file = files[position]
            holder.name.text = file.name
            holder.icon.text = if (file.isDirectory) "📁" else "📄"
            holder.size.text = if (file.isFile) formatSize(file.length()) else ""
        }

        override fun getItemCount() = files.size

        inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: TextView = itemView.findViewById(R.id.file_icon)
            val name: TextView = itemView.findViewById(R.id.file_name)
            val size: TextView = itemView.findViewById(R.id.file_size)
        }
    }

    private fun formatSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
}