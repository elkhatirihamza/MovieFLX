package com.iacontrol.movieflx.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iacontrol.movieflx.ui.LikedMoviesAdapter
import com.google.android.material.snackbar.Snackbar
import com.iacontrol.movieflx.R
import com.iacontrol.movieflx.databinding.FragmentFavoriteMoviesBinding
import com.iacontrol.movieflx.viewmodel.LikedMoviesUiState
import com.iacontrol.movieflx.viewmodel.LikedMoviesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LikedMoviesFragment : Fragment(R.layout.fragment_favorite_movies) {


    private val viewModel: LikedMoviesViewModel by viewModels()
    private lateinit var adapter: LikedMoviesAdapter
    private var _binding: FragmentFavoriteMoviesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoriteMoviesBinding.bind(view) // Initialise le binding
        binding.toolbar.apply {

            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }

        }

        setupRecyclerView()
        observeViewModel()
        setupSwipeToDelete()
    }

    private fun setupRecyclerView() {
        adapter = LikedMoviesAdapter()
        binding.likedMoviesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.likedMoviesRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        // Utiliser lifecycleScope.launch pour collecter le StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is LikedMoviesUiState.Loading -> {
                        // Afficher le chargement, masquer la liste et l'erreur
                        binding.progressBar.visibility = View.VISIBLE
                        binding.likedMoviesRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.GONE
                    }
                    is LikedMoviesUiState.Success -> {
                        // Afficher la liste, masquer le chargement et l'erreur
                        binding.progressBar.visibility = View.GONE
                        binding.errorTextView.visibility = View.GONE

                        if (uiState.movies.isEmpty()) {
                            // Cas où la liste est vide (peut afficher un message "Pas de favoris")
                            binding.likedMoviesRecyclerView.visibility = View.GONE
                            binding.errorTextView.text = "Aucun film favori trouvé."
                            binding.errorTextView.visibility = View.VISIBLE
                        } else if (uiState.movies.size < 5) {
                            // Afficher les films mais avec un message indiquant qu'il en faut 5
                            binding.likedMoviesRecyclerView.visibility = View.VISIBLE
                            adapter.submitList(uiState.movies)
                            
                            // Afficher un message en haut pour indiquer qu'il faut 5 films
                            val remaining = 5 - uiState.movies.size
                            Snackbar.make(
                                binding.root,
                                "Ajoutez encore $remaining film(s) pour obtenir des recommandations personnalisées",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            // Afficher les données (5 films ou plus)
                            binding.likedMoviesRecyclerView.visibility = View.VISIBLE
                            adapter.submitList(uiState.movies)
                        }
                    }
                    is LikedMoviesUiState.Error -> {
                        // Afficher l'erreur, masquer le chargement et la liste
                        binding.progressBar.visibility = View.GONE
                        binding.likedMoviesRecyclerView.visibility = View.GONE
                        binding.errorTextView.text = uiState.message // Affiche le message d'erreur
                        binding.errorTextView.visibility = View.VISIBLE

                        // Afficher une Snackbar pour un feedback rapide
                        Snackbar.make(binding.root, "Erreur: ${uiState.message}", Snackbar.LENGTH_LONG).show()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val movie = adapter.currentList[position]


                movie.id?.let { viewModel.removeMovie(it) }

                Snackbar.make(binding.root, "${movie.title} supprimé", Snackbar.LENGTH_LONG)
                    .setAction("Annuler") {

                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.likedMoviesRecyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Nettoyer le binding
    }
}