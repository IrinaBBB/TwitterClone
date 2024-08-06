package ru.aurorahost.twitterclone.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.aurorahost.twitterclone.R
import ru.aurorahost.twitterclone.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSearch
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission
                query?.let {
                    performSearch(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search query text change
//                newText?.let {
//                    performSearch(it)
//                }
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun performSearch(query: String) {
        // Perform the search operation and update the UI
        // For demonstration, we'll just update the ViewModel with the search query
        Toast.makeText(requireContext(), "Hello from search: $query", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}