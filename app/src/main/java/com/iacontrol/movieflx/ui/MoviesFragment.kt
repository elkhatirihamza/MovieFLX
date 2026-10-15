package com.iacontrol.movieflx.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.iacontrol.movieflx.R
import com.iacontrol.movieflx.databinding.FragmentMoviesBinding
import com.iacontrol.movieflx.viewmodel.MoviesViewModel

import dagger.hilt.android.AndroidEntryPoint

interface FilterApplyListener {
    fun onFiltersApplied(
        genre: String,
        sortType: String,
        minRating: Float,
        yearInterval: Pair<Int, Int>
    )
}

@AndroidEntryPoint
class MoviesFragment : Fragment(R.layout.fragment_movies), FilterApplyListener {

    private val viewModel: MoviesViewModel by viewModels()
    private lateinit var gridAdapter: MovieAdapter
    private lateinit var forYouAdapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMoviesBinding.bind(view)

        setupRecyclerViews(binding)
        setupListeners(binding)
        setupSearchDebounce(binding)
        observeViewModel(binding)
    }
    
    override fun onResume() {
        super.onResume()
        // Recharger les recommandations chaque fois qu'on revient au fragment
        // Cela permettra de mettre à jour les recommandations après avoir ajouté/retiré des favoris
        viewModel.loadRecommendations()
    }
    
    

    private fun setupRecyclerViews(binding: FragmentMoviesBinding) {
    
        forYouAdapter = MovieAdapter(isHorizontal = true) { movie ->
            val action = MoviesFragmentDirections.actionMoviesFragmentToMovieDetailFragment(movie)
            findNavController().navigate(action)
        }
        binding.forYouList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = forYouAdapter
        }

       
        gridAdapter = MovieAdapter { movie ->
            val action = MoviesFragmentDirections.actionMoviesFragmentToMovieDetailFragment(movie)
            findNavController().navigate(action)
        }

        binding.movieGrid.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = gridAdapter

            // IMPORTANT : On désactive le scroll interne du RecyclerView pour qu'il s'affiche en entier
            // et laisse le NestedScrollView gérer le défilement.
            isNestedScrollingEnabled = false
        }

        
        binding.mainScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->

            

                val totalHeight = v.getChildAt(0).measuredHeight
                val windowHeight = v.measuredHeight

                // Si (Position actuelle + Hauteur fenêtre) >= Hauteur totale
                // Alors on est tout en bas.
                if (scrollY + windowHeight >= totalHeight) {

                    android.util.Log.d("PAGINATION", ">>> Bas de page NestedScrollView atteint ! Chargement...")
                    viewModel.loadNextPage()
                }
            }
        )
    }

    private fun setupListeners(binding: FragmentMoviesBinding) {
        // Gestion du bouton de filtre (BottomSheet)
        binding.filterButtonContainer.root.setOnClickListener {

            // On récupère les filtres actuels pour les passer au BottomSheet
            val currentFilters = viewModel.currentFilters.value

            val currentGenre = currentFilters?.genre ?: "Tous   "
            val currentSortType = currentFilters?.sortType ?: ""
            val currentMinRating = currentFilters?.minRating ?: 0.0f
            val currentYearInterval = currentFilters?.yearInterval ?: Pair(1950, 2025)

            val bottomSheet = FilterBottomSheetFragment.newInstance(
                currentGenre = currentGenre,
                currentSortType = currentSortType,
                currentMinRating = currentMinRating,
                currentYearInterval = currentYearInterval
            )
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        // Gestion du Logout (Toolbar)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_logout) {
                val action = R.id.action_logout
                findNavController().navigate(action)
                true
            } else {
                false
            }
        }
    }

    private fun setupSearchDebounce(binding: FragmentMoviesBinding) {
        val searchEditText = binding.searchBarContainer.editSearch

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel(binding: FragmentMoviesBinding) {
        // Observer les données filtrées
        viewModel.displayedMovies.observe(viewLifecycleOwner) { movies ->
            // Mise à jour de la grille (nouvelles pages incluses)
            gridAdapter.submitList(movies)
        }
        
        // Observer les recommandations personnalisées
        viewModel.recommendationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is com.iacontrol.movieflx.viewmodel.RecommendationState.Success -> {
                    // Afficher les recommandations dans "Pour vous"
                    binding.forYouList.visibility = View.VISIBLE
                    binding.emptyRecommendationsMessage.visibility = View.GONE
                    forYouAdapter.submitList(state.movies)
                }
                is com.iacontrol.movieflx.viewmodel.RecommendationState.NeedMoreFavorites -> {
                    // Afficher le message rouge "Veuillez ajouter des films en favoris"
                    binding.forYouList.visibility = View.GONE
                    binding.emptyRecommendationsMessage.visibility = View.VISIBLE
                }
                is com.iacontrol.movieflx.viewmodel.RecommendationState.Loading -> {
                    // Optionnel: garder l'état actuel pendant le chargement
                    binding.emptyRecommendationsMessage.visibility = View.GONE
                }
                is com.iacontrol.movieflx.viewmodel.RecommendationState.Error -> {
                    // Masquer en cas d'erreur
                    binding.forYouList.visibility = View.GONE
                    binding.emptyRecommendationsMessage.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Implémentation de l'interface FilterApplyListener.
     */
    override fun onFiltersApplied(
        genre: String,
        sortType: String,
        minRating: Float,
        yearInterval: Pair<Int, Int>
    ) {
        // Appel du ViewModel pour appliquer TOUS les filtres
        viewModel.applyFilters(genre, sortType, minRating, yearInterval)
    }
}