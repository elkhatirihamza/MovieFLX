package com.iacontrol.movieflx.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.iacontrol.movieflx.R
import com.iacontrol.movieflx.databinding.FragmentFilterBottomSheetBinding
import kotlin.math.roundToInt


class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val genres = listOf("Tous", "Action", "Animation", "Biography", "Adventure", "Western", "Drama", "Crime",
        "Comedy", "Horror", "Mystery", "Film-Noir", "Fantasy", "Musical"
    )

    // État initial des filtres, récupéré via arguments
    private var selectedGenre: String = "Genre"
    private var selectedSortType: String = ""
    private var initialMinRating: Float = 0.0f
    private var initialMinYear: Int = 1950
    private var initialMaxYear: Int = 2025

    private var filterListener: FilterApplyListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is FilterApplyListener) {
            filterListener = parentFragment as FilterApplyListener
        } else {
            throw RuntimeException("${parentFragment} doit implémenter FilterApplyListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedGenre = it.getString(ARG_GENRE) ?: "Tous"
            selectedSortType = it.getString(ARG_SORT_TYPE) ?: ""
            // Récupérer l'état des nouveaux filtres
            initialMinRating = it.getFloat(ARG_MIN_RATING, 0.0f)
            initialMinYear = it.getInt(ARG_MIN_YEAR, 1950)
            initialMaxYear = it.getInt(ARG_MAX_YEAR, 2025)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Le nom de la classe de binding correspond au nom de votre fichier XML (FragmentFilterBottomSheetBinding)
        _binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGenreSpinner()
        setupSortChips()
        setupRatingAndYearControls()

        binding.btnApplyFilters.setOnClickListener {
            val newGenre = binding.spinnerGenre.selectedItem.toString()

            // --- CORRECTION : LOGIQUE DE TRI ADAPTÉE AU XML AVEC 2 CHIPS ---
            val newSortType = when (binding.chipGroupSort.checkedChipId) {
                R.id.chip_top_rated -> "rating" // Correspond à "Note globale"
                R.id.chip_recent -> "recent" // Correspond à "Date de sortie"
                else -> ""
            }

            // Récupérer les valeurs des nouveaux filtres
            val newMinRating = binding.ratingBarMin.rating

            val yearValues = binding.rangeSliderYear.values
            val newYearInterval = Pair(
                yearValues.first().roundToInt(),
                yearValues.last().roundToInt()
            )

            // Appeler le listener avec TOUS les filtres
            filterListener?.onFiltersApplied(
                newGenre,
                newSortType,
                newMinRating,
                newYearInterval
            )

            dismiss()
        }
    }

    private fun setupRatingAndYearControls() {
        binding.ratingBarMin.rating = initialMinRating
        binding.rangeSliderYear.values = listOf(initialMinYear.toFloat(), initialMaxYear.toFloat())
    }

    private fun setupGenreSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            genres
        )
        binding.spinnerGenre.adapter = adapter
        val genreIndex = genres.indexOf(selectedGenre)
        binding.spinnerGenre.setSelection(if (genreIndex != -1) genreIndex else 0)
    }


    private fun setupSortChips() {
        val chipIdToSelect = when (selectedSortType) {
            "rating" -> R.id.chip_top_rated
            "recent" -> R.id.chip_recent
            else -> View.NO_ID
        }
        if (chipIdToSelect != View.NO_ID) {
            binding.chipGroupSort.check(chipIdToSelect)
        }
    }

    override fun onDetach() {
        super.onDetach()
        filterListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_GENRE = "arg_genre"
        private const val ARG_SORT_TYPE = "arg_sort_type"
        private const val ARG_MIN_RATING = "arg_min_rating"
        private const val ARG_MIN_YEAR = "arg_min_year"
        private const val ARG_MAX_YEAR = "arg_max_year"

        fun newInstance(
            currentGenre: String,
            currentSortType: String,
            currentMinRating: Float,
            currentYearInterval: Pair<Int, Int>
        ) = FilterBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_GENRE, currentGenre)
                putString(ARG_SORT_TYPE, currentSortType)
                putFloat(ARG_MIN_RATING, currentMinRating)
                putInt(ARG_MIN_YEAR, currentYearInterval.first)
                putInt(ARG_MAX_YEAR, currentYearInterval.second)
            }
        }
    }
}