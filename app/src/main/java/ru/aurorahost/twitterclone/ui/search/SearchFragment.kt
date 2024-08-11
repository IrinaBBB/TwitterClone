package ru.aurorahost.twitterclone.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ru.aurorahost.twitterclone.R
import ru.aurorahost.twitterclone.adapters.TweetListAdapter
import ru.aurorahost.twitterclone.databinding.FragmentSearchBinding
import ru.aurorahost.twitterclone.listeners.TweetListener
import ru.aurorahost.twitterclone.util.DATA_PROFILE_IMAGES
import ru.aurorahost.twitterclone.util.DATA_TWEETS
import ru.aurorahost.twitterclone.util.DATA_USERS
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.User

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchViewModel: SearchViewModel

    /** Auth */
    private lateinit var firebaseAuth: FirebaseAuth
    private var userId: String? = null

    /** DB */
    private lateinit var databaseReference: DatabaseReference

    private var currentHashtag = ""
    private var hashtagFollowed = false
    private var tweetsAdapter: TweetListAdapter? = null
    private var listener: TweetListener? = null
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupMenu()

        firebaseAuth = FirebaseAuth.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid

        databaseReference = FirebaseDatabase.getInstance().reference




        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tweetsAdapter = TweetListAdapter(userId!!, arrayListOf())
        tweetsAdapter?.setListener(listener)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tweetsAdapter
            //addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        databaseReference.child(DATA_USERS).child(userId!!).get().addOnSuccessListener { snapshot ->
            currentUser = snapshot.getValue(User::class.java)
//            hashtagFollowed = currentUser?.followHashtags?.contains(currentHashtag) == true
//
//            if (hashtagFollowed) {
//                binding.followHashtag.setImageResource(R.drawable.follow) // Example active state
//            } else {
//                binding.followHashtag.setImageResource(R.drawable.follow_inactive) // Example inactive state
//            }
        }
        binding.followHashtag.setOnClickListener {
            binding.followHashtag.isClickable = false
            if (hashtagFollowed) {
                val followed = currentUser?.followHashtags ?: arrayListOf()
                if (followed.contains(currentHashtag)) {
                    followed.remove(currentHashtag)
                    currentUser?.followHashtags = followed

                    // Update the user in the database
                    databaseReference.child(DATA_USERS).child(userId!!).setValue(currentUser)
                        .addOnCompleteListener { task ->
                            binding.followHashtag.isClickable = true
                            if (task.isSuccessful) {
                                // Successfully updated
                                hashtagFollowed = false
                                binding.followHashtag.setImageResource(R.drawable.follow_inactive)
                            } else {
                                Toast.makeText(requireContext(), "Error! Could not unfollow tag", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    binding.followHashtag.isClickable = true
                }
            } else {
                // Follow logic
                val followed = currentUser?.followHashtags ?: arrayListOf()
                if (!followed.contains(currentHashtag)) {
                    followed.add(currentHashtag)
                    currentUser?.followHashtags = followed

                    // Update the user in the database
                    databaseReference.child(DATA_USERS).child(userId!!).setValue(currentUser)
                        .addOnCompleteListener { task ->
                            binding.followHashtag.isClickable = true
                            if (task.isSuccessful) {
                                // Successfully updated
                                hashtagFollowed = true
                                binding.followHashtag.setImageResource(R.drawable.follow)
                            } else {
                                // Handle the error
                                Toast.makeText(requireContext(), "Error! Could not follow tag", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    binding.followHashtag.isClickable = true
                }
            }
        }

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
        currentHashtag = query
        binding.followHashtag.visibility = View.VISIBLE
        findTweetsByTag(query)
    }

    private fun findTweetsByTag(tag: String) {
        // Hide the RecyclerView initially
        binding.recyclerView.visibility = View.GONE

        // Query the database for tweets that might contain the specified hashtag
        val query = databaseReference.child(DATA_TWEETS)

        // Attach a listener to get the data
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tweetList = mutableListOf<Tweet>() // Initialize an empty list for Tweet objects

                // Iterate through all tweets and check if they contain the hashtag
                for (snapshot in dataSnapshot.children) {
                    val tweet = snapshot.getValue(Tweet::class.java)
                    tweet?.let {
                        if (it.hashtags?.contains(tag) == true) {
                            tweetList.add(it)
                        }
                    }
                }

                val sortedTweetList = tweetList.sortedByDescending { it.timeStamp }

                // Update RecyclerView with the filtered list
                tweetsAdapter?.updateTweets(sortedTweetList)

                databaseReference.child(DATA_USERS).child(userId!!).get().addOnSuccessListener { snapshot ->
                    hashtagFollowed = currentUser?.followHashtags?.contains(currentHashtag) == true
                    if (hashtagFollowed) {
                        binding.followHashtag.setImageResource(R.drawable.follow) // Example active state
                    } else {
                        binding.followHashtag.setImageResource(R.drawable.follow_inactive) // Example inactive state
                    }
                }
                // Show the RecyclerView now that data is loaded
                binding.recyclerView.visibility = View.VISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle potential errors here
                Log.e("Firebase", "Error loading tweets: ${databaseError.message}")
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
