package com.example.scorpapp.util

import android.util.Log
import android.view.View
import com.example.scorpapp.databinding.FragmentPersonBinding

class StateHandler(_binding: FragmentPersonBinding) {

    private val binding: FragmentPersonBinding = _binding

    enum class State {
        Failed, Loading, NoData, Success
    }


    fun handleState(state: State) {
        Log.i("_StateHandler", "state is ${state.name}")
        when (state) {
            State.Failed -> handleError()
            State.Loading -> handleLoading()
            State.NoData -> handleNoData()
            State.Success -> handleSuccess()
        }
    }

    private fun handleSuccess() {
        hideAll()
    }

    private fun hideAll() {
        hideNoData()
        hideProgressBar()
        hideRetryButton()
    }

    private fun handleNoData() {
        showNoData()
        hideProgressBar()
        hideRetryButton()
    }

    private fun handleLoading() {
        showProgressBar()
        hideRetryButton()
        hideNoData()
    }

    private fun handleError() {
        showRetryButton()
        hideProgressBar()
        hideNoData()
    }


    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    private fun showRetryButton() {
        binding.retryButton.visibility = View.VISIBLE
    }

    private fun hideRetryButton() {
        binding.retryButton.visibility = View.INVISIBLE
    }

    private fun showNoData() {
        binding.recyclerView.visibility = View.GONE;
        binding.noDataView.emptyView.visibility = View.VISIBLE;
    }

    private fun hideNoData() {
        binding.recyclerView.visibility = View.VISIBLE;
        binding.noDataView.emptyView.visibility = View.GONE;
    }
}