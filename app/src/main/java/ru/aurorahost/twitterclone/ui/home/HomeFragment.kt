package ru.aurorahost.twitterclone.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.aurorahost.twitterclone.LoginActivity
import ru.aurorahost.twitterclone.ProfileActivity
import ru.aurorahost.twitterclone.R
import ru.aurorahost.twitterclone.TweetActivity
import ru.aurorahost.twitterclone.adapters.TweetListAdapter
import ru.aurorahost.twitterclone.databinding.FragmentHomeBinding
import ru.aurorahost.twitterclone.listeners.HomeCallback
import ru.aurorahost.twitterclone.listeners.TwitterListenerImpl
import ru.aurorahost.twitterclone.ui.MainViewModel
import ru.aurorahost.twitterclone.ui.TwitterFragment
import ru.aurorahost.twitterclone.util.DATA_TWEETS
import ru.aurorahost.twitterclone.util.DATA_TWEETS_USER_HASHTAGS
import ru.aurorahost.twitterclone.util.DATA_TWEETS_USER_IDS
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.User

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private lateinit var mainViewModel: MainViewModel
    private lateinit var homeViewModel: HomeViewModel

    private val binding get() = _binding!!
    private val firebaseReference = FirebaseDatabase.getInstance().reference
    private var listener: TwitterListenerImpl? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var tweetsAdapter: TweetListAdapter? = null
    private var callback: HomeCallback? = null

    private fun updateList(currentUser: User?) {
        binding.recyclerView.visibility = View.GONE

        firebaseReference.child(DATA_TWEETS).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tweets = mutableListOf<Tweet>()
                for (tweetSnapshot in dataSnapshot.children) {
                    val tweet = tweetSnapshot.getValue(Tweet::class.java) ?: continue
                    if (currentUser?.followHashtags?.any { tweet.hashtags?.contains(it) ?: false } == true ||
                        currentUser?.followUsers?.contains(tweet.userIds!![0]) == true) {
                        tweets.add(tweet)
                    }
                }
                updateAdapter(tweets)
                binding.recyclerView.visibility = View.VISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error fetching tweets: ", databaseError.toException())
                binding.recyclerView.visibility = View.VISIBLE
            }
        })
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.getCurrentUser()
        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            listener = TwitterListenerImpl(binding.recyclerView, user)
            tweetsAdapter = TweetListAdapter(userId!!, arrayListOf())
            tweetsAdapter?.setListener(listener)
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = tweetsAdapter
            }
        }
    }


    private fun updateAdapter(tweets: List<Tweet>) {
        val sortedTweets = tweets.sortedWith(compareByDescending { it.timeStamp })
        tweetsAdapter?.updateTweets(removeDuplicates(sortedTweets))
    }

    private fun removeDuplicates(originalList: List<Tweet>) = originalList.distinctBy { it.tweetId }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupMenu()
        binding.floatingActionButton.setOnClickListener {
            startActivity(TweetActivity.newIntent(requireContext(), userId))
        }
        return root
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menuProfile -> {
                        startActivity(ProfileActivity.newIntent(requireContext()))
                        true
                    }

                    R.id.menuLogoutAction -> {
                        homeViewModel.logOut()
                        startActivity(LoginActivity.newIntent(requireContext()))
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            updateList(user)
        }
    }
}
