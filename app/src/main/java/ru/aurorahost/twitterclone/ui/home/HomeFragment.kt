package ru.aurorahost.twitterclone.ui.home

import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ru.aurorahost.twitterclone.LoginActivity
import ru.aurorahost.twitterclone.ProfileActivity
import ru.aurorahost.twitterclone.R
import ru.aurorahost.twitterclone.TweetActivity
import ru.aurorahost.twitterclone.adapters.TweetListAdapter
import ru.aurorahost.twitterclone.databinding.FragmentHomeBinding
import ru.aurorahost.twitterclone.listeners.TwitterListenerImpl
import ru.aurorahost.twitterclone.ui.MainViewModel
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.User

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private lateinit var mainViewModel: MainViewModel
    private lateinit var homeViewModel: HomeViewModel

    private val binding get() = _binding!!
    private var listener: TwitterListenerImpl? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var tweetsAdapter: TweetListAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.getCurrentUser()
        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            homeViewModel.tweets.observe(viewLifecycleOwner) {
                setupRecyclerView(user!!, it!!)
            }
        }

        mainViewModel.changeTrigger.observe(viewLifecycleOwner) {
            mainViewModel.user.observe(viewLifecycleOwner) { user ->
                homeViewModel.updateList(user, binding.recyclerView, binding.progressBar)
            }
        }

    }

    private fun setupRecyclerView(user: User, list: ArrayList<Tweet>) {
        listener = TwitterListenerImpl(binding.recyclerView, user, mainViewModel.changeTrigger)
        tweetsAdapter = TweetListAdapter(userId!!, list)
        tweetsAdapter?.setListener(listener)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tweetsAdapter
        }
    }

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
            homeViewModel.updateList(user, binding.recyclerView, binding.progressBar)
        }
    }
}
