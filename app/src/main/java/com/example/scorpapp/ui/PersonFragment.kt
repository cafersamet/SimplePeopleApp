package com.example.scorpapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scorpapp.adapter.PersonAdapter
import com.example.scorpapp.databinding.FragmentPersonBinding
import com.example.scorpapp.util.Resource
import com.example.scorpapp.util.StateHandler
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PersonFragment : Fragment() {

    private var nextKey: String? = null
    private var pageSize: Int? = null
    private var isScrolling = false
    private var isLastPage: Boolean = false
    private var isFirstPage = true
    private var state = StateHandler.State.NoData

    private var _binding: FragmentPersonBinding? = null
    private val binding get() = _binding!!
    private var personAdapter: PersonAdapter = PersonAdapter()
    private val viewModel: PersonViewModel by viewModels()
    private lateinit var stateHandler: StateHandler
    private lateinit var adapterObserver: RecyclerView.AdapterDataObserver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateHandler = StateHandler(binding)
        initViews()
        observeData()
    }

    private fun observeData() {
        viewModel.people.observe(viewLifecycleOwner, { response ->
            val isRefreshing = binding.swipeRefreshLayout.isRefreshing
            when (response) {
                is Resource.Success -> {
                    val list = response.data
                    if (isRefreshing) {
                        personAdapter.refreshAdapter(list!!)
                    } else {
                        personAdapter.updateAdapter(list!!)
                    }
                    binding.swipeRefreshLayout.isRefreshing = false

                    val currentKey = nextKey?.toInt() ?: 0
                    val futureKey = response.nextKey?.toInt() ?: 0

                    pageSize = futureKey - currentKey
                    nextKey = response.nextKey
                    isLastPage = nextKey == null && !isFirstPage
                    if (isLastPage) {
                        binding.recyclerView.setPadding(0, 0, 0, 0)
                    }

                    if (personAdapter.itemCount == 0) {
                        state = StateHandler.State.NoData
                    } else {
                        isFirstPage = false
                        state = StateHandler.State.Success
                    }
                }
                is Resource.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    state = if (personAdapter.itemCount == 0) {
                        StateHandler.State.NoData
                    } else {
                        StateHandler.State.Failed
                    }
                }
                is Resource.Loading -> {
                    if (!isRefreshing) {
                        state = StateHandler.State.Loading
                    }
                }
            }
            stateHandler.handleState(state)
        })
    }

    private fun checkNeedMoreData() {
        Handler(Looper.getMainLooper()).postDelayed({
            val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            if (visibleItemCount > 0 && visibleItemCount == totalItemCount) {
                getData()
            }
        }, 100)
    }


    private fun initViews() {
        binding.recyclerView.apply {
            adapter = personAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(scrollListener)
        }

        adapterObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkNeedMoreData()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkNeedMoreData()
            }
        }
        personAdapter.registerAdapterDataObserver(adapterObserver)

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        binding.retryButton.setOnClickListener {
            getData()
        }
        binding.noDataView.emptyRetryButton.setOnClickListener {
            getData()
        }
    }

    private fun refreshData() {
        viewModel.nextKey = null
        isFirstPage = true
        val scale = resources.displayMetrics.density
        val dpAsPixels = (50.0f * scale + 0.5f).toInt()
        binding.recyclerView.setPadding(0, 0, 0, dpAsPixels)
        getData()
    }

    private fun getData() {
        viewModel.getPeople()
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy < 1) {
                return
            }

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager

            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoading = state != StateHandler.State.Loading
            val isNotFailed = state != StateHandler.State.Failed
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val shouldPaginate = !isLastPage && isNotLoading && isAtLastItem && isNotAtBeginning &&
                    isScrolling && isNotFailed
            if (shouldPaginate) {
                getData()
                isScrolling = false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        personAdapter.unregisterAdapterDataObserver(adapterObserver)
    }
}