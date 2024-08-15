package ru.aurorahost.twitterclone.ui.list

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import ru.aurorahost.twitterclone.adapters.TweetListAdapter
import ru.aurorahost.twitterclone.databinding.FragmentListBinding
import ru.aurorahost.twitterclone.listeners.TwitterListenerImpl
import ru.aurorahost.twitterclone.ui.MainViewModel
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.User

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var listViewModel: ListViewModel
    private lateinit var mainViewModel: MainViewModel

    private var listener: TwitterListenerImpl? = null
    private var tweetsAdapter: TweetListAdapter? = null

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        listViewModel = ViewModelProvider(this)[ListViewModel::class.java]

        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.getCurrentUser()

        listViewModel.tweets.observe(viewLifecycleOwner) {
            setupRecyclerView(mainViewModel.user.value!!, it!!)
        }

        mainViewModel.changeTrigger.observe(viewLifecycleOwner) {
            mainViewModel.user.observe(viewLifecycleOwner) { user ->
                listViewModel.updateList(user, binding.recyclerView, binding.progressBar)
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

    override fun onResume() {
        super.onResume()
        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            listViewModel.updateList(user, binding.recyclerView, binding.progressBar)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
