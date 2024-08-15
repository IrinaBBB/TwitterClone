package ru.aurorahost.twitterclone.ui.list

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import ru.aurorahost.twitterclone.R
import ru.aurorahost.twitterclone.adapters.TweetListAdapter
import ru.aurorahost.twitterclone.databinding.FragmentListBinding
import ru.aurorahost.twitterclone.listeners.HomeCallback
import ru.aurorahost.twitterclone.listeners.TweetListener
import ru.aurorahost.twitterclone.util.User

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var listViewModel: ListViewModel

    private var tweetsAdapter: TweetListAdapter? = null
    private var currentUser: User? = null
    private val firebaseDb = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val listener: TweetListener? = null
    private var callback: HomeCallback? = null
    private var tweetListener: HomeCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listViewModel = ViewModelProvider(this).get(ListViewModel::class.java)

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root



        setupMenu()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)
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
//                        newText?.let {
//                            performSearch(it)
//                        }
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle menu item selection if needed
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun performSearch(query: String) {
        // Perform the search operation and update the UI
        // For demonstration, we'll just update the ViewModel with the search query
        Toast.makeText(requireContext(), "Hello from search: $query", Toast.LENGTH_SHORT).show()
    }
}
